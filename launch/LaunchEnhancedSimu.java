package launch;

import agents.TravellerAgent;
import agents.AgenceAgent;

/**
 * Launch class for testing the enhanced travel system with Ollama integration
 * This class starts both traveller and agency agents to test the complete workflow
 * 
 * @author Assistant
 */
public class LaunchEnhancedSimu {
    
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        
        // Build agent string with both traveller and agencies
        StringBuilder sbAgents = new StringBuilder();
        
        // Add a traveller agent with enhanced GUI
        sbAgents.append("traveller1:agents.TravellerAgent;");
        
        // Add some agency agents
        sbAgents.append("agenceBus:agents.AgenceAgent(bus.csv);");
        sbAgents.append("agenceCar:agents.AgenceAgent(car.csv);");
        sbAgents.append("agenceBike:agents.AgenceAgent(bike.csv);");
        sbAgents.append("agenceTram:agents.AgenceAgent(tram.csv);");
        
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        
        System.out.println("=== Enhanced Travel Simulation with Ollama Integration ===");
        System.out.println("Starting agents: " + sbAgents.toString());
        System.out.println("");
        System.out.println("Features:");
        System.out.println("✓ Natural language travel requests");
        System.out.println("✓ AI-powered request analysis");
        System.out.println("✓ Transport type filtering");
        System.out.println("✓ Weather-aware suggestions");
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
}