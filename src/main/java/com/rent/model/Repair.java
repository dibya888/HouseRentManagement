package com.rent.model;

public class Repair {

    private int id;
    private String flatNo;
    private String repairDate;
    private String category;
    private String description;
    private double cost;
    private String paidBy;
    private String status;
    private String notes;
    private String createdAt;

    private String vendorName;
    private String vendorPhone;
    private String invoiceNo;

    public Repair() {
    }

    public Repair(int id, String flatNo, String repairDate, String category,
                  String description, double cost, String paidBy,
                  String status, String notes, String createdAt) {
        this.id = id;
        this.flatNo = flatNo;
        this.repairDate = repairDate;
        this.category = category;
        this.description = description;
        this.cost = cost;
        this.paidBy = paidBy;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Repair(String flatNo, String repairDate, String category,
                  String description, double cost, String paidBy,
                  String status, String notes) {
        this.flatNo = flatNo;
        this.repairDate = repairDate;
        this.category = category;
        this.description = description;
        this.cost = cost;
        this.paidBy = paidBy;
        this.status = status;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public String getFlatNo() {
        return flatNo;
    }

    public String getRepairDate() {
        return repairDate;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getCost() {
        return cost;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getVendorPhone() {
        return vendorPhone;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFlatNo(String flatNo) {
        this.flatNo = flatNo;
    }

    public void setRepairDate(String repairDate) {
        this.repairDate = repairDate;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public void setVendorPhone(String vendorPhone) {
        this.vendorPhone = vendorPhone;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
}