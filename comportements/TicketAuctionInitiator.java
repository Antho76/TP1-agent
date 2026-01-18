package comportements;

import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentServicesTools;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import agents.TravellerAgent;
import data.Journey;

import java.io.IOException;
import java.util.*;

/**
 * Comportement d'enchère pour la revente de billets
 * Le vendeur (client qui annule) lance une enchère aux acheteurs potentiels
 * 
 * Types d'acheteurs :
 * - Acheteurs génériques : "ticket-buyer" (tous types de billets)
 * - Acheteurs spécialisés : "ticket-buyer-bus", "ticket-buyer-tram", "ticket-buyer-car", "ticket-buyer-bike"
 * 
 * @author Agent TP
 */
public class TicketAuctionInitiator extends ContractNetInitiator {

    private final Journey ticketToSell;
    private final TravellerAgent sellerAgent;
    private final double minimumPrice;
    private final double originalPrice;
    private boolean sold = false;
    private AID buyer = null;
    private double soldPrice = 0.0;

    /**
     * Crée un initiateur d'enchère pour revendre un billet
     * 
     * @param agent Agent vendeur (le client qui revend)
     * @param ticket Le billet à revendre
     * @param minPrice Prix minimum accepté (peut être inférieur au prix original)
     */
    public TicketAuctionInitiator(Agent agent, Journey ticket, double minPrice) {
        super(agent, createAuctionCFP(agent, ticket, minPrice));
        this.sellerAgent = (TravellerAgent) agent;
        this.ticketToSell = ticket;
        this.minimumPrice = minPrice;
        this.originalPrice = ticket.getCost();
    }

    /**
     * Délai d'attente pour les enchères (en millisecondes)
     * 30 secondes pour laisser le temps aux utilisateurs humains de répondre
     */
    private static final long AUCTION_TIMEOUT_MS = 30000; // 30 secondes

    /**
     * Crée le message d'appel d'offres pour l'enchère
     */
    private static ACLMessage createAuctionCFP(Agent agent, Journey ticket, double minPrice) {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.setReplyByDate(new Date(System.currentTimeMillis() + AUCTION_TIMEOUT_MS)); // 30 secondes pour répondre
        
        // Ajouter les acheteurs génériques
        AID[] genericBuyers = AgentServicesTools.searchAgents(agent, "ticket-market", "ticket-buyer");
        if (genericBuyers != null) {
            for (AID buyer : genericBuyers) {
                cfp.addReceiver(buyer);
            }
        }
        
        // Ajouter les acheteurs spécialisés selon le type de transport
        String transportType = ticket.getMeans().toLowerCase();
        String specializedService = "ticket-buyer-" + transportType;
        AID[] specializedBuyers = AgentServicesTools.searchAgents(agent, "ticket-market", specializedService);
        if (specializedBuyers != null) {
            for (AID buyer : specializedBuyers) {
                cfp.addReceiver(buyer);
            }
        }
        
        // Ajouter aussi les autres agents voyageurs qui peuvent être intéressés
        AID[] travellers = AgentServicesTools.searchAgents(agent, "travel-client", "traveller");
        if (travellers != null) {
            for (AID traveller : travellers) {
                if (!traveller.equals(agent.getAID())) { // Exclure le vendeur lui-même
                    cfp.addReceiver(traveller);
                }
            }
        }
        
        // Contenu du message : informations sur le billet
        try {
            Map<String, Object> auctionData = new HashMap<>();
            auctionData.put("ticket", ticket);
            auctionData.put("minPrice", minPrice);
            auctionData.put("originalPrice", ticket.getCost());
            auctionData.put("seller", agent.getAID().getLocalName());
            cfp.setContentObject((java.io.Serializable) auctionData);
        } catch (IOException e) {
            // Fallback : utiliser une chaîne formatée
            String content = String.format("TICKET_AUCTION|%s|%s|%s|%d|%.2f|%.2f",
                ticket.getStart(), ticket.getStop(), ticket.getMeans(),
                ticket.getDepartureDate(), ticket.getCost(), minPrice);
            cfp.setContent(content);
        }
        
        return cfp;
    }

