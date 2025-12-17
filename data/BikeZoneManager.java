package data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager for bike availability in different zones
 * Handles bike movement between zones and time-based availability
 * 
 * @author System
 */
public class BikeZoneManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int INITIAL_BIKES_PER_ZONE = 20;
    
    /**
     * Current number of bikes available in each zone
     */
    private Map<String, Integer> currentBikes;
    
    /**
     * Scheduled bike returns: zone -> list of (arrival_time, number_of_bikes)
     */
    private Map<String, List<BikeReturn>> scheduledReturns;
    
    private static BikeZoneManager instance;
    
    /**
     * Inner class to represent scheduled bike returns
     */
    private static class BikeReturn {
        int arrivalTime;
        int numberOfBikes;
        
        BikeReturn(int arrivalTime, int numberOfBikes) {
            this.arrivalTime = arrivalTime;
            this.numberOfBikes = numberOfBikes;
        }
    }
    
    private BikeZoneManager() {
        currentBikes = new HashMap<>();
        scheduledReturns = new HashMap<>();
    }
    
    /**
     * Get the singleton instance
     */
    public static BikeZoneManager getInstance() {
        if (instance == null) {
            instance = new BikeZoneManager();
        }
        return instance;
    }
    
    /**
     * Initialize a zone with the default number of bikes
     */
    public void initializeZone(String zone) {
        currentBikes.putIfAbsent(zone.toUpperCase(), INITIAL_BIKES_PER_ZONE);
        scheduledReturns.putIfAbsent(zone.toUpperCase(), new ArrayList<>());
    }
    
    /**
     * Get available bikes in a zone at a specific time
     */
    public int getAvailableBikes(String zone, int currentTime) {
        String upperZone = zone.toUpperCase();
        initializeZone(upperZone);
        
        // Process any scheduled returns that should have happened by now
        processScheduledReturns(upperZone, currentTime);
        
        return currentBikes.get(upperZone);
    }
    
    /**
     * Try to book a bike from a zone
     * @param departureZone zone to take bike from
     * @param arrivalZone zone to return bike to
     * @param arrivalTime time when bike will be returned
     * @param currentTime current time for checking availability
     * @return true if booking successful
     */
    public boolean bookBike(String departureZone, String arrivalZone, int arrivalTime, int currentTime) {
        String upperDepartureZone = departureZone.toUpperCase();
        String upperArrivalZone = arrivalZone.toUpperCase();
        
        initializeZone(upperDepartureZone);
        initializeZone(upperArrivalZone);
        
        // Check if bike is available in departure zone
        int availableBikes = getAvailableBikes(upperDepartureZone, currentTime);
        if (availableBikes <= 0) {
            return false;
        }
        
        // Remove bike from departure zone
        currentBikes.put(upperDepartureZone, availableBikes - 1);
        
        // Schedule bike return to arrival zone
        scheduledReturns.get(upperArrivalZone).add(new BikeReturn(arrivalTime, 1));
        
        return true;
    }
    
    /**
     * Process scheduled returns up to the current time
     */
    private void processScheduledReturns(String zone, int currentTime) {
        List<BikeReturn> returns = scheduledReturns.get(zone);
        if (returns == null) return;
        
        int currentBikeCount = currentBikes.get(zone);
        
        // Process all returns that should have happened by now
        returns.removeIf(bikeReturn -> {
            if (bikeReturn.arrivalTime <= currentTime) {
                currentBikes.put(zone, currentBikeCount + bikeReturn.numberOfBikes);
                return true; // Remove from scheduled returns
            }
            return false; // Keep in scheduled returns
        });
    }
    
    /**
     * Get all zones with their current bike counts
     */
    public Map<String, Integer> getAllZonesBikes(int currentTime) {
        Map<String, Integer> result = new HashMap<>();
        for (String zone : currentBikes.keySet()) {
            result.put(zone, getAvailableBikes(zone, currentTime));
        }
        return result;
    }
    
    /**
     * Reset all zones to initial capacity (for testing/debugging)
     */
    public void resetAllZones() {
        for (String zone : currentBikes.keySet()) {
            currentBikes.put(zone, INITIAL_BIKES_PER_ZONE);
            scheduledReturns.get(zone).clear();
        }
    }
}