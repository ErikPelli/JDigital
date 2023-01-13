package me.erikpelli.jdigital.shipping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShippingLotTest {
    @Test
    void shippingDate() {
        var shippingLot = new ShippingLot();
        assertDoesNotThrow(() -> shippingLot.setShippingDate("2022-10-01"));
        assertEquals("2022-10-01", shippingLot.getShippingDate());

        assertThrows(Exception.class, () -> shippingLot.setShippingDate("2022-DD-01"));
        assertThrows(Exception.class, () -> shippingLot.setShippingDate("20221001"));
        assertThrows(Exception.class, () -> shippingLot.setShippingDate("2022"));
        assertThrows(Exception.class, () -> shippingLot.setShippingDate("2022-01"));
        assertThrows(Exception.class, () -> shippingLot.setShippingDate(null));

        try {
            shippingLot.setShippingDate("01-10-2022");
            assertNotEquals("2022-10-01", shippingLot.getShippingDate());
        } catch (Exception ignored) {
        }
    }
}