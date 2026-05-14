package com.rent.controller;

import com.rent.dao.ReportDAO;
import com.rent.model.ReportRow;
import com.rent.model.ReportSummary;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import com.rent.dao.RepairDAO;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;

public class ReportsController {

    @FXML private ComboBox<String> reportTypeCombo;

    @FXML private DatePicker monthPicker;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    @FXML private Label totalIncomeLabel;
    @FXML private Label monthIncomeLabel;
    @FXML private Label yearIncomeLabel;
    @FXML private Label totalDueLabel;
    @FXML private Label totalFlatsLabel;
    @FXML private Label occupiedFlatsLabel;
    @FXML private Label availableFlatsLabel;
    @FXML private Label totalTenantsLabel;

    @FXML private TableView<ReportRow> reportTable;
    @FXML private TableColumn<ReportRow, String> colTitle;
    @FXML private TableColumn<ReportRow, String> colMonth;
    @FXML private TableColumn<ReportRow, String> colDate;
    @FXML private TableColumn<ReportRow, String> colFlatNo;
    @FXML private TableColumn<ReportRow, String> colTenant;
    @FXML private TableColumn<ReportRow, Double> colTotal;
    @FXML private TableColumn<ReportRow, Double> colPaid;
    @FXML private TableColumn<ReportRow, Double> colDue;
    @FXML private TableColumn<ReportRow, String> colStatus;

    @FXML private Label totalRepairLabel;
    @FXML private Label monthRepairLabel;
    @FXML private Label yearRepairLabel;
    @FXML private Label netProfitLabel;
    @FXML private Label ownerRepairLabel;
    @FXML private Label tenantRepairLabel;
    @FXML private TableColumn<ReportRow, String> colExtra;

    @FXML private Label totalUtilityLabel;
    @FXML private Label monthUtilityLabel;
    @FXML private Label yearUtilityLabel;
    @FXML private Label utilityBreakdownLabel;

