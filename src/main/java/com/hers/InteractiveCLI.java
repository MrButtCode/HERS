package com.hers;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.util.GHUtility;
import com.hers.algorithms.AStarAlgorithm;
import com.hers.algorithms.DijkstraAlgorithm;
import com.hers.algorithms.PathResult;
import com.hers.model.*;
import com.hers.simulation.TrafficSimulator;

import java.util.*;

/**
 * Interactive CLI for HERS - Perfect for live demonstrations
 */
public class InteractiveCLI {
    
    private final Scanner scanner;
    private final GraphHopper hopper;
    private final GraphExtractor graph;
    private final AStarAlgorithm aStar;
    private final DijkstraAlgorithm dijkstra;
    private final HospitalDatabase hospitals;
    private final AmbulanceFleet fleet;
    private final TrafficSimulator traffic;
    private boolean running;
    
    public InteractiveCLI() {
        this.scanner = new Scanner(System.in);
        
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║    Healthcare Emergency Routing System (HERS)    ║");
        System.out.println("║           Interactive Command Interface          ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");
        
        System.out.println("🔄 Initializing system...");
        this.hopper = loadGraphHopper();
        this.graph = new GraphExtractor(hopper);
        this.aStar = new AStarAlgorithm(graph);
        this.dijkstra = new DijkstraAlgorithm(graph);
        this.hospitals = new HospitalDatabase();
        this.fleet = new AmbulanceFleet();
        this.traffic = new TrafficSimulator(graph);
        this.running = true;
        
        System.out.println("✅ System ready!");
        System.out.println("   Graph: " + graph.getNodeCount() + " nodes, " + graph.getEdgeCount() + " edges");
        System.out.println("   Hospitals: " + hospitals.getHospitalCount());
        System.out.println("   Ambulances: " + fleet.getAllAmbulances().size());
    }
    
