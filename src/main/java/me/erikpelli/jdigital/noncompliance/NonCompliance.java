package me.erikpelli.jdigital.noncompliance;

import jakarta.persistence.*;
import me.erikpelli.jdigital.noncompliance.state.NonComplianceState;
import me.erikpelli.jdigital.noncompliance.state.NonComplianceStatus;
import me.erikpelli.jdigital.noncompliance.type.NonComplianceType;
import me.erikpelli.jdigital.shipping.ShippingLot;

import java.sql.Date;
import java.time.LocalDate;

@Entity
public class NonCompliance {
    /**
     * Integer identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int code;

    /**
     * NonCompliance opening date.
     */
    @Column(nullable = false)
    private Date date;

    /**
     * Shipping lot associated with the product that has the problem.
     */
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private ShippingLot lot;

    /**
     * General identification of the problem (broken part, shipment lost, ecc.)
     */
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private NonComplianceType type;

    /**
     * Entity that generated the problem.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NonComplianceOrigin origin;

    /**
     * Optional comment written when the noncompliance is created to
     * better explain the problem.
     */
    private String comment;

    /**
     * Mutable noncompliance handling state.
     */
    @Embedded
    private NonComplianceState nonComplianceState;

    protected NonCompliance() {
    }

    public NonCompliance(ShippingLot lot, NonComplianceType type, NonComplianceOrigin origin, Date date, String comment) {
        this.date = date;
        this.type = type;
        this.lot = lot;
        this.origin = origin;
        this.comment = comment;
        this.nonComplianceState = new NonComplianceState(NonComplianceStatus.NEW);
    }

    /**
     * Constructor that set the noncompliance date to today.
     */
    public NonCompliance(ShippingLot lot, NonComplianceType type, NonComplianceOrigin origin, String comment) {
        this(lot, type, origin, Date.valueOf(LocalDate.now()), comment);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Get date in format YYYY-MM-DD.
     *
     * @return formatted date string
     */
    public String getFormattedDate() {
        return date.toString();
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public NonComplianceType getType() {
        return type;
    }

    public void setType(NonComplianceType type) {
        this.type = type;
    }

    public ShippingLot getLot() {
        return lot;
    }

    public NonComplianceOrigin getOrigin() {
        return origin;
    }

    public String getComment() {
        return comment;
    }

    public NonComplianceState getNonComplianceState() {
        return nonComplianceState;
    }

    public void setNonComplianceState(NonComplianceState nonComplianceState) {
        this.nonComplianceState = nonComplianceState;
    }
}
