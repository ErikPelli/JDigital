package me.erikpelli.jdigital.noncompliance;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Cause of the noncompliance creation.
 */
public enum NonComplianceOrigin {
    /**
     * Caused by an internal production process.
     */
    INTERNAL("internal"),
    /**
     * Caused by a customer.
     */
    CUSTOMER("customer"),
    /**
     * Caused by an input raw material lot.
     */
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
