package test;

import gui.TravellerGui;
import data.ComposedJourney;
import data.Journey;

/**
 * Test de l'organisation des billets par trajet avec nommage
 */
public class TestGroupedTickets {
    
    public static void main(String[] args) {
        // Création d'une interface pour test
        TravellerGui gui = new TravellerGui(null);
        
        System.out.println("=== TEST: Organisation par trajets ===");
        
        // Test 1: Trajet A→E avec 3 billets
        System.out.println("\n--- Trajet 1: A → E (3 segments) ---");
        ComposedJourney trip1 = new ComposedJourney();
        trip1.add(new Journey("A", "B", "bus", 900, 15, 3.50, 20, 5));
        trip1.add(new Journey("B", "C", "tram", 920, 10, 2.50, 15, 8));
        trip1.add(new Journey("C", "E", "bus", 935, 20, 4.00, 25, 6));
        gui.addTripForTest(trip1, "A → E");
        
        // Test 2: Trajet F→G avec 1 billet
        System.out.println("\n--- Trajet 2: F → G (1 segment) ---");
        ComposedJourney trip2 = new ComposedJourney();
        trip2.add(new Journey("F", "G", "bike", 1000, 25, 1.50, 10, 9));
        gui.addTripForTest(trip2, "F → G");
        
        // Test 3: Trajet H→K avec 3 billets
        System.out.println("\n--- Trajet 3: H → K (3 segments) ---");
        ComposedJourney trip3 = new ComposedJourney();
        trip3.add(new Journey("H", "I", "car", 1100, 30, 8.00, 35, 7));
        trip3.add(new Journey("I", "J", "tram", 1135, 12, 2.80, 18, 8));
        trip3.add(new Journey("J", "K", "bus", 1150, 18, 3.20, 22, 6));
        gui.addTripForTest(trip3, "H → K");
        
        System.out.println("\n=== RÉSULTATS ===");
        System.out.println("Total billets individuels: " + gui.getIndividualTickets().size());
        System.out.println("Total trajets composés: " + gui.getBookedJourneys().size());
        
        // Affichage de la GUI
        gui.setVisible(true);
        
        System.out.println("\n✅ Interface ouverte - Vérifiez l'onglet 'Mes billets'");
        System.out.println("Vous devriez voir:");
        System.out.println("📍 Trajet 1 : A → E (3 billets)");
        System.out.println("├─ Billet 1: A → B (bus)");
        System.out.println("├─ Billet 2: B → C (tram)");
        System.out.println("└─ Billet 3: C → E (bus)");
        System.out.println("📍 Trajet 2 : F → G (1 billets)");
        System.out.println("└─ Billet 1: F → G (bike)");
        System.out.println("📍 Trajet 3 : H → K (3 billets)");
        System.out.println("├─ Billet 1: H → I (car)");
        System.out.println("├─ Billet 2: I → J (tram)");
        System.out.println("└─ Billet 3: J → K (bus)");
    }
}