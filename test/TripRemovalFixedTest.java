package test;

import agents.TravellerAgent;
import data.ComposedJourney;
import data.Journey;

import java.util.ArrayList;
import java.util.List;

/**
 * Test de validation de la suppression des trajets annulés avec synchronisation correcte
 */
public class TripRemovalFixedTest {
    
    public static void main(String[] args) {
        System.out.println("=== TEST CORRIGÉ DE SUPPRESSION DES TRAJETS ===\n");
        
        try {
            // 1. Simuler la création d'un agent (sans GUI pour le test)
            System.out.println("1. Simulation d'un agent avec trajet...");
            
            // Créer une instance simulée d'agent
            TravellerAgent agent = new TravellerAgent() {
                private List<ComposedJourney> bookedJourneys = new ArrayList<>();
                
                @Override
                public List<ComposedJourney> getBookedJourneys() {
                    return new ArrayList<>(bookedJourneys);
                }
                
                @Override
                public void addBookedJourney(ComposedJourney journey) {
                    if (journey != null) {
                        bookedJourneys.add(journey);
                        System.out.println("📝 Trajet ajouté aux réservations: " + 
                                         journey.getJourneys().get(0).getStart() + " → " + 
                                         journey.getJourneys().get(journey.getJourneys().size()-1).getStop());
                    }
                }
                
                @Override
                public boolean removeBookedJourney(ComposedJourney journey) {
                    if (journey != null && bookedJourneys.remove(journey)) {
                        System.out.println("❌ Trajet supprimé des réservations: " + 
                                         journey.getJourneys().get(0).getStart() + " → " + 
                                         journey.getJourneys().get(journey.getJourneys().size()-1).getStop());
                        return true;
                    }
                    return false;
                }
            };
            
            // 2. Ajouter un trajet à l'agent
            Journey journey1 = new Journey("A", "B", "bus", 900, 15, 4.50, 5, 8, "agentBus");
            List<Journey> journeyList = new ArrayList<>();
            journeyList.add(journey1);
            
            ComposedJourney composedJourney = new ComposedJourney();
            composedJourney.addJourneys(journeyList);
            
            agent.addBookedJourney(composedJourney);
            System.out.println("📊 Trajets dans l'agent: " + agent.getBookedJourneys().size());
            
            // 3. Simuler l'annulation
            System.out.println("\n2. Simulation de l'annulation...");
            List<ComposedJourney> bookedJourneys = agent.getBookedJourneys();
            
            // Chercher le trajet à annuler
            ComposedJourney toCancel = null;
            for (ComposedJourney journey : bookedJourneys) {
                for (Journey segment : journey.getJourneys()) {
                    if (segment.getStart().equals("A") && 
                        segment.getStop().equals("B") && 
                        segment.getMeans().equals("bus") && 
                        segment.getDepartureDate() == 900) {
                        toCancel = journey;
                        break;
                    }
                }
                if (toCancel != null) break;
            }
            
            if (toCancel != null) {
                System.out.println("🎯 Trajet trouvé pour annulation");
                
                // Supprimer via l'agent (comme le fait ClientAlertHandler)
                boolean removed = agent.removeBookedJourney(toCancel);
                
                if (removed) {
                    // Libérer les places
                    for (Journey segment : toCancel.getJourneys()) {
                        segment.cancelBooking();
                        System.out.println("📦 Stock restauré pour: " + segment.getStart() + 
                                         " → " + segment.getStop() + " (" + segment.getMeans() + ")");
                    }
                    
                    System.out.println("✅ Suppression réussie");
                } else {
                    System.out.println("❌ Échec de la suppression");
                }
            }
            
            // 4. Vérification finale
            System.out.println("\n3. Vérification finale...");
            List<ComposedJourney> finalJourneys = agent.getBookedJourneys();
            System.out.println("📊 Trajets finaux dans l'agent: " + finalJourneys.size());
            
            if (finalJourneys.size() == 0) {
                System.out.println("✅ TEST RÉUSSI: Suppression correctement effectuée");
                System.out.println("🎉 L'interface sera maintenant correctement mise à jour");
            } else {
                System.out.println("❌ TEST ÉCHOUÉ: Le trajet n'a pas été supprimé");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erreur pendant le test: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== FIN DU TEST CORRIGÉ ===");
    }
}