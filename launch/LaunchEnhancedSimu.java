package launch;

import javax.swing.*;

/**
 * Launch class for testing the enhanced travel system with Ollama integration
 * This class starts both traveller and agency agents to test the complete workflow
 * 
 * @author Assistant
 */
public class LaunchEnhancedSimu {
    
    public static void main(String[] args) {
        // Demander le nombre de clients à démarrer
        int nbClients = askNumberOfClients();
        
        System.out.println("🚀 Démarrage de la simulation améliorée avec " + nbClients + " client(s)");
        
        String[] jadeArgs = new String[2];
        
        // Build agent string with both traveller and agencies
        StringBuilder sbAgents = new StringBuilder();
        
        // Créer plusieurs clients selon le choix de l'utilisateur
        for (int i = 1; i <= nbClients; i++) {
            sbAgents.append("traveller").append(i).append(":agents.TravellerAgent;");
        }
        
        // Add some agency agents
        sbAgents.append("agenceBus:agents.AgenceAgent(bus.csv);");
        sbAgents.append("agenceCar:agents.AgenceAgent(car.csv);");
        sbAgents.append("agenceBike:agents.AgenceAgent(bike.csv);");
        sbAgents.append("agenceTram:agents.AgenceAgent(tram.csv);");
        
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        
        System.out.println("=== Enhanced Travel Simulation with Ollama Integration ===");
        System.out.println("Starting " + nbClients + " agent(s): " + sbAgents.toString());
        System.out.println("");
        System.out.println("Features:");
        System.out.println("✓ Natural language travel requests");
        System.out.println("✓ AI-powered request analysis");
        System.out.println("✓ Transport type filtering");
        System.out.println("✓ Weather-aware suggestions");
        System.out.println("✓ Multi-client simulation");
        System.out.println("");
        System.out.println("Prerequisites:");
        System.out.println("- Ollama running on http://localhost:11434");
        System.out.println("- Model 'llama3.2:latest' available");
        System.out.println("");
        System.out.println("Example requests to try:");
        System.out.println("'Je veux aller de a vers c à 9h en bus, option économique'");
        System.out.println("'Besoin d'un trajet de b à f en vélo, le plus rapide'");
        System.out.println("'Transport de d à e vers 14h30 en tram, maximum confort'");
        System.out.println("");
        
        jade.Boot.main(jadeArgs);
    }
    
    /**
     * Demande à l'utilisateur combien de clients il souhaite démarrer
     * @return Le nombre de clients (entre 1 et 10)
     */
    private static int askNumberOfClients() {
        String[] options = {"1 client", "2 clients", "3 clients", "4 clients", "5 clients", 
                           "6 clients", "7 clients", "8 clients", "9 clients", "10 clients"};
        
        String message = "🚀 Configuration du Système Multi-Agents Amélioré (Ollama)\n\n" +
                        "Combien de voyageurs (TravellerAgent) souhaitez-vous démarrer ?\n\n" +
                        "💡 Plusieurs clients permettent de :\n" +
                        "   • Tester les interactions multi-agents\n" +
                        "   • Observer la concurrence pour les places\n" +
                        "   • Simuler des alertes affectant plusieurs voyageurs\n" +
                        "   • Comparer différentes stratégies de recherche\n" +
                        "   • Tester les requêtes en langage naturel (Ollama)";
        
        int choice = JOptionPane.showOptionDialog(
            null,
            message,
            "🎯 Nombre de Clients (Enhanced)",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        // Si l'utilisateur ferme la fenêtre, démarrer avec 1 client par défaut
        if (choice == -1) {
            System.out.println("⚠️ Aucun choix, démarrage avec 1 client par défaut");
            return 1;
        }
        
        return choice + 1; // choice va de 0 à 9, donc +1 pour avoir 1 à 10
    }
}
