package me.erikpelli.jdigital.user.settings;

import jakarta.persistence.*;
import me.erikpelli.jdigital.company.Company;

@Embeddable
public class UserSettings {
    /**
     * Employer of the current user.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Company employer;

    /**
     * Job of the user (ceo, manager, employee, ecc.)
     */
    private String job;

    /**
     * Role of the user inside the company (developer, accountant, ecc.)
     */
    private String role;

    protected UserSettings() {
    }

    public UserSettings(Company employer, String job, String role) {
        this.employer = employer;
        this.job = job;
        this.role = role;
    }

    public String getEmployerCode() {
        if (employer == null) {
            return null;
        }
        return employer.getVatNum();
    }

    public void setEmployer(Company employer) {
        this.employer = employer;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
