package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "transfer_info")
public class TransferInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;

    private LocalDate transferDate;
    private String transferNumber;

    @ManyToOne
    @JoinColumn(name = "assignment_history_id")
    private AssignmentHistory assignmentHistory;

    private int oldKmReading;
    private String designatedOfficial;
    private String driverName;
    private String transferReason;
    private String oldFuelLiters;

    private int newKmReading;
    private String currentDesignatedOfficial;
    private String newFuelLiters;

    private String verifyingBodyName;
    private String authorizingOfficerName;

    // Getters and Setters
    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDate transferDate) {
        this.transferDate = transferDate;
    }

    public String getTransferNumber() {
        return transferNumber;
    }

    public void setTransferNumber(String transferNumber) {
        this.transferNumber = transferNumber;
    }

    public AssignmentHistory getAssignmentHistory() {
        return assignmentHistory;
    }

    public void setAssignmentHistory(AssignmentHistory assignmentHistory) {
        this.assignmentHistory = assignmentHistory;
    }

    public int getOldKmReading() {
        return oldKmReading;
    }

    public void setOldKmReading(int oldKmReading) {
        this.oldKmReading = oldKmReading;
    }

    public String getDesignatedOfficial() {
        return designatedOfficial;
    }

    public void setDesignatedOfficial(String designatedOfficial) {
        this.designatedOfficial = designatedOfficial;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public String getOldFuelLiters() {
        return oldFuelLiters;
    }

    public void setOldFuelLiters(String oldFuelLiters) {
        this.oldFuelLiters = String.valueOf(oldFuelLiters);
    }

    public int getNewKmReading() {
        return newKmReading;
    }

    public void setNewKmReading(int newKmReading) {
        this.newKmReading = newKmReading;
    }

    public String getCurrentDesignatedOfficial() {
        return currentDesignatedOfficial;
    }

    public void setCurrentDesignatedOfficial(String currentDesignatedOfficial) {
        this.currentDesignatedOfficial = currentDesignatedOfficial;
    }

    public String getNewFuelLiters() {
        return newFuelLiters;
    }

    public void setNewFuelLiters(String newFuelLiters) {
        this.newFuelLiters = String.valueOf(newFuelLiters);
    }

    public String getVerifyingBodyName() {
        return verifyingBodyName;
    }

    public void setVerifyingBodyName(String verifyingBodyName) {
        this.verifyingBodyName = verifyingBodyName;
    }

    public String getAuthorizingOfficerName() {
        return authorizingOfficerName;
    }

    public void setAuthorizingOfficerName(String authorizingOfficerName) {
        this.authorizingOfficerName = authorizingOfficerName;
    }
}