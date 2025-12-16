package com.hers.model;

import java.util.*;

/**
 * Manages fleet of ambulances across Karachi
 */
public class AmbulanceFleet {
    
    private final Map<String, Ambulance> ambulances;
    
    public AmbulanceFleet() {
        this.ambulances = new LinkedHashMap<>();
        initializeFleet();
    }
    
    /**
     * Initialize ambulances at various stations across Karachi
     */
    private void initializeFleet() {
        // Clifton Station
        addAmbulance("AMB-001", "Clifton Station", 24.8200, 67.0300, Ambulance.AmbulanceType.ADVANCED);
        addAmbulance("AMB-002", "Clifton Station", 24.8200, 67.0300, Ambulance.AmbulanceType.BASIC);
        
        // Saddar Station
        addAmbulance("AMB-003", "Saddar Station", 24.8600, 67.0100, Ambulance.AmbulanceType.CRITICAL);
        addAmbulance("AMB-004", "Saddar Station", 24.8600, 67.0100, Ambulance.AmbulanceType.ADVANCED);
        
        // Gulshan Station
        addAmbulance("AMB-005", "Gulshan Station", 24.9200, 67.0800, Ambulance.AmbulanceType.ADVANCED);
        addAmbulance("AMB-006", "Gulshan Station", 24.9200, 67.0800, Ambulance.AmbulanceType.BASIC);
        
        // North Nazimabad Station
        addAmbulance("AMB-007", "North Nazimabad Station", 24.9300, 67.0400, Ambulance.AmbulanceType.BASIC);
        addAmbulance("AMB-008", "North Nazimabad Station", 24.9300, 67.0400, Ambulance.AmbulanceType.ADVANCED);
        
        // Korangi Station
        addAmbulance("AMB-009", "Korangi Station", 24.8400, 67.1200, Ambulance.AmbulanceType.BASIC);
        addAmbulance("AMB-010", "Korangi Station", 24.8400, 67.1200, Ambulance.AmbulanceType.ADVANCED);
        
        // Malir Station
        addAmbulance("AMB-011", "Malir Station", 24.8900, 67.1100, Ambulance.AmbulanceType.BASIC);
        
        // DHA Station
        addAmbulance("AMB-012", "DHA Station", 24.8100, 67.0600, Ambulance.AmbulanceType.CRITICAL);
    }
    
    /**
     * Add an ambulance to the fleet
     */
    private void addAmbulance(String id, String station, double lat, double lon, Ambulance.AmbulanceType type) {
        ambulances.put(id, new Ambulance(id, station, lat, lon, type));
    }
    
    /**
     * Get all ambulances
     */
    public List<Ambulance> getAllAmbulances() {
        return new ArrayList<>(ambulances.values());
    }
    
    /**
     * Get available ambulances
     */
    public List<Ambulance> getAvailableAmbulances() {
        List<Ambulance> available = new ArrayList<>();
        for (Ambulance ambulance : ambulances.values()) {
            if (ambulance.isAvailable()) {
                available.add(ambulance);
            }
        }
        return available;
    }
    
    /**
     * Get ambulance by ID
     */
    public Ambulance getAmbulance(String id) {
        return ambulances.get(id);
    }
    
    /**
     * Find nearest available ambulance to a location
     */
    public Ambulance findNearestAvailable(double lat, double lon) {
        Ambulance nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Ambulance ambulance : ambulances.values()) {
            if (ambulance.isAvailable()) {
                double distance = ambulance.distanceTo(lat, lon);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = ambulance;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * Find N nearest available ambulances
     */
    public List<Ambulance> findNearestAvailable(double lat, double lon, int count) {
        List<AmbulanceDistance> distances = new ArrayList<>();
        
        for (Ambulance ambulance : ambulances.values()) {
            if (ambulance.isAvailable()) {
                double distance = ambulance.distanceTo(lat, lon);
                distances.add(new AmbulanceDistance(ambulance, distance));
            }
        }
        
        distances.sort(Comparator.comparingDouble(ad -> ad.distance));
        
        List<Ambulance> nearest = new ArrayList<>();
        for (int i = 0; i < Math.min(count, distances.size()); i++) {
            nearest.add(distances.get(i).ambulance);
        }
        
        return nearest;
    }
    
    /**
     * Get fleet statistics
     */
    public FleetStats getStats() {
        int total = ambulances.size();
        int available = 0;
        int dispatched = 0;
        int transporting = 0;
        
        for (Ambulance ambulance : ambulances.values()) {
            switch (ambulance.status) {
                case AVAILABLE:
                    available++;
                    break;
                case DISPATCHED:
                case ON_SCENE:
                    dispatched++;
                    break;
                case TRANSPORTING:
                    transporting++;
                    break;
                default:
                    break;
            }
        }
        
        return new FleetStats(total, available, dispatched, transporting);
    }
    
    /**
     * Print fleet status
     */
    public void printFleetStatus() {
        System.out.println("\n📊 FLEET STATUS");
        System.out.println("=".repeat(80));
        
        FleetStats stats = getStats();
        System.out.println(String.format("Total: %d | Available: %d | Dispatched: %d | Transporting: %d",
            stats.total, stats.available, stats.dispatched, stats.transporting));
        
        System.out.println("\nDETAILED STATUS:");
        for (Ambulance ambulance : ambulances.values()) {
            String statusIcon = ambulance.isAvailable() ? "✅" : "🚨";
            System.out.println(statusIcon + " " + ambulance.getDetailedStatus());
        }
    }
    
    /**
     * Helper class for sorting ambulances by distance
     */
    private static class AmbulanceDistance {
        Ambulance ambulance;
        double distance;
        
        AmbulanceDistance(Ambulance ambulance, double distance) {
            this.ambulance = ambulance;
            this.distance = distance;
        }
    }
    
    /**
     * Fleet statistics
     */
    public static class FleetStats {
        public final int total;
        public final int available;
        public final int dispatched;
        public final int transporting;
        
        FleetStats(int total, int available, int dispatched, int transporting) {
            this.total = total;
            this.available = available;
            this.dispatched = dispatched;
            this.transporting = transporting;
        }
    }
}