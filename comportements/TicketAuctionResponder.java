package comportements;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import agents.TravellerAgent;
import data.Journey;

import javax.swing.*;
import java.util.Map;

/**
 * Comportement de réponse aux enchères de billets
 * Permet à un agent (client ou service spécialisé) de participer aux enchères
 * 
 * @author Agent TP
 */
public class TicketAuctionResponder extends ContractNetResponder {

    private final TravellerAgent buyerAgent;
    private final String[] preferredTransportTypes; // Types de transport préférés (null = tous)
    private final double maxBudget; // Budget maximum pour l'achat
    private Journey currentTicket = null;

    /**
     * Crée un répondeur d'enchère générique (tous types de billets)
     */
    public TicketAuctionResponder(Agent agent, MessageTemplate template, double budget) {
        this(agent, template, budget, null);
    }

    /**
     * Crée un répondeur d'enchère spécialisé
     * 
     * @param agent Agent acheteur
     * @param template Template de message
     * @param budget Budget maximum
     * @param transportTypes Types de transport acceptés (null = tous)
     */
    public TicketAuctionResponder(Agent agent, MessageTemplate template, double budget, String[] transportTypes) {
        super(agent, template);
        this.buyerAgent = (agent instanceof TravellerAgent) ? (TravellerAgent) agent : null;
        this.maxBudget = budget;
        this.preferredTransportTypes = transportTypes;
    }

