package com.rent.controller;

import com.rent.model.ReportRow;
import com.rent.model.ReportSummary;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportExcelExporter {

    public static void exportReport(ReportSummary summary,
                                    ObservableList<ReportRow> rows) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report Excel");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        chooser.setInitialFileName("Reports_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".xlsx");

        File file = chooser.showSaveDialog(null);

        if (file == null) {
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try {
            createExcel(summary, rows, file);

            new Alert(Alert.AlertType.INFORMATION,
                    "Excel report exported successfully:\n" + file.getAbsolutePath()
            ).show();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to export Excel report."
            ).show();
        }
    }

    private static void createExcel(ReportSummary summary,
                                    ObservableList<ReportRow> rows,
                                    File file) throws Exception {

        try (Workbook workbook = new XSSFWorkbook()) {

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            createSummarySheet(workbook, summary, titleStyle, headerStyle, moneyStyle);
            createDetailsSheet(workbook, rows, titleStyle, headerStyle, moneyStyle);

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
        }
    }

    private static void createSummarySheet(Workbook workbook,
                                           ReportSummary summary,
                                           CellStyle titleStyle,
                                           CellStyle headerStyle,
                                           CellStyle moneyStyle) {

        Sheet sheet = workbook.createSheet("Summary");

        int rowIndex = 0;

        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("House Rent Management - Reports Summary");
        titleCell.setCellStyle(titleStyle);

        rowIndex++;

        rowIndex = createSummaryRow(sheet, rowIndex, "Total Income", summary.getTotalIncome(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "This Month Income", summary.getMonthIncome(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "This Year Income", summary.getYearIncome(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "Total Due", summary.getTotalDue(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "Total Repair Cost", summary.getTotalRepairCost(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "Owner Paid Repairs", summary.getOwnerPaidRepairCost(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "Tenant Paid Repairs", summary.getTenantPaidRepairCost(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "This Month Repair", summary.getMonthRepairCost(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "This Year Repair", summary.getYearRepairCost(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "Net Income", summary.getNetProfit(), headerStyle, moneyStyle);

        rowIndex = createSummaryRow(sheet, rowIndex, "Total Utility Bills", summary.getTotalUtilityBills(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "This Month Utility Bills", summary.getMonthUtilityBills(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "This Year Utility Bills", summary.getYearUtilityBills(), headerStyle, moneyStyle);

        rowIndex = createSummaryRow(sheet, rowIndex, "Electricity Bills", summary.getElectricityBills(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "Water Bills", summary.getWaterBills(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "Gas Bills", summary.getGasBills(), headerStyle, moneyStyle);
        rowIndex = createSummaryRow(sheet, rowIndex, "Other Bills", summary.getOtherBills(), headerStyle, moneyStyle);

        rowIndex++;

        rowIndex = createNumberSummaryRow(sheet, rowIndex, "Total Flats", summary.getTotalFlats(), headerStyle);
        rowIndex = createNumberSummaryRow(sheet, rowIndex, "Occupied Flats", summary.getOccupiedFlats(), headerStyle);
        rowIndex = createNumberSummaryRow(sheet, rowIndex, "Available Flats", summary.getAvailableFlats(), headerStyle);
        createNumberSummaryRow(sheet, rowIndex, "Total Tenants", summary.getTotalTenants(), headerStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private static int createSummaryRow(Sheet sheet,
                                        int rowIndex,
                                        String label,
                                        double value,
                                        CellStyle headerStyle,
                                        CellStyle moneyStyle) {

        Row row = sheet.createRow(rowIndex);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(moneyStyle);

        return rowIndex + 1;
    }

    private static int createNumberSummaryRow(Sheet sheet,
                                              int rowIndex,
                                              String label,
                                              int value,
                                              CellStyle headerStyle) {

        Row row = sheet.createRow(rowIndex);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);

        return rowIndex + 1;
    }

    private static void createDetailsSheet(Workbook workbook,
                                           ObservableList<ReportRow> rows,
                                           CellStyle titleStyle,
                                           CellStyle headerStyle,
                                           CellStyle moneyStyle) {

        Sheet sheet = workbook.createSheet("Report Details");

        int rowIndex = 0;

        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Report Details");
        titleCell.setCellStyle(titleStyle);

        rowIndex++;

        Row header = sheet.createRow(rowIndex++);

        String[] headers = {
                "Title",
                "Month",
                "Date",
                "Flat No",
                "Tenant",
                "Total",
                "Paid",
                "Due",
                "Status"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (ReportRow reportRow : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(nz(reportRow.getTitle()));
            row.createCell(1).setCellValue(nz(reportRow.getMonth()));
            row.createCell(2).setCellValue(nz(reportRow.getDate()));
            row.createCell(3).setCellValue(nz(reportRow.getFlatNo()));
            row.createCell(4).setCellValue(nz(reportRow.getTenant()));

            Cell totalCell = row.createCell(5);
            totalCell.setCellValue(reportRow.getTotal());
            totalCell.setCellStyle(moneyStyle);

            Cell paidCell = row.createCell(6);
            paidCell.setCellValue(reportRow.getPaid());
            paidCell.setCellStyle(moneyStyle);

            Cell dueCell = row.createCell(7);
            dueCell.setCellValue(reportRow.getDue());
            dueCell.setCellStyle(moneyStyle);

            row.createCell(8).setCellValue(nz(reportRow.getStatus()));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static CellStyle createTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        return style;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        return style;
    }

    private static CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));

        return style;
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}