package me.erikpelli.jdigital.shipping;

import jakarta.persistence.*;
import me.erikpelli.jdigital.company.Company;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class ShippingLot {
    /**
     * Shipping unique ID.
     */
    @Id
    private String shippingCode;

    /**
     * Date of dispatch.
     */
    @Column(nullable = false)
    private Date shippingDate;

    /**
     * Customer company that ordered the product.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Company customer;

    private int productQuantity = 1;

    protected ShippingLot() {
    }

    public ShippingLot(String shippingCode, Company customer, Date shippingDate) {
        this.shippingCode = shippingCode;
        this.customer = customer;
        this.shippingDate = shippingDate;
    }

    public ShippingLot(String shippingCode, Company customer, Date shippingDate, int productQuantity) {
        this(shippingCode, customer, shippingDate);
        setProductQuantity(productQuantity);
    }

    /**
     * Constructor that set the shipping date to today.
     */
    public ShippingLot(String shippingCode, Company customer) {
        this(shippingCode, customer, Date.valueOf(LocalDate.now()));
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        if (productQuantity > 0) {
            this.productQuantity = productQuantity;
        }
    }

    public String getShippingCode() {
        return shippingCode;
    }

    public void setShippingCode(String shippingCode) {
        this.shippingCode = shippingCode;
    }

    /**
     * Get shipping date as String representation.
     *
     * @return date in format YYYY-MM-DD (e.g. 2023-01-31)
     */
    public String getShippingDate() {
        var dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(shippingDate);
    }

    /**
     * Set date from format YYYY-MM-DD. Set null if the String is null.
     *
     * @param shippingDate formatted date string
     * @throws ParseException if format is invalid
     */
    public void setShippingDate(String shippingDate) throws ParseException {
        var dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.shippingDate = new Date(dateFormat.parse(shippingDate).getTime());
    }

    public Company getCustomer() {
        return customer;
    }

    public void setCustomer(Company customer) {
        this.customer = customer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShippingLot that)) return false;
        return shippingCode.equals(that.shippingCode) && Objects.equals(shippingDate, that.shippingDate) && Objects.equals(customer, that.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shippingCode, shippingDate, customer);
    }
}

