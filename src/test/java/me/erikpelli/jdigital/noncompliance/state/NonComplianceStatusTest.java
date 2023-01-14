package me.erikpelli.jdigital.noncompliance.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NonComplianceStatusTest {
    @Test
    void forValue() {
        // Value "new" is invalid in JSON status input
        assertNull(NonComplianceStatus.forValue("new"));

        assertEquals(NonComplianceStatus.ANALYSIS, NonComplianceStatus.forValue("analysys"));
        assertEquals(NonComplianceStatus.CHECK, NonComplianceStatus.forValue("check"));
        assertEquals(NonComplianceStatus.RESULT, NonComplianceStatus.forValue("result"));

        assertNull(NonComplianceStatus.forValue("invalid"));
    }

    @Test
    void nextStatus() {
        assertEquals(NonComplianceStatus.ANALYSIS, NonComplianceStatus.NEW.nextStatus());
        assertEquals(NonComplianceStatus.CHECK, NonComplianceStatus.ANALYSIS.nextStatus());
        assertEquals(NonComplianceStatus.RESULT, NonComplianceStatus.CHECK.nextStatus());

        // There is no status after result
        assertNull(NonComplianceStatus.RESULT.nextStatus());
    }

    @Test
    void testToString() {
        assertEquals("new", NonComplianceStatus.NEW.toString());
        // "analysys" instead of "analysis" to keep it compatible with old APIs
        assertEquals("analysys", NonComplianceStatus.ANALYSIS.toString());
        assertEquals("check", NonComplianceStatus.CHECK.toString());
        assertEquals("result", NonComplianceStatus.RESULT.toString());
    }
}