    /**
     * Gestion d'un refus
     */
    @Override
    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("🔴 " + refuse.getSender().getLocalName() + " refuse l'enchère");
    }

    /**
     * Gestion d'un échec
     */
    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("⚠️ Acheteur non disponible");
        } else {
            System.out.println("❌ Échec de l'enchère avec " + failure.getSender().getLocalName());
        }
    }

    /**
     * Traitement de toutes les réponses reçues
     * Sélectionne la meilleure offre (prix le plus élevé >= prix minimum)
     */
    @Override
    protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> acceptances) {
        System.out.println("\n📊 RÉSULTATS DE L'ENCHÈRE");
        System.out.println("═".repeat(40));
        System.out.println("🎫 Billet: " + ticketToSell.getStart() + " → " + ticketToSell.getStop());
        System.out.println("💰 Prix original: " + String.format("%.2f€", originalPrice));
        System.out.println("💵 Prix minimum: " + String.format("%.2f€", minimumPrice));
        System.out.println("📬 Offres reçues: " + responses.size());
        System.out.println("─".repeat(40));

        ACLMessage bestOffer = null;
        double bestPrice = minimumPrice - 0.01; // Juste en dessous du minimum

        for (ACLMessage response : responses) {
            ACLMessage reply = response.createReply();
            
            if (response.getPerformative() == ACLMessage.PROPOSE) {
                try {
                    double offeredPrice = Double.parseDouble(response.getContent());
                    System.out.println("  📩 " + response.getSender().getLocalName() + 
                                     " propose: " + String.format("%.2f€", offeredPrice));
                    
                    if (offeredPrice >= minimumPrice && offeredPrice > bestPrice) {
                        // Rejeter l'ancienne meilleure offre si elle existe
                        if (bestOffer != null) {
                            ACLMessage oldReply = bestOffer.createReply();
                            oldReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                            oldReply.setContent("Une meilleure offre a été reçue");
                            acceptances.add(oldReply);
                        }
                        bestOffer = response;
                        bestPrice = offeredPrice;
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        if (offeredPrice < minimumPrice) {
                            reply.setContent("Offre en dessous du prix minimum (" + 
                                           String.format("%.2f€", minimumPrice) + ")");
                        } else {
                            reply.setContent("Une meilleure offre a été reçue");
                        }
                        acceptances.add(reply);
                    }
                } catch (NumberFormatException e) {
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    reply.setContent("Format d'offre invalide");
                    acceptances.add(reply);
                }
            } else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.add(reply);
            }
        }

        // Accepter la meilleure offre si elle existe
        if (bestOffer != null) {
            ACLMessage accept = bestOffer.createReply();
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            try {
                accept.setContentObject(ticketToSell);
            } catch (IOException e) {
                accept.setContent("TICKET_SOLD");
            }
            acceptances.add(accept);
            
            sold = true;
            buyer = bestOffer.getSender();
            soldPrice = bestPrice;
            
            System.out.println("─".repeat(40));
            System.out.println("✅ VENTE CONCLUE!");
            System.out.println("  🏆 Acheteur: " + buyer.getLocalName());
            System.out.println("  💰 Prix de vente: " + String.format("%.2f€", soldPrice));
            
            // Notifier immédiatement le vendeur avec une popup
            javax.swing.SwingUtilities.invokeLater(() -> {
                String message = String.format(
                    "🎉 BILLET VENDU!\n\n" +
                    "═══════════════════════════════\n" +
                    "🎫 Billet: %s → %s\n" +
                    "🚌 Transport: %s\n" +
                    "🕒 Départ: %s\n" +
                    "═══════════════════════════════\n\n" +
                    "🏆 Acheteur: %s\n" +
                    "💰 Prix de vente: %.2f€\n" +
                    "💵 Prix original: %.2f€\n" +
                    "📈 Récupération: %.0f%%\n\n" +
                    "Le billet a été transféré à l'acheteur.",
                    ticketToSell.getStart(), ticketToSell.getStop(),
                    ticketToSell.getMeans(),
                    formatTime(ticketToSell.getDepartureDate()),
                    buyer.getLocalName(),
                    soldPrice, originalPrice,
                    (soldPrice / originalPrice) * 100
                );
                
                javax.swing.JOptionPane.showMessageDialog(null,
                    message,
                    "✅ Enchère terminée - Billet vendu!",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            });
            
        } else {
            System.out.println("─".repeat(40));
            System.out.println("❌ Aucune offre acceptable reçue");
            
            // Notifier le vendeur qu'aucune offre n'a été acceptée
            javax.swing.SwingUtilities.invokeLater(() -> {
                String message = String.format(
                    "❌ AUCUNE OFFRE ACCEPTÉE\n\n" +
                    "🎫 Billet: %s → %s\n" +
                    "💵 Prix minimum demandé: %.2f€\n\n" +
                    "Aucun acheteur n'a fait d'offre suffisante.\n" +
                    "Le billet reste dans vos réservations.",
                    ticketToSell.getStart(), ticketToSell.getStop(),
                    minimumPrice
                );
                
                javax.swing.JOptionPane.showMessageDialog(null,
                    message,
                    "❌ Enchère terminée - Pas de vente",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            });;
        }
        System.out.println("═".repeat(40) + "\n");
    }

    /**
     * Confirmation de la vente
     */
    @Override
    protected void handleInform(ACLMessage inform) {
        System.out.println("✅ Vente confirmée par " + inform.getSender().getLocalName());
        
        // Notifier l'agent vendeur dans la console
        sellerAgent.getWindow().println("🎉 Votre billet " + ticketToSell.getStart() + " → " + 
                                        ticketToSell.getStop() + " a été vendu à " + 
                                        inform.getSender().getLocalName() + " pour " + 
                                        String.format("%.2f€", soldPrice));
        
        // IMPORTANT: Supprimer le billet vendu de la liste du vendeur
        sellerAgent.getWindow().removeSoldAuctionTicket(ticketToSell);
        
        System.out.println("📦 Billet transféré à " + inform.getSender().getLocalName());
    }

    /**
     * Formate l'heure en format lisible
     */
    private String formatTime(int time) {
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%02d:%02d", hours, minutes);
    }

    // Getters pour les résultats de l'enchère
    public boolean isSold() { return sold; }
    public AID getBuyer() { return buyer; }
    public double getSoldPrice() { return soldPrice; }
    public Journey getTicket() { return ticketToSell; }
}
