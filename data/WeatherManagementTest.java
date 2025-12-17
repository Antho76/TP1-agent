package data;

/**
 * Test class to demonstrate the weather management system
 * 
 * @author System
 */
public class WeatherManagementTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test du système de gestion météorologique ===\n");
        
        // Test configuration
        testWeatherConfiguration();
        
        // Test weather manager
        testWeatherManager();
        
        // Test journey adaptations
        testJourneyWeatherAdaptations();
        
        // Test filtering
        testWeatherFiltering();
        
        System.out.println("\n=== Tests météo terminés ===");
    }
    
    private static void testWeatherConfiguration() {
        System.out.println("1. Test de la configuration météo:");
        
        WeatherConfig config = WeatherConfig.getInstance();
        System.out.println(config.getConfigurationSummary());
        
        if (!config.isApiKeyConfigured()) {
            System.out.println("⚠️  Clé API non configurée - utilisation des données simulées");
            // Configurer une clé de test
            config.setApiKey("test_key_12345");
            System.out.println("Clé de test configurée: " + config.isApiKeyConfigured());
        }
        
        System.out.println();
    }
    
    private static void testWeatherManager() {
        System.out.println("2. Test du gestionnaire météo:");
        
        WeatherManager weatherManager = WeatherManager.getInstance();
        
        // Test changement de ville
        System.out.println("Ville par défaut: " + weatherManager.getCurrentCity());
        weatherManager.setCurrentCity("Paris");
        System.out.println("Nouvelle ville: " + weatherManager.getCurrentCity());
        
        // Test analyse des conditions
        WeatherManager.WeatherCondition condition = weatherManager.analyzeWeatherConditions();
        System.out.println("Condition météo analysée: " + condition);
        
        // Test disponibilité des vélos
        boolean bikesAllowed = weatherManager.areBikeJourneysAllowed();
        System.out.println("Vélos autorisés: " + bikesAllowed);
        
        // Test facteurs d'ajustement
        double bikeDurationFactor = weatherManager.getDurationAdjustmentFactor("bike");
        double carDurationFactor = weatherManager.getDurationAdjustmentFactor("car");
        double carCostFactor = weatherManager.getCostAdjustmentFactor("car");
        
        System.out.println("Facteur durée vélo: " + bikeDurationFactor);
        System.out.println("Facteur durée voiture: " + carDurationFactor);
        System.out.println("Facteur coût voiture: " + carCostFactor);
        
        System.out.println("Impact météo: " + weatherManager.getWeatherImpactDescription());
        System.out.println();
    }
    
    private static void testJourneyWeatherAdaptations() {
        System.out.println("3. Test des adaptations météo sur les voyages:");
        
        // Créer différents types de voyages
        Journey bikeJourney = new Journey("LILLE", "VALENCIENNES", "BIKE", 900, 60, 2.0);
        Journey carJourney = new Journey("LILLE", "PARIS", "CAR", 800, 120, 25.0);
        Journey busJourney = new Journey("PARIS", "LYON", "BUS", 1000, 180, 45.0);
        
        System.out.println("=== Voyage vélo ===");
        System.out.println("Durée de base: " + bikeJourney.getBaseDuration() + " min");
        System.out.println("Durée ajustée: " + bikeJourney.getDuration() + " min");
        System.out.println("Coût de base: " + String.format("%.2f", bikeJourney.getBaseCost()) + "€");
        System.out.println("Coût ajusté: " + String.format("%.2f", bikeJourney.getCost()) + "€");
        System.out.println("Disponible avec météo: " + bikeJourney.isAvailableWithWeather());
        System.out.println("Impact météo: " + bikeJourney.getWeatherImpactDescription());
        
        System.out.println("\n=== Voyage voiture ===");
        System.out.println("Durée de base: " + carJourney.getBaseDuration() + " min");
        System.out.println("Durée ajustée: " + carJourney.getDuration() + " min");
        System.out.println("Coût de base: " + String.format("%.2f", carJourney.getBaseCost()) + "€");
        System.out.println("Coût ajusté: " + String.format("%.2f", carJourney.getCost()) + "€");
        System.out.println("Disponible avec météo: " + carJourney.isAvailableWithWeather());
        System.out.println("Impact météo: " + carJourney.getWeatherImpactDescription());
        
        System.out.println("\n=== Voyage bus ===");
        System.out.println("Durée de base: " + busJourney.getBaseDuration() + " min");
        System.out.println("Durée ajustée: " + busJourney.getDuration() + " min");
        System.out.println("Disponible avec météo: " + busJourney.isAvailableWithWeather());
        System.out.println("Impact météo: " + busJourney.getWeatherImpactDescription());
        
        System.out.println();
    }
    
    private static void testWeatherFiltering() {
        System.out.println("4. Test du filtrage météorologique:");
        
        JourneysList catalog = new JourneysList();
        
        // Ajouter différents types de voyages
        catalog.addJourney(new Journey("LILLE", "VALENCIENNES", "BIKE", 900, 60, 2.0));
        catalog.addJourney(new Journey("LILLE", "PARIS", "CAR", 800, 120, 25.0));
        catalog.addJourney(new Journey("LILLE", "PARIS", "BUS", 830, 140, 30.0));
        
        System.out.println("Nombre total de voyages dans le catalogue: 3");
        
        // Test recherche directe
        var directJourneys = catalog.findDirectJourneys("LILLE", "VALENCIENNES");
        System.out.println("Voyages directs Lille->Valenciennes trouvés: " + 
                         (directJourneys != null ? directJourneys.size() : 0));
        
        if (directJourneys != null) {
            for (Journey j : directJourneys) {
                System.out.println("  - " + j.getMeans() + " disponible: " + j.isAvailableWithWeather());
            }
        }
        
        var parisJourneys = catalog.findDirectJourneys("LILLE", "PARIS");
        System.out.println("Voyages directs Lille->Paris trouvés: " + 
                         (parisJourneys != null ? parisJourneys.size() : 0));
        
        if (parisJourneys != null) {
            for (Journey j : parisJourneys) {
                System.out.println("  - " + j.getMeans() + " disponible: " + j.isAvailableWithWeather() +
                                 ", impact: " + j.getWeatherImpactDescription());
            }
        }
        
        System.out.println();
    }
}