package com.hers.model;

/**
 * Represents an ambulance with location and status
 */
public class Ambulance {
    public final String id;
    public final String stationName;
    public double currentLat;
    public double currentLon;
    public AmbulanceStatus status;
    public String currentAssignment; // Emergency ID or null
    public final AmbulanceType type;
    
    public enum AmbulanceStatus {
        AVAILABLE,    // Ready for dispatch
        DISPATCHED,   // En route to emergency
        ON_SCENE,     // At emergency location
        TRANSPORTING, // Transporting patient
        UNAVAILABLE   // Out of service
    }
    
    public enum AmbulanceType {
        BASIC,        // Basic Life Support (BLS)
        ADVANCED,     // Advanced Life Support (ALS)
        CRITICAL      // Critical Care Transport
    }
    
    public Ambulance(String id, String stationName, double lat, double lon, AmbulanceType type) {
        this.id = id;
        this.stationName = stationName;
        this.currentLat = lat;
        this.currentLon = lon;
        this.status = AmbulanceStatus.AVAILABLE;
        this.currentAssignment = null;
        this.type = type;
    }
    
    /**
     * Dispatch this ambulance to an emergency
     */
    public void dispatch(String emergencyId) {
        if (status != AmbulanceStatus.AVAILABLE) {
            throw new IllegalStateException("Ambulance " + id + " is not available");
        }
        this.status = AmbulanceStatus.DISPATCHED;
        this.currentAssignment = emergencyId;
    }
    
    /**
     * Mark ambulance as available
     */
    public void makeAvailable() {
        this.status = AmbulanceStatus.AVAILABLE;
        this.currentAssignment = null;
    }
    
    /**
     * Update ambulance location
     */
    public void updateLocation(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
    }
    
    /**
     * Check if ambulance is available for dispatch
     */
    public boolean isAvailable() {
        return status == AmbulanceStatus.AVAILABLE;
    }
    
    /**
     * Get distance to a location
     */
    public double distanceTo(double lat, double lon) {
        return GraphExtractor.haversineDistance(this.currentLat, this.currentLon, lat, lon);
    }
    
    @Override
    public String toString() {
        return String.format("Ambulance %s (%s) - %s - Station: %s - Status: %s",
            id, type, isAvailable() ? "READY" : "BUSY", stationName, status);
    }
    
    /**
     * Get detailed status string
     */
    public String getDetailedStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ID: %s | Type: %s | Status: %s", id, type, status));
        if (currentAssignment != null) {
            sb.append(String.format(" | Assignment: %s", currentAssignment));
        }
        sb.append(String.format(" | Location: (%.4f, %.4f)", currentLat, currentLon));
        return sb.toString();
    }
}