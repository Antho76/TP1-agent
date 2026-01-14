package comportements;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.lang.acl.ACLMessage;
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
    
    // Cache global pour éviter les notifications multiples du même trajet
    private static java.util.Set<String> notifiedCancellations = new java.util.HashSet<>();
    
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
        AgenceAgent agence = (AgenceAgent) myAgent;
        
        // Notifier les clients des trajets annulés AVANT de les supprimer
        for (Journey journey : impactedJourneys) {
            notifyClientsOfCancellation(journey);
        }
        
        // Utiliser removeIf pour supprimer les trajets impactés
        catalog.removeIf(journey -> {
            boolean isImpacted = isJourneyImpacted(journey);
            if (isImpacted) {
                agence.println("🗑️ Supprimé: " + journey.getStart() + " → " + journey.getStop() + 
                              " (" + journey.getMeans() + ", " + journey.getDepartureDate() + ")");
            }
            return isImpacted;
        });
    }
    
    /**
     * Notifie tous les clients qu'un trajet spécifique a été annulé
     */
    private void notifyClientsOfCancellation(Journey cancelledJourney) {
        try {
            // Créer une clé unique pour ce trajet annulé
            String journeyKey = String.format("%s|%s|%s|%d", 
                cancelledJourney.getStart(),
                cancelledJourney.getStop(), 
                cancelledJourney.getMeans(),
                cancelledJourney.getDepartureDate());
            
            // Vérifier si ce trajet a déjà été notifié
            if (notifiedCancellations.contains(journeyKey)) {
                System.out.println("ℹ️ Notification déjà envoyée pour: " + journeyKey);
                return;
            }
            
            // Marquer comme notifié
            notifiedCancellations.add(journeyKey);
            
            AgenceAgent agence = (AgenceAgent) myAgent;
            
            // Créer un message d'annulation de trajet
            ACLMessage cancellationMsg = new ACLMessage(ACLMessage.INFORM);
            
            // Format du message: JOURNEY_CANCELLED|START|STOP|MEANS|DEPARTURE|REASON
            String cancellationContent = String.format("JOURNEY_CANCELLED|%s|%s|%s|%d|Incident trafic sur %s ↔ %s",
                cancelledJourney.getStart(),
                cancelledJourney.getStop(),
                cancelledJourney.getMeans(),
                cancelledJourney.getDepartureDate(),
                impactedStart,
                impactedStop
            );
            
            cancellationMsg.setContent(cancellationContent);
            
            // Générer le même topic que celui utilisé pour les alertes trafic
            AID topic = AgentServicesTools.generateTopicAID(myAgent, "TRAFFIC NEWS");
            cancellationMsg.setConversationId("JOURNEY_CANCELLATION");
            cancellationMsg.addReceiver(topic);
            myAgent.send(cancellationMsg);
            
            agence.println("📢 Notification d'annulation envoyée pour: " + 
                          cancelledJourney.getStart() + " → " + cancelledJourney.getStop());
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de notification d'annulation: " + e.getMessage());
        }
    }
}