package data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to initialize bike zones from CSV files
 * 
 * @author System
 */
public class BikeZoneInitializer {
    
    /**
     * Initialize bike zones by scanning all bike journeys in CSV files
     * @param csvFiles list of CSV files to scan for bike journeys
     */
    public static void initializeBikeZones(String... csvFiles) {
        Set<String> zones = new HashSet<>();
        BikeZoneManager bikeManager = BikeZoneManager.getInstance();
        
        for (String csvFile : csvFiles) {
            try {
                List<String> lines = Files.readAllLines(new File(csvFile).toPath());
                for (String line : lines) {
                    if (line.trim().isEmpty() || line.startsWith("#")) continue;
                    
                    String[] fields = line.split(";");
                    if (fields.length >= 3) {
                        String means = fields[2].trim();
                        if (means.equalsIgnoreCase("BIKE") || means.equalsIgnoreCase("VELO") || means.equalsIgnoreCase("VÉLO")) {
                            String start = fields[0].trim().toUpperCase();
                            String stop = fields[1].trim().toUpperCase();
                            zones.add(start);
                            zones.add(stop);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading CSV file: " + csvFile + " - " + e.getMessage());
            }
        }
        
        // Initialize all found zones
        for (String zone : zones) {
            bikeManager.initializeZone(zone);
            System.out.println("Initialized bike zone: " + zone + " with 20 bikes");
        }
    }
    
    /**
     * Initialize common bike zones manually (for testing or default setup)
     */
    public static void initializeCommonBikeZones() {
        BikeZoneManager bikeManager = BikeZoneManager.getInstance();
        
        String[] commonZones = {
            "LILLE", "VALENCIENNES", "MAUBEUGE", "CAMBRAI", "DOUAI", 
            "ARRAS", "BETHUNE", "CALAIS", "DUNKERQUE", "BOULOGNE"
        };
        
        for (String zone : commonZones) {
            bikeManager.initializeZone(zone);
            System.out.println("Initialized common bike zone: " + zone + " with 20 bikes");
        }
    }
}