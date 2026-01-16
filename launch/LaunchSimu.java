package launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * launch the simulation of travelers and travel agencies
 *
 * @author emmanueladam
 */
public class LaunchSimu {

    public static final Logger logger = Logger.getLogger("simu");

    /**
     *
     */
    public static void main(String[] args) {

        // Réduire les logs pour une interface plus propre
        logger.setLevel(Level.WARNING);
        
        // Réduire les logs JADE
        Logger.getLogger("jade").setLevel(Level.WARNING);
        Logger.getLogger("jade.core").setLevel(Level.WARNING);
        
        Handler fh;
        try {
            fh = new FileHandler("./simuAgences.xml", false);
            logger.addHandler(fh);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        // Demander le nombre de clients à démarrer
        int nbClients = askNumberOfClients();
        
        System.out.println("🚀 Démarrage de la simulation avec " + nbClients + " client(s)");

        // ******************JADE******************

        // allow to send arguments to the JADE launcher
        var pp = new ExtendedProperties();
        // Interface JADE activée pour voir la gestion des stocks des agents
        pp.setProperty(Profile.GUI, "true");
        // add the Topic Management Service
        pp.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");

        var lesAgents = new StringBuilder();
        
        // Créer plusieurs clients selon le choix de l'utilisateur
        for (int i = 1; i <= nbClients; i++) {
            lesAgents.append("client").append(i).append(":agents.TravellerAgent;");
        }
        
        // Ajouter les agences de transport
        lesAgents.append("agentBike:agents.AgenceAgent(./bike.csv);");
        lesAgents.append("agentCar:agents.AgenceAgent(./car.csv);");
        lesAgents.append("agentBus:agents.AgenceAgent(./bus.csv);");
        lesAgents.append("agentTram:agents.AgenceAgent(./tram.csv);");
        lesAgents.append("alert1:agents.AlertAgent");
        
        pp.setProperty(Profile.AGENTS, lesAgents.toString());
        // create a default Profile
        var pMain = new ProfileImpl(pp);

        // launch the main jade container
        Runtime.instance().createMainContainer(pMain);

    }
    
    /**
     * Demande à l'utilisateur combien de clients il souhaite démarrer
     * @return Le nombre de clients (entre 1 et 10)
     */
    private static int askNumberOfClients() {
        String[] options = {"1 client", "2 clients", "3 clients", "4 clients", "5 clients", 
                           "6 clients", "7 clients", "8 clients", "9 clients", "10 clients"};
        
        String message = "🚀 Configuration du Système Multi-Agents\n\n" +
                        "Combien de voyageurs (TravellerAgent) souhaitez-vous démarrer ?\n\n" +
                        "💡 Plusieurs clients permettent de :\n" +
                        "   • Tester les interactions multi-agents\n" +
                        "   • Observer la concurrence pour les places\n" +
                        "   • Simuler des alertes affectant plusieurs voyageurs\n" +
                        "   • Comparer différentes stratégies de recherche";
        
        int choice = JOptionPane.showOptionDialog(
            null,
            message,
            "🎯 Nombre de Clients",
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
