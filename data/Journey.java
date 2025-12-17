package data;

import java.io.Serializable;
import java.util.Objects;

/**
 * class that represents a journey<br>
 * departure and arrival point, departure and arrival date, duration, means
 * (car, bus, ..), cost, co2, confort, provider
 *
 * @author emmanueladam
 */
@SuppressWarnings("serial")
public class Journey implements Cloneable, Serializable, Comparable<Journey> {
    /**
     * some fields to improve the memory management
     */
    private static final String TO = " to ";
    private static final String TRAJECTFROM = "traject from ";
    private static final String BY = " by ";
    private static final String DEPARTURE = ", departure: ";
    private static final String ARRIVAL = ", arrival:";
    private static final String COST = ", cost = ";
    private static final String PROPOSEDBY = ", proposed by ";
    private static final String PLACES = ", nb tickets = ";
    /**
     * origin
     */
    String start;
    /**
     * destination
     */
    String stop;
    /**
     * transport means
     */
    String means;
    /**
     * duration of the journey, in minutes
     */
    int duration;
    /**
     * date of departure, format hhmm
     */
    int departureDate;
    /**
     * date of arrival, format hhmm
     */
    int arrivalDate;
    /**
     * cost in money
     */
    double cost;
    /**
     * cost in co2
     */
    int co2;
    /**
     * level of confort (0 = worst)
     */
    int confort;
    /**
     * name of the service that propose the journey
     */
    String proposedBy;
    /**
     * nb of remaining places
     */
    private int places = 1;
    
    /**
     * initial capacity for this type of journey
     */
    private static final int CAR_CAPACITY = 3;
    private static final int BIKE_CAPACITY = 20;
    private static final int BUS_CAPACITY = 50;
    private static final int TRAM_CAPACITY = 200;

    public Journey(final String _start, final String _stop, final String _means, final int _departureDate,
                   final int _duration) {
        start = _start;
        stop = _stop;
        means = _means;
        departureDate = _departureDate;
        duration = _duration;
        arrivalDate = Journey.addTime(departureDate, duration);
        initializePlacesBasedOnMeans();
    }

    Journey(final String _start, final String _stop, final String _means, final int _departureDate, final int _duration,
            final double _cost) {
        this(_start, _stop, _means, _departureDate, _duration);
        cost = _cost;
    }

    public Journey(final String _start, final String _stop, final String _means, final int _departureDate,
                   final int _duration, final double _cost, final int _co2, final int _confort) {
        this(_start, _stop, _means, _departureDate, _duration, _cost);
        co2 = _co2;
        confort = _confort;
    }

    public Journey(final String _start, final String _stop, final String _means, final int _departureDate,
                   final int _duration, final double _cost, final int _co2, final int _confort, String _proposedBy) {
        this(_start, _stop, _means, _departureDate, _duration, _cost, _co2, _confort);
        proposedBy = _proposedBy;
    }

    public Journey(final Journey j){
        this(j.start, j.stop, j.means, j.departureDate, j.duration, j.cost, j.co2, j.confort, j.proposedBy);
        this.places = j.places;
    }

    /**
     * @param time    a date in the format hhmm (the two last digits corresponds to
     *                the minutes)
     * @param minutes nb of mn to add
     * @return the result of the addition time + x in the format hhmm
     */
    public static int addTime(final int time, final int minutes) {
        final int h = time / 100;
        final int mn = time - h * 100;
        return (h + (mn + minutes) / 60) * 100 + (mn + minutes) % 60;
    }

    public static void main(final String... args) {
        final Journey test = new Journey("Val", "Lille", "car", 1440, 90);
        System.out.println(test);
    }

