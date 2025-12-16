package com.hers;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.util.GHUtility;
import com.hers.algorithms.AStarAlgorithm;
import com.hers.algorithms.PathResult;
import com.hers.model.*;

import java.util.*;

/**
 * Demonstrates multi-ambulance dispatch system
 */
public class AmbulanceDispatchDemo {

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║  HERS - Multi-Ambulance Dispatch System          ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");

        // Load system
        System.out.println("📍 Loading Karachi map and ambulance fleet...");
        GraphHopper hopper = loadGraphHopper();
        GraphExtractor graph = new GraphExtractor(hopper);
        AmbulanceFleet fleet = new AmbulanceFleet();

        System.out.println("✅ System ready!");
        fleet.printFleetStatus();

        AStarAlgorithm aStar = new AStarAlgorithm(graph);

        // EMERGENCY 1: Traffic accident in Clifton
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚨 EMERGENCY #1: Traffic Accident");
        System.out.println("=".repeat(70));
        
        double emergency1Lat = 24.8150, emergency1Lon = 67.0250;
        System.out.println("Location: Clifton Beach Road (" + emergency1Lat + ", " + emergency1Lon + ")");
        System.out.println("Type: Multi-vehicle collision");
        System.out.println("Severity: Critical - 2 injured");

        // Find and dispatch best ambulance
        System.out.println("\n🔍 Analyzing available ambulances...");
        AmbulanceAssignmentResult assignment1 = dispatchBestAmbulance(
            fleet, graph, aStar, emergency1Lat, emergency1Lon, "EMG-001"
        );

        if (assignment1 != null) {
            System.out.println("\n✅ DISPATCHED: " + assignment1.ambulance.id);
            System.out.println("   ETA: " + String.format("%.2f", assignment1.pathResult.totalTime / 60.0) + " minutes");
            System.out.println("   Distance: " + String.format("%.2f", assignment1.pathResult.totalDistance / 1000.0) + " km");
        }

        // Wait a bit (simulate time passing)
        sleep(1000);

        // EMERGENCY 2: Medical emergency in Gulshan
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚨 EMERGENCY #2: Cardiac Arrest");
        System.out.println("=".repeat(70));
        
        double emergency2Lat = 24.9150, emergency2Lon = 67.0750;
        System.out.println("Location: Gulshan-e-Iqbal Block 13 (" + emergency2Lat + ", " + emergency2Lon + ")");
        System.out.println("Type: Cardiac emergency");
        System.out.println("Severity: Critical - immediate response needed");

        System.out.println("\n🔍 Finding nearest available ambulance...");
        AmbulanceAssignmentResult assignment2 = dispatchBestAmbulance(
            fleet, graph, aStar, emergency2Lat, emergency2Lon, "EMG-002"
        );

        if (assignment2 != null) {
            System.out.println("\n✅ DISPATCHED: " + assignment2.ambulance.id);
            System.out.println("   ETA: " + String.format("%.2f", assignment2.pathResult.totalTime / 60.0) + " minutes");
            System.out.println("   Distance: " + String.format("%.2f", assignment2.pathResult.totalDistance / 1000.0) + " km");
        }

        sleep(1000);

        // EMERGENCY 3: Fire injury in Saddar
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚨 EMERGENCY #3: Fire-related Injury");
        System.out.println("=".repeat(70));
        
        double emergency3Lat = 24.8600, emergency3Lon = 67.0100;
        System.out.println("Location: Saddar Empress Market (" + emergency3Lat + ", " + emergency3Lon + ")");
        System.out.println("Type: Burns - fire accident");
        System.out.println("Severity: Moderate");

        System.out.println("\n🔍 Finding nearest available ambulance...");
        AmbulanceAssignmentResult assignment3 = dispatchBestAmbulance(
            fleet, graph, aStar, emergency3Lat, emergency3Lon, "EMG-003"
        );

