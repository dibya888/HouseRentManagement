package com.rent.controller;

import com.rent.dao.TenantDAO;
import com.rent.model.Tenant;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.rent.dao.MoveOutSettlementDAO;
import com.rent.model.MoveOutSettlement;
import java.time.LocalDate;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import com.rent.dao.RentDAO;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import com.rent.model.RentRow;
import java.time.LocalDate;
import java.util.List;

public class PastTenantsController {

    @FXML private TableView<Tenant> pastTenantTable;

    @FXML private TableColumn<Tenant, Integer> colId;
    @FXML private TableColumn<Tenant, String> colName;
    @FXML private TableColumn<Tenant, String> colPhone;
    @FXML private TableColumn<Tenant, String> colNid;
    @FXML private TableColumn<Tenant, String> colFlatNo;
    @FXML private TableColumn<Tenant, Double> colRent;
    @FXML private TableColumn<Tenant, String> colMoveOutDate;
    @FXML private TableColumn<Tenant, String> colMoveOutReason;
    @FXML private TableColumn<Tenant, Void> colAction;
    @FXML private TableColumn<Tenant, String> colSettlement;
    @FXML private TableColumn<Tenant, String> colRefund;
    @FXML private TableColumn<Tenant, String> colPayable;
    @FXML private TextField searchField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    private final ObservableList<Tenant> masterList =
            FXCollections.observableArrayList();

    private FilteredList<Tenant> filteredList;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadPastTenants();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colNid.setCellValueFactory(new PropertyValueFactory<>("nid"));
        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        colRent.setCellValueFactory(new PropertyValueFactory<>("rent"));

        colMoveOutDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(nullSafe(cellData.getValue().getMoveOutDate()))
        );

        colMoveOutReason.setCellValueFactory(cellData ->
                new SimpleStringProperty(nullSafe(cellData.getValue().getMoveOutReason()))
        );
        colSettlement.setCellValueFactory(cellData -> {
            MoveOutSettlement s =
                    MoveOutSettlementDAO.getLatestSettlementByTenantId(cellData.getValue().getId());

            return new SimpleStringProperty(s == null ? "-" : nullSafe(s.getResult()));
        });

        colSettlement.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String result, boolean empty) {
                super.updateItem(result, empty);

                if (empty || result == null || result.isBlank() || result.equals("-")) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(result);
                setStyle("-fx-font-weight:bold;");

                if ("REFUND".equalsIgnoreCase(result)) {
                    setStyle("-fx-text-fill:#2563eb; -fx-font-weight:bold;");
                } else if ("PAYABLE".equalsIgnoreCase(result)) {
                    setStyle("-fx-text-fill:#dc2626; -fx-font-weight:bold;");
                } else if ("SETTLED".equalsIgnoreCase(result)) {
                    setStyle("-fx-text-fill:#16a34a; -fx-font-weight:bold;");
                }
            }
        });

        colRefund.setCellValueFactory(cellData -> {
            MoveOutSettlement s =
                    MoveOutSettlementDAO.getLatestSettlementByTenantId(cellData.getValue().getId());

            return new SimpleStringProperty(s == null ? "৳ 0.00" : money(s.getRefundAmount()));
        });

        colPayable.setCellValueFactory(cellData -> {
            MoveOutSettlement s =
                    MoveOutSettlementDAO.getLatestSettlementByTenantId(cellData.getValue().getId());

            return new SimpleStringProperty(s == null ? "৳ 0.00" : money(s.getPayableAmount()));
        });

        setupActionColumn();

        filteredList = new FilteredList<>(masterList, p -> true);
        pastTenantTable.setItems(filteredList);

        pastTenantTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void setupFilters() {
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    @FXML
    public void loadPastTenants() {
        masterList.setAll(TenantDAO.getMovedOutTenants());
        applyFilters();
    }

    @FXML
    private void onSearch() {
        applyFilters();
    }

    private void applyFilters() {
        if (filteredList == null) return;

        String q = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        filteredList.setPredicate(tenant -> {

            if (!q.isEmpty()) {
                boolean matched =
                        contains(tenant.getName(), q)
                                || contains(tenant.getPhone(), q)
                                || contains(tenant.getNid(), q)
                                || contains(tenant.getFlatNo(), q)
                                || contains(tenant.getMoveOutReason(), q);

                if (!matched) return false;
            }

            if (fromDate != null || toDate != null) {
                String dateText = tenant.getMoveOutDate();

                if (dateText == null || dateText.isBlank()) {
                    return false;
                }

                try {
                    LocalDate moveOutDate = LocalDate.parse(dateText);

                    if (fromDate != null && moveOutDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null && moveOutDate.isAfter(toDate)) {
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
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        applyFilters();
    }

    private void setupActionColumn() {
        colAction.setCellFactory(tc -> new TableCell<>() {

            private final Button viewBtn = new Button("View");
            private final Button settlementBtn = new Button("Settlement");
            private final Button deleteBtn = new Button("Delete");

            private final HBox box = new HBox(8, viewBtn, settlementBtn, deleteBtn);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);

                viewBtn.setStyle(
                        "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                settlementBtn.setStyle(
                        "-fx-background-color:#0f766e; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                deleteBtn.setStyle(
                        "-fx-background-color:#dc2626; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                viewBtn.setOnAction(e -> {
                    Tenant tenant = getTableView().getItems().get(getIndex());
                    openViewTenant(tenant);
                });

                settlementBtn.setOnAction(e -> {
                    Tenant tenant = getTableView().getItems().get(getIndex());
                    showSettlementDetails(tenant);
                });

                deleteBtn.setOnAction(e -> {
                    Tenant tenant = getTableView().getItems().get(getIndex());
                    deletePastTenant(tenant);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setText(null);
            }
        });
    }

    private void openViewTenant(Tenant tenant) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pages/view-tenant.fxml")
            );

            Parent root = loader.load();

            ViewTenantController controller = loader.getController();
            controller.setTenant(tenant);

            Stage stage = new Stage();
            stage.setTitle("Past Tenant Details");

            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );

            stage.setScene(new Scene(root, 700, 600));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to open tenant details.").showAndWait();
        }
    }

    @FXML
    private void backToTenants() {
        DashboardController.getInstance().showTenants();
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private void deletePastTenant(Tenant tenant) {
        if (tenant == null) {
            return;
        }

        int historyCount = TenantDAO.countTenantRentHistory(tenant.getId());

        if (historyCount > 0) {
            new Alert(
                    Alert.AlertType.WARNING,
                    """
                    This tenant cannot be deleted because rent/payment history exists.
    
                    Keeping this tenant preserves old receipts, archive records, and reports.
                    """
            ).showAndWait();

            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Past Tenant");
        confirm.setHeaderText("Delete this past tenant permanently?");
        confirm.setContentText("""
            This tenant has no rent history.

            This action cannot be undone.
            """);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        TenantDAO.deleteTenant(tenant.getId());

        loadPastTenants();

        new Alert(
                Alert.AlertType.INFORMATION,
                "Past tenant deleted successfully."
        ).showAndWait();
    }

    private String money(double value) {
        return "৳ " + String.format("%,.2f", value);
    }

    private void showSettlementDetails(Tenant tenant) {
        if (tenant == null) {
            return;
        }

        MoveOutSettlement settlement =
                MoveOutSettlementDAO.getLatestSettlementByTenantId(tenant.getId());

        if (settlement == null) {
            new Alert(
                    Alert.AlertType.INFORMATION,
                    "No settlement record found for this tenant."
            ).showAndWait();

            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Move-Out Settlement");
        alert.setHeaderText("Settlement Details");

        String details = settlementDetailsText(settlement);

        TextArea area = new TextArea(details);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(540);
        area.setPrefHeight(330);

        alert.getDialogPane().setContent(area);

        ButtonType settleBtn = new ButtonType("Settle");
        ButtonType printPdfBtn = new ButtonType("Print PDF");
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(settleBtn, printPdfBtn, closeBtn);

        alert.showAndWait().ifPresent(result -> {

            if (result == settleBtn) {
                settleTenantFromDeposit(tenant, settlement);
                loadPastTenants();
            }

            if (result == printPdfBtn) {
                boolean exported = SettlementPdfExporter.exportSettlement(settlement);

                if (exported) {
                    AuditLogDAO.log(
                            AuditActions.SETTLEMENT_PDF_EXPORTED,
                            "Settlement PDF exported. Tenant: "
                                    + settlement.getTenantName()
                                    + ", Flat: "
                                    + settlement.getFlatNo()
                                    + ", Result: "
                                    + settlement.getResult()
                    );
                }
            }
        });
    }

    private String settlementDetailsText(MoveOutSettlement settlement) {
        return """
            Tenant: %s
            Phone: %s
            Flat No: %s

            Move Out Date: %s
            Result: %s

            Unpaid Due: %s
            Security Deposit: %s
            Refund Amount: %s
            Payable Amount: %s

            Reason / Notes:
            %s

            Created At: %s
            """.formatted(
                nullSafe(settlement.getTenantName()),
                nullSafe(settlement.getTenantPhone()),
                nullSafe(settlement.getFlatNo()),

                nullSafe(settlement.getMoveOutDate()),
                nullSafe(settlement.getResult()),

                money(settlement.getUnpaidDue()),
                money(settlement.getSecurityDeposit()),
                money(settlement.getRefundAmount()),
                money(settlement.getPayableAmount()),

                nullSafe(settlement.getReason()),
                nullSafe(settlement.getCreatedAt())
        );
    }

    private void settleTenantFromDeposit(Tenant tenant, MoveOutSettlement settlement) {
        if (tenant == null || settlement == null) {
            return;
        }

        if ("SETTLED".equalsIgnoreCase(settlement.getResult())) {
            new Alert(
                    Alert.AlertType.INFORMATION,
                    "This settlement is already marked as settled."
            ).showAndWait();
            return;
        }

        List<RentRow> unpaidRows =
                RentDAO.getUnpaidRentRowsByTenant(tenant.getId());

        if (unpaidRows.isEmpty()) {
            boolean updated = MoveOutSettlementDAO.markSettlementAsSettled(
                    settlement.getId(),
                    settlement.getSecurityDeposit(),
                    0,
                    "Settlement marked as settled. No unpaid rent found."
            );

            if (updated) {
                AuditLogDAO.log(
                        AuditActions.SETTLEMENT_SETTLED,
                        "Settlement marked as settled. Tenant: "
                                + tenant.getName()
                                + ", Flat: "
                                + tenant.getFlatNo()
                );

                new Alert(
                        Alert.AlertType.INFORMATION,
                        "Settlement marked as settled."
                ).showAndWait();
            }

            return;
        }

        if (unpaidRows.size() > 1) {
            new Alert(
                    Alert.AlertType.WARNING,
                    """
                    This tenant has multiple unpaid rent rows.
    
                    Please settle those from the Rent page first.
                    This simple settlement button supports one due row only.
                    """
            ).showAndWait();
            return;
        }

        RentRow row = unpaidRows.get(0);

        double due = Math.max(row.getTotal() - row.getPaidAmount(), 0);
        double deposit = Math.max(settlement.getSecurityDeposit(), 0);

        if (due <= 0) {
            MoveOutSettlementDAO.markSettlementAsSettled(
                    settlement.getId(),
                    deposit,
                    0,
                    "Settlement marked as settled. Due amount was zero."
            );

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Settlement marked as settled."
            ).showAndWait();

            return;
        }

        if (deposit <= 0) {
            new Alert(
                    Alert.AlertType.WARNING,
                    """
                    No security deposit is available.
    
                    Use the normal Rent Payment screen to settle this due.
                    """
            ).showAndWait();
            return;
        }

        double refund = 0;
        double payable = 0;
        double discountGiven = 0;
        double amountUsedFromDeposit;

        if (deposit >= due) {
            amountUsedFromDeposit = due;
            refund = deposit - due;

        } else {
            amountUsedFromDeposit = deposit;
            double remaining = due - deposit;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Waive Remaining Due?");
            confirm.setHeaderText("Security deposit is less than unpaid due.");
            confirm.setContentText("""
                Unpaid Due: %s
                Security Deposit: %s
                Remaining: %s

                Waive the remaining amount as discount?
                """.formatted(
                    money(due),
                    money(deposit),
                    money(remaining)
            ));

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                new Alert(
                        Alert.AlertType.INFORMATION,
                        "Settlement was not changed."
                ).showAndWait();
                return;
            }

            discountGiven = remaining;
            refund = 0;
        }

        double newDiscount = row.getDiscount() + discountGiven;
        double newPaidAmount = row.getPaidAmount() + amountUsedFromDeposit;

        RentDAO.applyPayment(
                row.getId(),
                row.getElectricity(),
                row.getWater(),
                row.getGas(),
                row.getOtherBills(),
                row.getFine(),
                newDiscount,
                newPaidAmount,
                settlement.getMoveOutDate() == null || settlement.getMoveOutDate().isBlank()
                        ? LocalDate.now()
                        : LocalDate.parse(settlement.getMoveOutDate())
        );

        String note = """
            Settlement completed using security deposit.
            Deposit Used: %s
            Discount/Waiver: %s
            Refund: %s
            """.formatted(
                money(amountUsedFromDeposit),
                money(discountGiven),
                money(refund)
        );

        boolean updated = MoveOutSettlementDAO.markSettlementAsSettled(
                settlement.getId(),
                refund,
                payable,
                note
        );

        if (updated) {
            AuditLogDAO.log(
                    AuditActions.SETTLEMENT_SETTLED,
                    "Settlement settled using deposit. Tenant: "
                            + tenant.getName()
                            + ", Flat: "
                            + tenant.getFlatNo()
                            + ", Deposit Used: "
                            + amountUsedFromDeposit
                            + ", Discount: "
                            + discountGiven
                            + ", Refund: "
                            + refund
            );

            new Alert(
                    Alert.AlertType.INFORMATION,
                    """
                    Settlement completed.
                    Related rent row was moved to archive if fully paid.
                    """
            ).showAndWait();
        }
    }
}