    public Journey clone() {
        Journey clone = null;
        try {
            clone = (Journey) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clone;
    }

    public String getStart() {
        return start;
    }

    public void setStart(final String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(final String stop) {
        this.stop = stop;
    }

    public String getMeans() {
        return means;
    }

    public void setMeans(final String means) {
        this.means = means;
    }

    public int getTime() {
        return duration;
    }

    public void setTime(final int time) {
        this.duration = time;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(final double cost) {
        this.cost = cost;
    }

    public int getCo2() {
        return co2;
    }

    public void setCo2(final int co2) {
        this.co2 = co2;
    }

    public int getConfort() {
        return confort;
    }

    public void setConfort(final int confort) {
        this.confort = confort;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }

    public int getDepartureDate() {
        return departureDate;
    }

    /**
     * set the departure date, and set the arrival date related
     *
     * @param departureDate departure date to set
     */
    public void setDepartureDate(final int departureDate) {
        this.departureDate = departureDate;
        this.arrivalDate = Journey.addTime(departureDate, duration);
        if (departureDate==1210 && means.equalsIgnoreCase("car"))
            arrivalDate =arrivalDate;
    }

    public int getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(final int arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getProposedBy() {
        return proposedBy;
    }

    public void setProposedBy(final String proposedBy) {
        this.proposedBy = proposedBy;
    }


    public int getPlaces() {
        return places;
    }
    
    /**
     * Get available places for this journey at a specific time
     * @param currentTime current time for bike zone management
     * @return number of available places
     */
    public int getAvailablePlaces(int currentTime) {
        // Special handling for bikes
        if (means != null && (means.equalsIgnoreCase("BIKE") || means.equalsIgnoreCase("VELO") || means.equalsIgnoreCase("VÉLO"))) {
            BikeZoneManager bikeManager = BikeZoneManager.getInstance();
            return bikeManager.getAvailableBikes(start, currentTime);
        }
        
        // Regular handling for other transport types
        return places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }
    
    /**
     * Initialize places based on means of transport
     */
    private void initializePlacesBasedOnMeans() {
        if (means == null) {
            places = 1;
            return;
        }
        
        switch (means.toUpperCase()) {
            case "CAR":
            case "VOITURE":
                places = CAR_CAPACITY;
                break;
            case "BIKE":
            case "VELO":
            case "VÉLO":
                places = BIKE_CAPACITY;
                break;
            case "BUS":
                places = BUS_CAPACITY;
                break;
            case "TRAM":
            case "TRAMWAY":
                places = TRAM_CAPACITY;
                break;
            default:
                places = 1;
        }
    }
    
    /**
     * Book a place on this journey
     * @param currentTime current time for bike zone management
     * @return true if booking successful, false if no places available
     */
    public boolean bookPlace(int currentTime) {
        // Special handling for bikes
        if (means != null && (means.equalsIgnoreCase("BIKE") || means.equalsIgnoreCase("VELO") || means.equalsIgnoreCase("VÉLO"))) {
            BikeZoneManager bikeManager = BikeZoneManager.getInstance();
            return bikeManager.bookBike(start, stop, arrivalDate, currentTime);
        }
        
        // Regular handling for other transport types
        if (places > 0) {
            places--;
            return true;
        }
        return false;
    }
    
    /**
     * Book a place on this journey (backward compatibility)
     * @return true if booking successful, false if no places available
     */
    public boolean bookPlace() {
        return bookPlace(departureDate);
    }
    
    /**
     * Check if journey has available places
     * @param currentTime current time for bike zone management
     * @return true if places available
     */
    public boolean hasAvailablePlaces(int currentTime) {
        // Special handling for bikes
        if (means != null && (means.equalsIgnoreCase("BIKE") || means.equalsIgnoreCase("VELO") || means.equalsIgnoreCase("VÉLO"))) {
            BikeZoneManager bikeManager = BikeZoneManager.getInstance();
            return bikeManager.getAvailableBikes(start, currentTime) > 0;
        }
        
        // Regular handling for other transport types
        return places > 0;
    }
    
    /**
     * Check if journey has available places (backward compatibility)
     * @return true if places available
     */
    public boolean hasAvailablePlaces() {
        return hasAvailablePlaces(departureDate);
    }
    
    /**
     * Get the initial capacity for this type of transport
     * @return initial capacity
     */
    public int getInitialCapacity() {
        if (means == null) return 1;
        
        switch (means.toUpperCase()) {
            case "CAR":
            case "VOITURE":
                return CAR_CAPACITY;
            case "BIKE":
            case "VELO":
            case "VÉLO":
                return BIKE_CAPACITY;
            case "BUS":
                return BUS_CAPACITY;
            case "TRAM":
            case "TRAMWAY":
                return TRAM_CAPACITY;
            default:
                return 1;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder(Journey.TRAJECTFROM).append(start).append(Journey.TO).
                append(stop).append(Journey.BY).append(means).append(Journey.DEPARTURE).
                append(departureDate).append(Journey.ARRIVAL).append(arrivalDate).
                append(Journey.COST).append(cost).append(Journey.PROPOSEDBY).append(proposedBy).append(Journey.PLACES).append(places).toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Journey journey = (Journey) o;

        if (departureDate != journey.departureDate) return false;
        if (!Objects.equals(start, journey.start)) return false;
        if (!Objects.equals(stop, journey.stop)) return false;
        return Objects.equals(means, journey.means);
    }



    @Override
    public int compareTo(Journey o) {
        return (int) (cost - o.cost);
    }
}
