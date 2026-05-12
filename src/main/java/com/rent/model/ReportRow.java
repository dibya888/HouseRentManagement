package com.rent.model;

public class ReportRow {

    private String title;
    private String month;
    private String date;
    private String flatNo;
    private String tenant;
    private double total;
    private double paid;
    private double due;
    private String status;

    public ReportRow(String title, String month, String date, String flatNo, String tenant,
                     double total, double paid, double due, String status) {
        this.title = title;
        this.month = month;
        this.date = date;
        this.flatNo = flatNo;
        this.tenant = tenant;
        this.total = total;
        this.paid = paid;
        this.due = due;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getMonth() {
        return month;
    }

    public String getDate() {
        return date;
    }

    public String getFlatNo() {
        return flatNo;
    }

    public String getTenant() {
        return tenant;
    }

    public double getTotal() {
        return total;
    }

    public double getPaid() {
        return paid;
    }

    public double getDue() {
        return due;
    }

    public String getStatus() {
        return status;
    }
}