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

    // Used for extra report info.
    // For Repair Report, this will store Paid By: Owner/Tenant
    private String extraInfo;

    public ReportRow(String title, String month, String date, String flatNo, String tenant,
                     double total, double paid, double due, String status) {

        this(title, month, date, flatNo, tenant, total, paid, due, status, "");
    }

    public ReportRow(String title, String month, String date, String flatNo, String tenant,
                     double total, double paid, double due, String status, String extraInfo) {

        this.title = title;
        this.month = month;
        this.date = date;
        this.flatNo = flatNo;
        this.tenant = tenant;
        this.total = total;
        this.paid = paid;
        this.due = due;
        this.status = status;
        this.extraInfo = extraInfo;
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

    /**
     * Display-only derived status, mirroring RentRow.getDisplayStatus().
     * If status is "DUE" and the date field holds a real, parseable
     * due date that has already passed, returns "LATE" for display
     * purposes only — the underlying status/database is untouched.
     *
     * This is safe across every DAO query that produces a "DUE"-status
     * ReportRow: those queries consistently populate the date field
     * from rc.due_date (not payment_date), since payment_date only
     * ever appears on already-PAID rows, which never carry status
     * "DUE". One exception (Tenant-wise Due, an aggregated row with
     * no real date) is safely handled by the parse failure fallback.
     */
    public String getDisplayStatus() {
        if ("DUE".equalsIgnoreCase(status) && date != null && !date.isBlank()) {
            try {
                java.time.LocalDate due = java.time.LocalDate.parse(date);
                if (java.time.LocalDate.now().isAfter(due)) {
                    return "LATE";
                }
            } catch (Exception e) {
                // Not a real/parseable due date (e.g. aggregated rows) —
                // fall through and show the stored status as-is.
            }
        }
        return status;
    }

    public String getExtraInfo() {
        return extraInfo;
    }
}