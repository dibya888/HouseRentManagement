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
}