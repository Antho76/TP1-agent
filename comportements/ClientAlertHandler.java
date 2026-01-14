package comportements;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import javax.swing.*;

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
        
        // Afficher simplement l'alerte
        showSimpleAlert();
    }
    
    /**
     * Affiche une alerte simple sans proposition d'alternatives
     */
    private void showSimpleAlert() {
        SwingUtilities.invokeLater(() -> {
            String alertMessage = "🚨 ALERTE TRAFIC\n\n" +
                                "Tronçon impacté: " + impactedStart + " ↔ " + impactedStop + "\n\n" +
                                "Vérifiez vos trajets réservés et consultez les infos trafic en temps réel.";
            
            JOptionPane.showMessageDialog(null,
                alertMessage,
                "� Information Trafic",
                JOptionPane.WARNING_MESSAGE);
        });
    }
}