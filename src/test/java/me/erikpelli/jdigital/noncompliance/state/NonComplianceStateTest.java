package me.erikpelli.jdigital.noncompliance.state;

import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

class NonComplianceStateTest {
    @Test
    void dateFormat() {
        assertThrows(Exception.class, () -> Date.valueOf("2022-DD-01"));
        assertThrows(Exception.class, () -> Date.valueOf("20221001"));
        assertThrows(Exception.class, () -> Date.valueOf("2022"));
        assertThrows(Exception.class, () -> Date.valueOf("2022-01"));
    }

    @Test
    void analysisDate() {
        var nonComplianceState = new NonComplianceState();
        assertDoesNotThrow(() -> nonComplianceState.setAnalysisDate(Date.valueOf("2022-10-01")));
        assertEquals("2022-10-01", nonComplianceState.getFormattedAnalysisDate());

        assertThrows(Exception.class, () -> nonComplianceState.setAnalysisDate(Date.valueOf("01-10-2023")));
        assertNotEquals("2023-10-01", nonComplianceState.getFormattedAnalysisDate());
    }

    @Test
    void checkDate() {
        var nonComplianceState = new NonComplianceState();
        assertDoesNotThrow(() -> nonComplianceState.setCheckDate(Date.valueOf("2022-10-01")));
        assertEquals("2022-10-01", nonComplianceState.getFormattedCheckDate());

        assertThrows(Exception.class, () -> nonComplianceState.setCheckDate(Date.valueOf("01-10-2023")));
        assertNotEquals("2023-10-01", nonComplianceState.getFormattedCheckDate());
    }
}