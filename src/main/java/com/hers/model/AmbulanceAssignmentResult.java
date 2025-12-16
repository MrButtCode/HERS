package com.hers.model;

import com.hers.algorithms.PathResult;

/**
 * Result of ambulance assignment including route details
 */
public class AmbulanceAssignmentResult implements Comparable<AmbulanceAssignmentResult> {
    public final Ambulance ambulance;
    public final PathResult pathResult;
    public final double straightLineDistance;
    
    public AmbulanceAssignmentResult(Ambulance ambulance, PathResult pathResult, double straightLineDistance) {
        this.ambulance = ambulance;
        this.pathResult = pathResult;
        this.straightLineDistance = straightLineDistance;
    }
    
    @Override
    public int compareTo(AmbulanceAssignmentResult other) {
        // Compare by ETA
        return Double.compare(this.pathResult.totalTime, other.pathResult.totalTime);
    }
    
    /**
     * Get formatted table row for comparison
     */
    public String getTableRow(int rank) {
        return String.format(
            "%-3d | %-10s | %-25s | %8.2f km | %7.2f min | %-8s",
            rank,
            ambulance.id,
            truncate(ambulance.stationName, 25),
            pathResult.totalDistance / 1000.0,
            pathResult.totalTime / 60.0,
            ambulance.type
        );
    }
    
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    @Override
    public String toString() {
        return String.format(
            "Ambulance: %s (%s)\n" +
            "Station: %s\n" +
            "Distance: %.2f km\n" +
            "ETA: %.2f minutes\n" +
            "Type: %s",
            ambulance.id,
            ambulance.type,
            ambulance.stationName,
            pathResult.totalDistance / 1000.0,
            pathResult.totalTime / 60.0,
            ambulance.type
        );
    }
}