    public void start() {
        showWelcome();
        
        while (running) {
            System.out.print("\nHERS> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.toLowerCase().split("\\s+");
            String command = parts[0];
            
            try {
                switch (command) {
                    case "help":
                        showHelp();
                        break;
                    case "route":
                        handleRoute();
                        break;
                    case "hospitals":
                        handleHospitals();
                        break;
                    case "ambulances":
                    case "fleet":
                        handleFleet();
                        break;
                    case "dispatch":
                        handleDispatch();
                        break;
                    case "traffic":
                        handleTraffic();
                        break;
                    case "compare":
                        handleCompare();
                        break;
                    case "demo":
                        handleDemo();
                        break;
                    case "status":
                        showStatus();
                        break;
                    case "clear":
                        clearScreen();
                        break;
                    case "exit":
                    case "quit":
                        exit();
                        break;
                    default:
                        System.out.println("❌ Unknown command: " + command);
                        System.out.println("   Type 'help' for available commands");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }
    
    private void showWelcome() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Welcome to HERS Interactive CLI!");
        System.out.println("=".repeat(60));
        System.out.println("Type 'help' to see available commands");
        System.out.println("Type 'demo' for a quick demonstration");
    }
    
    private void showHelp() {
        System.out.println("\n📖 AVAILABLE COMMANDS:");
        System.out.println("=".repeat(60));
        System.out.println("  route        - Calculate route between two points");
        System.out.println("  hospitals    - Find nearest hospitals");
        System.out.println("  ambulances   - View ambulance fleet status");
        System.out.println("  dispatch     - Dispatch ambulance to emergency");
        System.out.println("  traffic      - Simulate traffic conditions");
        System.out.println("  compare      - Compare Dijkstra vs A*");
        System.out.println("  demo         - Run quick demonstration");
        System.out.println("  status       - Show system status");
        System.out.println("  clear        - Clear screen");
        System.out.println("  exit         - Exit the system");
    }
    
    private void handleRoute() {
        System.out.println("\n🗺️  ROUTE CALCULATION");
        System.out.println("Enter coordinates (or press Enter for default Clifton → Saddar):");
        
        System.out.print("From Latitude [24.8200]: ");
        String fromLatStr = scanner.nextLine().trim();
        double fromLat = fromLatStr.isEmpty() ? 24.8200 : Double.parseDouble(fromLatStr);
        
        System.out.print("From Longitude [67.0300]: ");
        String fromLonStr = scanner.nextLine().trim();
        double fromLon = fromLonStr.isEmpty() ? 67.0300 : Double.parseDouble(fromLonStr);
        
        System.out.print("To Latitude [24.8609]: ");
        String toLatStr = scanner.nextLine().trim();
        double toLat = toLatStr.isEmpty() ? 24.8609 : Double.parseDouble(toLatStr);
        
        System.out.print("To Longitude [67.0300]: ");
        String toLonStr = scanner.nextLine().trim();
        double toLon = toLonStr.isEmpty() ? 67.0300 : Double.parseDouble(toLonStr);
        
        int source = graph.findNearestNode(fromLat, fromLon);
        int dest = graph.findNearestNode(toLat, toLon);
        
        System.out.println("\n🔍 Calculating route...");
        PathResult result = aStar.findPath(source, dest);
        
        System.out.println("\n" + result);
        System.out.println("Path: " + result.getPathString());
    }
    
    private void handleHospitals() {
        System.out.println("\n🏥 HOSPITAL FINDER");
        System.out.print("Enter location latitude [24.8200]: ");
        String latStr = scanner.nextLine().trim();
        double lat = latStr.isEmpty() ? 24.8200 : Double.parseDouble(latStr);
        
        System.out.print("Enter location longitude [67.0300]: ");
        String lonStr = scanner.nextLine().trim();
        double lon = lonStr.isEmpty() ? 67.0300 : Double.parseDouble(lonStr);
        
        System.out.print("How many hospitals to find? [5]: ");
        String countStr = scanner.nextLine().trim();
        int count = countStr.isEmpty() ? 5 : Integer.parseInt(countStr);
        
        List<Hospital> nearest = hospitals.findNearestHospitals(lat, lon, count);
        int source = graph.findNearestNode(lat, lon);
        
        System.out.println("\n📋 Nearest Hospitals:");
        System.out.println("Rank| Hospital Name                       | Distance | ETA");
        System.out.println("----|-------------------------------------|----------|----------");
        
        for (int i = 0; i < nearest.size(); i++) {
            Hospital h = nearest.get(i);
            int destNode = graph.findNearestNode(h.lat, h.lon);
            PathResult path = aStar.findPath(source, destNode);
            
            System.out.printf("%-3d | %-35s | %6.2f km | %5.2f min\n",
                i + 1,
                h.name.length() > 35 ? h.name.substring(0, 32) + "..." : h.name,
                path.totalDistance / 1000.0,
                path.totalTime / 60.0);
        }
    }
    
    private void handleFleet() {
        fleet.printFleetStatus();
    }
    
    private void handleDispatch() {
        System.out.println("\n🚑 AMBULANCE DISPATCH");
        
        AmbulanceFleet.FleetStats stats = fleet.getStats();
        if (stats.available == 0) {
            System.out.println("❌ No ambulances available!");
            return;
        }
        
        System.out.print("Emergency latitude [24.8200]: ");
        String latStr = scanner.nextLine().trim();
        double lat = latStr.isEmpty() ? 24.8200 : Double.parseDouble(latStr);
        
        System.out.print("Emergency longitude [67.0300]: ");
        String lonStr = scanner.nextLine().trim();
        double lon = lonStr.isEmpty() ? 67.0300 : Double.parseDouble(lonStr);
        
        List<Ambulance> available = fleet.findNearestAvailable(lat, lon, 3);
        int emergencyNode = graph.findNearestNode(lat, lon);
        
        System.out.println("\n📋 Available Ambulances:");
        System.out.println("Rank| ID         | Station                   | ETA");
        System.out.println("----|------------|---------------------------|----------");
        
        List<AmbulanceAssignmentResult> results = new ArrayList<>();
        for (Ambulance amb : available) {
            int ambNode = graph.findNearestNode(amb.currentLat, amb.currentLon);
            PathResult path = aStar.findPath(ambNode, emergencyNode);
            double dist = amb.distanceTo(lat, lon);
            results.add(new AmbulanceAssignmentResult(amb, path, dist));
        }
        
        Collections.sort(results);
        
        for (int i = 0; i < results.size(); i++) {
            System.out.println(results.get(i).getTableRow(i + 1));
        }
        
        if (!results.isEmpty()) {
            AmbulanceAssignmentResult best = results.get(0);
            best.ambulance.dispatch("CLI-EMG");
            System.out.println("\n✅ Dispatched: " + best.ambulance.id);
            System.out.println("   ETA: " + String.format("%.2f", best.pathResult.totalTime / 60.0) + " minutes");
        }
    }
    
    private void handleTraffic() {
        System.out.println("\n🚦 TRAFFIC SIMULATOR");
        System.out.println("1. Show traffic status");
        System.out.println("2. Clear all traffic");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine().trim();
        
        if (choice.equals("1")) {
            traffic.printTrafficState();
        } else if (choice.equals("2")) {
            traffic.clearAllTraffic();
        }
    }
    
    private void handleCompare() {
        System.out.println("\n⚖️  ALGORITHM COMPARISON");
        System.out.print("From Latitude [24.8200]: ");
        String fromLatStr = scanner.nextLine().trim();
        double fromLat = fromLatStr.isEmpty() ? 24.8200 : Double.parseDouble(fromLatStr);
        
        System.out.print("From Longitude [67.0300]: ");
        String fromLonStr = scanner.nextLine().trim();
        double fromLon = fromLonStr.isEmpty() ? 67.0300 : Double.parseDouble(fromLonStr);
        
        System.out.print("To Latitude [24.8609]: ");
        String toLatStr = scanner.nextLine().trim();
        double toLat = toLatStr.isEmpty() ? 24.8609 : Double.parseDouble(toLatStr);
        
        System.out.print("To Longitude [67.0300]: ");
        String toLonStr = scanner.nextLine().trim();
        double toLon = toLonStr.isEmpty() ? 67.0300 : Double.parseDouble(toLonStr);
        
        int source = graph.findNearestNode(fromLat, fromLon);
        int dest = graph.findNearestNode(toLat, toLon);
        
        System.out.println("\n🔍 Running both algorithms...");
        PathResult dijkstraResult = dijkstra.findPath(source, dest);
        PathResult aStarResult = aStar.findPath(source, dest);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("COMPARISON RESULTS");
        System.out.println("=".repeat(60));
        System.out.printf("%-20s | %-15s | %-15s\n", "Metric", "Dijkstra", "A*");
        System.out.println("-".repeat(60));
        System.out.printf("%-20s | %-15.2f | %-15.2f\n", "Distance (km)", 
            dijkstraResult.totalDistance / 1000.0, aStarResult.totalDistance / 1000.0);
        System.out.printf("%-20s | %-15.2f | %-15.2f\n", "Time (min)", 
            dijkstraResult.totalTime / 60.0, aStarResult.totalTime / 60.0);
        System.out.printf("%-20s | %-15d | %-15d\n", "Nodes expanded", 
            dijkstraResult.nodesExpanded, aStarResult.nodesExpanded);
        System.out.printf("%-20s | %-15.3f | %-15.3f\n", "Compute time (ms)", 
            dijkstraResult.computeTimeMs, aStarResult.computeTimeMs);
    }
    
    private void handleDemo() {
        System.out.println("\n🎬 RUNNING QUICK DEMO...\n");
        System.out.println("Scenario: Emergency in Clifton, find nearest hospital");
        
        double lat = 24.8200, lon = 67.0300;
        List<Hospital> nearest = hospitals.findNearestHospitals(lat, lon, 3);
        int source = graph.findNearestNode(lat, lon);
        
        System.out.println("\n✅ Top 3 nearest hospitals:");
        for (int i = 0; i < Math.min(3, nearest.size()); i++) {
            Hospital h = nearest.get(i);
            int dest = graph.findNearestNode(h.lat, h.lon);
            PathResult path = aStar.findPath(source, dest);
            System.out.printf("%d. %s - %.2f km, %.2f min\n",
                i + 1, h.name, path.totalDistance / 1000.0, path.totalTime / 60.0);
        }
    }
    
    private void showStatus() {
        System.out.println("\n📊 SYSTEM STATUS");
        System.out.println("=".repeat(60));
        System.out.println("Graph: " + graph.getNodeCount() + " nodes, " + graph.getEdgeCount() + " edges");
        System.out.println("Hospitals: " + hospitals.getHospitalCount());
        
        AmbulanceFleet.FleetStats stats = fleet.getStats();
        System.out.println("Ambulances: " + stats.total + " total, " + 
                         stats.available + " available, " + stats.dispatched + " dispatched");
    }
    
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    private void exit() {
        System.out.println("\n👋 Shutting down HERS...");
        hopper.close();
        scanner.close();
        running = false;
        System.out.println("✅ System shutdown complete. Goodbye!");
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
    
    public static void main(String[] args) {
        InteractiveCLI cli = new InteractiveCLI();
        cli.start();
    }
}