    /**
     * Traite l'appel d'offres reçu
     */
    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, NotUnderstoodException {
        System.out.println("🔔 " + myAgent.getLocalName() + " reçoit une offre d'enchère de " + 
                          cfp.getSender().getLocalName());

        // Pour les agents automatiques (pas de GUI), ajouter un délai de "réflexion"
        // Cela laisse le temps aux utilisateurs humains de répondre en premier
        if (buyerAgent == null) {
            int thinkingDelay = 5 + (int)(Math.random() * 5); // 5-10 secondes
            System.out.println("🤖 " + myAgent.getLocalName() + " analyse l'offre... (" + thinkingDelay + "s)");
            try {
                Thread.sleep(thinkingDelay * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Journey ticket = null;
        double minPrice = 0;
        double originalPrice = 0;

        // Parser le contenu du message
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> auctionData = (Map<String, Object>) cfp.getContentObject();
            ticket = (Journey) auctionData.get("ticket");
            minPrice = (Double) auctionData.get("minPrice");
            originalPrice = (Double) auctionData.get("originalPrice");
        } catch (UnreadableException | ClassCastException e) {
            // Fallback : parser la chaîne formatée
            String content = cfp.getContent();
                    if (content != null && content.startsWith("TICKET_AUCTION|")) {
                String[] parts = content.split("\\|");
                if (parts.length >= 7) {
                    String start = parts[1];
                    String stop = parts[2];
                    String means = parts[3];
                    int departure = Integer.parseInt(parts[4]);
                    originalPrice = Double.parseDouble(parts[5]);
                    minPrice = Double.parseDouble(parts[6]);
                    // Utiliser le constructeur public avec 8 paramètres
                    ticket = new Journey(start, stop, means, departure, 30, originalPrice, 0, 5);
                }
            }
        }

        if (ticket == null) {
            throw new NotUnderstoodException("Impossible de parser les informations du billet");
        }

        currentTicket = ticket;

        // Vérifier si le type de transport correspond aux préférences
        if (preferredTransportTypes != null) {
            boolean matches = false;
            for (String type : preferredTransportTypes) {
                if (ticket.getMeans().equalsIgnoreCase(type)) {
                    matches = true;
                    break;
                }
            }
            if (!matches) {
                throw new RefuseException("Type de transport non souhaité: " + ticket.getMeans());
            }
        }

        // Vérifier le budget
        if (minPrice > maxBudget) {
            throw new RefuseException("Prix minimum trop élevé: " + minPrice + "€ > budget " + maxBudget + "€");
        }

        // Demander confirmation à l'utilisateur si c'est un TravellerAgent avec GUI
        if (buyerAgent != null) {
            return askUserForBid(cfp, ticket, minPrice, originalPrice);
        } else {
            // Agent automatique : faire une offre au prix minimum
            return createAutomaticBid(cfp, minPrice);
        }
    }

    /**
     * Demande à l'utilisateur s'il veut enchérir
     */
    private ACLMessage askUserForBid(ACLMessage cfp, Journey ticket, double minPrice, double originalPrice) 
            throws RefuseException {
        
        // Créer le message de proposition
        StringBuilder message = new StringBuilder();
        message.append("🎫 BILLET EN ENCHÈRE!\n\n");
        message.append("═".repeat(35)).append("\n");
        message.append("📍 Trajet: ").append(ticket.getStart()).append(" → ").append(ticket.getStop()).append("\n");
        message.append("🚌 Transport: ").append(ticket.getMeans()).append("\n");
        message.append("🕒 Départ: ").append(formatTime(ticket.getDepartureDate())).append("\n");
        message.append("💰 Prix original: ").append(String.format("%.2f€", originalPrice)).append("\n");
        message.append("💵 Prix minimum: ").append(String.format("%.2f€", minPrice)).append("\n");
        message.append("═".repeat(35)).append("\n\n");
        message.append("Voulez-vous faire une offre?");

        int response = JOptionPane.showConfirmDialog(null,
            message.toString(),
            "🔔 Enchère de billet disponible",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (response != JOptionPane.YES_OPTION) {
            throw new RefuseException("L'utilisateur a décliné l'offre");
        }

        // Demander le prix
        String priceInput = JOptionPane.showInputDialog(null,
            "Entrez votre offre (minimum " + String.format("%.2f€", minPrice) + ", max budget " + 
            String.format("%.2f€", maxBudget) + "):",
            "💰 Votre offre",
            JOptionPane.QUESTION_MESSAGE);

        if (priceInput == null || priceInput.trim().isEmpty()) {
            throw new RefuseException("Pas d'offre saisie");
        }

        try {
            double offeredPrice = Double.parseDouble(priceInput.replace(",", ".").replace("€", "").trim());
            
            if (offeredPrice < minPrice) {
                JOptionPane.showMessageDialog(null,
                    "⚠️ Votre offre est en dessous du prix minimum!",
                    "Offre refusée",
                    JOptionPane.WARNING_MESSAGE);
                throw new RefuseException("Offre en dessous du minimum");
            }
            
            if (offeredPrice > maxBudget) {
                JOptionPane.showMessageDialog(null,
                    "⚠️ Votre offre dépasse votre budget!",
                    "Offre refusée",
                    JOptionPane.WARNING_MESSAGE);
                throw new RefuseException("Offre dépasse le budget");
            }

            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContent(String.valueOf(offeredPrice));
            
            System.out.println("📤 " + myAgent.getLocalName() + " propose " + 
                             String.format("%.2f€", offeredPrice));
            
            return propose;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                "⚠️ Format de prix invalide!",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            throw new RefuseException("Format de prix invalide");
        }
    }

    /**
     * Crée une offre automatique (pour les agents automatiques)
     */
    private ACLMessage createAutomaticBid(ACLMessage cfp, double minPrice) {
        // Offrir le prix minimum + une petite marge aléatoire
        double offer = minPrice + (Math.random() * (maxBudget - minPrice) * 0.3);
        offer = Math.min(offer, maxBudget);
        offer = Math.round(offer * 100.0) / 100.0; // Arrondir à 2 décimales

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        propose.setContent(String.valueOf(offer));

        System.out.println("🤖 " + myAgent.getLocalName() + " propose automatiquement " + 
                         String.format("%.2f€", offer));
        
        return propose;
    }

    /**
     * Traite l'acceptation de l'offre
     */
    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) 
            throws FailureException {
        
        System.out.println("🎉 " + myAgent.getLocalName() + " a remporté l'enchère!");

        Journey ticket = null;
        try {
            ticket = (Journey) accept.getContentObject();
        } catch (UnreadableException e) {
            ticket = currentTicket;
        }

        if (ticket == null) {
            throw new FailureException("Impossible de récupérer le billet");
        }

        // Prix payé pour le billet
        double price = Double.parseDouble(propose.getContent());

        // Confirmer l'achat à l'utilisateur
        if (buyerAgent != null) {
            // ========== AJOUTER LE BILLET AUX RÉSERVATIONS DE L'ACHETEUR ==========
            
            // 1. Ajouter le billet via l'interface graphique (met aussi à jour l'agent)
            final Journey finalTicket = ticket;
            final double finalPrice = price;
            buyerAgent.getWindow().addAuctionPurchasedTicket(finalTicket, finalPrice);
            
            // 2. Réserver la place dans le système
            ticket.bookPlace();
            
            // 3. Afficher notification de succès
            StringBuilder successMessage = new StringBuilder();
            successMessage.append("🎉 FÉLICITATIONS!\n\n");
            successMessage.append("Vous avez remporté l'enchère!\n\n");
            successMessage.append("🎫 Billet: ").append(ticket.getStart()).append(" → ").append(ticket.getStop()).append("\n");
            successMessage.append("🚌 Transport: ").append(ticket.getMeans()).append("\n");
            successMessage.append("💰 Prix payé: ").append(String.format("%.2f€", price)).append("\n\n");
            successMessage.append("✅ Le billet a été ajouté à vos réservations!");
            
            JOptionPane.showMessageDialog(null,
                successMessage.toString(),
                "✅ Achat réussi - Billet ajouté",
                JOptionPane.INFORMATION_MESSAGE);
            
            System.out.println("📝 Billet ajouté aux réservations de " + myAgent.getLocalName() + 
                             ": " + ticket.getStart() + " → " + ticket.getStop() + 
                             " pour " + String.format("%.2f€", price));
        }

        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        inform.setContent("Achat confirmé");
        
        return inform;
    }

    /**
     * Traite le rejet de l'offre
     */
    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        String reason = reject.getContent();
        System.out.println("❌ " + myAgent.getLocalName() + " : offre rejetée - " + reason);
        
        if (buyerAgent != null) {
            buyerAgent.getWindow().println("❌ Votre offre a été rejetée: " + reason);
        }
    }

    /**
     * Formate l'heure
     */
    private String formatTime(int time) {
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%02d:%02d", hours, minutes);
    }
}
