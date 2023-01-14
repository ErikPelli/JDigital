package me.erikpelli.jdigital.noncompliance.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Current noncompliance status identifier.
 */
public enum NonComplianceStatus {
    /**
     * Noncompliance created and not managed yet.
     */
    NEW("new"),
    /**
     * An analysis of the causes and possible solutions is underway.
     */
    ANALYSIS("analysys"),
    /**
     * A review of the previously performed analysis is underway.
     */
    CHECK("check"),
    /**
     * Management ended and a result was produced.
     */
    RESULT("result");

    private final String value;

    NonComplianceStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static NonComplianceStatus forValue(String value) {
        return switch (value) {
            case "analysys" -> ANALYSIS;
            case "check" -> CHECK;
            case "result" -> RESULT;
            default -> null;
        };
    }

    public NonComplianceStatus nextStatus() {
        return switch (value) {
            case "new" -> ANALYSIS;
            case "analysys" -> CHECK;
            case "check" -> RESULT;
            default -> null;
        };
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
