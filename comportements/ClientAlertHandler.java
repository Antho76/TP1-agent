package comportements;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import agents.TravellerAgent;
import data.ComposedJourney;
import data.Journey;

import javax.swing.*;
import java.util.List;

/**
 * Comportement de gestion des alertes pour le client
 * Vérifie si ses trajets réservés sont impactés et ne déclenche l'alerte
 * que si le client est réellement concerné
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
        
        TravellerAgent traveller = (TravellerAgent) myAgent;
        
        // Vérifier si l'alerte concerne l'un des trajets réservés du client
        List<ComposedJourney> bookedJourneys = traveller.getBookedJourneys();
        System.out.println("DEBUG: Nombre de trajets réservés: " + bookedJourneys.size());
        
        boolean isImpacted = false;
        
        for (int i = 0; i < bookedJourneys.size(); i++) {
            ComposedJourney journey = bookedJourneys.get(i);
            System.out.println("DEBUG: Vérification trajet " + i + ": " + journey);
            
            if (journey != null && journey.getJourneys() != null) {
                System.out.println("DEBUG: Trajet a " + journey.getJourneys().size() + " segments");
                for (Journey segment : journey.getJourneys()) {
                    System.out.println("DEBUG: Segment: " + segment.getStart() + " → " + segment.getStop());
                    System.out.println("DEBUG: Comparaison avec alerte: " + impactedStart + " → " + impactedStop);
                }
            }
            
            if (isJourneyImpacted(journey)) {
                isImpacted = true;
                break;
            }
        }
        
        // Ne déclencher l'alerte que si le client est réellement impacté
        if (isImpacted) {
            System.out.println("🚨 ALERTE: Le client a des trajets impactés par l'incident!");
            showImpactAlert();
        } else {
            System.out.println("ℹ️ INFO: L'incident n'affecte aucun trajet du client. Pas d'alerte nécessaire.");
        }
    }
    
    /**
     * Vérifie si un trajet composé est impacté par l'alerte
     */
    private boolean isJourneyImpacted(ComposedJourney journey) {
        if (journey == null || journey.getJourneys() == null) return false;
        
        for (Journey segment : journey.getJourneys()) {
            if (isSegmentImpacted(segment)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si un segment de trajet est impacté
     */
    private boolean isSegmentImpacted(Journey segment) {
        String start = segment.getStart().toUpperCase();
        String stop = segment.getStop().toUpperCase();
        
        return (start.equals(impactedStart) && stop.equals(impactedStop)) ||
               (start.equals(impactedStop) && stop.equals(impactedStart));
    }
    
    /**
     * Affiche l'alerte car le client est réellement impacté
     */
    private void showImpactAlert() {
        SwingUtilities.invokeLater(() -> {
            String alertMessage = "🚨 ALERTE TRAFIC - VOS TRAJETS IMPACTÉS\n\n" +
                                "⚠️ Incident sur le tronçon: " + impactedStart + " ↔ " + impactedStop + "\n\n" +
                                "❌ Un ou plusieurs de vos trajets réservés sont affectés.\n" +
                                "Vérifiez vos réservations et recherchez des alternatives.";
            
            JOptionPane.showMessageDialog(null,
                alertMessage,
                "🚨 Alerte - Trajets Impactés",
                JOptionPane.WARNING_MESSAGE);
        });
    }
}