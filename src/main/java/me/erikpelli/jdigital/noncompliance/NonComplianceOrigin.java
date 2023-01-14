package me.erikpelli.jdigital.noncompliance;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NonComplianceOrigin {
    INTERNAL("internal"),
    CUSTOMER("customer"),
    SUPPLIER("supplier");

    private final String value;

    NonComplianceOrigin(String value) {
        this.value = value;
    }

    @JsonCreator
    public static NonComplianceOrigin forValue(String value) {
        return switch (value) {
            case "internal" -> INTERNAL;
            case "customer" -> CUSTOMER;
            case "supplier" -> SUPPLIER;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return value;
    }
}
