package agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import comportements.ContractNetAchat;
import comportements.TicketAuctionInitiator;
import comportements.TicketAuctionResponder;
import data.ComposedJourney;
import data.Journey;
import data.JourneysList;
import data.TextEnhancementService;
import data.WeatherManager;
import gui.TravellerGui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

/**
 * Journey searcher
 *
 * @author Emmanuel ADAM
 */
public class TravellerAgent extends GuiAgent {
    /**
     * code pour ajout de livre par la gui
     */
    public static final int EXIT = 0;
    /**
     * code pour achat de livre par la gui
     */
    public static final int BUY_TRAVEL = 1;

    /**
     * liste des vendeurs
     */
    private ArrayList<AID> vendeurs;

    /**
     * catalog received by the sellers
     */
    private JourneysList catalogs;

    /**
     * delay in minute between two rides in a junction (in minutes)
     * */
    int delay = 90;

    /**
     * the journey chosen by the agent
     */
    private ComposedJourney myJourney;

    /**
     * topic from which the alert will be received
     */
    private AID topic;

    /**
     * gui
     */
    private TravellerGui window;
    
    /**
     * Liste des trajets réservés par l'agent (source de vérité)
     */
    private List<ComposedJourney> bookedJourneys;

    /**
     * Text enhancement service for improving communication
     */
    private TextEnhancementService textEnhancer;

    /**
     * Cache pour éviter les traitements multiples d'annulation
     */
    private java.util.Set<String> processedCancellations = new java.util.HashSet<>();

    /**
     * Budget pour les enchères de billets
     */
    private double auctionBudget = 50.0;

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        this.window = new TravellerGui(this);
        window.setColor(Color.cyan);
        
        // Initialize text enhancement service
        textEnhancer = TextEnhancementService.getInstance();
        
        // Initialize booked journeys list
        this.bookedJourneys = new ArrayList<>();
        
        // Enregistrement en tant que client voyageur (pour les enchères)
        AgentServicesTools.register(this, "travel-client", "traveller");
        
        // Démarrer l'écoute des enchères de billets
        startAuctionListener();
        
        // Message de bienvenue simplifié (pas de logs techniques)
        window.println("🎉 Bienvenue dans votre assistant de voyage !");
        window.println("Utilisez le formulaire ci-dessus pour rechercher un trajet.");
        
        // Les logs techniques vont dans la zone debug cachée
        if (textEnhancer.isAvailable()) {
            window.printDebug("Service d'amélioration de texte activé");
        }
        
        window.setVisible(true);

        vendeurs = new ArrayList<>();
        detectAgences();

