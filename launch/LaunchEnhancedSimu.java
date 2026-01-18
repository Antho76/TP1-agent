package launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import javax.swing.*;

/**
 * Launch class for testing the enhanced travel system with Ollama integration
 * This class starts both traveller and agency agents to test the complete workflow
 * with Ollama/LLM support for natural language processing.
 * 
 * ⚠️ IMPORTANT: Requires Ollama running on http://localhost:11434
 * 
 * @author Assistant
 */
public class LaunchEnhancedSimu {
    
    public static void main(String[] args) {
        try {
            // Demander le nombre de clients à démarrer
            int nbClients = askNumberOfClients();
            
            System.out.println("🚀 Démarrage de la simulation améliorée avec " + nbClients + " client(s)");
            
            // Configuration JADE avec TopicManagementService
            Runtime rt = Runtime.instance();
            ExtendedProperties props = new ExtendedProperties();
            props.setProperty(Profile.GUI, "true");
            // IMPORTANT: Activer le service TopicManagement pour les alertes et communications
            props.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");
            Profile profile = new ProfileImpl(props);
            AgentContainer container = rt.createMainContainer(profile);
            
            System.out.println("═".repeat(60));
            System.out.println("🚀 DÉMARRAGE DU SYSTÈME AVEC OLLAMA (IA)");
            System.out.println("═".repeat(60));
            
            // Créer les agents agences
            System.out.println("\n📦 Création des agences de transport...");
            container.createNewAgent("agenceBus", "agents.AgenceAgent", 
                new Object[]{"bus.csv", "bus"}).start();
            container.createNewAgent("agenceTram", "agents.AgenceAgent", 
                new Object[]{"tram.csv", "tram"}).start();
            container.createNewAgent("agenceCar", "agents.AgenceAgent", 
                new Object[]{"car.csv", "car"}).start();
            container.createNewAgent("agenceBike", "agents.AgenceAgent", 
                new Object[]{"bike.csv", "bike"}).start();
            
            Thread.sleep(500);
            
            // Créer les agents voyageurs
            System.out.println("\n👥 Création de " + nbClients + " voyageur(s)...");
            for (int i = 1; i <= nbClients; i++) {
                container.createNewAgent("traveller" + i, "agents.TravellerAgent", null).start();
                System.out.println("   ✅ traveller" + i + " créé");
                Thread.sleep(200);
            }
            
            Thread.sleep(500);
            
            // Créer l'agent d'alertes
            System.out.println("\n🚨 Création de l'agent d'alertes...");
            container.createNewAgent("AlertManager", "agents.AlertAgent", null).start();
            
            System.out.println("\n" + "═".repeat(60));
            System.out.println("✅ SYSTÈME AVEC IA PRÊT!");
            System.out.println("═".repeat(60));
            System.out.println("\n📋 CONFIGURATION:");
            System.out.println("   🚌 4 Agences de transport");
            System.out.println("   👥 " + nbClients + " Voyageur(s)");
            System.out.println("   🤖 IA Ollama (langage naturel)");
            System.out.println("    1 Agent d'alertes");
            
            System.out.println("\n📝 EXEMPLE DE REQUÊTE EN LANGAGE NATUREL:");
            System.out.println("   'Je veux aller de a vers c à 9h en bus, option économique'");
            System.out.println("   'Besoin d'un trajet de b à f en vélo, le plus rapide'");
            System.out.println("   'Transport de d à e vers 14h30 en tram, maximum confort'");
            
            System.out.println("\n⚠️  PRÉREQUIS:");
            System.out.println("   • Ollama doit tourner: http://localhost:11434");
            System.out.println("   • Modèle disponible: llama3.2:latest ou granite3.3:latest");
            System.out.println("═".repeat(60) + "\n");
            
        } catch (StaleProxyException e) {
            System.err.println("❌ Erreur lors de la création des agents: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Interruption lors du démarrage: " + e.getMessage());
        }
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
