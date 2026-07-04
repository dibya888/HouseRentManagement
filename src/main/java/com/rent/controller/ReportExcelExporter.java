package com.rent.controller;

import com.rent.model.ReportRow;
import com.rent.model.ReportSummary;
import com.rent.util.FileOpenUtil;
import com.rent.util.FileSaveUtil;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.common.usermodel.HyperlinkType;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportExcelExporter {

    public static boolean exportReport(ReportSummary summary,
                                       ObservableList<ReportRow> rows,
                                       ReportExportContext context) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report Excel");
        FileSaveUtil.defaultToDownloads(chooser);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        chooser.setInitialFileName("Reports_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".xlsx");

        File file = chooser.showSaveDialog(null);

        if (file == null) {
            return false;
        }

        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try {
            createExcel(summary, rows, context, file);

            FileOpenUtil.showSavedAlertWithOpen(
                    "Excel report exported successfully:\n" + file.getAbsolutePath(),
                    file
            );
            return true;

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to export Excel report."
            ).show();
            return false;
        }
    }

    private static void createExcel(ReportSummary summary,
                                    ObservableList<ReportRow> rows,
                                    ReportExportContext context,
                                    File file) throws Exception {

        try (Workbook workbook = new XSSFWorkbook()) {

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle subTitleStyle = createSubTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            createSummarySheet(workbook, summary, context, titleStyle, subTitleStyle, headerStyle, moneyStyle);
            createDetailsSheet(workbook, rows, context, titleStyle, subTitleStyle, headerStyle, moneyStyle);

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
        }
    }

    private static void createSummarySheet(Workbook workbook,
                                           ReportSummary summary,
                                           ReportExportContext context,
                                           CellStyle titleStyle,
                                           CellStyle subTitleStyle,
                                           CellStyle headerStyle,
                                           CellStyle moneyStyle) {

        Sheet sheet = workbook.createSheet("Summary");

        int rowIndex = 0;

        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("House Rent Management - Report Summary");
        titleCell.setCellStyle(titleStyle);

        rowIndex = writeContextLines(sheet, rowIndex, context, subTitleStyle);
        rowIndex++;

        for (String[] line : relevantSummaryLines(summary, context.getReportType())) {
            boolean isCount = line[2].equals("count");
            if (isCount) {
                rowIndex = createNumberSummaryRow(sheet, rowIndex, line[0],
                        (int) Double.parseDouble(line[1]), headerStyle);
            } else {
                rowIndex = createSummaryRow(sheet, rowIndex, line[0],
                        Double.parseDouble(line[1]), headerStyle, moneyStyle);
            }
        }

        sheet.setColumnWidth(0, 28 * 256);
        sheet.setColumnWidth(1, 18 * 256);
    }

    /**
     * Returns only the summary lines relevant to the selected report type
     * instead of always exporting every ReportSummary field. Each entry is
     * {label, rawNumericValue, "money"|"count"}.
     */
    private static java.util.List<String[]> relevantSummaryLines(ReportSummary summary, String reportType) {
        java.util.List<String[]> lines = new java.util.ArrayList<>();

        switch (reportType) {
            case "Repair Report" -> {
                lines.add(new String[]{"Total Repair Cost", String.valueOf(summary.getTotalRepairCost()), "money"});
                lines.add(new String[]{"This Month Repair", String.valueOf(summary.getMonthRepairCost()), "money"});
                lines.add(new String[]{"This Year Repair", String.valueOf(summary.getYearRepairCost()), "money"});
                lines.add(new String[]{"Owner Paid Repairs", String.valueOf(summary.getOwnerPaidRepairCost()), "money"});
                lines.add(new String[]{"Tenant Paid Repairs", String.valueOf(summary.getTenantPaidRepairCost()), "money"});
            }
            case "Utility Bills Report" -> {
                lines.add(new String[]{"Total Utility Bills", String.valueOf(summary.getTotalUtilityBills()), "money"});
                lines.add(new String[]{"This Month Utility Bills", String.valueOf(summary.getMonthUtilityBills()), "money"});
                lines.add(new String[]{"This Year Utility Bills", String.valueOf(summary.getYearUtilityBills()), "money"});
                lines.add(new String[]{"Electricity", String.valueOf(summary.getElectricityBills()), "money"});
                lines.add(new String[]{"Water", String.valueOf(summary.getWaterBills()), "money"});
                lines.add(new String[]{"Gas", String.valueOf(summary.getGasBills()), "money"});
                lines.add(new String[]{"Other", String.valueOf(summary.getOtherBills()), "money"});
            }
            case "Due Rent" -> {
                lines.add(new String[]{"Total Due", String.valueOf(summary.getTotalDue()), "money"});
                lines.add(new String[]{"Total Tenants", String.valueOf(summary.getTotalTenants()), "count"});
            }
            case "Monthly Income", "Yearly Income", "Flat-wise Income", "Tenant-wise Report" -> {
                lines.add(new String[]{"Total Collected Rent", String.valueOf(summary.getTotalIncome()), "money"});
                lines.add(new String[]{"This Month Income", String.valueOf(summary.getMonthIncome()), "money"});
                lines.add(new String[]{"This Year Income", String.valueOf(summary.getYearIncome()), "money"});
                lines.add(new String[]{"Total Due", String.valueOf(summary.getTotalDue()), "money"});
            }
            default -> {
                lines.add(new String[]{"Total Collected Rent", String.valueOf(summary.getTotalIncome()), "money"});
                lines.add(new String[]{"Total Due", String.valueOf(summary.getTotalDue()), "money"});
                lines.add(new String[]{"Total Repair Cost", String.valueOf(summary.getTotalRepairCost()), "money"});
                lines.add(new String[]{"Total Utility Bills", String.valueOf(summary.getTotalUtilityBills()), "money"});
                lines.add(new String[]{"Net Income", String.valueOf(summary.getNetProfit()), "money"});
                lines.add(new String[]{"Total Tenants", String.valueOf(summary.getTotalTenants()), "count"});
            }
        }

        return lines;
    }

    private static int writeContextLines(Sheet sheet, int rowIndex,
                                         ReportExportContext context,
                                         CellStyle subTitleStyle) {

        rowIndex = writeLabelValue(sheet, rowIndex, "Report Type:", context.getReportType(), subTitleStyle);
        rowIndex = writeLabelValue(sheet, rowIndex, "Date Range:", context.getDateRangeText(), subTitleStyle);
        rowIndex = writeLabelValue(sheet, rowIndex, "Generated:",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")),
                subTitleStyle);

        if (context.getGeneratedBy() != null && !context.getGeneratedBy().isBlank()) {
            rowIndex = writeLabelValue(sheet, rowIndex, "Generated By:", context.getGeneratedBy(), subTitleStyle);
        }

        return rowIndex;
    }

    private static int writeLabelValue(Sheet sheet, int rowIndex, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        row.createCell(1).setCellValue(value);
        return rowIndex + 1;
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
                                           ReportExportContext context,
                                           CellStyle titleStyle,
                                           CellStyle subTitleStyle,
                                           CellStyle headerStyle,
                                           CellStyle moneyStyle) {

        Sheet sheet = workbook.createSheet("Report Details");

        int rowIndex = 0;

        Row titleRow = sheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("House Rent Management - " + context.getReportType());
        titleCell.setCellStyle(titleStyle);

        rowIndex = writeLabelValue(sheet, rowIndex, "Date Range:", context.getDateRangeText(), subTitleStyle);
        rowIndex++;

        int headerRowIndex = rowIndex;
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

        CellStyle borderedHeader = withThinBorder(workbook, headerStyle);
        CellStyle borderedText = withThinBorder(workbook, null);
        CellStyle borderedMoney = withThinBorder(workbook, moneyStyle);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(borderedHeader);
        }

        for (ReportRow reportRow : rows) {
            Row row = sheet.createRow(rowIndex++);

            createTextCell(row, 0, nz(reportRow.getTitle()), borderedText);
            createTextCell(row, 1, nz(reportRow.getMonth()), borderedText);
            createTextCell(row, 2, nz(reportRow.getDate()), borderedText);
            createTextCell(row, 3, nz(reportRow.getFlatNo()), borderedText);
            createTextCell(row, 4, nz(reportRow.getTenant()), borderedText);

            Cell totalCell = row.createCell(5);
            totalCell.setCellValue(reportRow.getTotal());
            totalCell.setCellStyle(borderedMoney);

            Cell paidCell = row.createCell(6);
            paidCell.setCellValue(reportRow.getPaid());
            paidCell.setCellStyle(borderedMoney);

            Cell dueCell = row.createCell(7);
            dueCell.setCellValue(reportRow.getDue());
            dueCell.setCellStyle(borderedMoney);

            createTextCell(row, 8, nz(reportRow.getStatus()), borderedText);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Freeze the header row so it stays visible while scrolling a long
        // detail sheet — directly addresses "freeze header row" requirement.
        sheet.createFreezePane(0, headerRowIndex + 1);
    }

    private static void createTextCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static CellStyle withThinBorder(Workbook workbook, CellStyle base) {
        CellStyle style = workbook.createCellStyle();
        if (base != null) {
            style.cloneStyleFrom(base);
        }
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        return style;
    }

    private static CellStyle createSubTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        return style;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);

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