    private final ObservableList<ReportRow> reportList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBox();
        setupTable();
        loadOccupancySummary();
        loadReportRows();
        applyReportFilter();
        monthPicker.valueProperty().addListener((obs, oldVal, newVal) -> applyReportFilter());
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyReportFilter());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyReportFilter());
    }


    private FilteredList<ReportRow> filteredList;

    private void setupComboBox() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "All Reports",
                "Monthly Income",
                "Flat-wise Income",
                "Yearly Income",
                "Due Rent",
                "Tenant-wise Report",
                "Repair Report",
                "Utility Bills Report"
        ));

        reportTypeCombo.getSelectionModel().selectFirst();
        reportTypeCombo.setOnAction(e -> {
            loadReportRows();
            applyReportFilter();
        });
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        colTenant.setCellValueFactory(new PropertyValueFactory<>("tenant"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colExtra.setCellValueFactory(new PropertyValueFactory<>("extraInfo"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("due"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        reportTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        filteredList = new FilteredList<>(reportList, p -> true);
        reportTable.setItems(filteredList);
    }

    private void loadSummary() {
        ReportSummary summary = ReportDAO.getSummary();

        totalIncomeLabel.setText(money(summary.getTotalIncome()));
        monthIncomeLabel.setText(money(summary.getMonthIncome()));
        yearIncomeLabel.setText(money(summary.getYearIncome()));
        totalDueLabel.setText(money(summary.getTotalDue()));

        totalFlatsLabel.setText(String.valueOf(summary.getTotalFlats()));
        occupiedFlatsLabel.setText(String.valueOf(summary.getOccupiedFlats()));
        availableFlatsLabel.setText(String.valueOf(summary.getAvailableFlats()));
        totalTenantsLabel.setText(String.valueOf(summary.getTotalTenants()));
    }

    private void loadReportRows() {
        updateColumnTitles();

        String type = reportTypeCombo.getValue();

        if (type == null || type.equals("All Reports")) {
            reportList.setAll(ReportDAO.getAllReportRows());
            return;
        }

        switch (type) {
            case "Monthly Income" ->
                    reportList.setAll(ReportDAO.getMonthlyIncomeRows());

            case "Flat-wise Income" ->
                    reportList.setAll(ReportDAO.getFlatWiseIncomeRows());

            case "Yearly Income" ->
                    reportList.setAll(ReportDAO.getYearlyIncomeRows());

            case "Tenant-wise Report" ->
                    reportList.setAll(ReportDAO.getTenantWiseIncomeRows());

            case "Due Rent" ->
                    reportList.setAll(ReportDAO.getDueRentSummaryRows());

            case "Repair Report" ->
                    reportList.setAll(ReportDAO.getRepairReportRows());

            case "Utility Bills Report" ->
                    reportList.setAll(ReportDAO.getUtilityBillReportRows());

            default ->
                    reportList.setAll(ReportDAO.getAllReportRows());
        }
    }

    @FXML
    private void generateReport() {
        loadReportRows();
        applyReportFilter();

        new Alert(Alert.AlertType.INFORMATION,
                "Report generated successfully.").show();
    }

    @FXML
    private void exportPdf() {
        loadReportRows();
        applyReportFilter();

        ReportSummary summary = buildSummaryFromFilteredRows();

        javafx.collections.ObservableList<ReportRow> exportRows =
                javafx.collections.FXCollections.observableArrayList();

        if (filteredList != null) {
            exportRows.addAll(filteredList);
        }

        if (exportRows.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "No report rows found to export. Please click Generate or check filters.").show();
            return;
        }

        boolean exported = ReportPdfExporter.exportReport(summary, exportRows);

        if (exported) {
            AuditLogDAO.log(
                    AuditActions.REPORT_PDF_EXPORTED,
                    "PDF report exported. Type: "
                            + reportTypeCombo.getValue()
                            + ", Rows: "
                            + exportRows.size()
            );
        }
    }



    @FXML
    private void exportExcel() {
        loadReportRows();
        applyReportFilter();

        ReportSummary summary = buildSummaryFromFilteredRows();

        javafx.collections.ObservableList<ReportRow> exportRows =
                javafx.collections.FXCollections.observableArrayList();

        if (filteredList != null) {
            exportRows.addAll(filteredList);
        }

        if (exportRows.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "No report rows found to export. Please click Generate or check filters.").show();
            return;
        }

        boolean exported = ReportExcelExporter.exportReport(summary, exportRows);

        if (exported) {
            AuditLogDAO.log(
                    AuditActions.REPORT_EXCEL_EXPORTED,
                    "Excel report exported. Type: "
                            + reportTypeCombo.getValue()
                            + ", Rows: "
                            + exportRows.size()
            );
        }
    }

    @FXML
    private void printReport() {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(reportTable.getScene().getWindow())) {
            boolean success = job.printPage(reportTable);

            if (success) {
                job.endJob();

                AuditLogDAO.log(
                        AuditActions.REPORT_PRINTED,
                        "Report printed. Type: "
                                + reportTypeCombo.getValue()
                                + ", Rows: "
                                + (filteredList == null ? 0 : filteredList.size())
                );

                new Alert(Alert.AlertType.INFORMATION,
                        "Report sent to printer.").show();
            } else {
                new Alert(Alert.AlertType.ERROR,
                        "Failed to print report.").show();
            }
        }
    }

    @FXML
    private void clearFilters() {
        reportTypeCombo.getSelectionModel().selectFirst();
        monthPicker.setValue(null);
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);

        applyReportFilter();
    }

    private String money(double value) {
        return "৳ " + String.format("%,.2f", value);
    }

    private void applyReportFilter() {
        if (filteredList == null) return;

        String type = reportTypeCombo.getValue();

        String selectedMonth = null;
        if (monthPicker != null && monthPicker.getValue() != null) {
            selectedMonth = java.time.YearMonth.from(monthPicker.getValue()).toString();
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        final String monthFinal = selectedMonth;

        filteredList.setPredicate(row -> {

            // Month filter
            if (monthFinal != null) {
                if (row.getMonth() == null || !row.getMonth().equals(monthFinal)) {
                    return false;
                }
            }

            // Date range filter
            if (fromDate != null || toDate != null) {
                if (row.getDate() == null || row.getDate().isBlank()) {
                    return false;
                }

                try {
                    LocalDate rowDate = LocalDate.parse(row.getDate());

                    if (fromDate != null && rowDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null && rowDate.isAfter(toDate)) {
                        return false;
                    }

                } catch (Exception e) {
                    return false;
                }
            }

            // Report type filter
            if (type == null || type.equals("All Reports")) {
                return true;
            }

            if (type.equals("Due Rent")) {
                return row.getDue() > 0;
            }

            if (type.equals("Monthly Income")) {
                return row.getPaid() > 0;
            }

            if (type.equals("Flat-wise Income")) {
                return row.getFlatNo() != null && !row.getFlatNo().isBlank();
            }

            if (type.equals("Yearly Income")) {
                return row.getPaid() > 0;
            }

            if (type.equals("Tenant-wise Report")) {
                return row.getTenant() != null && !row.getTenant().isBlank();
            }

            if (type.equals("Repair Report")) {
                return row.getTitle() != null && row.getTitle().startsWith("Repair");
            }
            if (type.equals("Utility Bills Report")) {
                return row.getTitle() != null && row.getTitle().equals("Utility Bills");
            }

            return true;
        });

        updateSummaryFromFilteredRows();
    }

    private void loadOccupancySummary() {
        ReportSummary summary = ReportDAO.getSummary();

        totalFlatsLabel.setText(String.valueOf(summary.getTotalFlats()));
        occupiedFlatsLabel.setText(String.valueOf(summary.getOccupiedFlats()));
        availableFlatsLabel.setText(String.valueOf(summary.getAvailableFlats()));
        totalTenantsLabel.setText(String.valueOf(summary.getTotalTenants()));
    }

    private ReportSummary buildSummaryFromFilteredRows() {
        ReportSummary summary = new ReportSummary();

        if (filteredList == null) {
            return summary;
        }

        double totalIncome = 0;
        double totalDue = 0;
        double monthIncome = 0;
        double yearIncome = 0;

        String currentMonth = java.time.YearMonth.now().toString();
        String currentYear = String.valueOf(java.time.Year.now().getValue());

        for (ReportRow row : filteredList) {
            totalIncome += row.getPaid();
            totalDue += row.getDue();

            if (row.getMonth() != null && row.getMonth().equals(currentMonth)) {
                monthIncome += row.getPaid();
            }

            if (row.getMonth() != null && row.getMonth().startsWith(currentYear + "-")) {
                yearIncome += row.getPaid();
            }
        }

        double totalRepair = RepairDAO.getTotalRepairCost();
        double monthRepair = RepairDAO.getMonthRepairCost(currentMonth);
        double yearRepair = RepairDAO.getYearRepairCost(currentYear);

        double ownerPaidRepair = RepairDAO.getOwnerPaidTotalRepairCost();
        double tenantPaidRepair = RepairDAO.getTenantPaidTotalRepairCost();

        ReportSummary base = ReportDAO.getSummary();

        summary.setTotalUtilityBills(base.getTotalUtilityBills());
        summary.setMonthUtilityBills(base.getMonthUtilityBills());
        summary.setYearUtilityBills(base.getYearUtilityBills());

        summary.setElectricityBills(base.getElectricityBills());
        summary.setWaterBills(base.getWaterBills());
        summary.setGasBills(base.getGasBills());
        summary.setOtherBills(base.getOtherBills());

        summary.setTotalIncome(totalIncome);
        summary.setMonthIncome(monthIncome);
        summary.setYearIncome(yearIncome);
        summary.setTotalDue(totalDue);

        summary.setTotalRepairCost(totalRepair);
        summary.setMonthRepairCost(monthRepair);
        summary.setYearRepairCost(yearRepair);

        summary.setOwnerPaidRepairCost(ownerPaidRepair);
        summary.setTenantPaidRepairCost(tenantPaidRepair);

        /*
         * Net Profit should be based on real rent income,
         * not selected table rows.
         */
        summary.setNetProfit(base.getTotalIncome() - ownerPaidRepair);

        summary.setTotalFlats(base.getTotalFlats());
        summary.setOccupiedFlats(base.getOccupiedFlats());
        summary.setAvailableFlats(base.getAvailableFlats());
        summary.setTotalTenants(base.getTotalTenants());

        return summary;
    }

    private void updateSummaryFromFilteredRows() {
        if (filteredList == null) return;

        double totalIncome = 0;
        double totalDue = 0;
        double monthIncome = 0;
        double yearIncome = 0;

        String currentMonth = java.time.YearMonth.now().toString();
        String currentYear = String.valueOf(java.time.Year.now().getValue());

        for (ReportRow row : filteredList) {
            totalIncome += row.getPaid();
            totalDue += row.getDue();

            if (row.getMonth() != null && row.getMonth().equals(currentMonth)) {
                monthIncome += row.getPaid();
            }

            if (row.getMonth() != null && row.getMonth().startsWith(currentYear + "-")) {
                yearIncome += row.getPaid();
            }
        }

        double totalRepair = RepairDAO.getTotalRepairCost();
        double monthRepair = RepairDAO.getMonthRepairCost(currentMonth);
        double yearRepair = RepairDAO.getYearRepairCost(currentYear);

        double ownerPaidRepair = RepairDAO.getOwnerPaidTotalRepairCost();
        double tenantPaidRepair = RepairDAO.getTenantPaidTotalRepairCost();

        /*
         * Important:
         * Net Profit should use overall rent income,
         * not only currently visible table rows.
         */
        double overallRentIncome = ReportDAO.getSummary().getTotalIncome();
        double netProfit = overallRentIncome - ownerPaidRepair;

        totalIncomeLabel.setText(money(totalIncome));
        monthIncomeLabel.setText(money(monthIncome));
        yearIncomeLabel.setText(money(yearIncome));
        totalDueLabel.setText(money(totalDue));

        totalRepairLabel.setText(money(totalRepair));
        monthRepairLabel.setText(money(monthRepair));
        yearRepairLabel.setText(money(yearRepair));

        ownerRepairLabel.setText(money(ownerPaidRepair));
        tenantRepairLabel.setText(money(tenantPaidRepair));

        netProfitLabel.setText(money(netProfit));

        ReportSummary baseSummary = ReportDAO.getSummary();

        totalUtilityLabel.setText(money(baseSummary.getTotalUtilityBills()));
        monthUtilityLabel.setText(money(baseSummary.getMonthUtilityBills()));
        yearUtilityLabel.setText(money(baseSummary.getYearUtilityBills()));

        utilityBreakdownLabel.setText(
                "E: " + money(baseSummary.getElectricityBills())
                        + " | W: " + money(baseSummary.getWaterBills())
                        + " | G: " + money(baseSummary.getGasBills())
                        + " | O: " + money(baseSummary.getOtherBills())
        );
    }
    private void updateColumnTitles() {
        String type = reportTypeCombo.getValue();

        if ("Repair Report".equals(type)) {

            colTitle.setText("Repair Type");
            colMonth.setText("Month");
            colDate.setText("Repair Date");
            colFlatNo.setText("Flat No");
            colTenant.setText("Description");
            colTotal.setText("Repair Cost");
            colExtra.setText("Paid By");
            colStatus.setText("Status");

            colExtra.setVisible(true);
            colPaid.setVisible(false);
            colDue.setVisible(false);

        } else if ("Utility Bills Report".equals(type)) {

            colTitle.setText("Report Type");
            colMonth.setText("Month");
            colDate.setText("Payment Date");
            colFlatNo.setText("Flat No");
            colTenant.setText("Tenant");
            colTotal.setText("Utility Total");
            colExtra.setText("Breakdown");
            colStatus.setText("Status");

            colExtra.setVisible(true);
            colPaid.setVisible(false);
            colDue.setVisible(false);

        } else {

            colTitle.setText("Title");
            colMonth.setText("Month");
            colDate.setText("Date");
            colFlatNo.setText("Flat No");
            colTenant.setText("Tenant");
            colTotal.setText("Total");
            colExtra.setText("Info");
            colPaid.setText("Paid");
            colDue.setText("Due");
            colStatus.setText("Status");

            colExtra.setVisible(false);
            colPaid.setVisible(true);
            colDue.setVisible(true);
        }
    }
}