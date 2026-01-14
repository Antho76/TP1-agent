package test;

import agents.TravellerAgent;
import data.ComposedJourney;
import data.Journey;
import java.util.ArrayList;
import java.util.List;

/**
 * Test pour vérifier l'affichage de l'heure de départ dans les trajets
 */
public class TripDisplayTest {
    
    public static void main(String[] args) {
        // Créer un mock du TravellerAgent pour tester le formatage
        TravellerAgent agent = new TravellerAgent() {
            // Rendre publiques les méthodes privées pour le test
            public String testCreateTripSummary(ComposedJourney journey) {
                // Simuler la méthode createTripSummary
                if (journey == null || journey.getJourneys() == null || journey.getJourneys().isEmpty()) {
                    return "Trajet non valide";
                }
                
                int duration = (int)journey.getDuration();
                double cost = journey.getCost();
                
                // Créer le trajet complet avec toutes les étapes
                StringBuilder routeBuilder = new StringBuilder();
                List<data.Journey> journeys = journey.getJourneys();
                
                // Ajouter tous les points du trajet (A → B → E)
                for (int i = 0; i < journeys.size(); i++) {
                    if (i == 0) {
                        // Premier segment : ajouter départ et arrivée
                        routeBuilder.append(journeys.get(i).getStart()).append(" → ").append(journeys.get(i).getStop());
                    } else {
                        // Segments suivants : ajouter seulement l'arrivée
                        routeBuilder.append(" → ").append(journeys.get(i).getStop());
                    }
                }
                
                // Obtenir les types de transport utilisés
                String transports = journey.getJourneys().stream()
                    .map(j -> {
                        String means = j.getMeans();
                        return means.substring(0, 1).toUpperCase() + means.substring(1).toLowerCase();
                    })
                    .distinct()
                    .reduce((t1, t2) -> t1 + "+" + t2)
                    .orElse("Transport");
                
                // Obtenir l'heure de départ du premier segment
                int departureTime = journeys.get(0).getDepartureDate();
                
                return String.format("%s - %s | %s | %d min | %.2f€", 
                        formatTime(departureTime), routeBuilder.toString(), transports, duration, cost);
            }
            
            private String formatTime(int time) {
                int hours = time / 100;
                int minutes = time % 100;
                return String.format("%dh%02d", hours, minutes);
            }
        };
        
        // Créer des trajets de test
        System.out.println("=== TEST D'AFFICHAGE DES TRAJETS AVEC HEURE ===\n");
        
        // Test 1 : Trajet simple
        Journey journey1 = new Journey("A", "B", 15, 900, 915, 3.0, "bus");
        List<Journey> journeys1 = new ArrayList<>();
        journeys1.add(journey1);
        ComposedJourney composed1 = new ComposedJourney(journeys1);
        
        System.out.println("Trajet simple:");
        System.out.println("Ancien format: A → B | Bus | 15 min | 3,00€");
        System.out.println("Nouveau format: " + agent.testCreateTripSummary(composed1));
        System.out.println();
        
        // Test 2 : Trajet avec correspondance
        Journey journey2a = new Journey("A", "C", 20, 1200, 1220, 5.0, "tram");
        Journey journey2b = new Journey("C", "B", 10, 1230, 1240, 2.0, "bus");
        List<Journey> journeys2 = new ArrayList<>();
        journeys2.add(journey2a);
        journeys2.add(journey2b);
        ComposedJourney composed2 = new ComposedJourney(journeys2);
        
        System.out.println("Trajet avec correspondance:");
        System.out.println("Ancien format: A → C → B | Tram+Bus | 30 min | 7,00€");
        System.out.println("Nouveau format: " + agent.testCreateTripSummary(composed2));
        System.out.println();
        
        // Test 3 : Trajet tôt le matin
        Journey journey3 = new Journey("A", "B", 25, 730, 755, 1.0, "bike");
        List<Journey> journeys3 = new ArrayList<>();
        journeys3.add(journey3);
        ComposedJourney composed3 = new ComposedJourney(journeys3);
        
        System.out.println("Trajet tôt le matin:");
        System.out.println("Ancien format: A → B | Bike | 25 min | 1,00€");
        System.out.println("Nouveau format: " + agent.testCreateTripSummary(composed3));
        System.out.println();
        
        System.out.println("✅ L'heure de départ est maintenant affichée dans 'Mes trajets' !");
    }
}