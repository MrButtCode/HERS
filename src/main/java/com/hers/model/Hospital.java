package com.hers.model;

/**
 * Represents a hospital or medical facility
 */
public class Hospital {
    public final String name;
    public final String type;  // "General", "Emergency", "Trauma Center", "Specialized"
    public final double lat;
    public final double lon;
    public final int capacity; // Available beds
    public final boolean hasEmergency;
    public final boolean hasTraumaCenter;
    
    public Hospital(String name, String type, double lat, double lon, 
                   int capacity, boolean hasEmergency, boolean hasTraumaCenter) {
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.capacity = capacity;
        this.hasEmergency = hasEmergency;
        this.hasTraumaCenter = hasTraumaCenter;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - Emergency: %s, Trauma: %s",
            name, type, hasEmergency ? "Yes" : "No", hasTraumaCenter ? "Yes" : "No");
    }
    
    /**
     * Calculate distance to this hospital from given coordinates
     */
    public double distanceTo(double fromLat, double fromLon) {
        return GraphExtractor.haversineDistance(fromLat, fromLon, this.lat, this.lon);
    }
}