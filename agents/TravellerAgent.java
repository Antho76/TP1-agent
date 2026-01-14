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
import data.ComposedJourney;
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
     * Text enhancement service for improving communication
     */
    private TextEnhancementService textEnhancer;

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        this.window = new TravellerGui(this);
        window.setColor(Color.cyan);
        
        // Initialize text enhancement service
        textEnhancer = TextEnhancementService.getInstance();
        
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
        //ecoute des messages radio
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topic), true, (a, m) -> {
            // Les alertes de trafic sont affichées à l'utilisateur
            window.println("🚨 Info trafic: " + m.getContent());
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
        
        // Affichage naturel du voyage sélectionné et demande de confirmation
        String naturalMessage = formatJourneyNaturally(myJourney);
        String tripSummary = createTripSummary(myJourney);
        
        // Utiliser la nouvelle méthode avec confirmation de l'interface
        window.addBookedTripWithConfirmation(tripSummary, naturalMessage);
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
        
        String fromStation = journey.getJourneys().get(0).getStart();
        String toStation = journey.getJourneys().get(journey.getJourneys().size()-1).getStop();
        int duration = (int)journey.getDuration();
        double cost = journey.getCost();
        
        // Obtenir les types de transport utilisés
        String transports = journey.getJourneys().stream()
            .map(j -> getTransportEmoji(j.getMeans()).split(" ")[1]) // Enlever l'emoji
            .distinct()
            .reduce((t1, t2) -> t1 + "+" + t2)
            .orElse("Transport");
        
        return String.format("%s → %s | %s | %d min | %.2f€", 
                fromStation, toStation, transports, duration, cost);
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

}
