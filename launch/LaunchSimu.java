package launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

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

        // ******************JADE******************

        // allow to send arguments to the JADE launcher
        var pp = new ExtendedProperties();
        // Interface JADE désactivée pour une expérience utilisateur plus propre
        pp.setProperty(Profile.GUI, "false");
        // add the Topic Management Service
        pp.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");

        var lesAgents = new StringBuilder();
        lesAgents.append("client1:agents.TravellerAgent;");
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

}
