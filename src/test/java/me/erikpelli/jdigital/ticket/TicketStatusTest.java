package me.erikpelli.jdigital.ticket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketStatusTest {
    @Test
    void testToString() {
        assertEquals("new", TicketStatus.NEW.toString());
        assertEquals("progress", TicketStatus.PROGRESS.toString());
        assertEquals("closed", TicketStatus.CLOSED.toString());
    }
}