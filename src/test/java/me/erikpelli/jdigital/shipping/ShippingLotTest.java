package me.erikpelli.jdigital.shipping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShippingLotTest {
    @Test
    void shippingDate() {
        var shippingLot = new ShippingLot();
        assertDoesNotThrow(() -> shippingLot.setShippingDate("2022-10-01"));
        assertEquals("2022-10-01", shippingLot.getShippingDate());

        assertThrows(IllegalArgumentException.class, () -> shippingLot.setShippingDate("2022-DD-01"));
        assertThrows(IllegalArgumentException.class, () -> shippingLot.setShippingDate("20221001"));
        assertThrows(IllegalArgumentException.class, () -> shippingLot.setShippingDate("2022"));
        assertThrows(IllegalArgumentException.class, () -> shippingLot.setShippingDate("2022-01"));
        assertThrows(IllegalArgumentException.class, () -> shippingLot.setShippingDate(null));

        try {
            shippingLot.setShippingDate("01-10-2023");
        } catch (IllegalArgumentException ignored) {
        } finally {
            assertNotEquals("2023-10-01", shippingLot.getShippingDate());
        }
    }
}