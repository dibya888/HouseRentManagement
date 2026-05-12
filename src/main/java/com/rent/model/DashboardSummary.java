package com.rent.model;

public class DashboardSummary {

    private int totalFlats;
    private int occupiedFlats;
    private int availableFlats;
    private int totalTenants;

    private double monthIncome;
    private double totalDue;
    private double monthOwnerRepair;
    private double monthNetProfit;

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

    public double getMonthIncome() {
        return monthIncome;
    }

    public void setMonthIncome(double monthIncome) {
        this.monthIncome = monthIncome;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(double totalDue) {
        this.totalDue = totalDue;
    }

    public double getMonthOwnerRepair() {
        return monthOwnerRepair;
    }

    public void setMonthOwnerRepair(double monthOwnerRepair) {
        this.monthOwnerRepair = monthOwnerRepair;
    }

    public double getMonthNetProfit() {
        return monthNetProfit;
    }

    public void setMonthNetProfit(double monthNetProfit) {
        this.monthNetProfit = monthNetProfit;
    }
}