package com.hers.service;

import com.hers.model.*;
import com.hers.model.Patient.EmergencyType;
import com.hers.model.Patient.SeverityLevel;

import java.util.*;

/**
 * Intelligent hospital matching system that scores hospitals based on patient needs
 */
public class HospitalMatcher {
    
    private static final double AVERAGE_AMBULANCE_SPEED_KMH = 40.0; // km/h in city traffic
    private static final int BASE_SCORE = 100;
    
    /**
     * Find and score the best hospitals for a patient
     */
    public static List<HospitalRecommendation> findBestHospitals(
            Patient patient, 
            double emergencyLat, 
            double emergencyLon, 
            List<Hospital> allHospitals,
            int topN) {
        
        List<HospitalRecommendation> recommendations = new ArrayList<>();
        
        for (Hospital hospital : allHospitals) {
            // Calculate distance and ETA
            double distance = hospital.distanceTo(emergencyLat, emergencyLon);
            double eta = calculateETA(distance);
            
            // Score the hospital
            int score = scoreHospital(hospital, patient, distance);
            
            // Generate reasoning
            String reasoning = generateReasoning(hospital, patient, distance, score);
            
            recommendations.add(new HospitalRecommendation(
                hospital, distance, eta, score, reasoning
            ));
        }
        
        // Sort by score (highest first)
        Collections.sort(recommendations);
        
        // Return top N results
        return recommendations.subList(0, Math.min(topN, recommendations.size()));
    }
    
