package com.hers.model;

import java.util.*;

/**
 * Database of major hospitals in Karachi with real coordinates
 */
public class HospitalDatabase {
    
    private final List<Hospital> hospitals;
    
    public HospitalDatabase() {
        this.hospitals = new ArrayList<>();
        loadKarachiHospitals();
    }
    
    /**
     * Load major hospitals in Karachi with real coordinates
     */
    private void loadKarachiHospitals() {
        // Major hospitals in Karachi with approximate coordinates
        
        // Saddar & Downtown area
        hospitals.add(new Hospital(
            "Jinnah Postgraduate Medical Centre",
            "General",
            24.8615, 67.0099,
            1500, true, true
        ));
        
        hospitals.add(new Hospital(
            "Civil Hospital Karachi",
            "General",
            24.8606, 67.0084,
            1800, true, true
        ));
        
        // Clifton & DHA area
        hospitals.add(new Hospital(
            "South City Hospital",
            "General",
            24.8276, 67.0340,
            300, true, false
        ));
        
        hospitals.add(new Hospital(
            "Aga Khan University Hospital",
            "General",
            24.8905, 67.0718,
            700, true, true
        ));
        
        // Gulshan & Gulistan-e-Johar area
        hospitals.add(new Hospital(
            "Liaquat National Hospital",
            "General",
            24.8796, 67.0679,
            600, true, true
        ));
        
        hospitals.add(new Hospital(
            "Patel Hospital",
            "General",
            24.8952, 67.0630,
            400, true, false
        ));
        
        // North Karachi & North Nazimabad
        hospitals.add(new Hospital(
            "Abbasi Shaheed Hospital",
            "General",
            24.9252, 67.0583,
            500, true, true
        ));
        
        hospitals.add(new Hospital(
            "Ziauddin Hospital (North)",
            "General",
            24.9100, 67.0350,
            350, true, false
        ));
        
        // Clifton & Saddar (near coast)
        hospitals.add(new Hospital(
            "Ziauddin Hospital (Clifton)",
            "General",
            24.8245, 67.0295,
            300, true, false
        ));
        
        hospitals.add(new Hospital(
            "Jamal Noor Hospital",
            "Specialized",
            24.8850, 67.0450,
            200, true, false
        ));
        
        // Korangi & Landhi area
        hospitals.add(new Hospital(
            "Jinnah Hospital (Korangi)",
            "General",
            24.8350, 67.1150,
            400, true, false
        ));
        
        // Malir area
        hospitals.add(new Hospital(
            "National Medical Centre",
            "General",
            24.8900, 67.1000,
            350, true, false
        ));
    }
    
    /**
     * Get all hospitals
     */
    public List<Hospital> getAllHospitals() {
        return new ArrayList<>(hospitals);
    }
    
    /**
     * Find N nearest hospitals to given location
     */
    public List<Hospital> findNearestHospitals(double lat, double lon, int count) {
        List<HospitalDistance> distances = new ArrayList<>();
        
        for (Hospital hospital : hospitals) {
            double distance = hospital.distanceTo(lat, lon);
            distances.add(new HospitalDistance(hospital, distance));
        }
        
        // Sort by distance
        distances.sort(Comparator.comparingDouble(hd -> hd.distance));
        
        // Return top N
        List<Hospital> nearest = new ArrayList<>();
        for (int i = 0; i < Math.min(count, distances.size()); i++) {
            nearest.add(distances.get(i).hospital);
        }
        
        return nearest;
    }
    
    /**
     * Find hospitals with emergency facilities
     */
    public List<Hospital> findEmergencyHospitals() {
        List<Hospital> emergency = new ArrayList<>();
        for (Hospital hospital : hospitals) {
            if (hospital.hasEmergency) {
                emergency.add(hospital);
            }
        }
        return emergency;
    }
    
    /**
     * Find trauma centers
     */
    public List<Hospital> findTraumaCenters() {
        List<Hospital> trauma = new ArrayList<>();
        for (Hospital hospital : hospitals) {
            if (hospital.hasTraumaCenter) {
                trauma.add(hospital);
            }
        }
        return trauma;
    }
    
    /**
     * Get total number of hospitals
     */
    public int getHospitalCount() {
        return hospitals.size();
    }
    
    /**
     * Helper class for sorting hospitals by distance
     */
    private static class HospitalDistance {
        Hospital hospital;
        double distance;
        
        HospitalDistance(Hospital hospital, double distance) {
            this.hospital = hospital;
            this.distance = distance;
        }
    }
}