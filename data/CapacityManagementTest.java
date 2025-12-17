package data;

/**
 * Test class to demonstrate the capacity management system
 * 
 * @author System
 */
public class CapacityManagementTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test du système de gestion des capacités ===\n");
        
        // Test des capacités initiales
        testInitialCapacities();
        
        // Test des réservations normales
        testNormalBookings();
        
        // Test de la gestion des vélos
        testBikeManagement();
        
        // Test du filtrage des voyages sans places
        testJourneyFiltering();
        
        System.out.println("\n=== Tests terminés ===");
    }
    
    private static void testInitialCapacities() {
        System.out.println("1. Test des capacités initiales:");
        
        Journey carJourney = new Journey("LILLE", "PARIS", "CAR", 800, 120);
        Journey bikeJourney = new Journey("LILLE", "VALENCIENNES", "BIKE", 900, 60);
        Journey busJourney = new Journey("PARIS", "LYON", "BUS", 1000, 180);
        Journey tramJourney = new Journey("LILLE", "ROUBAIX", "TRAM", 1100, 30);
        
        System.out.println("Voiture: " + carJourney.getPlaces() + " places (attendu: 3)");
        System.out.println("Vélo: " + bikeJourney.getPlaces() + " places (attendu: 20)");
        System.out.println("Bus: " + busJourney.getPlaces() + " places (attendu: 50)");
        System.out.println("Tram: " + tramJourney.getPlaces() + " places (attendu: 200)");
        System.out.println();
    }
    
    private static void testNormalBookings() {
        System.out.println("2. Test des réservations normales:");
        
        Journey carJourney = new Journey("LILLE", "PARIS", "CAR", 800, 120);
        System.out.println("Voiture - Places initiales: " + carJourney.getPlaces());
        
        // Réserver 3 places
        for (int i = 1; i <= 4; i++) {
            boolean success = carJourney.bookPlace();
            System.out.println("Réservation " + i + ": " + (success ? "Réussie" : "Échec") + 
                             " - Places restantes: " + carJourney.getPlaces());
        }
        System.out.println();
    }
    
    private static void testBikeManagement() {
        System.out.println("3. Test de la gestion des vélos:");
        
        BikeZoneManager bikeManager = BikeZoneManager.getInstance();
        bikeManager.resetAllZones(); // Reset pour test propre
        
        // Initialiser les zones
        bikeManager.initializeZone("LILLE");
        bikeManager.initializeZone("VALENCIENNES");
        
        System.out.println("Vélos initiaux à LILLE: " + bikeManager.getAvailableBikes("LILLE", 800));
        System.out.println("Vélos initiaux à VALENCIENNES: " + bikeManager.getAvailableBikes("VALENCIENNES", 800));
        
        // Réserver un vélo de LILLE vers VALENCIENNES
        Journey bikeJourney = new Journey("LILLE", "VALENCIENNES", "BIKE", 900, 60);
        boolean success1 = bikeJourney.bookPlace(800);
        System.out.println("Réservation vélo LILLE->VALENCIENNES: " + (success1 ? "Réussie" : "Échec"));
        System.out.println("Vélos à LILLE après réservation: " + bikeManager.getAvailableBikes("LILLE", 800));
        System.out.println("Vélos à VALENCIENNES avant arrivée: " + bikeManager.getAvailableBikes("VALENCIENNES", 900));
        
        // Simulation de l'arrivée (après 960 = 900 + 60 minutes)
        System.out.println("Vélos à VALENCIENNES après arrivée (960): " + bikeManager.getAvailableBikes("VALENCIENNES", 960));
        System.out.println();
    }
    
    private static void testJourneyFiltering() {
        System.out.println("4. Test du filtrage des voyages:");
        
        JourneysList catalog = new JourneysList();
        
        // Ajouter des voyages avec différentes capacités
        Journey carJourney = new Journey("LILLE", "PARIS", "CAR", 800, 120);
        catalog.addJourney(carJourney);
        
        System.out.println("Voyage voiture disponible initialement: " + carJourney.hasAvailablePlaces());
        
        // Réserver toutes les places
        carJourney.bookPlace();
        carJourney.bookPlace();
        carJourney.bookPlace();
        
        System.out.println("Voyage voiture après réservation complète: " + carJourney.hasAvailablePlaces());
        
        // Test de recherche - le voyage ne devrait plus apparaître
        var directJourneys = catalog.findDirectJourneys("LILLE", "PARIS");
        System.out.println("Voyages directs trouvés après réservation complète: " + 
                         (directJourneys != null ? directJourneys.size() : 0));
        System.out.println();
    }
}