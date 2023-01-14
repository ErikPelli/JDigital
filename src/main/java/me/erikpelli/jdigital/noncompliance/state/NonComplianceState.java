package me.erikpelli.jdigital.noncompliance.state;

import jakarta.persistence.*;
import me.erikpelli.jdigital.user.User;

import java.sql.Date;

@Embeddable
public class NonComplianceState {
    /**
     * Current status of the noncompliance.
     */
    @Enumerated(EnumType.STRING)
    private NonComplianceStatus status;

    /**
     * Expire date of the noncompliance causes analysis.
     */
    private Date analysisDate;

    /**
     * Expire date of the check of noncompliance causes analysis.
     */
    private Date checkDate;

    /**
     * Manager that supervises this noncompliance handling.
     */
    @ManyToOne
    private User manager;

    /**
     * Optional description written when the analysis is finished.
     */
    private String resultDescription;

    protected NonComplianceState() {
    }

    public NonComplianceState(NonComplianceStatus status) {
        this.status = status;
    }

    public NonComplianceState(NonComplianceStatus status, Date analysisDate, Date checkDate, User manager, String resultDescription) {
        this.status = status;
        this.analysisDate = analysisDate;
        this.checkDate = checkDate;
        this.manager = manager;
        this.resultDescription = resultDescription;
    }

    public NonComplianceStatus getStatus() {
        return status;
    }

    public void setStatus(NonComplianceStatus status) {
        this.status = status;
    }

    /**
     * Get date in yyyy-mm-dd format.
     *
     * @return formatted date string
     */
    public String getFormattedAnalysisDate() {
        return (analysisDate == null) ? null : analysisDate.toString();
    }

    public void setAnalysisDate(Date analysisDate) {
        this.analysisDate = analysisDate;
    }

    /**
     * Get date in yyyy-mm-dd format.
     *
     * @return formatted date string
     */
    public String getFormattedCheckDate() {
        return (checkDate == null) ? null : checkDate.toString();
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }
}
