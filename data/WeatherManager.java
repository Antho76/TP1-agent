package data;

import sandbox.ollama.data.Meteo;
import java.util.logging.Logger;

/**
 * Weather manager for transport system
 * Handles weather conditions and their impact on journey availability and characteristics
 * 
 * Rules:
 * - Rain and/or very strong wind: bike journeys not offered
 * - Strong wind: bike journeys with 50% longer duration
 * - Snow: car journeys with 50% longer duration and 20% higher cost
 * 
 * @author System
 */
public class WeatherManager {
    private static final Logger logger = Logger.getLogger(WeatherManager.class.getName());
    
    // Weather condition thresholds
    private static final double STRONG_WIND_THRESHOLD = 10.0; // m/s (36 km/h)
    private static final double VERY_STRONG_WIND_THRESHOLD = 15.0; // m/s (54 km/h)
    
    // Adjustment factors
    private static final double WIND_DURATION_FACTOR = 1.5; // +50%
    private static final double SNOW_DURATION_FACTOR = 1.5; // +50%
    private static final double SNOW_COST_FACTOR = 1.2; // +20%
    
    private static WeatherManager instance;
    private Meteo meteoService;
    private String currentCity;
    private Meteo.WeatherData cachedWeatherData;
    private long lastWeatherUpdate = 0;
    private static final long CACHE_DURATION = 10 * 60 * 1000; // 10 minutes
    
    public enum WeatherCondition {
        CLEAR,
        RAIN,
        SNOW,
        STRONG_WIND,
        VERY_STRONG_WIND,
        RAIN_AND_STRONG_WIND
    }
    
    private WeatherManager() {
        meteoService = new Meteo();
        currentCity = "Lille"; // Default city
    }
    
    /**
     * Get singleton instance
     */
    public static WeatherManager getInstance() {
        if (instance == null) {
            instance = new WeatherManager();
        }
        return instance;
    }
    
    /**
     * Set the current city for weather monitoring
     */
    public void setCurrentCity(String city) {
        if (city != null && !city.trim().isEmpty()) {
            this.currentCity = city.trim();
            // Clear cache when city changes
            cachedWeatherData = null;
            lastWeatherUpdate = 0;
            logger.info("Current city set to: " + this.currentCity);
        }
    }
    
    /**
     * Get current city
     */
    public String getCurrentCity() {
        return currentCity;
    }
    
    /**
     * Get current weather data with caching
     */
    public Meteo.WeatherData getCurrentWeather() {
        long now = System.currentTimeMillis();
        
        // Use cached data if fresh enough
        if (cachedWeatherData != null && 
            cachedWeatherData.isValid() && 
            (now - lastWeatherUpdate) < CACHE_DURATION) {
            return cachedWeatherData;
        }
        
        // Fetch fresh weather data
        try {
            cachedWeatherData = meteoService.getWeatherByCity(currentCity);
            if (cachedWeatherData != null && cachedWeatherData.isValid()) {
                lastWeatherUpdate = now;
                logger.info("Weather data updated for " + currentCity);
            } else {
                // Fallback to simulated weather when API fails
                logger.warning("API failed, using simulated weather data for " + currentCity);
                cachedWeatherData = SimulatedWeatherService.getInstance().getSimulatedWeather(currentCity);
                if (cachedWeatherData != null) {
                    lastWeatherUpdate = now;
                }
            }
        } catch (Exception e) {
            logger.severe("Error fetching weather data, using simulation: " + e.getMessage());
            cachedWeatherData = SimulatedWeatherService.getInstance().getSimulatedWeather(currentCity);
            if (cachedWeatherData != null) {
                lastWeatherUpdate = now;
            }
        }
        
        return cachedWeatherData;
    }
    
    /**
     * Analyze current weather conditions
     */
    public WeatherCondition analyzeWeatherConditions() {
        Meteo.WeatherData weather = getCurrentWeather();
        if (weather == null || !weather.isValid()) {
            logger.warning("No valid weather data available, assuming clear conditions");
            return WeatherCondition.CLEAR;
        }
        
        boolean hasRain = isRainy(weather);
        boolean hasSnow = isSnowy(weather);
        double windSpeed = weather.getWindSpeed();
        
        if (hasRain && windSpeed >= VERY_STRONG_WIND_THRESHOLD) {
            return WeatherCondition.RAIN_AND_STRONG_WIND;
        } else if (hasRain) {
            return WeatherCondition.RAIN;
        } else if (hasSnow) {
            return WeatherCondition.SNOW;
        } else if (windSpeed >= VERY_STRONG_WIND_THRESHOLD) {
            return WeatherCondition.VERY_STRONG_WIND;
        } else if (windSpeed >= STRONG_WIND_THRESHOLD) {
            return WeatherCondition.STRONG_WIND;
        }
        
        return WeatherCondition.CLEAR;
    }
    
