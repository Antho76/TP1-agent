package data;

import sandbox.ollama.data.Meteo;

/**
 * Diagnostic tool for weather API configuration
 * Helps troubleshoot API key and connection issues
 * 
 * @author System
 */
public class WeatherApiDiagnostic {
    
    public static void main(String[] args) {
        System.out.println("=== Diagnostic de l'API Météo ===\n");
        
        // Test configuration
        testConfiguration();
        
        // Test API key
        testApiKey();
        
        // Test connection
        testConnection();
        
        System.out.println("\n=== Fin du diagnostic ===");
    }
    
    private static void testConfiguration() {
        System.out.println("1. Test de la configuration:");
        
        WeatherConfig config = WeatherConfig.getInstance();
        String apiKey = config.getApiKey();
        
        System.out.println("Clé API: " + (apiKey.length() > 8 ? 
            apiKey.substring(0, 8) + "..." : apiKey));
        System.out.println("URL API: " + config.getApiUrl());
        System.out.println("Ville par défaut: " + config.getDefaultCity());
        System.out.println("Durée cache: " + config.getCacheDurationMinutes() + " min");
        System.out.println("Clé configurée: " + config.isApiKeyConfigured());
        System.out.println();
    }
    
    private static void testApiKey() {
        System.out.println("2. Test de la clé API:");
        
        WeatherConfig config = WeatherConfig.getInstance();
        String apiKey = config.getApiKey();
        
        // Vérifications de base
        System.out.println("Longueur clé: " + apiKey.length() + " caractères");
        System.out.println("Contient guillemets: " + (apiKey.contains("\"") ? "OUI ⚠️" : "Non"));
        System.out.println("Contient espaces: " + (apiKey.contains(" ") ? "OUI ⚠️" : "Non"));
        System.out.println(Boolean.parseBoolean("Format hexadécimal: " + apiKey.matches("[a-fA-F0-9]+")) ? "OUI" : "Non");
        
        if (apiKey.contains("\"")) {
            System.out.println("⚠️  PROBLÈME: La clé contient des guillemets!");
            System.out.println("   Supprimez les guillemets du fichier weather.properties");
        }
        
        if (apiKey.length() != 32) {
            System.out.println("⚠️  ATTENTION: Longueur inhabituelle pour une clé OpenWeatherMap");
            System.out.println("   Les clés standard font 32 caractères");
        }
        
        System.out.println();
    }
    
    private static void testConnection() {
        System.out.println("3. Test de connexion API:");
        
        try {
            Meteo meteoService = new Meteo();
            
            System.out.println("Tentative de connexion à l'API...");
            
            Meteo.WeatherData weather = meteoService.getWeatherByCity("London");
            
            if (weather != null && weather.isValid()) {
                System.out.println("✅ Connexion réussie!");
                System.out.println("Ville: " + weather.getCityName());
                System.out.println("Température: " + weather.getTemperature() + "°C");
                System.out.println("Description: " + weather.getDescription());
            } else {
                System.out.println("❌ Échec de récupération des données");
                System.out.println("Vérifiez:");
                System.out.println("1. Que votre clé API est valide et active");
                System.out.println("2. Que vous avez une connexion internet");
                System.out.println("3. Que votre clé n'a pas dépassé les quotas");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du test: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * Test avec une ville française
     */
    public static void testFrenchCity() {
        System.out.println("4. Test avec ville française:");
        
        try {
            Meteo meteoService = new Meteo();
            Meteo.WeatherData weather = meteoService.getWeatherByCity("Lille");
            
            if (weather != null && weather.isValid()) {
                System.out.println("✅ Test Lille réussi!");
                System.out.println(weather.toString());
            } else {
                System.out.println("❌ Échec test Lille");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erreur test Lille: " + e.getMessage());
        }
    }
    
    /**
     * Suggestions pour résoudre les problèmes courants
     */
    public static void printTroubleshootingTips() {
        System.out.println("=== Guide de résolution des problèmes ===");
        System.out.println();
        System.out.println("Si vous obtenez 'Clé API invalide':");
        System.out.println("1. Vérifiez que votre clé est correcte sur https://openweathermap.org/api");
        System.out.println("2. Supprimez les guillemets dans weather.properties");
        System.out.println("3. Attendez quelques minutes après création de la clé");
        System.out.println("4. Vérifiez que votre compte est activé");
        System.out.println();
        System.out.println("Si vous obtenez des erreurs de connexion:");
        System.out.println("1. Vérifiez votre connexion internet");
        System.out.println("2. Vérifiez que le firewall n'bloque pas l'accès");
        System.out.println("3. Essayez avec une autre ville (ex: London)");
        System.out.println();
        System.out.println("Formats de clé corrects:");
        System.out.println("✅ a40c8adb0ed9179f41224123d7f80d38");
        System.out.println("❌ \"a40c8adb0ed9179f41224123d7f80d38\"");
        System.out.println("❌ 'a40c8adb0ed9179f41224123d7f80d38'");
    }
}