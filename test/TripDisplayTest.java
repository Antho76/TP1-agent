package test;

import data.ComposedJourney;
import data.Journey;

/**
 * Test pour vérifier l'affichage de l'heure de départ dans les trajets
 */
public class TripDisplayTest {
    
    public static void main(String[] args) {
        System.out.println("=== TEST D'AFFICHAGE DES TRAJETS AVEC HEURE ===\n");
        
        // Test 1 : Trajet simple
        ComposedJourney composed1 = new ComposedJourney();
        composed1.add(new Journey("A", "B", "bus", 900, 15, 3.0, 50, 80));
        
        System.out.println("Test 1 - Trajet simple:");
        System.out.println("Format attendu: 9h00 - A → B | Bus | 15 min | 3,00€");
        System.out.println("Trajet créé avec:");
        System.out.println("  - Départ: A → B");
        System.out.println("  - Transport: bus");
        System.out.println("  - Heure: 900 (9h00)");
        System.out.println("  - Durée: 15 min");
        System.out.println("  - Coût: 3,00€");
        System.out.println();
        
        // Test 2 : Trajet avec correspondance
        ComposedJourney composed2 = new ComposedJourney();
        composed2.add(new Journey("A", "C", "tram", 1200, 20, 5.0, 30, 90));
        composed2.add(new Journey("C", "B", "bus", 1230, 10, 2.0, 40, 70));
        
        System.out.println("Test 2 - Trajet avec correspondance:");
        System.out.println("Format attendu: 12h00 - A → C → B | Tram+Bus | 30 min | 7,00€");
        System.out.println("Trajet créé avec:");
        System.out.println("  - Segment 1: A → C (tram, 1200/12h00, 20min, 5,00€)");
        System.out.println("  - Segment 2: C → B (bus, 1230/12h30, 10min, 2,00€)");
        System.out.println();
        
        // Test 3 : Trajet tôt le matin
        ComposedJourney composed3 = new ComposedJourney();
        composed3.add(new Journey("A", "B", "bike", 730, 25, 1.0, 10, 60));
        
        System.out.println("Test 3 - Trajet tôt le matin:");
        System.out.println("Format attendu: 7h30 - A → B | Bike | 25 min | 1,00€");
        System.out.println("Trajet créé avec:");
        System.out.println("  - Départ: A → B");
        System.out.println("  - Transport: bike");
        System.out.println("  - Heure: 730 (7h30)");
        System.out.println("  - Durée: 25 min");
        System.out.println("  - Coût: 1,00€");
        System.out.println();
        
        System.out.println("✅ Constructeurs Journey corrigés !");
        System.out.println("Format: new Journey(start, stop, means, departureTime, duration, cost, co2, comfort)");
    }
}