package comportements;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import agents.TravellerAgent;
import data.ComposedJourney;
import data.Journey;

import javax.swing.*;
import java.util.List;

/**
 * Comportement de gestion des alertes pour le client
 * Vérifie si les trajets réservés sont impactés par l'alerte et propose des alternatives
 */
public class ClientAlertHandler extends OneShotBehaviour {
    private String alertContent;
    private String impactedStart;
    private String impactedStop;

    public ClientAlertHandler(Agent agent, String alertContent) {
        super(agent);
        this.alertContent = alertContent;
    }

    @Override
    public void action() {
        try {
            String content = alertContent;
            System.out.println("\n🔔 Client reçoit une alerte: " + content);

            // Parser le message d'alerte
            String[] parts = content.split("\\|");
            if (parts.length >= 6) {
                String alertType = parts[0]; // JOURNEY_CANCELLED
                String start = parts[1];
                String stop = parts[2];
                String means = parts[3];
                int departure = Integer.parseInt(parts[4]);
                String reason = parts[5];

                this.impactedStart = start;
                this.impactedStop = stop;

                // Vérifier si un trajet réservé est impacté
                TravellerAgent travellerAgent = (TravellerAgent) myAgent;
                List<ComposedJourney> bookedJourneys = travellerAgent.getBookedJourneys();

                System.out.println("📊 Trajets réservés actuels: " + bookedJourneys.size());

                // Rechercher le trajet annulé
                ComposedJourney cancelledJourney = findCancelledJourney(bookedJourneys, start, stop, means, departure);

                if (cancelledJourney != null) {
                    String journeyKey = start + "_" + stop + "_" + means + "_" + departure;
                    
                    // Anti-duplication : vérifier si cette annulation a déjà été traitée
                    if (travellerAgent.isAlreadyProcessed(journeyKey)) {
                        System.out.println("⏭️ Annulation déjà traitée pour ce client, ignorée");
                        return;
                    }
                    
                    // Marquer cette annulation comme traitée
                    travellerAgent.markAsProcessed(journeyKey);
                    
                    System.out.println("🎯 TRAJET IMPACTÉ TROUVÉ !");
                    System.out.println("   Suppression du trajet: " + start + " → " + stop + " (" + means + ")");

                    // Supprimer le trajet des réservations de l'agent
                    travellerAgent.removeBookedJourney(cancelledJourney);

                    // Libérer les places dans le stock
                    for (Journey journey : cancelledJourney.getJourneys()) {
                        journey.cancelBooking();
                        System.out.println("📦 Stock restauré pour: " + journey.getStart() + " → " + journey.getStop() + " (" + journey.getMeans() + ")");
                    }

                    System.out.println("✅ Trajet supprimé et stock restauré");
                    System.out.println("📊 Trajets restants: " + travellerAgent.getBookedJourneys().size());

                    // Mettre à jour l'interface graphique pour supprimer le trajet de la liste
                    // Forcer la mise à jour avec un délai pour s'assurer de la synchronisation
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Thread.sleep(100); // Court délai pour assurer la synchronisation
                            travellerAgent.getWindow().refreshTripsList();
                            System.out.println("🔄 Interface 'Mes trajets' mise à jour avec force");
                            
                            // Double vérification de la synchronisation
                            travellerAgent.getWindow().forceSynchronization();
                            System.out.println("🔄 Synchronisation forcée terminée");
                            
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });

                    // Proposer un trajet alternatif avec le système séquentiel
                    proposeAlternativeRoutes(cancelledJourney, reason);

                } else {
                    System.out.println("ℹ️ Aucun trajet réservé impacté par cette alerte");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur dans ClientAlertHandler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si un segment de trajet correspond aux critères d'annulation
     */
    private boolean hasMatchingSegment(ComposedJourney journey, String start, String stop, String means, int departure) {
        for (Journey segment : journey.getJourneys()) {
            boolean startMatch = segment.getStart().equals(start);
            boolean stopMatch = segment.getStop().equals(stop);
            boolean meansMatch = segment.getMeans().equals(means);
            boolean departureMatch = segment.getDepartureDate() == departure;

            System.out.println("🔍 Vérification segment: " + segment.getStart() + "→" + segment.getStop() + 
                             " (" + segment.getMeans() + ") à " + segment.getDepartureDate());
            System.out.println("   Start: " + startMatch + ", Stop: " + stopMatch + 
                             ", Means: " + meansMatch + ", Departure: " + departureMatch);

            if (startMatch && stopMatch && meansMatch && departureMatch) {
                System.out.println("✅ Segment correspondant trouvé !");
                return true;
            }
        }
        return false;
    }

    /**
     * Trouve le trajet annulé dans les réservations du client
     */
    private ComposedJourney findCancelledJourney(List<ComposedJourney> bookedJourneys, 
            String start, String stop, String means, int departure) {
        for (ComposedJourney journey : bookedJourneys) {
            if (hasMatchingSegment(journey, start, stop, means, departure)) {
                return journey;
            }
        }
        return null;
    }

    /**
     * Propose des itinéraires alternatifs pour remplacer le trajet annulé
     */
    private void proposeAlternativeRoutes(ComposedJourney cancelledJourney, String reason) {
        if (cancelledJourney == null || cancelledJourney.getJourneys().isEmpty()) return;
        
        // Récupérer les informations du trajet global (point de départ et d'arrivée)
        Journey firstSegment = cancelledJourney.getJourneys().get(0);
        Journey lastSegment = cancelledJourney.getJourneys().get(cancelledJourney.getJourneys().size() - 1);
        
        String originalDeparture = firstSegment.getStart();
        String originalDestination = lastSegment.getStop();
        int originalTime = firstSegment.getDepartureDate();
        
        // Récupérer les préférences originales sauvegardées
        String originalCriteria = cancelledJourney.getOriginalCriteria();
        String originalTransportType = cancelledJourney.getOriginalTransportType();
        
        System.out.println("🔍 Proposition de recherche d'alternatives pour le trajet annulé: " + 
                          originalDeparture + " → " + originalDestination);
        System.out.println("📋 Préférences récupérées - Critère: '" + originalCriteria + 
                          "', Transport: '" + originalTransportType + "'");
        System.out.println("📊 DEBUG: Valeurs brutes - getCriteria()='" + originalCriteria + 
                          "', getTransportType()='" + originalTransportType + "'");
        
        // ÉTAPE 1 : Notification d'incident
        showIncidentNotification(reason, () -> {
            // ÉTAPE 2 : Annonce d'annulation du trajet
            showJourneyCancellationNotification(originalDeparture, originalDestination, 
                    originalTime, reason, () -> {
                // ÉTAPE 3 : Proposition de recherche d'alternative
                showReplacementProposal(originalDeparture, originalDestination, originalTime, 
                        originalCriteria, originalTransportType);
            });
        });
    }

    /**
     * ÉTAPE 1 : Affiche la notification d'incident sur la ligne
     */
    private void showIncidentNotification(String reason, Runnable onContinue) {
        String message = String.format(
            "🚨 INCIDENT SIGNALÉ\n\n" +
            "⚠️ Un incident a été signalé sur le réseau de transport :\n\n" +
            "📝 Motif : %s\n\n" +
            "Vérification de l'impact sur vos trajets en cours...",
            reason);
            
        JOptionPane.showMessageDialog(null,
            message,
            "🚨 Alerte Trafic",
            JOptionPane.WARNING_MESSAGE);
            
        // Passer à l'étape suivante APRÈS que l'utilisateur ait fermé le dialog
        if (onContinue != null) {
            SwingUtilities.invokeLater(onContinue);
        }
    }

    /**
     * ÉTAPE 2 : Annonce l'annulation du trajet spécifique
     */
    private void showJourneyCancellationNotification(String departure, String destination, 
            int time, String reason, Runnable onContinue) {
        String formattedTime = formatTime(time);
        String message = String.format(
            "❌ TRAJET ANNULÉ\n\n" +
            "📍 Trajet : %s → %s\n" +
            "🕐 Heure : %s\n" +
            "📝 Motif : %s\n\n" +
            "Ce trajet a été retiré de vos réservations.",
            departure, destination, formattedTime, reason);
            
        JOptionPane.showMessageDialog(null,
            message,
            "❌ Trajet Annulé",
            JOptionPane.ERROR_MESSAGE);
            
        // Passer à l'étape suivante APRÈS que l'utilisateur ait fermé le dialog
        if (onContinue != null) {
            SwingUtilities.invokeLater(onContinue);
        }
    }

    /**
     * ÉTAPE 3 : Propose un trajet de remplacement
     */
    private void showReplacementProposal(String departure, String destination, int time,
            String originalCriteria, String originalTransportType) {
        String formattedTime = formatTime(time);
        String message = String.format(
            "🔄 RECHERCHE DE REMPLACEMENT\n\n" +
            "💡 Souhaitez-vous rechercher un trajet alternatif ?\n\n" +
            "📍 Parcours : %s → %s\n" +
            "🕐 Heure souhaitée : %s\n" +
            "🎯 Préférences sauvegardées :\n" +
            "   • Critère : %s\n" +
            "   • Transport : %s",
            departure, destination, formattedTime, 
            originalCriteria != null ? originalCriteria : "Non spécifié",
            originalTransportType != null ? originalTransportType : "Tous"
        );

        int response = JOptionPane.showConfirmDialog(null,
            message,
            "🔄 Trajet de Remplacement",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            System.out.println("✅ L'utilisateur souhaite un trajet de remplacement");
            SwingUtilities.invokeLater(() -> {
                searchAlternativeWithPreferences(departure, destination, time, 
                        originalCriteria, originalTransportType);
            });
        } else {
            System.out.println("❌ L'utilisateur refuse le trajet de remplacement");
        }
    }

    /**
     * Lance une recherche alternative avec les préférences sauvegardées
     */
    private void searchAlternativeWithPreferences(String departure, String destination, int time,
            String originalCriteria, String originalTransportType) {
        System.out.println("🚀 Lancement de la recherche d'alternative avec les préférences:");
        System.out.println("   📍 " + departure + " → " + destination);
        System.out.println("   🕐 " + formatTime(time));
        System.out.println("   🎯 Critère: " + originalCriteria);
        System.out.println("   🚌 Transport: " + originalTransportType);

        TravellerAgent travellerAgent = (TravellerAgent) myAgent;
        
        // Créer un ACLMessage fictif pour ContractNet
        ACLMessage dummyMsg = new ACLMessage(ACLMessage.CFP);
        dummyMsg.setSender(myAgent.getAID());
        
        // Créer un comportement ContractNet avec les préférences sauvegardées
        ContractNetAchat contractNetBehaviour = new ContractNetAchat(
            myAgent, dummyMsg, departure, destination, time, originalCriteria, originalTransportType);
        
        travellerAgent.addBehaviour(contractNetBehaviour);
        System.out.println("✅ Recherche alternative lancée avec les préférences d'origine");
    }

    /**
     * Formate l'heure en format lisible
     */
    private String formatTime(int time) {
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%02dh%02d", hours, minutes);
    }
}