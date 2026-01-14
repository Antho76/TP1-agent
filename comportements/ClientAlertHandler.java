package comportements;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/**
 * Comportement de gestion des alertes pour le client
 * Vérifie si ses trajets réservés sont impactés et propose des alternatives
 * 
 * @author System
 */
public class ClientAlertHandler extends OneShotBehaviour {
    
    private String alertContent;
    private String impactedStart;
    private String impactedStop;
    
    public ClientAlertHandler(Agent agent, String alertContent) {
        super(agent);
        this.alertContent = alertContent;
        parseAlert();
    }
    
    /**
     * Parse le contenu de l'alerte pour extraire les informations
     */
    private void parseAlert() {
        try {
            // Format attendu : TYPE|SEVERITY|FROM|TO|DESCRIPTION
            String[] parts = alertContent.split("\\|");
            if (parts.length >= 4) {
                this.impactedStart = parts[2].trim().toUpperCase();
                this.impactedStop = parts[3].trim().toUpperCase();
            } else {
                // Fallback pour ancien format "start,stop"
                String[] oldFormat = alertContent.split(",");
                if (oldFormat.length >= 2) {
                    this.impactedStart = oldFormat[0].trim().toUpperCase();
                    this.impactedStop = oldFormat[1].trim().toUpperCase();
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du parsing de l'alerte: " + e.getMessage());
        }
    }
    
    @Override
    public void action() {
        System.out.println("ClientAlertHandler: Traitement de l'alerte pour " + impactedStart + " → " + impactedStop);
        
        // Afficher l'information d'alerte au client
        showAlertInfo("🚨 ALERTE TRAFIC\n\nTronçon impacté: " + impactedStart + " ↔ " + impactedStop + 
                     "\n\nVérifiez vos trajets réservés et recherchez des alternatives si nécessaire.");
    }
    
    /**
     * Affiche une information d'alerte à l'utilisateur
     */
    private void showAlertInfo(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                message,
                "🚨 Information Trafic",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
}