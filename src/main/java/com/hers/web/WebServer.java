package com.hers.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.util.GHUtility;
import com.hers.algorithms.AStarAlgorithm;
import com.hers.algorithms.PathResult;
import com.hers.model.*;
import com.hers.service.HospitalMatcher;
import spark.Spark;

import java.util.*;

import static spark.Spark.*;

/**
 * REST API Server for HERS Web Dashboard
 * Exposes Java backend functionality via HTTP endpoints
 */
public class WebServer {
    
    private final GraphExtractor graph;
    private final AStarAlgorithm aStar;
    private final HospitalDatabase hospitals;
    private final AmbulanceFleet fleet;
    private final Gson gson;
    
    public WebServer() {
        System.out.println("🚀 Starting HERS Web Server...");
        
        // IMPORTANT: Configure port and static files FIRST, before anything else
        port(4567);
        staticFiles.location("/public");
        
        // Initialize backend
        GraphHopper hopper = loadGraphHopper();
        this.graph = new GraphExtractor(hopper);
        this.aStar = new AStarAlgorithm(graph);
        this.hospitals = new HospitalDatabase();
        this.fleet = new AmbulanceFleet();
        this.gson = new Gson();
        
        System.out.println("✅ Backend initialized");
        
        // Enable CORS and setup routes
        enableCORS();
        setupRoutes();
        
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║  HERS Web Server Running                         ║");
        System.out.println("║  Open: http://localhost:4567                     ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
    }
    
    private void setupRoutes() {
        // Health check
        get("/api/status", (req, res) -> {
            res.type("application/json");
            Map<String, Object> status = new HashMap<>();
            status.put("status", "online");
            status.put("nodes", graph.getNodeCount());
            status.put("edges", graph.getEdgeCount());
            status.put("hospitals", hospitals.getHospitalCount());
            status.put("ambulances", fleet.getAllAmbulances().size());
            return gson.toJson(status);
        });
        
        // Calculate route
        post("/api/route", (req, res) -> {
            res.type("application/json");
            Map<String, Double> body = gson.fromJson(req.body(), Map.class);
            
            double fromLat = body.get("fromLat");
            double fromLon = body.get("fromLon");
            double toLat = body.get("toLat");
            double toLon = body.get("toLon");
            
            int source = graph.findNearestNode(fromLat, fromLon);
            int dest = graph.findNearestNode(toLat, toLon);
            
            PathResult result = aStar.findPath(source, dest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isPathFound());
            response.put("distance", result.totalDistance);
            response.put("time", result.totalTime);
            response.put("nodesExpanded", result.nodesExpanded);
            response.put("computeTime", result.computeTimeMs);
            
            // Convert path to coordinates
            List<Map<String, Double>> pathCoords = new ArrayList<>();
            for (int nodeId : result.path) {
                Node node = graph.getNode(nodeId);
                if (node != null) {
                    Map<String, Double> coord = new HashMap<>();
                    coord.put("lat", node.lat);
                    coord.put("lon", node.lon);
                    pathCoords.add(coord);
                }
            }
            response.put("path", pathCoords);
            
            return gson.toJson(response);
        });
        
        // Get all hospitals
        get("/api/hospitals", (req, res) -> {
            res.type("application/json");
            List<Map<String, Object>> hospitalList = new ArrayList<>();
            
            for (Hospital h : hospitals.getAllHospitals()) {
                Map<String, Object> hMap = new HashMap<>();
                hMap.put("name", h.name);
                hMap.put("type", h.type);
                hMap.put("lat", h.lat);
                hMap.put("lon", h.lon);
                hMap.put("hasEmergency", h.hasEmergency);
                hMap.put("hasTrauma", h.hasTraumaCenter);
                hospitalList.add(hMap);
            }
            
            return gson.toJson(hospitalList);
        });
        
        // Find nearest hospitals
        post("/api/hospitals/nearest", (req, res) -> {
            res.type("application/json");
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            
            double lat = (double) body.get("lat");
            double lon = (double) body.get("lon");
            int count = body.containsKey("count") ? 
                ((Double) body.get("count")).intValue() : 5;
            
            List<Hospital> nearest = hospitals.findNearestHospitals(lat, lon, count);
            int source = graph.findNearestNode(lat, lon);
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Hospital h : nearest) {
                int dest = graph.findNearestNode(h.lat, h.lon);
                PathResult path = aStar.findPath(source, dest);
                
                Map<String, Object> hMap = new HashMap<>();
                hMap.put("name", h.name);
                hMap.put("type", h.type);
                hMap.put("lat", h.lat);
                hMap.put("lon", h.lon);
                hMap.put("distance", path.totalDistance);
                hMap.put("eta", path.totalTime / 60.0);
                hMap.put("hasEmergency", h.hasEmergency);
                hMap.put("hasTrauma", h.hasTraumaCenter);
                results.add(hMap);
            }
            
            return gson.toJson(results);
        });
        
        // Get all ambulances
        get("/api/ambulances", (req, res) -> {
            res.type("application/json");
            List<Map<String, Object>> ambulanceList = new ArrayList<>();
            
            for (Ambulance a : fleet.getAllAmbulances()) {
                Map<String, Object> aMap = new HashMap<>();
                aMap.put("id", a.id);
                aMap.put("station", a.stationName);
                aMap.put("lat", a.currentLat);
                aMap.put("lon", a.currentLon);
                aMap.put("type", a.type.toString());
                aMap.put("status", a.status.toString());
                aMap.put("available", a.isAvailable());
                ambulanceList.add(aMap);
            }
            
            return gson.toJson(ambulanceList);
        });
        
        // Dispatch ambulance
        post("/api/dispatch", (req, res) -> {
            res.type("application/json");
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            
            double lat = (double) body.get("lat");
            double lon = (double) body.get("lon");
            
            Ambulance nearest = fleet.findNearestAvailable(lat, lon);
            
            if (nearest == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "No ambulances available");
                return gson.toJson(error);
            }
            
            int ambNode = graph.findNearestNode(nearest.currentLat, nearest.currentLon);
            int emergencyNode = graph.findNearestNode(lat, lon);
            PathResult path = aStar.findPath(ambNode, emergencyNode);
            
            nearest.dispatch("WEB-EMG-" + System.currentTimeMillis());
            
            // Convert path to coordinates
            List<Map<String, Double>> pathCoords = new ArrayList<>();
            for (int nodeId : path.path) {
                Node node = graph.getNode(nodeId);
                if (node != null) {
                    Map<String, Double> coord = new HashMap<>();
                    coord.put("lat", node.lat);
                    coord.put("lon", node.lon);
                    pathCoords.add(coord);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ambulanceId", nearest.id);
            response.put("station", nearest.stationName);
            response.put("distance", path.totalDistance);
            response.put("eta", path.totalTime / 60.0);
            response.put("path", pathCoords);
            
            return gson.toJson(response);
        });
        
        // ⭐ NEW ENDPOINT: Patient Assessment & Hospital Matching
        post("/api/patient/assess", (req, res) -> {
            res.type("application/json");
            
            try {
                // Parse request
                JsonObject requestBody = gson.fromJson(req.body(), JsonObject.class);
                JsonObject emergencyLoc = requestBody.getAsJsonObject("emergencyLocation");
                JsonObject patientData = requestBody.getAsJsonObject("patient");
                
                double lat = emergencyLoc.get("lat").getAsDouble();
                double lon = emergencyLoc.get("lon").getAsDouble();
                
                // Create Patient object
                String name = patientData.get("name").getAsString();
                int age = patientData.get("age").getAsInt();
                String emergencyTypeStr = patientData.get("emergencyType").getAsString().toUpperCase();
                String severityStr = patientData.get("severity").getAsString().toUpperCase();
                String notes = patientData.has("notes") ? patientData.get("notes").getAsString() : "";
                
                Patient.EmergencyType emergencyType = Patient.EmergencyType.valueOf(emergencyTypeStr);
                Patient.SeverityLevel severity = Patient.SeverityLevel.valueOf(severityStr);
                
                Patient patient = new Patient(name, age, emergencyType, severity, notes);
                
                System.out.println("\n=== PATIENT ASSESSMENT REQUEST ===");
                System.out.println(patient.getDetailedInfo());
                System.out.println(String.format("Emergency Location: (%.4f, %.4f)", lat, lon));
                
                // Get hospital recommendations
                List<HospitalRecommendation> recommendations = HospitalMatcher.findBestHospitals(
                    patient, lat, lon, hospitals.getAllHospitals(), 5
                );
                
                System.out.println("\n--- Recommendations Generated ---");
                for (int i = 0; i < recommendations.size(); i++) {
                    System.out.println(String.format("#%d: %s", i + 1, recommendations.get(i)));
                }
                
                // Build response
                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                
                // Add patient info
                JsonObject patientInfo = new JsonObject();
                patientInfo.addProperty("name", patient.name);
                patientInfo.addProperty("age", patient.age);
                patientInfo.addProperty("emergencyType", patient.emergencyType.name());
                patientInfo.addProperty("emergencyTypeDesc", patient.emergencyType.description);
                patientInfo.addProperty("severity", patient.severity.name());
                patientInfo.addProperty("severityDesc", patient.severity.description);
                patientInfo.addProperty("notes", patient.notes);
                patientInfo.addProperty("requiredAmbulanceType", patient.getRequiredAmbulanceType().name());
                response.add("patient", patientInfo);
                
                // Add recommendations array
                com.google.gson.JsonArray recsArray = new com.google.gson.JsonArray();
                for (HospitalRecommendation rec : recommendations) {
                    JsonObject recObj = new JsonObject();
                    
                    // Hospital details
                    JsonObject hospitalObj = new JsonObject();
                    hospitalObj.addProperty("name", rec.hospital.name);
                    hospitalObj.addProperty("type", rec.hospital.type);
                    hospitalObj.addProperty("lat", rec.hospital.lat);
                    hospitalObj.addProperty("lon", rec.hospital.lon);
                    hospitalObj.addProperty("capacity", rec.hospital.capacity);
                    hospitalObj.addProperty("hasEmergency", rec.hospital.hasEmergency);
                    hospitalObj.addProperty("hasTrauma", rec.hospital.hasTraumaCenter);
                    recObj.add("hospital", hospitalObj);
                    
                    // Scoring details
                    recObj.addProperty("distance", rec.distance);
                    recObj.addProperty("eta", rec.eta);
                    recObj.addProperty("score", rec.score);
                    recObj.addProperty("reasoning", rec.reasoning);
                    
                    recsArray.add(recObj);
                }
                response.add("recommendations", recsArray);
                
                // Add best hospital (first recommendation)
                if (!recommendations.isEmpty()) {
                    HospitalRecommendation best = recommendations.get(0);
                    JsonObject bestHospital = new JsonObject();
                    bestHospital.addProperty("name", best.hospital.name);
                    bestHospital.addProperty("type", best.hospital.type);
                    bestHospital.addProperty("lat", best.hospital.lat);
                    bestHospital.addProperty("lon", best.hospital.lon);
                    bestHospital.addProperty("distance", best.distance);
                    bestHospital.addProperty("eta", best.eta);
                    bestHospital.addProperty("score", best.score);
                    bestHospital.addProperty("reasoning", best.reasoning);
                    response.add("bestHospital", bestHospital);
                }
                
                System.out.println("\n✓ Assessment complete\n");
                
                return gson.toJson(response);
                
            } catch (Exception e) {
                System.err.println("Error in patient assessment: " + e.getMessage());
                e.printStackTrace();
                
                JsonObject error = new JsonObject();
                error.addProperty("success", false);
                error.addProperty("error", e.getMessage());
                res.status(500);
                return gson.toJson(error);
            }
        });
    }
    
    private void enableCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            
            return "OK";
        });
        
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type");
        });
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
        new WebServer();
    }
}