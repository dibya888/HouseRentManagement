package com.rent.controller;

import com.rent.dao.RentDAO;
import com.rent.model.RentRow;
import com.rent.util.StatusBadgeCellFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class ArchiveController {

    @FXML private TableView<RentRow> archiveTable;

    @FXML private TableColumn<RentRow, String> colFlatNo;
    @FXML private TableColumn<RentRow, String> colTenantName;
    @FXML private TableColumn<RentRow, String> colPhone;
    @FXML private TableColumn<RentRow, String> colBillMonth;
    @FXML private TableColumn<RentRow, String> colPaymentDate;
    @FXML private TableColumn<RentRow, Double> colTotal;
    @FXML private TableColumn<RentRow, String> colStatus;
    @FXML private TableColumn<RentRow, Void> colAction;

    @FXML private TextField searchField;
    @FXML private DatePicker monthPicker;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    private final ObservableList<RentRow> masterList =
            FXCollections.observableArrayList();

    private FilteredList<RentRow> filteredList;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadArchive();
    }

    private void setupTable() {
        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        colTenantName.setCellValueFactory(new PropertyValueFactory<>("tenantName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        colBillMonth.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatMonth(cellData.getValue().getBillMonth()))
        );

        colPaymentDate.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));

        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(StatusBadgeCellFactory.forStatus());

        setupAction();

        filteredList = new FilteredList<>(masterList, p -> true);
        archiveTable.setItems(filteredList);

        archiveTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void setupFilters() {
        monthPicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    @FXML
    public void loadArchive() {
        masterList.setAll(RentDAO.getArchiveRows());
        applyFilters();
    }

    @FXML
    private void applyFilters() {
        if (filteredList == null) return;

        String q = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String selectedMonth = null;
        if (monthPicker != null && monthPicker.getValue() != null) {
            selectedMonth = YearMonth.from(monthPicker.getValue()).toString();
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        String finalSelectedMonth = selectedMonth;

        filteredList.setPredicate(row -> {

            // Search filter
            if (!q.isEmpty()) {
                boolean matched =
                        contains(row.getFlatNo(), q)
                                || contains(row.getTenantName(), q)
                                || contains(row.getPhone(), q)
                                || contains(row.getBillMonth(), q)
                                || contains(row.getPaymentDate(), q)
                                || contains(row.getStatus(), q);

                if (!matched) return false;
            }

            // Month filter
            if (finalSelectedMonth != null) {
                if (row.getBillMonth() == null
                        || !row.getBillMonth().equals(finalSelectedMonth)) {
                    return false;
                }
            }

            // Payment date range filter
            if (fromDate != null || toDate != null) {
                if (row.getPaymentDate() == null || row.getPaymentDate().isBlank()) {
                    return false;
                }

                try {
                    LocalDate paymentDate = LocalDate.parse(row.getPaymentDate());

                    if (fromDate != null && paymentDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null && paymentDate.isAfter(toDate)) {
                        return false;
                    }

                } catch (Exception e) {
                    return false;
                }
            }

            return true;
        });
    }

    @FXML
    private void clearFilters() {
        searchField.clear();
        monthPicker.setValue(null);
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        applyFilters();
    }

    private void setupAction() {
        colAction.setCellFactory(tc -> new TableCell<>() {

            private final Button viewBtn = new Button("View");
            private final Button printBtn = new Button("Print");
            private final Button restoreBtn = new Button("Restore");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(8, viewBtn, printBtn, restoreBtn, deleteBtn);

            {
                viewBtn.setStyle(
                        "-fx-background-color:#111827; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                printBtn.setStyle(
                        "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                restoreBtn.setStyle(
                        "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                deleteBtn.setStyle(
                        "-fx-background-color:#7f1d1d; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                viewBtn.setOnAction(e -> {
                    RentRow row = getTableView().getItems().get(getIndex());
                    showDetails(row);
                });

                printBtn.setOnAction(e -> {
                    RentRow row = getTableView().getItems().get(getIndex());
                    printReceipt(row);
                });

                restoreBtn.setOnAction(e -> {
                    RentRow row = getTableView().getItems().get(getIndex());
                    restoreArchive(row);
                });

                deleteBtn.setOnAction(e -> {
                    RentRow row = getTableView().getItems().get(getIndex());
                    deleteArchivePayment(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void printReceipt(RentRow row) {
        if (row == null) return;

        ReceiptPrinter.printReceipt(
                row,
                true,
                true
        );
    }

    private void restoreArchive(RentRow row) {
        if (row == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Restore Rent");
        confirm.setHeaderText("Restore this paid rent back to due?");
        confirm.setContentText("""
                This will move the selected archive row back to current rent.
                Paid amount will become 0 and status will become DUE.
                """);

        if (confirm.showAndWait().isPresent()
                && confirm.getResult() == ButtonType.OK) {

            RentDAO.restoreFromArchive(row.getId());

            AuditLogDAO.log(
                    AuditActions.ARCHIVE_RESTORE,
                    "Archive restored to due. Receipt No: "
                            + row.getReceiptNo()
                            + ", Flat: "
                            + row.getFlatNo()
                            + ", Tenant: "
                            + row.getTenantName()
                            + ", Month: "
                            + row.getBillMonth()
            );

            loadArchive();
        }
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
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

    private void showDetails(RentRow row) {
        if (row == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Archive Details");
        alert.setHeaderText("Rent Archive Details");

        String details = """
            Tenant: %s
            Phone: %s
            Flat No: %s
            Meter No: %s

            Bill Month: %s
            Payment Date: %s
            Status: %s

            House Rent: ৳ %,.2f
            Electricity: ৳ %,.2f
            Water: ৳ %,.2f
            Gas: ৳ %,.2f
            Other Bills: ৳ %,.2f
            Fine: ৳ %,.2f
            Discount: ৳ %,.2f

            Total: ৳ %,.2f
            Paid Amount: ৳ %,.2f

            Notes:
            %s
            """.formatted(
                nullSafe(row.getTenantName()),
                nullSafe(row.getPhone()),
                nullSafe(row.getFlatNo()),
                nullSafe(row.getMeterNo()),

                formatMonth(row.getBillMonth()),
                nullSafe(row.getPaymentDate()),
                nullSafe(row.getStatus()),

                row.getHouseRent(),
                row.getElectricity(),
                row.getWater(),
                row.getGas(),
                row.getOtherBills(),
                row.getFine(),
                row.getDiscount(),

                row.getTotal(),
                row.getPaidAmount(),

                nullSafe(row.getNotes())
        );

        alert.setContentText(details);
        alert.showAndWait();
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void deleteArchivePayment(RentRow row) {
        if (row == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Archive Payment");
        confirm.setHeaderText("Delete this archived payment?");
        confirm.setContentText("""
            This will permanently delete this archived payment record.

            Income reports and dashboard totals may decrease.
            Use this only if this payment was created by mistake.

            Continue?
            """);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        boolean deleted = RentDAO.deleteArchivePayment(row.getId());

        if (deleted) {
            AuditLogDAO.log(
                    AuditActions.ARCHIVE_PAYMENT_DELETED,
                    "Archive payment deleted. Receipt No: "
                            + row.getReceiptNo()
                            + ", Flat: "
                            + row.getFlatNo()
                            + ", Tenant: "
                            + row.getTenantName()
                            + ", Month: "
                            + row.getBillMonth()
                            + ", Amount: "
                            + row.getPaidAmount()
            );

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Archived payment deleted successfully."
            ).showAndWait();

            loadArchive();

        } else {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Failed to delete archived payment."
            ).showAndWait();
        }
    }
}