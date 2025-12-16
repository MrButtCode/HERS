package com.hers.model;

import com.hers.algorithms.PathResult;

/**
 * Combines hospital information with route details
 */
public class HospitalRouteResult implements Comparable<HospitalRouteResult> {
    public final Hospital hospital;
    public final PathResult pathResult;
    public final double straightLineDistance; // Direct distance in meters
    
    public HospitalRouteResult(Hospital hospital, PathResult pathResult, double straightLineDistance) {
        this.hospital = hospital;
        this.pathResult = pathResult;
        this.straightLineDistance = straightLineDistance;
    }
    
    @Override
    public int compareTo(HospitalRouteResult other) {
        // Compare by total time (ETA)
        return Double.compare(this.pathResult.totalTime, other.pathResult.totalTime);
    }
    
    @Override
    public String toString() {
        return String.format(
            "%s\n" +
            "  Route Distance: %.2f km\n" +
            "  Straight-line Distance: %.2f km\n" +
            "  ETA: %.2f minutes\n" +
            "  Emergency: %s | Trauma Center: %s",
            hospital.name,
            pathResult.totalDistance / 1000.0,
            straightLineDistance / 1000.0,
            pathResult.totalTime / 60.0,
            hospital.hasEmergency ? "✓" : "✗",
            hospital.hasTraumaCenter ? "✓" : "✗"
        );
    }
    
    /**
     * Get formatted summary for comparison table
     */
    public String getTableRow(int rank) {
        return String.format(
            "%-3d | %-35s | %6.2f km | %5.2f min | %3s | %3s",
            rank,
            truncate(hospital.name, 35),
            pathResult.totalDistance / 1000.0,
            pathResult.totalTime / 60.0,
            hospital.hasEmergency ? "Yes" : "No",
            hospital.hasTraumaCenter ? "Yes" : "No"
        );
    }
    
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}