    /**
     * Score a hospital based on patient needs and distance
     */
    private static int scoreHospital(Hospital hospital, Patient patient, double distance) {
        int score = BASE_SCORE;
        
        // 1. DISTANCE PENALTY: Closer is better (-2 points per km)
        double distanceKm = distance / 1000.0;
        score -= (int)(distanceKm * 2);
        
        // 2. EMERGENCY TYPE MATCHING
        switch (patient.emergencyType) {
            case TRAUMA:
                if (hospital.hasTraumaCenter) {
                    score += 35; // Major bonus for trauma center
                } else if (hospital.hasEmergency) {
                    score += 10; // Small bonus for emergency dept
                }
                break;
                
            case CARDIAC:
                if (hospital.type.toLowerCase().contains("cardiac") || 
                    hospital.type.toLowerCase().contains("heart")) {
                    score += 35; // Cardiac specialty
                } else if (hospital.hasEmergency) {
                    score += 15; // Emergency can handle cardiac
                }
                break;
                
            case STROKE:
                if (hospital.type.toLowerCase().contains("neuro") ||
                    hospital.type.toLowerCase().contains("stroke")) {
                    score += 35; // Stroke specialty
                } else if (hospital.hasEmergency) {
                    score += 15;
                }
                break;
                
            case RESPIRATORY:
                if (hospital.type.toLowerCase().contains("pulmonary") ||
                    hospital.type.toLowerCase().contains("respiratory")) {
                    score += 30;
                } else if (hospital.hasEmergency) {
                    score += 15;
                }
                break;
                
            case PEDIATRIC:
                if (hospital.type.toLowerCase().contains("child") ||
                    hospital.type.toLowerCase().contains("pediatric")) {
                    score += 40; // Strong preference for pediatric hospitals
                } else if (hospital.hasEmergency) {
                    score += 10;
                }
                break;
                
            case GENERAL:
                if (hospital.hasEmergency) {
                    score += 20;
                }
                break;
        }
        
        // 3. EMERGENCY DEPARTMENT AVAILABILITY
        if (hospital.hasEmergency) {
            score += 20; // Always prefer hospitals with ER
        } else {
            score -= 30; // Significant penalty for no ER
        }
        
        // 4. SEVERITY LEVEL MATCHING
        switch (patient.severity) {
            case CRITICAL:
                // Critical patients need best facilities
                if (hospital.hasTraumaCenter) {
                    score += 25;
                }
                if (hospital.type.toLowerCase().contains("tertiary") ||
                    hospital.type.toLowerCase().contains("medical center")) {
                    score += 20; // Prefer larger medical centers
                }
                // For critical cases, don't penalize distance as much
                score += (int)(distanceKm * 0.5); // Reduce distance penalty by half
                break;
                
            case SERIOUS:
                if (hospital.hasEmergency) {
                    score += 15;
                }
                break;
                
            case MODERATE:
                // For moderate cases, distance is more important
                // No additional bonuses, distance penalty already applied
                break;
        }
        
        // 5. AGE-BASED CONSIDERATIONS
        if (patient.isPediatric()) {
            if (hospital.type.toLowerCase().contains("child") ||
                hospital.type.toLowerCase().contains("pediatric")) {
                score += 25; // Pediatric facilities for children
            }
        }
        
        if (patient.isGeriatric()) {
            if (hospital.type.toLowerCase().contains("geriatric") ||
                hospital.capacity > 200) { // Larger hospitals better for elderly
                score += 10;
            }
        }
        
        // 6. CAPACITY BONUS: More beds = better equipped
        if (hospital.capacity > 300) {
            score += 15; // Large hospital
        } else if (hospital.capacity > 150) {
            score += 10; // Medium hospital
        } else if (hospital.capacity > 50) {
            score += 5;  // Small hospital
        } else {
            score -= 10; // Very small facility
        }
        
        // 7. HOSPITAL TYPE BONUS
        if (hospital.type.toLowerCase().contains("tertiary") ||
            hospital.type.toLowerCase().contains("teaching")) {
            score += 12; // Teaching/tertiary hospitals have more resources
        }
        
        // Ensure score doesn't go below 0 or above 100
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Generate human-readable reasoning for the score
     */
    private static String generateReasoning(Hospital hospital, Patient patient, 
                                           double distance, int score) {
        StringBuilder reasoning = new StringBuilder();
        double distanceKm = distance / 1000.0;
        
        if (score >= 80) {
            reasoning.append("Excellent match. ");
        } else if (score >= 60) {
            reasoning.append("Good match. ");
        } else if (score >= 40) {
            reasoning.append("Acceptable match. ");
        } else {
            reasoning.append("Suboptimal match. ");
        }
        
        // Distance factor
        if (distanceKm < 2) {
            reasoning.append("Very close proximity. ");
        } else if (distanceKm < 5) {
            reasoning.append("Close proximity. ");
        } else if (distanceKm < 10) {
            reasoning.append("Moderate distance. ");
        } else {
            reasoning.append("Far distance. ");
        }
        
        // Specialty matching
        if (patient.emergencyType == Patient.EmergencyType.TRAUMA && hospital.hasTraumaCenter) {
            reasoning.append("Has trauma center for injury treatment. ");
        }
        
        if (patient.emergencyType == Patient.EmergencyType.PEDIATRIC && 
            hospital.type.toLowerCase().contains("pediatric")) {
            reasoning.append("Specialized pediatric care available. ");
        }
        
        if (patient.emergencyType == Patient.EmergencyType.CARDIAC &&
            hospital.type.toLowerCase().contains("cardiac")) {
            reasoning.append("Cardiac specialty center. ");
        }
        
        // Emergency department
        if (hospital.hasEmergency) {
            reasoning.append("24/7 emergency department. ");
        } else {
            reasoning.append("No dedicated emergency department. ");
        }
        
        // Severity considerations
        if (patient.severity == Patient.SeverityLevel.CRITICAL) {
            if (hospital.hasTraumaCenter || hospital.capacity > 200) {
                reasoning.append("Well-equipped for critical cases.");
            } else {
                reasoning.append("May lack resources for critical care.");
            }
        }
        
        return reasoning.toString().trim();
    }
    
    /**
     * Calculate estimated time of arrival in minutes
     */
    private static double calculateETA(double distanceMeters) {
        double distanceKm = distanceMeters / 1000.0;
        double timeHours = distanceKm / AVERAGE_AMBULANCE_SPEED_KMH;
        return timeHours * 60.0; // Convert to minutes
    }
    
    /**
     * Get a summary of the matching process
     */
    public static String getMatchingSummary(Patient patient, 
                                           List<HospitalRecommendation> recommendations) {
        StringBuilder summary = new StringBuilder();
        summary.append("=== HOSPITAL MATCHING SUMMARY ===\n\n");
        summary.append(patient.getDetailedInfo());
        summary.append("\n--- Top Recommendations ---\n\n");
        
        for (int i = 0; i < recommendations.size(); i++) {
            HospitalRecommendation rec = recommendations.get(i);
            summary.append(String.format("#%d: %s\n", i + 1, rec.toString()));
            summary.append(String.format("    %s\n\n", rec.reasoning));
        }
        
        return summary.toString();
    }
}