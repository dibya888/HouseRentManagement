package com.rent.controller;

import com.rent.dao.DashboardDAO;
import com.rent.model.ChartItem;
import com.rent.model.DashboardSummary;
import com.rent.model.Repair;
import com.rent.model.ReportRow;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DashboardHomeController {

    @FXML private Label totalFlatsLabel;
    @FXML private Label occupiedFlatsLabel;
    @FXML private Label availableFlatsLabel;
    @FXML private Label totalTenantsLabel;
    @FXML private Label monthIncomeLabel;
    @FXML private Label totalDueLabel;
    @FXML private Label monthOwnerRepairLabel;
    @FXML private Label monthNetProfitLabel;

    @FXML private VBox monthlyIncomeChartBox;
    @FXML private VBox paidDueChartBox;
    @FXML private VBox incomeRepairChartBox;
    @FXML private VBox occupancyChartBox;

    @FXML private TableView<ReportRow> dueTable;
    @FXML private TableColumn<ReportRow, String> dueColMonth;
    @FXML private TableColumn<ReportRow, String> dueColFlat;
    @FXML private TableColumn<ReportRow, String> dueColTenant;
    @FXML private TableColumn<ReportRow, Double> dueColDue;
    @FXML private TableColumn<ReportRow, String> dueColStatus;

    @FXML private TableView<Repair> repairTable;
    @FXML private TableColumn<Repair, String> repairColDate;
    @FXML private TableColumn<Repair, String> repairColFlat;
    @FXML private TableColumn<Repair, String> repairColCategory;
    @FXML private TableColumn<Repair, Double> repairColCost;
    @FXML private TableColumn<Repair, String> repairColPaidBy;
    @FXML private TableColumn<Repair, String> repairColStatus;

    @FXML private TableView<ReportRow> paymentTable;
    @FXML private TableColumn<ReportRow, String> paymentColDate;
    @FXML private TableColumn<ReportRow, String> paymentColMonth;
    @FXML private TableColumn<ReportRow, String> paymentColFlat;
    @FXML private TableColumn<ReportRow, String> paymentColTenant;
    @FXML private TableColumn<ReportRow, Double> paymentColPaid;
    @FXML private TableColumn<ReportRow, String> paymentColStatus;

    @FXML
    public void initialize() {
        setupTables();
        loadDashboard();
    }

    private void setupTables() {
        dueColMonth.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        formatMonth(cellData.getValue().getMonth())
                )
        );
        dueColFlat.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        dueColTenant.setCellValueFactory(new PropertyValueFactory<>("tenant"));
        dueColDue.setCellValueFactory(new PropertyValueFactory<>("due"));
        dueColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        dueTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        repairColDate.setCellValueFactory(new PropertyValueFactory<>("repairDate"));
        repairColFlat.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        repairColCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        repairColCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        repairColPaidBy.setCellValueFactory(new PropertyValueFactory<>("paidBy"));
        repairColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        repairTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        paymentColDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        paymentColMonth.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        formatMonth(cellData.getValue().getMonth())
                )
        );
        paymentColFlat.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        paymentColTenant.setCellValueFactory(new PropertyValueFactory<>("tenant"));
        paymentColPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        paymentColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        paymentTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    @FXML
    public void loadDashboard() {
        loadSummary();
        loadCharts();
        loadTables();
    }

    private void loadSummary() {
        DashboardSummary s = DashboardDAO.getSummary();

        totalFlatsLabel.setText(String.valueOf(s.getTotalFlats()));
        occupiedFlatsLabel.setText(String.valueOf(s.getOccupiedFlats()));
        availableFlatsLabel.setText(String.valueOf(s.getAvailableFlats()));
        totalTenantsLabel.setText(String.valueOf(s.getTotalTenants()));

        monthIncomeLabel.setText(money(s.getMonthIncome()));
        totalDueLabel.setText(money(s.getTotalDue()));
        monthOwnerRepairLabel.setText(money(s.getMonthOwnerRepair()));
        monthNetProfitLabel.setText(money(s.getMonthNetProfit()));
    }

    private void loadCharts() {
        loadMonthlyIncomeChart();
        loadPaidDueChart();
        loadIncomeRepairChart();
        loadOccupancyChart();
    }

    private void loadMonthlyIncomeChart() {
        monthlyIncomeChartBox.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Month");
        yAxis.setLabel("Income");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (ChartItem item : DashboardDAO.getMonthlyIncomeChartData()) {
            String displayMonth = formatMonth(item.getLabel());

            XYChart.Data<String, Number> data =
                    new XYChart.Data<>(displayMonth, item.getValue());

            series.getData().add(data);

            // If you already added setBarColor helper
            setBarColor(data, "#2563eb");
        }

        chart.getData().add(series);

        monthlyIncomeChartBox.getChildren().add(chart);
    }

    private void loadPaidDueChart() {
        paidDueChartBox.getChildren().clear();

        PieChart chart = new PieChart();

        for (ChartItem item : DashboardDAO.getPaidDueChartData()) {
            PieChart.Data data = new PieChart.Data(
                    item.getLabel() + " - " + money(item.getValue()),
                    item.getValue()
            );

            chart.getData().add(data);

            if ("Paid".equalsIgnoreCase(item.getLabel())) {
                setPieColor(data, "#16a34a"); // green
            } else if ("Due".equalsIgnoreCase(item.getLabel())) {
                setPieColor(data, "#dc2626"); // red
            }
        }

        chart.setLegendVisible(true);
        chart.setAnimated(false);

        paidDueChartBox.getChildren().add(chart);
    }

    private void loadIncomeRepairChart() {
        incomeRepairChartBox.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (ChartItem item : DashboardDAO.getIncomeRepairChartData()) {
            XYChart.Data<String, Number> data =
                    new XYChart.Data<>(formatMonth(item.getLabel()), item.getValue());

            series.getData().add(data);

            if ("Income".equalsIgnoreCase(item.getLabel())) {
                setBarColor(data, "#16a34a"); // green
            } else {
                setBarColor(data, "#f97316"); // orange
            }
        }

        chart.getData().add(series);
        incomeRepairChartBox.getChildren().add(chart);
    }

    private void loadOccupancyChart() {
        occupancyChartBox.getChildren().clear();

        PieChart chart = new PieChart();

        for (ChartItem item : DashboardDAO.getOccupancyChartData()) {
            PieChart.Data data = new PieChart.Data(
                    item.getLabel() + " - " + (int) item.getValue(),
                    item.getValue()
            );

            chart.getData().add(data);

            if ("Occupied".equalsIgnoreCase(item.getLabel())) {
                setPieColor(data, "#16a34a"); // green
            } else if ("Available".equalsIgnoreCase(item.getLabel())) {
                setPieColor(data, "#0f766e"); // teal
            }
        }

        chart.setLegendVisible(true);
        chart.setAnimated(false);

        occupancyChartBox.getChildren().add(chart);
    }

    private void loadTables() {
        dueTable.setItems(FXCollections.observableArrayList(
                DashboardDAO.getRecentDueRows()
        ));

        paymentTable.setItems(FXCollections.observableArrayList(
                DashboardDAO.getRecentPaymentRows()
        ));

        repairTable.setItems(FXCollections.observableArrayList(
                DashboardDAO.getRecentRepairs()
        ));
    }

    private void setBarColor(XYChart.Data<String, Number> data, String color) {
        data.nodeProperty().addListener((obs, oldNode, node) -> {
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + color + ";");
            }
        });

        if (data.getNode() != null) {
            data.getNode().setStyle("-fx-bar-fill: " + color + ";");
        }
    }

    private void setPieColor(PieChart.Data data, String color) {
        data.nodeProperty().addListener((obs, oldNode, node) -> {
            if (node != null) {
                node.setStyle("-fx-pie-color: " + color + ";");
            }
        });

        if (data.getNode() != null) {
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }
    }

    private String formatMonth(String month) {
        try {
            if (month == null || month.isBlank()) return "";
            return YearMonth.parse(month)
                    .format(DateTimeFormatter.ofPattern("MMMM, yyyy"));
        } catch (Exception e) {
            return month;
        }
    }

    private String money(double value) {
        return "৳ " + String.format("%,.2f", value);
    }
}