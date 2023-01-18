package me.erikpelli.jdigital.ticket;

import me.erikpelli.jdigital.company.Company;
import me.erikpelli.jdigital.noncompliance.NonCompliance;

import java.io.Serializable;

public class TicketKey implements Serializable {
    /**
     * Customer that opens the ticket.
     */
    private Company customer;

    /**
     * NonCompliance related to the ticket.
     */
    private NonCompliance nonCompliance;

    protected TicketKey() {
    }

    public TicketKey(Company customer, NonCompliance nonCompliance) {
        this.customer = customer;
        this.nonCompliance = nonCompliance;
    }

    public Company getCustomer() {
        return customer;
    }

    public void setCustomer(Company customer) {
        this.customer = customer;
    }

    public NonCompliance getNonCompliance() {
        return nonCompliance;
    }

    public void setNonCompliance(NonCompliance nonCompliance) {
        this.nonCompliance = nonCompliance;
    }
}

record TicketIdentifier(String companyId, Integer nonComplianceId) {
}
