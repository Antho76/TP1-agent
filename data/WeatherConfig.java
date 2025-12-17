package data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration manager for weather API
 * Handles API key management and configuration
 * 
 * @author System
 */
public class WeatherConfig {
    private static final Logger logger = Logger.getLogger(WeatherConfig.class.getName());
    private static final String CONFIG_FILE = "weather.properties";
    private static final String DEFAULT_API_KEY = "YOUR_API_KEY_HERE"; // Replace with your actual API key
    
    private static WeatherConfig instance;
    private Properties properties;
    
    private WeatherConfig() {
        properties = new Properties();
        loadConfiguration();
    }
    
    public static WeatherConfig getInstance() {
        if (instance == null) {
            instance = new WeatherConfig();
        }
        return instance;
    }
    
    /**
     * Load configuration from properties file
     */
    private void loadConfiguration() {
        try {
            properties.load(new FileInputStream(CONFIG_FILE));
            logger.info("Weather configuration loaded from " + CONFIG_FILE);
        } catch (IOException e) {
            logger.info("Configuration file not found, creating default configuration");
            createDefaultConfiguration();
        }
    }
    
    /**
     * Create default configuration file
     */
    private void createDefaultConfiguration() {
        properties.setProperty("openweather.api.key", DEFAULT_API_KEY);
        properties.setProperty("openweather.api.url", "http://api.openweathermap.org/data/2.5/weather");
        properties.setProperty("weather.cache.duration.minutes", "10");
        properties.setProperty("weather.default.city", "Lille");
        
        saveConfiguration();
        
        logger.warning("Created default weather configuration. Please update your API key in " + CONFIG_FILE);
    }
    
    /**
     * Save configuration to file
     */
    public void saveConfiguration() {
        try {
            properties.store(new FileOutputStream(CONFIG_FILE), 
                           "Weather API Configuration - Generated automatically");
            logger.info("Configuration saved to " + CONFIG_FILE);
        } catch (IOException e) {
            logger.severe("Failed to save configuration: " + e.getMessage());
        }
    }
    
    /**
     * Get OpenWeatherMap API key
     */
    public String getApiKey() {
        return properties.getProperty("openweather.api.key", DEFAULT_API_KEY);
    }
    
    /**
     * Set OpenWeatherMap API key
     */
    public void setApiKey(String apiKey) {
        properties.setProperty("openweather.api.key", apiKey);
    }
    
    /**
     * Get API URL
     */
    public String getApiUrl() {
        return properties.getProperty("openweather.api.url", "http://api.openweathermap.org/data/2.5/weather");
    }
    
    /**
     * Get cache duration in minutes
     */
    public int getCacheDurationMinutes() {
        try {
            return Integer.parseInt(properties.getProperty("weather.cache.duration.minutes", "10"));
        } catch (NumberFormatException e) {
            logger.warning("Invalid cache duration in config, using default 10 minutes");
            return 10;
        }
    }
    
    /**
     * Get default city
     */
    public String getDefaultCity() {
        return properties.getProperty("weather.default.city", "Lille");
    }
    
    /**
     * Set default city
     */
    public void setDefaultCity(String city) {
        properties.setProperty("weather.default.city", city);
    }
    
    /**
     * Check if API key is configured (not default)
     */
    public boolean isApiKeyConfigured() {
        String apiKey = getApiKey();
        return !DEFAULT_API_KEY.equals(apiKey) && 
               apiKey != null && 
               !apiKey.trim().isEmpty();
    }
    
    /**
     * Get configuration summary for logging
     */
    public String getConfigurationSummary() {
        return String.format("Weather API Config: Key=%s, City=%s, Cache=%d minutes", 
                           isApiKeyConfigured() ? "Configured" : "NOT_CONFIGURED",
                           getDefaultCity(),
                           getCacheDurationMinutes());
    }
}