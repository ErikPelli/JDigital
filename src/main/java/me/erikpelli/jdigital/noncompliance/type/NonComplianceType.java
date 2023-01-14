package me.erikpelli.jdigital.noncompliance.type;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class NonComplianceType {
    /**
     * Unique VAT Number.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer code;

    /**
     * Name of the non-compliance type.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Optional description of this type.
     */
    private String description;

    protected NonComplianceType() {
    }

    public NonComplianceType(String name, String description) {
        this.code = null;
        this.name = name;
        this.description = description;
    }

    public NonComplianceType(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NonComplianceType that)) return false;
        return name.equals(that.name) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
}
