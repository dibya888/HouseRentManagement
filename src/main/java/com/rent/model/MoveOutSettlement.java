package com.rent.model;

public class MoveOutSettlement {

    private int id;
    private int tenantId;
    private String tenantName;
    private String tenantPhone;
    private String flatNo;
    private String moveOutDate;

    private double unpaidDue;
    private double securityDeposit;
    private double refundAmount;
    private double payableAmount;

    private String result;
    private String reason;
    private String createdAt;

    public int getId() {
        return id;
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getTenantPhone() {
        return tenantPhone;
    }

    public String getFlatNo() {
        return flatNo;
    }

    public String getMoveOutDate() {
        return moveOutDate;
    }

    public double getUnpaidDue() {
        return unpaidDue;
    }

    public double getSecurityDeposit() {
        return securityDeposit;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public double getPayableAmount() {
        return payableAmount;
    }

    public String getResult() {
        return result;
    }

    public String getReason() {
        return reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setTenantPhone(String tenantPhone) {
        this.tenantPhone = tenantPhone;
    }

    public void setFlatNo(String flatNo) {
        this.flatNo = flatNo;
    }

    public void setMoveOutDate(String moveOutDate) {
        this.moveOutDate = moveOutDate;
    }

    public void setUnpaidDue(double unpaidDue) {
        this.unpaidDue = unpaidDue;
    }

    public void setSecurityDeposit(double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public void setPayableAmount(double payableAmount) {
        this.payableAmount = payableAmount;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}