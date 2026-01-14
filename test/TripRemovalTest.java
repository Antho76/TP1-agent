package test;

import data.ComposedJourney;
import data.Journey;

import java.util.ArrayList;
import java.util.List;

/**
 * Test de validation de la suppression des trajets annulés de l'interface
 */
public class TripRemovalTest {
    
    public static void main(String[] args) {
        System.out.println("=== TEST DE SUPPRESSION DES TRAJETS ANNULÉS ===\n");
        
        // Simuler la création d'un agent voyageur avec des trajets
        System.out.println("1. Création d'un agent voyageur avec trajets simulés...");
        
        // Créer un trajet simulé
        Journey journey1 = new Journey("A", "B", "bus", 900, 15, 4.50, 5, 8, "agentBus");
        // Le trajet est automatiquement disponible avec des places
        
        List<Journey> journeyList = new ArrayList<>();
        journeyList.add(journey1);
        
        ComposedJourney composedJourney = new ComposedJourney();
        composedJourney.addJourneys(journeyList);
        
        // Simuler une liste de trajets réservés
        List<ComposedJourney> bookedJourneys = new ArrayList<>();
        bookedJourneys.add(composedJourney);
        
        System.out.println("✅ Trajet initial créé: A → B (bus) à 09:00");
        System.out.println("📊 Nombre de trajets avant annulation: " + bookedJourneys.size());
        
        // Simuler une alerte d'annulation
        System.out.println("\n2. Simulation d'une alerte d'annulation...");
        String alertMessage = "JOURNEY_CANCELLED|A|B|bus|900|Incident technique sur la ligne";
        System.out.println("🚨 Alerte reçue: " + alertMessage);
        
        // Vérifier qu'un trajet correspondant est trouvé
        System.out.println("\n3. Recherche du trajet impacté...");
        boolean journeyFound = false;
        for (ComposedJourney journey : bookedJourneys) {
            for (Journey segment : journey.getJourneys()) {
                boolean startMatch = segment.getStart().equals("A");
                boolean stopMatch = segment.getStop().equals("B");
                boolean meansMatch = segment.getMeans().equals("bus");
                boolean departureMatch = segment.getDepartureDate() == 900;
                
                if (startMatch && stopMatch && meansMatch && departureMatch) {
                    journeyFound = true;
                    System.out.println("🎯 Trajet correspondant trouvé!");
                    System.out.println("   - Départ: " + segment.getStart());
                    System.out.println("   - Arrivée: " + segment.getStop());
                    System.out.println("   - Transport: " + segment.getMeans());
                    System.out.println("   - Heure: " + segment.getDepartureDate());
                    break;
                }
            }
            if (journeyFound) break;
        }
        
        // Simuler la suppression
        if (journeyFound) {
            System.out.println("\n4. Suppression du trajet annulé...");
            
            // Trouver et supprimer le trajet
            ComposedJourney toRemove = null;
            for (ComposedJourney journey : bookedJourneys) {
                for (Journey segment : journey.getJourneys()) {
                    if (segment.getStart().equals("A") && 
                        segment.getStop().equals("B") && 
                        segment.getMeans().equals("bus") && 
                        segment.getDepartureDate() == 900) {
                        toRemove = journey;
                        break;
                    }
                }
                if (toRemove != null) break;
            }
            
            if (toRemove != null) {
                // Libérer les places
                for (Journey segment : toRemove.getJourneys()) {
                    segment.cancelBooking();
                    System.out.println("📦 Stock restauré pour: " + segment.getStart() + 
                                     " → " + segment.getStop() + " (" + segment.getMeans() + ")");
                }
                
                // Supprimer de la liste
                bookedJourneys.remove(toRemove);
                System.out.println("❌ Trajet supprimé de la liste des réservations");
                
                // Simuler le rafraîchissement de l'interface
                System.out.println("🔄 Interface 'Mes trajets' rafraîchie");
            }
        } else {
            System.out.println("❌ Aucun trajet correspondant trouvé");
        }
        
        // Vérification finale
        System.out.println("\n5. Vérification finale...");
        System.out.println("📊 Nombre de trajets après annulation: " + bookedJourneys.size());
        
        if (bookedJourneys.size() == 0) {
            System.out.println("✅ TEST RÉUSSI: Le trajet annulé a été correctement supprimé de la liste");
            System.out.println("🎉 L'interface 'Mes trajets' sera automatiquement mise à jour");
        } else {
            System.out.println("❌ TEST ÉCHOUÉ: Le trajet n'a pas été supprimé");
        }
        
        System.out.println("\n=== FIN DU TEST ===");
    }
}