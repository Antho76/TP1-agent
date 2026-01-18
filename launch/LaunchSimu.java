package launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Lanceur de simulation avec système d'enchères de billets
 * 
 * Inclut :
 * - Agents agences (bus, tram, voiture, vélo)
 * - Agents voyageurs (nombre configurable)
 * - Agents acheteurs spécialisés pour les enchères
 * - Agent d'alertes
 * 
 * @author Agent TP
 */
public class LaunchSimu {
    
    public static final Logger logger = Logger.getLogger(LaunchSimu.class.getName());

    public static void main(String[] args) {
        try {
            // Demander le nombre de voyageurs à créer
            int nbVoyageurs = askNumberOfTravellers();
            if (nbVoyageurs <= 0) {
                System.out.println("❌ Lancement annulé.");
                return;
            }
            
            // Demander le nombre d'acheteurs
            int nbAcheteurs = askNumberOfBuyers();
            if (nbAcheteurs < 0) {
                System.out.println("❌ Lancement annulé.");
                return;
            }
            
            // Configuration JADE avec TopicManagementService
            Runtime rt = Runtime.instance();
            ExtendedProperties props = new ExtendedProperties();
            props.setProperty(Profile.GUI, "true");
            // IMPORTANT: Activer le service TopicManagement pour les alertes
            props.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");
            Profile profile = new ProfileImpl(props);
            AgentContainer container = rt.createMainContainer(profile);
            
            System.out.println("═".repeat(60));
            System.out.println("🚀 DÉMARRAGE DU SYSTÈME MULTI-AGENTS AVEC ENCHÈRES");
            System.out.println("═".repeat(60));
            
            // Créer les agents agences
            System.out.println("\n📦 Création des agences de transport...");
            container.createNewAgent("AgenceBus", "agents.AgenceAgent", 
                new Object[]{"bus.csv", "bus"}).start();
            container.createNewAgent("AgenceTram", "agents.AgenceAgent", 
                new Object[]{"tram.csv", "tram"}).start();
            container.createNewAgent("AgenceVoiture", "agents.AgenceAgent", 
                new Object[]{"car.csv", "car"}).start();
            container.createNewAgent("AgenceVelo", "agents.AgenceAgent", 
                new Object[]{"bike.csv", "bike"}).start();
            
            Thread.sleep(500);
            
            // Créer les agents voyageurs
            System.out.println("\n👥 Création de " + nbVoyageurs + " voyageur(s)...");
            for (int i = 1; i <= nbVoyageurs; i++) {
                container.createNewAgent("Voyageur" + i, "agents.TravellerAgent", null).start();
                System.out.println("   ✅ Voyageur" + i + " créé");
                Thread.sleep(200);
            }
            
            Thread.sleep(500);
            
            // Créer l'agent d'alertes
            System.out.println("\n🚨 Création de l'agent d'alertes...");
            container.createNewAgent("AlertManager", "agents.AlertAgent", null).start();
            
            Thread.sleep(500);
            
            // Créer les agents acheteurs (pour les enchères)
            System.out.println("\n🏪 Création des agents acheteurs (enchères)...");
            for (int i = 1; i <= nbAcheteurs; i++) {
                container.createNewAgent("Acheteur" + i, "agents.BuyerAgent", 
                    new Object[]{"Voyageur" + (i % nbVoyageurs + 1)}).start();
                System.out.println("   ✅ Acheteur" + i + " créé");
                Thread.sleep(100);
            }
            
            System.out.println("\n" + "═".repeat(60));
            System.out.println("✅ SYSTÈME PRÊT!");
            System.out.println("═".repeat(60));
            System.out.println("\n📋 RÉCAPITULATIF:");
            System.out.println("   🚌 4 Agences de transport");
            System.out.println("   👥 " + nbVoyageurs + " Voyageur(s)");
            System.out.println("   🏪 " + nbAcheteurs + " Agent(s) acheteur(s) pour les enchères");
            System.out.println("   🚨 1 Agent d'alertes");
            System.out.println("\n📖 UTILISATION:");
            System.out.println("   1. Réservez un trajet via l'interface Voyageur");
            System.out.println("   2. Onglet 'Mes billets' → sélectionnez un billet");
            System.out.println("   3. Cliquez sur '🔔 Revendre' pour lancer une enchère");
            System.out.println("   4. Les autres voyageurs seront notifiés");
            System.out.println("═".repeat(60));
            
        } catch (StaleProxyException e) {
            System.err.println("❌ Erreur lors de la création des agents: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Interruption lors du démarrage: " + e.getMessage());
        }
    }
    
    /**
     * Demande à l'utilisateur le nombre de voyageurs à créer
     */
    private static int askNumberOfTravellers() {
        String[] options = {"1", "2", "3", "4", "5", "6"};
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("🚀 Système Multi-Agents de Transport");
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        JLabel label = new JLabel("Combien de voyageurs voulez-vous créer ?");
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setSelectedIndex(1); // Par défaut : 2 voyageurs
        panel.add(combo);
        
        int result = JOptionPane.showConfirmDialog(null, panel, 
            "Configuration", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            return Integer.parseInt((String) combo.getSelectedItem());
        }
        return -1; // Annulé
    }
    
    /**
     * Demande à l'utilisateur le nombre d'acheteurs à créer
     */
    private static int askNumberOfBuyers() {
        String[] options = {"0", "1", "2", "3", "4", "5", "6"};
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("🏪 Configuration des Acheteurs");
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        JLabel label = new JLabel("Combien d'agents acheteurs pour les enchères ?");
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setSelectedIndex(2); // Par défaut : 2 acheteurs
        panel.add(combo);
        
        int result = JOptionPane.showConfirmDialog(null, panel, 
            "Configuration", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            return Integer.parseInt((String) combo.getSelectedItem());
        }
        return -1; // Annulé
    }
}