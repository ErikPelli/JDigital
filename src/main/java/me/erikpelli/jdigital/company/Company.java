package me.erikpelli.jdigital.company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Company {
    /**
     * Unique VAT Number.
     */
    @Id
    @Column(length = 11)
    private String vatNum;

    /**
     * Name of the company.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Company street address.
     */
    private String address;

    protected Company() {
    }

    public Company(String vatNum, String name, String address) {
        this.vatNum = vatNum;
        this.name = name;
        this.address = address;
    }

    public String getVatNum() {
        return vatNum;
    }

    public void setVatNum(String vatNum) {
        this.vatNum = vatNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company company)) return false;

        if (!Objects.equals(vatNum, company.vatNum)) return false;
        if (!Objects.equals(name, company.name)) return false;
        return Objects.equals(address, company.address);
    }

    @Override
    public int hashCode() {
        int result = vatNum != null ? vatNum.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }
}
