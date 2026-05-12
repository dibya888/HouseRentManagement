package com.rent.model;

public class ReportSummary {

    private double totalIncome;
    private double monthIncome;
    private double yearIncome;
    private double totalDue;

    private int totalFlats;
    private int occupiedFlats;
    private int availableFlats;
    private int totalTenants;

    private double totalRepairCost;
    private double monthRepairCost;
    private double yearRepairCost;
    private double netProfit;
    private double ownerPaidRepairCost;
    private double tenantPaidRepairCost;

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getMonthIncome() {
        return monthIncome;
    }

    public void setMonthIncome(double monthIncome) {
        this.monthIncome = monthIncome;
    }

    public double getYearIncome() {
        return yearIncome;
    }

    public void setYearIncome(double yearIncome) {
        this.yearIncome = yearIncome;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(double totalDue) {
        this.totalDue = totalDue;
    }

    public int getTotalFlats() {
        return totalFlats;
    }

    public void setTotalFlats(int totalFlats) {
        this.totalFlats = totalFlats;
    }

    public int getOccupiedFlats() {
        return occupiedFlats;
    }

    public void setOccupiedFlats(int occupiedFlats) {
        this.occupiedFlats = occupiedFlats;
    }

    public int getAvailableFlats() {
        return availableFlats;
    }

    public void setAvailableFlats(int availableFlats) {
        this.availableFlats = availableFlats;
    }

    public int getTotalTenants() {
        return totalTenants;
    }

    public void setTotalTenants(int totalTenants) {
        this.totalTenants = totalTenants;
    }

    public double getTotalRepairCost() {
        return totalRepairCost;
    }

    public void setTotalRepairCost(double totalRepairCost) {
        this.totalRepairCost = totalRepairCost;
    }

    public double getMonthRepairCost() {
        return monthRepairCost;
    }

    public void setMonthRepairCost(double monthRepairCost) {
        this.monthRepairCost = monthRepairCost;
    }

    public double getYearRepairCost() {
        return yearRepairCost;
    }

    public void setYearRepairCost(double yearRepairCost) {
        this.yearRepairCost = yearRepairCost;
    }

    public double getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(double netProfit) {
        this.netProfit = netProfit;
    }
    public double getOwnerPaidRepairCost() {
        return ownerPaidRepairCost;
    }

    public void setOwnerPaidRepairCost(double ownerPaidRepairCost) {
        this.ownerPaidRepairCost = ownerPaidRepairCost;
    }

    public double getTenantPaidRepairCost() {
        return tenantPaidRepairCost;
    }

    public void setTenantPaidRepairCost(double tenantPaidRepairCost) {
        this.tenantPaidRepairCost = tenantPaidRepairCost;
    }
}