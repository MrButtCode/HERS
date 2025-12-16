package com.hers.model;

/**
 * Represents a patient requiring emergency medical care
 */
public class Patient {
    public final String name;
    public final int age;
    public final EmergencyType emergencyType;
    public final SeverityLevel severity;
    public final String notes;
    public final long timestamp;
    
    public enum EmergencyType {
        CARDIAC("Cardiac Emergency", true, false),
        TRAUMA("Trauma/Injury", false, true),
        STROKE("Stroke", true, false),
        RESPIRATORY("Respiratory Distress", true, false),
        PEDIATRIC("Pediatric Emergency", false, false),
        GENERAL("General Emergency", false, false);
        
        public final String description;
        public final boolean requiresCardiacCare;
        public final boolean requiresTraumaCenter;
        
        EmergencyType(String description, boolean cardiac, boolean trauma) {
            this.description = description;
            this.requiresCardiacCare = cardiac;
            this.requiresTraumaCenter = trauma;
        }
    }
    
    public enum SeverityLevel {
        CRITICAL(3, "Life Threatening - Immediate Care Required"),
        SERIOUS(2, "Urgent Care Needed"),
        MODERATE(1, "Stable but Urgent");
        
        public final int priority;
        public final String description;
        
        SeverityLevel(int priority, String description) {
            this.priority = priority;
            this.description = description;
        }
    }
    
    public Patient(String name, int age, EmergencyType emergencyType, 
                   SeverityLevel severity, String notes) {
        this.name = name;
        this.age = age;
        this.emergencyType = emergencyType;
        this.severity = severity;
        this.notes = notes;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Check if patient is pediatric (under 18)
     */
    public boolean isPediatric() {
        return age < 18;
    }
    
    /**
     * Check if patient is geriatric (over 65)
     */
    public boolean isGeriatric() {
        return age >= 65;
    }
    
    /**
     * Get required ambulance type based on severity
     */
    public Ambulance.AmbulanceType getRequiredAmbulanceType() {
        switch (severity) {
            case CRITICAL:
                return Ambulance.AmbulanceType.CRITICAL;
            case SERIOUS:
                return Ambulance.AmbulanceType.ADVANCED;
            case MODERATE:
            default:
                return Ambulance.AmbulanceType.BASIC;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Patient: %s (Age: %d) - %s - Severity: %s",
            name, age, emergencyType.description, severity);
    }
    
    /**
     * Get detailed patient information
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Name: %s\n", name));
        sb.append(String.format("Age: %d years", age));
        if (isPediatric()) sb.append(" (Pediatric)");
        if (isGeriatric()) sb.append(" (Geriatric)");
        sb.append("\n");
        sb.append(String.format("Emergency Type: %s\n", emergencyType.description));
        sb.append(String.format("Severity: %s\n", severity.description));
        sb.append(String.format("Required Ambulance Type: %s\n", getRequiredAmbulanceType()));
        if (notes != null && !notes.isEmpty()) {
            sb.append(String.format("Notes: %s\n", notes));
        }
        return sb.toString();
    }
}