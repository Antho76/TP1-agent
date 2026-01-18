package agents;

import jade.core.Agent;
import jade.core.AgentServicesTools;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import comportements.TicketAuctionResponder;

/**
 * Agent acheteur de billets aux enchères
 * Peut être générique (tous types) ou spécialisé (bus, tram, car, bike)
 * 
 * Les agents acheteurs s'inscrivent auprès des pages jaunes pour être notifiés
 * des enchères de billets
 * 
 * Types de services :
 * - "ticket-buyer" : acheteur générique
 * - "ticket-buyer-bus" : acheteur spécialisé bus
 * - "ticket-buyer-tram" : acheteur spécialisé tram
 * - "ticket-buyer-car" : acheteur spécialisé voiture
 * - "ticket-buyer-bike" : acheteur spécialisé vélo
 * 
 * @author Agent TP
 */
public class BuyerAgent extends Agent {

    private String[] specializedTransports = null;
    private double budget = 50.0; // Budget par défaut
    private boolean isAutomatic = true; // Mode automatique par défaut

    @Override
    protected void setup() {
        System.out.println("🛒 Agent acheteur " + getLocalName() + " démarré");

        // Récupérer les arguments
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            parseArguments(args);
        }

        // Enregistrement auprès des pages jaunes
        registerServices();

        // Démarrer le comportement d'écoute des enchères
        startAuctionListener();

        System.out.println("✅ " + getLocalName() + " prêt à participer aux enchères");
        System.out.println("   💰 Budget: " + String.format("%.2f€", budget));
        if (specializedTransports != null) {
            System.out.println("   🚌 Spécialisation: " + String.join(", ", specializedTransports));
        } else {
            System.out.println("   🚌 Spécialisation: Tous types");
        }
    }

    /**
     * Parse les arguments de l'agent
     * Format: budget, type1, type2, ...
     * Exemple: "30.0", "bus", "tram"
     */
    private void parseArguments(Object[] args) {
        try {
            // Premier argument : budget
            if (args[0] instanceof String) {
                budget = Double.parseDouble((String) args[0]);
            } else if (args[0] instanceof Number) {
                budget = ((Number) args[0]).doubleValue();
            }

            // Arguments suivants : types de transport
            if (args.length > 1) {
                specializedTransports = new String[args.length - 1];
                for (int i = 1; i < args.length; i++) {
                    specializedTransports[i - 1] = args[i].toString().toLowerCase();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur parsing arguments: " + e.getMessage());
        }
    }

    /**
     * Enregistre l'agent auprès des pages jaunes
     */
    private void registerServices() {
        // Service générique
        AgentServicesTools.register(this, "ticket-market", "ticket-buyer");

        // Services spécialisés si définis
        if (specializedTransports != null) {
            for (String transport : specializedTransports) {
                String serviceName = "ticket-buyer-" + transport;
                AgentServicesTools.register(this, "ticket-market", serviceName);
                System.out.println("   📝 Enregistré pour: " + serviceName);
            }
        }
    }

    /**
     * Démarre le comportement d'écoute des enchères
     */
    private void startAuctionListener() {
        MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP)
        );

        addBehaviour(new TicketAuctionResponder(this, template, budget, specializedTransports));
    }

    @Override
    protected void takeDown() {
        // Désinscription des pages jaunes
        AgentServicesTools.deregisterAll(this);
        System.out.println("👋 Agent acheteur " + getLocalName() + " arrêté");
    }

    // Getters
    public double getBudget() { return budget; }
    public String[] getSpecializedTransports() { return specializedTransports; }
    public boolean isAutomatic() { return isAutomatic; }

    // Setters
    public void setBudget(double budget) { this.budget = budget; }
    public void setAutomatic(boolean automatic) { isAutomatic = automatic; }
}
