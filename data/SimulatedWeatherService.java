package data;

import sandbox.ollama.data.Meteo;
import java.util.Random;

/**
 * Simulated weather service for testing when API is not available
 * Provides realistic weather data for demonstration purposes
 * 
 * @author System
 */
public class SimulatedWeatherService {
    
    private static SimulatedWeatherService instance;
    private Random random = new Random();
    
    private SimulatedWeatherService() {}
    
    public static SimulatedWeatherService getInstance() {
        if (instance == null) {
            instance = new SimulatedWeatherService();
        }
        return instance;
    }
    
    /**
     * Get simulated weather data for a city
     */
    public Meteo.WeatherData getSimulatedWeather(String city) {
        Meteo.WeatherData weather = new Meteo.WeatherData();
        
        // Set basic info
        weather.setCityName(city);
        weather.setCountry("FR");
        weather.setTimestamp(System.currentTimeMillis());
        
        // Generate realistic weather based on season (winter in December)
        int weatherType = random.nextInt(100);
        
        if (weatherType < 30) {
            // Clear weather
            weather.setDescription("ciel dégagé");
            weather.setMainCondition("Clear");
            weather.setTemperature(5 + random.nextDouble() * 8); // 5-13°C
            weather.setWindSpeed(3 + random.nextDouble() * 5); // 3-8 m/s
            
        } else if (weatherType < 50) {
            // Cloudy
            weather.setDescription("nuageux");
            weather.setMainCondition("Clouds");
            weather.setTemperature(2 + random.nextDouble() * 8); // 2-10°C
            weather.setWindSpeed(5 + random.nextDouble() * 8); // 5-13 m/s
            
        } else if (weatherType < 70) {
            // Rainy (should disable bikes)
            weather.setDescription("pluie légère");
            weather.setMainCondition("Rain");
            weather.setTemperature(3 + random.nextDouble() * 6); // 3-9°C
            weather.setWindSpeed(8 + random.nextDouble() * 7); // 8-15 m/s
            
        } else if (weatherType < 85) {
            // Strong wind (should affect bike duration)
            weather.setDescription("vent fort");
            weather.setMainCondition("Clear");
            weather.setTemperature(1 + random.nextDouble() * 7); // 1-8°C
            weather.setWindSpeed(11 + random.nextDouble() * 8); // 11-19 m/s (strong wind)
            
        } else {
            // Snow (should affect car cost/duration)
            weather.setDescription("neige légère");
            weather.setMainCondition("Snow");
            weather.setTemperature(-2 + random.nextDouble() * 4); // -2 to 2°C
            weather.setWindSpeed(6 + random.nextDouble() * 6); // 6-12 m/s
        }
        
        // Set other realistic values
        weather.setFeelsLike(weather.getTemperature() - 2);
        weather.setTempMin(weather.getTemperature() - 3);
        weather.setTempMax(weather.getTemperature() + 3);
        weather.setHumidity(60 + random.nextInt(35)); // 60-95%
        weather.setPressure(1010 + random.nextInt(20)); // 1010-1030 hPa
        weather.setVisibility(10000 - random.nextInt(5000)); // 5-10km
        weather.setWindDirection(random.nextInt(360));
        
        // Set coordinates (approximative for French cities)
        setApproximateCoordinates(weather, city);
        
        return weather;
    }
    
    /**
     * Set approximate coordinates for French cities
     */
    private void setApproximateCoordinates(Meteo.WeatherData weather, String city) {
        switch (city.toLowerCase()) {
            case "lille":
                weather.setLatitude(50.6292);
                weather.setLongitude(3.0573);
                break;
            case "paris":
                weather.setLatitude(48.8566);
                weather.setLongitude(2.3522);
                break;
            case "lyon":
                weather.setLatitude(45.7640);
                weather.setLongitude(4.8357);
                break;
            case "marseille":
                weather.setLatitude(43.2965);
                weather.setLongitude(5.3698);
                break;
            case "toulouse":
                weather.setLatitude(43.6047);
                weather.setLongitude(1.4442);
                break;
            case "nice":
                weather.setLatitude(43.7102);
                weather.setLongitude(7.2620);
                break;
            default:
                // Default to Lille coordinates
                weather.setLatitude(50.6292 + (random.nextDouble() - 0.5) * 2);
                weather.setLongitude(3.0573 + (random.nextDouble() - 0.5) * 2);
        }
    }
    
    /**
     * Get a description of the simulated weather scenario
     */
    public String getSimulationInfo() {
        return "Mode simulation activé - données météo générées aléatoirement\n" +
               "Scénarios possibles : temps clair, nuageux, pluie, vent fort, neige\n" +
               "Pour utiliser l'API réelle, configurez une clé valide dans weather.properties";
    }
}