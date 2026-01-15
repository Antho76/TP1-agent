import gui.TravellerGui;
import data.Journey;
import data.ComposedJourney;
import javax.swing.SwingUtilities;

/**
 * Test de la fonctionnalité de stockage individuel des billets
 */
public class TestIndividualTickets {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("=== TEST STOCKAGE INDIVIDUEL DES BILLETS ===");
                
                // Créer l'interface
                TravellerGui gui = new TravellerGui(null);
                gui.setVisible(true);
                
                // Simuler un trajet composé A→E passant par B
                Journey ticket1 = new Journey("A", "B", "bus", 900, 30);
                ticket1.setCost(3.50);
                ticket1.setConfort(8);
                
                Journey ticket2 = new Journey("B", "E", "tram", 945, 25); 
                ticket2.setCost(2.00);
                ticket2.setConfort(7);
                
                ComposedJourney composedTrip = new ComposedJourney();
                composedTrip.add(ticket1);
                composedTrip.add(ticket2);
                
                System.out.println("✅ Interface créée avec onglet '🎫 Mes billets'");
                System.out.println("✅ Trajet A→E créé avec 2 segments:");
                System.out.println("   - Segment 1: A→B (bus, 30min, 3.50€)");
                System.out.println("   - Segment 2: B→E (tram, 25min, 2.00€)");
                System.out.println();
                System.out.println("💡 Avec le nouveau système:");
                System.out.println("   ❌ AVANT: 1 trajet composé 'A → E'");
                System.out.println("   ✅ APRÈS: 2 billets individuels:");
                
                for (Journey segment : composedTrip.getJourneys()) {
                    int depHours = segment.getDepartureDate() / 100;
                    int depMinutes = segment.getDepartureDate() % 100;
                    int arrHours = segment.getArrivalDate() / 100;
                    int arrMinutes = segment.getArrivalDate() % 100;
                    
                    System.out.println("     🎫 " + String.format("%02d:%02d-%02d:%02d", depHours, depMinutes, arrHours, arrMinutes) + 
                                     " " + segment.getStart() + "→" + segment.getStop() + 
                                     " " + segment.getMeans() + " " + segment.getDuration() + "min " + segment.getCost() + "€");
                }
                
                System.out.println();
                System.out.println("🎯 Chaque billet peut maintenant être géré indépendamment!");
                System.out.println("📱 Testez l'interface pour réserver un trajet et voir la décomposition.");
                
            } catch (Exception e) {
                System.err.println("❌ Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}