        if (assignment3 != null) {
            System.out.println("\n✅ DISPATCHED: " + assignment3.ambulance.id);
            System.out.println("   ETA: " + String.format("%.2f", assignment3.pathResult.totalTime / 60.0) + " minutes");
            System.out.println("   Distance: " + String.format("%.2f", assignment3.pathResult.totalDistance / 1000.0) + " km");
        }

        // Current fleet status
        System.out.println("\n" + "=".repeat(70));
        fleet.printFleetStatus();

        // Summary
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DISPATCH SUMMARY");
        System.out.println("=".repeat(70));
        System.out.println("Total emergencies handled: 3");
        System.out.println("Ambulances dispatched: 3");
        
        AmbulanceFleet.FleetStats stats = fleet.getStats();
        System.out.println("Remaining available: " + stats.available + "/" + stats.total);
        
        double avgETA = 0;
        int count = 0;
        if (assignment1 != null) { avgETA += assignment1.pathResult.totalTime; count++; }
        if (assignment2 != null) { avgETA += assignment2.pathResult.totalTime; count++; }
        if (assignment3 != null) { avgETA += assignment3.pathResult.totalTime; count++; }
        
        if (count > 0) {
            avgETA = (avgETA / count) / 60.0;
            System.out.println("Average response ETA: " + String.format("%.2f", avgETA) + " minutes");
        }

        System.out.println("\n✅ All emergencies handled successfully!");
        System.out.println("   System operating at " + 
                         String.format("%.1f", (stats.available * 100.0 / stats.total)) + "% capacity");

        hopper.close();
    }

    /**
     * Find and dispatch the best ambulance for an emergency
     */
    private static AmbulanceAssignmentResult dispatchBestAmbulance(
            AmbulanceFleet fleet, GraphExtractor graph, AStarAlgorithm aStar,
            double emergencyLat, double emergencyLon, String emergencyId) {
        
        List<Ambulance> available = fleet.getAvailableAmbulances();
        
        if (available.isEmpty()) {
            System.out.println("⚠️  WARNING: No ambulances available!");
            return null;
        }

        System.out.println("Available ambulances: " + available.size());

        // Calculate routes for all available ambulances
        List<AmbulanceAssignmentResult> assignments = new ArrayList<>();
        int emergencyNode = graph.findNearestNode(emergencyLat, emergencyLon);

        for (Ambulance ambulance : available) {
            int ambulanceNode = graph.findNearestNode(ambulance.currentLat, ambulance.currentLon);
            PathResult path = aStar.findPath(ambulanceNode, emergencyNode);
            
            if (path.isPathFound()) {
                double straightDist = ambulance.distanceTo(emergencyLat, emergencyLon);
                assignments.add(new AmbulanceAssignmentResult(ambulance, path, straightDist));
            }
        }

        if (assignments.isEmpty()) {
            System.out.println("⚠️  ERROR: No valid routes found!");
            return null;
        }

        // Sort by ETA and show options
        Collections.sort(assignments);

        System.out.println("\n📋 Ambulance Options (sorted by ETA):");
        System.out.println("Rank| ID         | Station                   |  Distance |     ETA | Type");
        System.out.println("----|------------|---------------------------|-----------|---------|----------");
        
        for (int i = 0; i < Math.min(3, assignments.size()); i++) {
            System.out.println(assignments.get(i).getTableRow(i + 1));
        }

        // Dispatch the fastest
        AmbulanceAssignmentResult best = assignments.get(0);
        best.ambulance.dispatch(emergencyId);

        return best;
    }

    private static GraphHopper loadGraphHopper() {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("pakistan-251202.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");
        hopper.setEncodedValuesString("car_access, car_average_speed");
        hopper.setProfiles(
            new Profile("car_profile").setCustomModel(GHUtility.loadCustomModelFromJar("car.json"))
        );
        hopper.importOrLoad();
        return hopper;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}