        topic = AgentServicesTools.generateTopicAID(this, "TRAFFIC NEWS");
        //ecoute des messages radio avec gestion intelligente des alertes
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topic), true, (a, m) -> {
            // Vérifier d'abord si ce message a déjà été traité
            String messageContent = m.getContent();
            if (messageContent != null && !messageContent.isEmpty()) {
                // Créer une clé unique pour ce message
                String messageKey = "MSG_" + messageContent.hashCode();
                
                // Ne traiter que si pas déjà traité
                if (!isAlreadyProcessed(messageKey)) {
                    markAsProcessed(messageKey);
                    // Afficher brièvement l'alerte
                    window.println("🚨 Alerte trafic reçue: " + messageContent);
                    
                    // Déclencher le gestionnaire d'alertes intelligent
                    addBehaviour(new comportements.ClientAlertHandler(this, messageContent));
                }
            }
        }));
    }


    /**
     * ecoute des evenement de type enregistrement en tant qu'agence aupres des pages jaunes
     */
    private void detectAgences() {
        var model = AgentServicesTools.createAgentDescription("travel agency", "seller");
        vendeurs = new ArrayList<>();

        //souscription au service des pages jaunes pour recevoir une alerte en cas mouvement sur le service travel agency'seller
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) {
                vendeurs.add(dfd.getName());
                // Envoyer les logs d'enregistrement des agences dans la zone debug
                window.printDebug("Agence connectée: " + dfd.getName().getLocalName());
            }

            @Override
            public void onDeregister(DFAgentDescription dfd) {
                vendeurs.remove(dfd.getName());
                // Envoyer les logs de désenregistrement des agences dans la zone debug
                window.printDebug("Agence déconnectée: " + dfd.getName().getLocalName());
            }

        });

    }

    /**
     * compute a composed journey from a departure to an arrival point
     * @param from       departure point
     * @param to         arrival point
     * @param departure  desired departure time (in hhmm)
     * @param preference preference for the choice of the journey (cost, confort, duration, duration-cost)
     * */
    public void computeComposedJourney(final String from, final String to, final int departure,
                                       final String preference) {
        computeComposedJourney(from, to, departure, preference, "any");
    }

    /**
     * compute a composed journey from a departure to an arrival point with transport type filter
     * @param from       departure point
     * @param to         arrival point
     * @param departure  desired departure time (in hhmm)
     * @param preference preference for the choice of the journey (cost, confort, duration, duration-cost)
     * @param transportType preferred transport type (bus, car, bike, tram, any)
     * */
    public void computeComposedJourney(final String from, final String to, final int departure,
                                       final String preference, final String transportType) {
        final List<ComposedJourney> journeys = new ArrayList<>();

        final boolean result = catalogs.findIndirectJourney(from, to, departure, 60, new ArrayList<>(),
                new ArrayList<>(), journeys);

        if (!result) {
            println("😔 Aucun trajet trouvé !\n\n" +
                   "💡 Suggestions :\n" +
                   "• Essayez des horaires différents\n" +
                   "• Vérifiez vos stations de départ et d'arrivée\n" +
                   "• Considérez d'autres moyens de transport\n" +
                   "• Modifiez vos critères de recherche");
            return;
        }
        
        // Filter by transport type if specified
        if (transportType != null && !transportType.equals("any")) {
            journeys.removeIf(journey -> 
                journey.getJourneys().stream()
                    .anyMatch(j -> !j.getMeans().toLowerCase().contains(transportType.toLowerCase()))
            );
            
            if (journeys.isEmpty()) {
                println(String.format("😔 Aucun trajet trouvé avec le transport '%s' !\n\n" +
                       "💡 Essayez :\n" +
                       "• 'any' pour voir tous les transports disponibles\n" +
                       "• D'autres types : bus, car, bike, tram\n" +
                       "• Des horaires différents", transportType));
                return;
            }
        }
        
        //oter les voyages demarrant trop tard
        journeys.removeIf(j -> j.getJourneys().getFirst().getDepartureDate() - departure > delay);
        
        if (journeys.isEmpty()) {
            println("⏰ Aucun trajet trouvé dans vos créneaux horaires !\n\n" +
                   "💡 Solutions :\n" +
                   "• Élargissez votre plage horaire\n" +
                   "• Essayez un départ plus tôt ou plus tard\n" +
                   "• Vérifiez les horaires de service des transports");
            return;
        }
        
        switch (preference) {
            case "duration" -> {
                journeys.sort(Comparator.comparingDouble(ComposedJourney::getDuration));
            }
            case "confort" -> journeys.sort(Comparator.comparingInt(ComposedJourney::getConfort).reversed());
            case "cost" -> journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
            case "duration-cost" ->
            //        journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
            journeys.sort((j1, j2) -> {
                var difDuration = j1.getDuration() - j2.getDuration() / Math.max(j2.getDuration(),j1.getDuration());
                var difCost = j1.getCost() - j2.getCost() / Math.max(j2.getCost(),j1.getCost());
                return (int)(10*(difDuration + difCost));});
            default -> journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
        }
        myJourney = journeys.getFirst();
        
        // Sauvegarder les préférences de recherche originales dans le trajet
        if (myJourney != null) {
            myJourney.setOriginalCriteria(preference);
            myJourney.setOriginalTransportType(transportType);
            System.out.println("DEBUG: Préférences sauvegardées - Critère: " + preference + ", Transport: " + transportType);
        }
        
        // Affichage naturel du voyage sélectionné et demande de confirmation
        String naturalMessage = formatJourneyNaturally(myJourney);
        String tripSummary = createTripSummary(myJourney);
        
        // Utiliser la méthode avec gestion complète des stocks et objets ComposedJourney
        window.addBookedTripWithConfirmation(myJourney, tripSummary, naturalMessage);
    }

    /**
     * get event from the GUI
     */
    @Override
    protected void onGuiEvent(final GuiEvent eventFromGui) {
        if (eventFromGui.getType() == TravellerAgent.EXIT) {
            doDelete();
        }
        if (eventFromGui.getType() == TravellerAgent.BUY_TRAVEL) {
            // Handle both old (4 params) and new (5 params) format for backward compatibility
            String departure = (String) eventFromGui.getParameter(0);
            String arrival = (String) eventFromGui.getParameter(1);
            Integer time = (Integer) eventFromGui.getParameter(2);
            String criteria = (String) eventFromGui.getParameter(3);
            String transportType = "any"; // default value
            
            // Check if transport type parameter is provided
            try {
                transportType = (String) eventFromGui.getParameter(4);
            } catch (IndexOutOfBoundsException e) {
                // Transport type not provided, use default
                transportType = "any";
            }
            
            // Log technique dans la zone debug
            String logMsg = String.format("Requête de voyage - De: %s, Vers: %s, Heure: %d, Critère: %s, Transport: %s", 
                    departure, arrival, time, criteria, transportType);
            window.printDebug(logMsg);
            
            addBehaviour(new ContractNetAchat(this, new ACLMessage(ACLMessage.CFP),
                    departure, arrival, time, criteria, transportType));
        }
    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        if (window != null) {
            window.dispose();
            System.out.println(getLocalName() + ">>> I leave the platform. ");
        }
    }

    ///// SETTERS AND GETTERS

    /**
     * @return agent gui
     */
    public TravellerGui getWindow() {
        return window;
    }


    /**
     * @return the vendeurs
     */
    public List<AID> getVendeurs() {
        return (ArrayList<AID>) vendeurs.clone();
    }


    /**
     * print a message on the window lined to the agent
     *
     * @param msg text to display in th window
     */
    public void println(final String msg) {
        window.println(msg);
    }

    /**
     * print an enhanced message using text enhancement service
     *
     * @param msg         text to enhance and display
     * @param messageType type of message for appropriate enhancement
     */
    public void printlnEnhanced(final String msg, TextEnhancementService.MessageType messageType) {
        String enhancedMsg = textEnhancer.enhanceMessage(msg, messageType);
        window.println(enhancedMsg);
        
        // Show original in small text if different
        if (!enhancedMsg.equals(msg) && textEnhancer.isAvailable()) {
            window.println("  [Original: " + msg + "]");
        }
    }

    /**
     * set the list of journeys
     */
    public void setCatalogs(final JourneysList catalogs) {
        this.catalogs = catalogs;
    }


    /**
     * Crée un résumé court du trajet pour la liste des trajets réservés
     */
    private String createTripSummary(ComposedJourney journey) {
        if (journey == null || journey.getJourneys() == null || journey.getJourneys().isEmpty()) {
            return "Trajet non valide";
        }
        
        int duration = (int)journey.getDuration();
        double cost = journey.getCost();
        
        // Créer le trajet complet avec toutes les étapes
        StringBuilder routeBuilder = new StringBuilder();
        List<data.Journey> journeys = journey.getJourneys();
        
        // Ajouter tous les points du trajet (A → B → E)
        for (int i = 0; i < journeys.size(); i++) {
            if (i == 0) {
                // Premier segment : ajouter départ et arrivée
                routeBuilder.append(journeys.get(i).getStart()).append(" → ").append(journeys.get(i).getStop());
            } else {
                // Segments suivants : ajouter seulement l'arrivée
                routeBuilder.append(" → ").append(journeys.get(i).getStop());
            }
        }
        
        // Obtenir les types de transport utilisés
        String transports = journey.getJourneys().stream()
            .map(j -> getTransportEmoji(j.getMeans()).split(" ")[1]) // Enlever l'emoji
            .distinct()
            .reduce((t1, t2) -> t1 + "+" + t2)
            .orElse("Transport");
        
        // Obtenir l'heure de départ du premier segment
        int departureTime = journeys.get(0).getDepartureDate();
        
        return String.format("%s - %s | %s | %d min | %.2f€", 
                formatTime(departureTime), routeBuilder.toString(), transports, duration, cost);
    }

    /**
     * Formate un voyage de manière naturelle et conviviale
     */
    private String formatJourneyNaturally(ComposedJourney journey) {
        if (journey == null || journey.getJourneys() == null || journey.getJourneys().isEmpty()) {
            return "😔 Désolé, aucun trajet n'a été trouvé pour votre demande.";
        }

        StringBuilder naturalMsg = new StringBuilder();
        
        // Message d'introduction
        naturalMsg.append("🎯 Parfait ! J'ai trouvé le meilleur trajet pour vous :\n\n");
        
        // Informations générales du voyage
        String fromStation = journey.getJourneys().get(0).getStart();
        String toStation = journey.getJourneys().get(journey.getJourneys().size()-1).getStop();
        
        naturalMsg.append(String.format("📍 De %s à %s\n", fromStation, toStation));
        naturalMsg.append(String.format("⏱️ Durée totale : %d minutes\n", (int)journey.getDuration()));
        naturalMsg.append(String.format("💰 Coût total : %.2f€\n", journey.getCost()));
        naturalMsg.append(String.format("⭐ Confort : %d/5\n\n", journey.getConfort()));
        
        // Détail des étapes
        if (journey.getJourneys().size() == 1) {
            var singleJourney = journey.getJourneys().get(0);
            naturalMsg.append("🚌 Trajet direct :\n");
            naturalMsg.append(String.format("   • %s de %s à %s\n", 
                getTransportEmoji(singleJourney.getMeans()), 
                singleJourney.getStart(), 
                singleJourney.getStop()));
            naturalMsg.append(String.format("   • Départ : %s - Arrivée : %s\n", 
                formatTime(singleJourney.getDepartureDate()), 
                formatTime(singleJourney.getArrivalDate())));
        } else {
            naturalMsg.append("🔄 Trajet avec correspondances :\n");
            for (int i = 0; i < journey.getJourneys().size(); i++) {
                var step = journey.getJourneys().get(i);
                naturalMsg.append(String.format("   %d. %s de %s à %s (%s - %s)\n", 
                    i + 1,
                    getTransportEmoji(step.getMeans()), 
                    step.getStart(), 
                    step.getStop(),
                    formatTime(step.getDepartureDate()),
                    formatTime(step.getArrivalDate())));
            }
        }
        
        naturalMsg.append("\n✅ Réservation en cours...");
        
        return naturalMsg.toString();
    }
    
    /**
     * Retourne l'emoji correspondant au type de transport
     */
    private String getTransportEmoji(String transportType) {
        return switch (transportType.toLowerCase()) {
            case "bus" -> "🚌 Bus";
            case "car" -> "🚗 Voiture";
            case "bike" -> "🚲 Vélo";
            case "tram" -> "🚊 Tramway";
            default -> "🚌 " + transportType;
        };
    }
    
    /**
     * Formate l'heure de façon lisible
     */
    private String formatTime(int time) {
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%02d:%02d", hours, minutes);
    }

    public ComposedJourney getMyJourney() {
        return myJourney;
    }

    /**
     * Récupère la liste de tous les trajets réservés par le client
     * @return Liste des trajets réservés
     */
    public List<ComposedJourney> getBookedJourneys() {
        return new ArrayList<>(bookedJourneys);
    }
    
    /**
     * Ajoute un trajet à la liste des réservations
     * @param journey Le trajet à ajouter
     */
    public void addBookedJourney(ComposedJourney journey) {
        if (journey != null) {
            bookedJourneys.add(journey);
            System.out.println("📝 Trajet ajouté aux réservations de l'agent: " + journey.getJourneys().get(0).getStart() + 
                             " → " + journey.getJourneys().get(journey.getJourneys().size()-1).getStop());
        }
    }
    
    /**
     * Supprime un trajet de la liste des réservations
     * @param journey Le trajet à supprimer
     * @return true si le trajet a été supprimé, false sinon
     */
    public boolean removeBookedJourney(ComposedJourney journey) {
        if (journey != null && bookedJourneys.remove(journey)) {
            System.out.println("❌ Trajet supprimé des réservations de l'agent: " + journey.getJourneys().get(0).getStart() + 
                             " → " + journey.getJourneys().get(journey.getJourneys().size()-1).getStop());
            return true;
        }
        return false;
    }
    
    /**
     * Supprime un trajet annulé des réservations via la GUI
     */
    public void removeCancelledJourney(String start, String stop, String means, int departure) {
        if (window != null) {
            window.removeCancelledJourney(start, stop, means, departure);
        }
    }
    
    /**
     * Vérifie si une annulation a déjà été traitée
     */
    public boolean isAlreadyProcessed(String cancellationKey) {
        return processedCancellations.contains(cancellationKey);
    }
    
    /**
     * Marque une annulation comme traitée
     */
    public void markAsProcessed(String cancellationKey) {
        processedCancellations.add(cancellationKey);
    }

    /**
     * Book places for a composed journey and update stock
     * @param journey the journey to book places for
     */
    public void bookJourneyPlaces(ComposedJourney journey) {
        if (journey != null && journey.getJourneys() != null) {
            journey.getJourneys().forEach(segment -> {
                segment.bookPlace();
            });
        }
    }

    /**
     * Cancel places for a composed journey and restore stock
     * @param journey the journey to cancel places for
     */
    public void cancelJourneyPlaces(ComposedJourney journey) {
        if (journey != null && journey.getJourneys() != null) {
            journey.getJourneys().forEach(segment -> {
                segment.cancelBooking();
            });
        }
    }

    /**
     * Démarre l'écoute des enchères de billets d'autres clients
     */
    private void startAuctionListener() {
        jade.lang.acl.MessageTemplate template = jade.lang.acl.MessageTemplate.and(
            jade.lang.acl.MessageTemplate.MatchProtocol(jade.domain.FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            jade.lang.acl.MessageTemplate.MatchPerformative(jade.lang.acl.ACLMessage.CFP)
        );
        
        addBehaviour(new TicketAuctionResponder(this, template, auctionBudget, null));
        window.printDebug("Écoute des enchères de billets activée (budget: " + String.format("%.2f€", auctionBudget) + ")");
    }

    /**
     * Lance une enchère pour revendre un billet
     * @param ticket Le billet à revendre
     * @param minPrice Le prix minimum accepté
     */
    public void startTicketAuction(Journey ticket, double minPrice) {
        if (ticket == null) {
            window.println("❌ Impossible de lancer l'enchère: billet invalide");
            return;
        }
        
        window.println("🔔 Lancement de l'enchère pour le billet: " + 
                      ticket.getStart() + " → " + ticket.getStop());
        window.println("   💰 Prix minimum: " + String.format("%.2f€", minPrice));
        
        addBehaviour(new TicketAuctionInitiator(this, ticket, minPrice));
    }

    /**
     * Lance une enchère pour revendre un billet avec prix minimum = 50% du prix original
     * @param ticket Le billet à revendre
     */
    public void startTicketAuction(Journey ticket) {
        if (ticket != null) {
            double minPrice = ticket.getCost() * 0.5; // 50% du prix original par défaut
            startTicketAuction(ticket, minPrice);
        }
    }

    /**
     * Propose à l'utilisateur de mettre un billet en enchère
     * @param ticket Le billet à potentiellement revendre
     * @param reason La raison de la revente (ex: annulation)
     * @return true si l'utilisateur accepte de mettre en enchère
     */
    public boolean proposeAuction(Journey ticket, String reason) {
        if (ticket == null) return false;
        
        StringBuilder message = new StringBuilder();
        message.append("🎫 PROPOSITION DE REVENTE\n\n");
        message.append("Suite à: ").append(reason).append("\n\n");
        message.append("Voulez-vous mettre ce billet en enchère?\n\n");
        message.append("📍 Trajet: ").append(ticket.getStart()).append(" → ").append(ticket.getStop()).append("\n");
        message.append("🚌 Transport: ").append(ticket.getMeans()).append("\n");
        message.append("💰 Prix original: ").append(String.format("%.2f€", ticket.getCost())).append("\n");
        message.append("💵 Prix minimum suggéré: ").append(String.format("%.2f€", ticket.getCost() * 0.5)).append("\n\n");
        message.append("Les autres voyageurs et services spécialisés seront notifiés.");
        
        int response = javax.swing.JOptionPane.showConfirmDialog(null,
            message.toString(),
            "🔔 Revente de billet",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE);
        
        if (response == javax.swing.JOptionPane.YES_OPTION) {
            // Demander le prix minimum
            String priceInput = javax.swing.JOptionPane.showInputDialog(null,
                "Entrez le prix minimum souhaité (suggestion: " + 
                String.format("%.2f€", ticket.getCost() * 0.5) + "):",
                "💰 Prix minimum",
                javax.swing.JOptionPane.QUESTION_MESSAGE);
            
            if (priceInput != null && !priceInput.trim().isEmpty()) {
                try {
                    double minPrice = Double.parseDouble(priceInput.replace(",", ".").replace("€", "").trim());
                    startTicketAuction(ticket, minPrice);
                    return true;
                } catch (NumberFormatException e) {
                    window.println("⚠️ Format de prix invalide, utilisation du prix par défaut");
                    startTicketAuction(ticket);
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Propose la revente de plusieurs billets liés à une annulation
     * @param tickets Liste des billets à potentiellement revendre
     * @param reason Raison de la proposition
     */
    public void proposeMultipleAuctions(List<Journey> tickets, String reason) {
        if (tickets == null || tickets.isEmpty()) return;
        
        StringBuilder message = new StringBuilder();
        message.append("🎫 BILLETS DISPONIBLES POUR REVENTE\n\n");
        message.append("Suite à: ").append(reason).append("\n\n");
        message.append("Les billets suivants ne sont plus utiles:\n\n");
        
        for (int i = 0; i < tickets.size(); i++) {
            Journey ticket = tickets.get(i);
            message.append(String.format("%d. %s → %s (%s) - %.2f€\n",
                i + 1, ticket.getStart(), ticket.getStop(), 
                ticket.getMeans(), ticket.getCost()));
        }
        
        message.append("\nVoulez-vous les mettre en enchère?");
        
        int response = javax.swing.JOptionPane.showConfirmDialog(null,
            message.toString(),
            "🔔 Revente de billets",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE);
        
        if (response == javax.swing.JOptionPane.YES_OPTION) {
            for (Journey ticket : tickets) {
                startTicketAuction(ticket);
            }
            window.println("🔔 " + tickets.size() + " billet(s) mis en enchère");
        }
    }

    /**
     * Définit le budget pour les enchères
     * @param budget Le nouveau budget
     */
    public void setAuctionBudget(double budget) {
        this.auctionBudget = budget;
        window.printDebug("Budget d'enchère mis à jour: " + String.format("%.2f€", budget));
    }

    /**
     * Récupère le budget pour les enchères
     * @return Le budget actuel
     */
    public double getAuctionBudget() {
        return auctionBudget;
    }

}
