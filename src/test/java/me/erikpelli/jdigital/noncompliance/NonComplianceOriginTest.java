package me.erikpelli.jdigital.noncompliance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NonComplianceOriginTest {
    @Test
    void forValue() {
        assertEquals(NonComplianceOrigin.INTERNAL, NonComplianceOrigin.forValue("internal"));
        assertEquals(NonComplianceOrigin.CUSTOMER, NonComplianceOrigin.forValue("customer"));
        assertEquals(NonComplianceOrigin.SUPPLIER, NonComplianceOrigin.forValue("supplier"));
        assertNull(NonComplianceOrigin.forValue("invalid"));
    }

    @Test
    void testToString() {
        assertEquals("internal", NonComplianceOrigin.INTERNAL.toString());
        assertEquals("customer", NonComplianceOrigin.CUSTOMER.toString());
        assertEquals("supplier", NonComplianceOrigin.SUPPLIER.toString());
    }
}