    /**
     * Check if bike journeys should be offered
     */
    public boolean areBikeJourneysAllowed() {
        WeatherCondition condition = analyzeWeatherConditions();
        
        // Bikes not allowed in rain and/or very strong wind
        return condition != WeatherCondition.RAIN && 
               condition != WeatherCondition.VERY_STRONG_WIND && 
               condition != WeatherCondition.RAIN_AND_STRONG_WIND;
    }
    
    /**
     * Get duration adjustment factor for a transport type
     */
    public double getDurationAdjustmentFactor(String transportType) {
        WeatherCondition condition = analyzeWeatherConditions();
        
        if (transportType == null) return 1.0;
        
        String upperType = transportType.toUpperCase();
        
        switch (condition) {
            case STRONG_WIND:
                if (upperType.equals("BIKE") || upperType.equals("VELO") || upperType.equals("VÉLO")) {
                    return WIND_DURATION_FACTOR;
                }
                break;
                
            case SNOW:
                if (upperType.equals("CAR") || upperType.equals("VOITURE")) {
                    return SNOW_DURATION_FACTOR;
                }
                break;
        }
        
        return 1.0;
    }
    
    /**
     * Get cost adjustment factor for a transport type
     */
    public double getCostAdjustmentFactor(String transportType) {
        WeatherCondition condition = analyzeWeatherConditions();
        
        if (transportType == null) return 1.0;
        
        String upperType = transportType.toUpperCase();
        
        if (condition == WeatherCondition.SNOW && 
            (upperType.equals("CAR") || upperType.equals("VOITURE"))) {
            return SNOW_COST_FACTOR;
        }
        
        return 1.0;
    }
    
    /**
     * Get weather impact description for logging/display
     */
    public String getWeatherImpactDescription() {
        WeatherCondition condition = analyzeWeatherConditions();
        Meteo.WeatherData weather = getCurrentWeather();
        
        StringBuilder description = new StringBuilder();
        description.append("Météo à ").append(currentCity).append(": ");
        
        if (weather != null && weather.isValid()) {
            description.append(weather.getDescription())
                      .append(", vent: ").append(String.format("%.1f", weather.getWindSpeedKmh()))
                      .append(" km/h");
        }
        
        description.append("\nImpact sur les transports: ");
        
        switch (condition) {
            case RAIN:
                description.append("Vélos non disponibles (pluie)");
                break;
            case VERY_STRONG_WIND:
                description.append("Vélos non disponibles (vent très fort)");
                break;
            case RAIN_AND_STRONG_WIND:
                description.append("Vélos non disponibles (pluie et vent fort)");
                break;
            case STRONG_WIND:
                description.append("Durée vélo +50% (vent fort)");
                break;
            case SNOW:
                description.append("Durée voiture +50%, coût +20% (neige)");
                break;
            case CLEAR:
                description.append("Aucun impact");
                break;
        }
        
        return description.toString();
    }
    
    /**
     * Check if weather condition indicates rain
     */
    private boolean isRainy(Meteo.WeatherData weather) {
        String main = weather.getMainCondition();
        String description = weather.getDescription();
        
        if (main == null && description == null) return false;
        
        String[] rainKeywords = {"rain", "pluie", "drizzle", "bruine", "shower", "averse"};
        
        for (String keyword : rainKeywords) {
            if ((main != null && main.toLowerCase().contains(keyword)) ||
                (description != null && description.toLowerCase().contains(keyword))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if weather condition indicates snow
     */
    private boolean isSnowy(Meteo.WeatherData weather) {
        String main = weather.getMainCondition();
        String description = weather.getDescription();
        
        if (main == null && description == null) return false;
        
        String[] snowKeywords = {"snow", "neige", "sleet", "grésil", "blizzard"};
        
        for (String keyword : snowKeywords) {
            if ((main != null && main.toLowerCase().contains(keyword)) ||
                (description != null && description.toLowerCase().contains(keyword))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Force refresh weather data
     */
    public void refreshWeatherData() {
        cachedWeatherData = null;
        lastWeatherUpdate = 0;
        getCurrentWeather();
    }
    
    /**
     * Check if weather data is available and fresh
     */
    public boolean isWeatherDataAvailable() {
        return cachedWeatherData != null && cachedWeatherData.isValid() &&
               (System.currentTimeMillis() - lastWeatherUpdate) < CACHE_DURATION;
    }
    
    /**
     * Check if we're using simulated data
     */
    public boolean isUsingSimulatedData() {
        // Try to fetch real data first
        try {
            Meteo.WeatherData realData = meteoService.getWeatherByCity("London"); // Test city
            return (realData == null || !realData.isValid());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Get information about current data source
     */
    public String getDataSourceInfo() {
        if (isUsingSimulatedData()) {
            return "🔄 Mode simulation - " + SimulatedWeatherService.getInstance().getSimulationInfo();
        } else {
            return "🌐 Données API OpenWeatherMap en temps réel";
        }
    }
}