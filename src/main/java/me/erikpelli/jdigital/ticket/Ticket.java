package me.erikpelli.jdigital.ticket;

import jakarta.persistence.*;
import me.erikpelli.jdigital.company.Company;
import me.erikpelli.jdigital.noncompliance.NonCompliance;
import me.erikpelli.jdigital.shipping.ShippingLot;

@Entity
@IdClass(TicketKey.class)
public class Ticket {
    /**
     * Company that complains about something.
     */
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Company customer;

    /**
     * Reference to an existing non-compliance.
     */
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private NonCompliance nonCompliance;

    /**
     * Shipping lot that has the problem to resolve.
     */
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private ShippingLot lot;

    @Column(nullable = false)
    private String description;

    private String answer;

    private boolean closed;

    protected Ticket() {
    }

    public Ticket(Company customer, NonCompliance nonCompliance, ShippingLot lot, String description, String answer) {
        this.customer = customer;
        this.nonCompliance = nonCompliance;
        this.lot = lot;
        this.description = description;
        this.answer = answer;
    }

    public Ticket(Company customer, NonCompliance nonCompliance, ShippingLot lot) {
        this.customer = customer;
        this.nonCompliance = nonCompliance;
        this.lot = lot;
        this.description = "";
        this.answer = null;
    }

    /**
     * Calculate actual status with ticket data.
     * @return TicketStatus enum
     */
    public TicketStatus calculateStatus() {
        if (closed) {
            return TicketStatus.CLOSED;
        }
        if (answer != null) {
            return TicketStatus.PROGRESS;
        }
        return TicketStatus.NEW;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
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

    public ShippingLot getLot() {
        return lot;
    }

    public void setLot(ShippingLot lot) {
        this.lot = lot;
    }
}
