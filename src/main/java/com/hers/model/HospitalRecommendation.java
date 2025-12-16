package com.hers.model;

/**
 * Represents a hospital recommendation with scoring
 */
public class HospitalRecommendation implements Comparable<HospitalRecommendation> {
    public final Hospital hospital;
    public final double distance; // in meters
    public final double eta; // in minutes
    public final int score;
    public final String reasoning;
    
    public HospitalRecommendation(Hospital hospital, double distance, double eta, 
                                  int score, String reasoning) {
        this.hospital = hospital;
        this.distance = distance;
        this.eta = eta;
        this.score = score;
        this.reasoning = reasoning;
    }
    
    @Override
    public int compareTo(HospitalRecommendation other) {
        // Higher score = better match (reverse order for sorting)
        return Integer.compare(other.score, this.score);
    }
    
    @Override
    public String toString() {
        return String.format("%s - Score: %d/100 - Distance: %.2f km - ETA: %.1f min",
            hospital.name, score, distance / 1000.0, eta);
    }
    
    /**
     * Get detailed recommendation report
     */
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Hospital: %s\n", hospital.name));
        sb.append(String.format("Type: %s\n", hospital.type));
        sb.append(String.format("Match Score: %d/100\n", score));
        sb.append(String.format("Distance: %.2f km\n", distance / 1000.0));
        sb.append(String.format("Estimated Time: %.1f minutes\n", eta));
        sb.append(String.format("Capacity: %d beds\n", hospital.capacity));
        sb.append(String.format("Emergency Department: %s\n", hospital.hasEmergency ? "Yes" : "No"));
        sb.append(String.format("Trauma Center: %s\n", hospital.hasTraumaCenter ? "Yes" : "No"));
        sb.append(String.format("Reasoning: %s\n", reasoning));
        return sb.toString();
    }
}