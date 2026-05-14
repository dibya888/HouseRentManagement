package com.rent.controller;

import com.rent.dao.MoveOutSettlementDAO;
import com.rent.dao.TenantDAO;
import com.rent.model.Tenant;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class MoveOutTenantController {

    @FXML private Label tenantInfoLabel;
    @FXML private Label dueWarningLabel;

    @FXML private Label unpaidDueLabel;
    @FXML private Label securityDepositLabel;
    @FXML private Label settlementResultLabel;

    @FXML private DatePicker moveOutDatePicker;
    @FXML private TextArea reasonArea;

    private Tenant tenant;

    private double unpaidDue = 0;
    private double securityDeposit = 0;
    private double finalBalance = 0;

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;

        if (tenant == null) {
            return;
        }

        tenantInfoLabel.setText(
                "Tenant: " + nullSafe(tenant.getName())
                        + " | Phone: " + nullSafe(tenant.getPhone())
                        + " | Flat: " + nullSafe(tenant.getFlatNo())
        );

        moveOutDatePicker.setValue(LocalDate.now());

        loadSettlementPreview();
    }

    private void loadSettlementPreview() {
        if (tenant == null) {
            return;
        }

        int unpaidCount = TenantDAO.countUnpaidRentRows(tenant.getId());

        unpaidDue = TenantDAO.getTotalUnpaidDueByTenant(tenant.getId());
        securityDeposit = Math.max(0, tenant.getSecurityDeposit());
        finalBalance = securityDeposit - unpaidDue;

        if (unpaidCount > 0) {
            dueWarningLabel.setText(
                    "Warning: This tenant has "
                            + unpaidCount
                            + " unpaid rent row(s). Move out will not delete dues."
            );
            dueWarningLabel.setStyle("-fx-text-fill:#dc2626; -fx-font-weight:bold;");
        } else {
            dueWarningLabel.setText("No unpaid rent found.");
            dueWarningLabel.setStyle("-fx-text-fill:#16a34a; -fx-font-weight:bold;");
        }

        unpaidDueLabel.setText(money(unpaidDue));

        if (securityDeposit > 0) {
            securityDepositLabel.setText(money(securityDeposit));
        } else {
            securityDepositLabel.setText("No deposit taken");
        }

        settlementResultLabel.setText(buildSettlementResultText());
        settlementResultLabel.setStyle(buildSettlementResultStyle());
    }

    private String buildSettlementResultText() {
        if (finalBalance > 0) {
            return "Refund to Tenant: " + money(finalBalance);
        }

        if (finalBalance < 0) {
            return "Tenant Still Owes: " + money(Math.abs(finalBalance));
        }

        return "Fully Settled";
    }

    private String buildSettlementResultStyle() {
        if (finalBalance > 0) {
            return "-fx-text-fill:#2563eb; -fx-font-weight:bold;";
        }

        if (finalBalance < 0) {
            return "-fx-text-fill:#dc2626; -fx-font-weight:bold;";
        }

        return "-fx-text-fill:#16a34a; -fx-font-weight:bold;";
    }

    @FXML
    private void moveOut() {
        if (tenant == null) {
            showError("Tenant data missing.");
            return;
        }

        if (moveOutDatePicker.getValue() == null) {
            showWarning("Please select move-out date.");
            return;
        }

        int unpaidCount = TenantDAO.countUnpaidRentRows(tenant.getId());

        if (unpaidCount > 0 || finalBalance != 0) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Move Out");
            confirm.setHeaderText("Review settlement before moving out");
            confirm.setContentText("""
                    Unpaid Due: %s
                    Security Deposit: %s
                    Result: %s

                    Move out will keep unpaid rent records unchanged.
                    Settlement can be completed later from Past Tenants.
                    The flat will become available.

                    Continue?
                    """.formatted(
                    money(unpaidDue),
                    securityDeposit > 0 ? money(securityDeposit) : "No deposit taken",
                    buildSettlementResultText()
            ));

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        String reason = reasonArea.getText() == null
                ? ""
                : reasonArea.getText().trim();

        boolean success = TenantDAO.moveOutTenant(
                tenant.getId(),
                moveOutDatePicker.getValue().toString(),
                reason
        );

        if (success) {
            MoveOutSettlementDAO.saveSettlement(
                    tenant,
                    moveOutDatePicker.getValue().toString(),
                    unpaidDue,
                    securityDeposit,
                    refundAmount(),
                    payableAmount(),
                    settlementResult(),
                    reason
            );

            showInfo("Tenant moved out successfully.");
            close();

        } else {
            showError("Failed to move out tenant.");
        }
    }

    private double refundAmount() {
        return finalBalance > 0 ? finalBalance : 0;
    }

    private double payableAmount() {
        return finalBalance < 0 ? Math.abs(finalBalance) : 0;
    }

    private String settlementResult() {
        if (finalBalance > 0) {
            return "REFUND";
        }

        if (finalBalance < 0) {
            return "PAYABLE";
        }

        return "SETTLED";
    }

    @FXML
    private void close() {
        Stage stage = (Stage) tenantInfoLabel
                .getScene()
                .getWindow();

        stage.close();
    }

    private String money(double value) {
        return "৳ " + String.format("%,.2f", value);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}