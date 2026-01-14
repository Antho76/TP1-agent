package comportements;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import agents.AgenceAgent;
import data.Journey;
import data.JourneysList;

import java.util.ArrayList;
import java.util.List;

/**
 * Comportement de gestion des alertes pour les agences
 * Supprime les trajets impactés et notifie les changements
 * 
 * @author System
 */
public class AlertHandler extends OneShotBehaviour {
    
    private String alertContent;
    private String impactedStart;
    private String impactedStop;
    
    public AlertHandler(Agent agent, String alertContent) {
        super(agent);
        this.alertContent = alertContent;
        parseAlert();
    }
    
    /**
     * Parse l'alerte pour extraire les stations impactées
     */
    private void parseAlert() {
        // Format attendu: "Problème entre X et Y"
        if (alertContent.contains("entre") && alertContent.contains("et")) {
            String[] parts = alertContent.split("entre|et");
            if (parts.length >= 3) {
                impactedStart = parts[1].trim().toUpperCase();
                impactedStop = parts[2].trim().split("\\s+")[0].toUpperCase(); // Prendre le premier mot
            }
        }
    }
    
    @Override
    public void action() {
        if (impactedStart == null || impactedStop == null) {
            return;
        }
        
        AgenceAgent agence = (AgenceAgent) myAgent;
        JourneysList catalog = agence.getCatalog();
        
        if (catalog == null) {
            return;
        }
        
        // Trouver les trajets impactés
        List<Journey> impactedJourneys = findImpactedJourneys(catalog);
        
        if (!impactedJourneys.isEmpty()) {
            // Afficher l'alerte
            agence.println("🚨 ALERTE TRAFIC REÇUE:");
            agence.println("📍 Tronçon impacté: " + impactedStart + " ↔ " + impactedStop);
            agence.println("⚠️ " + impactedJourneys.size() + " trajet(s) impacté(s)");
            
            // Supprimer les trajets impactés
            removeImpactedJourneys(catalog, impactedJourneys);
            
            agence.println("✅ Catalogue mis à jour - trajets impactés supprimés");
            agence.println("─".repeat(50));
        }
    }
    
    /**
     * Trouve les trajets impactés par l'alerte
     */
    private List<Journey> findImpactedJourneys(JourneysList catalog) {
        List<Journey> impacted = new ArrayList<>();
        
        for (Journey journey : catalog.getAllJourneys()) {
            if (isJourneyImpacted(journey)) {
                impacted.add(journey);
            }
        }
        
        return impacted;
    }
    
    /**
     * Vérifie si un trajet est impacté par l'alerte
     */
    private boolean isJourneyImpacted(Journey journey) {
        String start = journey.getStart().toUpperCase();
        String stop = journey.getStop().toUpperCase();
        
        // Vérifier si le trajet utilise le tronçon impacté
        return (start.equals(impactedStart) && stop.equals(impactedStop)) ||
               (start.equals(impactedStop) && stop.equals(impactedStart));
    }
    
    /**
     * Supprime les trajets impactés du catalogue
     */
    private void removeImpactedJourneys(JourneysList catalog, List<Journey> impactedJourneys) {
        // Utiliser removeIf pour supprimer les trajets impactés
        catalog.removeIf(journey -> {
            boolean isImpacted = isJourneyImpacted(journey);
            if (isImpacted) {
                AgenceAgent agence = (AgenceAgent) myAgent;
                agence.println("🗑️ Supprimé: " + journey.getStart() + " → " + journey.getStop() + 
                              " (" + journey.getMeans() + ", " + journey.getDepartureDate() + ")");
            }
            return isImpacted;
        });
    }
}