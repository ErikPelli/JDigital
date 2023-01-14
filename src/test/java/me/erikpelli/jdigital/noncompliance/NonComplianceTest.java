package me.erikpelli.jdigital.noncompliance;

import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

class NonComplianceTest {
    @Test
    void date() {
        var nonCompliance = new NonCompliance();
        assertDoesNotThrow(() -> nonCompliance.setDate(Date.valueOf("2022-10-01")));
        assertEquals("2022-10-01", nonCompliance.getFormattedDate());

        assertThrows(Exception.class, () -> Date.valueOf("2022-DD-01"));
        assertThrows(Exception.class, () -> Date.valueOf("20221001"));
        assertThrows(Exception.class, () -> Date.valueOf("2022"));
        assertThrows(Exception.class, () -> Date.valueOf("2022-01"));

        assertThrows(Exception.class, () -> nonCompliance.setDate(Date.valueOf("01-10-2023")));
        assertNotEquals("2023-10-01", nonCompliance.getFormattedDate());
    }
}