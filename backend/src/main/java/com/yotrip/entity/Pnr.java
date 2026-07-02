package com.yotrip.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "pnrs")
public class Pnr {

    @Id
    @Column(length = 6)
    private String pnrCode;

    @Column(name = "customer_id", length = 20, nullable = false)
    private String customerId;

    @Column(nullable = false, length = 20)
    private String pnrStatus = "issued";

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public String getPnrCode() {
        return pnrCode;
    }

    public void setPnrCode(String pnrCode) {
        this.pnrCode = pnrCode;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setUser(User user) {
        this.customerId = user != null ? user.getCustomerId() : null;
    }

    public String getPnrStatus() {
        return pnrStatus;
    }

    public void setPnrStatus(String pnrStatus) {
        this.pnrStatus = pnrStatus;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
