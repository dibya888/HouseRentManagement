package com.rent.controller;

/**
 * Carries the report's selection context (type + date range + user) from
 * ReportsController into the PDF/Excel exporters, so the exported file's
 * header and summary section can reflect what was actually selected,
 * instead of the exporters always showing every available metric.
 */
public class ReportExportContext {

    private final String reportType;
    private final String dateRangeText;
    private final String generatedBy;

    public ReportExportContext(String reportType, String dateRangeText, String generatedBy) {
        this.reportType = reportType;
        this.dateRangeText = dateRangeText;
        this.generatedBy = generatedBy;
    }

    public String getReportType() {
        return reportType;
    }

    public String getDateRangeText() {
        return dateRangeText;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }
}