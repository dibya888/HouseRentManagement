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

    private final ObservableList<ReportRow> reportList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBox();
        setupTable();
        loadSummary();
        loadReportRows();
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
                "Repair Report Later"
        ));

        reportTypeCombo.getSelectionModel().selectFirst();
        reportTypeCombo.setOnAction(e -> applyReportFilter());
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        colTenant.setCellValueFactory(new PropertyValueFactory<>("tenant"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
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
        reportList.setAll(ReportDAO.getAllReportRows());
    }

    @FXML
    private void generateReport() {
        loadSummary();
        loadReportRows();
        applyReportFilter();

        new Alert(Alert.AlertType.INFORMATION,
                "Report generated successfully.").show();
    }

    @FXML
    private void exportPdf() {
        ReportSummary summary = ReportDAO.getSummary();
        ReportPdfExporter.exportReport(summary, filteredList);
    }

    @FXML
    private void exportExcel() {
        ReportSummary summary = ReportDAO.getSummary();

        javafx.collections.ObservableList<ReportRow> exportRows =
                javafx.collections.FXCollections.observableArrayList(filteredList);

        ReportExcelExporter.exportReport(summary, exportRows);
    }

    @FXML
    private void printReport() {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(reportTable.getScene().getWindow())) {
            boolean success = job.printPage(reportTable);

            if (success) {
                job.endJob();
                new Alert(Alert.AlertType.INFORMATION,
                        "Report sent to printer.").show();
            } else {
                new Alert(Alert.AlertType.ERROR,
                        "Failed to print report.").show();
            }
        }
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

            return true;
        });
    }
}