package com.hers;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.util.GHUtility;
import com.hers.algorithms.AStarAlgorithm;
import com.hers.algorithms.PathResult;
import com.hers.model.*;

import java.util.*;

/**
 * Demonstrates finding nearest hospitals and comparing routes
 */
public class HospitalFinderDemo {

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║  HERS - Hospital Finder & Route Comparison       ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");

        // Load map and hospital database
        System.out.println("📍 Loading Karachi map and hospital database...");
        GraphHopper hopper = loadGraphHopper();
        GraphExtractor graph = new GraphExtractor(hopper);
        HospitalDatabase hospitalDB = new HospitalDatabase();

        System.out.println("✅ Loaded " + hospitalDB.getHospitalCount() + " hospitals in Karachi");

        // Emergency scenario - accident in Clifton
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚨 EMERGENCY CALL: Accident victim in Clifton");
        System.out.println("=".repeat(60));
        
        double accidentLat = 24.8200, accidentLon = 67.0350;
        System.out.println("Location: Clifton Beach Area (" + accidentLat + ", " + accidentLon + ")");
        System.out.println("Condition: Trauma - requires immediate hospital transport");

        int sourceNode = graph.findNearestNode(accidentLat, accidentLon);
        System.out.println("Nearest road node: " + sourceNode);

        // Find 5 nearest hospitals
        System.out.println("\n🔍 Searching for nearest hospitals...");
        List<Hospital> nearestHospitals = hospitalDB.findNearestHospitals(accidentLat, accidentLon, 5);

        // Calculate routes to each hospital
        System.out.println("\n🗺️  Calculating routes to candidate hospitals...");
        AStarAlgorithm aStar = new AStarAlgorithm(graph);
        List<HospitalRouteResult> routeResults = new ArrayList<>();

        for (Hospital hospital : nearestHospitals) {
            int destNode = graph.findNearestNode(hospital.lat, hospital.lon);
            PathResult path = aStar.findPath(sourceNode, destNode);
            double straightDist = hospital.distanceTo(accidentLat, accidentLon);
            
            if (path.isPathFound()) {
                routeResults.add(new HospitalRouteResult(hospital, path, straightDist));
            }
        }

        // Sort by ETA
        Collections.sort(routeResults);

        // Display results
        System.out.println("\n" + "=".repeat(60));
        System.out.println("HOSPITAL COMPARISON - Sorted by ETA");
        System.out.println("=".repeat(60));
        
        System.out.println("Rank| Hospital Name                       | Distance | ETA      | ER  | Trauma");
        System.out.println("----|-------------------------------------|----------|----------|-----|-------");
        
        for (int i = 0; i < routeResults.size(); i++) {
            System.out.println(routeResults.get(i).getTableRow(i + 1));
        }

        // Recommend best hospital
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RECOMMENDATION");
        System.out.println("=".repeat(60));
        
        HospitalRouteResult fastest = routeResults.get(0);
        System.out.println("🏥 RECOMMENDED: " + fastest.hospital.name);
        System.out.println("\nReason: Fastest ETA (" + 
                         String.format("%.2f", fastest.pathResult.totalTime / 60.0) + " minutes)");
        System.out.println("Route Distance: " + 
                         String.format("%.2f", fastest.pathResult.totalDistance / 1000.0) + " km");
        System.out.println("Facilities:");
        System.out.println("  • Emergency Room: " + (fastest.hospital.hasEmergency ? "✓ Available" : "✗ Not Available"));
        System.out.println("  • Trauma Center: " + (fastest.hospital.hasTraumaCenter ? "✓ Available" : "✗ Not Available"));
        System.out.println("  • Capacity: " + fastest.hospital.capacity + " beds");

        // Show alternative if recommended doesn't have trauma center
        if (!fastest.hospital.hasTraumaCenter) {
            System.out.println("\n⚠️  NOTE: Recommended hospital lacks trauma center");
            
            // Find nearest trauma center
            for (HospitalRouteResult result : routeResults) {
                if (result.hospital.hasTraumaCenter) {
                    double timeDiff = (result.pathResult.totalTime - fastest.pathResult.totalTime) / 60.0;
                    System.out.println("\nNearest Trauma Center: " + result.hospital.name);
                    System.out.println("  Additional travel time: +" + String.format("%.2f", timeDiff) + " minutes");
                    System.out.println("  Consider if case severity requires trauma specialists");
                    break;
                }
            }
        }

        // Detailed route for recommended hospital
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DETAILED ROUTE TO RECOMMENDED HOSPITAL");
        System.out.println("=".repeat(60));
        System.out.println(fastest.pathResult);
        System.out.println("\nRoute path: " + fastest.pathResult.getPathString());

        // Statistics
        System.out.println("\n" + "=".repeat(60));
        System.out.println("STATISTICS");
        System.out.println("=".repeat(60));
        
        double avgETA = routeResults.stream()
            .mapToDouble(r -> r.pathResult.totalTime)
            .average()
            .orElse(0.0) / 60.0;
            
        double maxETA = routeResults.stream()
            .mapToDouble(r -> r.pathResult.totalTime)
            .max()
            .orElse(0.0) / 60.0;
            
        System.out.println("Hospitals analyzed: " + routeResults.size());
        System.out.println("Fastest ETA: " + String.format("%.2f", fastest.pathResult.totalTime / 60.0) + " minutes");
        System.out.println("Average ETA: " + String.format("%.2f", avgETA) + " minutes");
        System.out.println("Slowest ETA: " + String.format("%.2f", maxETA) + " minutes");
        
        long traumaCenters = routeResults.stream()
            .filter(r -> r.hospital.hasTraumaCenter)
            .count();
        System.out.println("Trauma centers in range: " + traumaCenters + "/" + routeResults.size());

        hopper.close();
        System.out.println("\n✅ Analysis completed successfully!");
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
}