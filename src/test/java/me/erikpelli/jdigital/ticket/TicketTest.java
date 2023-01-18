package me.erikpelli.jdigital.ticket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {
    @Test
    void calculateStatus() {
        var ticket = new Ticket(null, null, null);
        assertEquals(TicketStatus.NEW, ticket.calculateStatus());
        ticket.setAnswer("Hi!");
        assertEquals(TicketStatus.PROGRESS, ticket.calculateStatus());
        ticket.setClosed(true);
        assertEquals(TicketStatus.CLOSED, ticket.calculateStatus());
    }
}