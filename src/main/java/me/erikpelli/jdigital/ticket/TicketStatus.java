package me.erikpelli.jdigital.ticket;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketStatus {
    /**
     * User sent a message and opened a ticket
     */
    NEW("new"),
    /**
     * User received a response and ticket is a Work in Progress
     */
    PROGRESS("progress"),
    /**
     * Problem resolved and ticket is no more relevant
     */
    CLOSED("closed");

    private final String value;

    TicketStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }
}
