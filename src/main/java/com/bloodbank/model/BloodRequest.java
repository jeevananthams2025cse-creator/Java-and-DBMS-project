package com.bloodbank.model;

import java.time.LocalDateTime;

public class BloodRequest {

    private int requestId;
    private int patientId;
    private String bloodGroup;
    private String productType;
    private int unitsRequired;
    private String priority;
    private LocalDateTime requestDate;
    private String status;

    public BloodRequest(
            int requestId,
            int patientId,
            String bloodGroup,
            String productType,
            int unitsRequired,
            String priority,
            LocalDateTime requestDate,
            String status) {

        this.requestId = requestId;
        this.patientId = patientId;
        this.bloodGroup = bloodGroup;
        this.productType = productType;
        this.unitsRequired = unitsRequired;
        this.priority = priority;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getProductType() {
        return productType;
    }

    public int getUnitsRequired() {
        return unitsRequired;
    }

    public String getPriority() {
        return priority;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}