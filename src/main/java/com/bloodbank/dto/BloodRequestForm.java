package com.bloodbank.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class BloodRequestForm {

    @NotNull(message = "Please select a registered patient.")
    private Integer patientId;

    @NotBlank(message = "Required blood group must be specified.")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Please select a valid blood group.")
    private String bloodGroup;

    private String productType = "WHOLE_BLOOD";

    @NotNull(message = "Units required must be specified.")
    @Min(value = 1, message = "Must request at least 1 unit.")
    @Max(value = 20, message = "Cannot request more than 20 units in a single order.")
    private Integer unitsRequired = 1;

    @NotBlank(message = "Clinical priority must be selected.")
    @Pattern(regexp = "^(NORMAL|URGENT|EMERGENCY)$", message = "Priority must be NORMAL, URGENT, or EMERGENCY.")
    private String priority = "NORMAL";

    public BloodRequestForm() {
    }

    public BloodRequestForm(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Integer getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(Integer unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
