package com.rent.controller;

import com.rent.dao.RentDAO;
import com.rent.model.RentRow;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class RentPaymentController {

    @FXML private Label tenantNameLabel;
    @FXML private Label flatLabel;
    @FXML private Label monthLabel;

    @FXML private TextField electricityField;
    @FXML private TextField waterField;
    @FXML private TextField gasField;
    @FXML private TextField otherField;
    @FXML private TextField fineField;
    @FXML private TextField discountField;

    @FXML private Label totalLabel;
    @FXML private TextField paidAmountField;
    @FXML private DatePicker paymentDatePicker;

    private RentRow row;

    public void setRow(RentRow row) {
        this.row = row;

        tenantNameLabel.setText("👤 " + row.getTenantName());
        flatLabel.setText("🏢 Flat: " + row.getFlatNo());

        YearMonth ym = YearMonth.parse(row.getBillMonth());
        monthLabel.setText("📅 " + ym.format(DateTimeFormatter.ofPattern("MMMM, yyyy")));

        electricityField.setText(String.valueOf(row.getElectricity()));
        waterField.setText(String.valueOf(row.getWater()));
        gasField.setText(String.valueOf(row.getGas()));
        otherField.setText(String.valueOf(row.getOtherBills()));
        fineField.setText(String.valueOf(row.getFine()));
        discountField.setText(String.valueOf(row.getDiscount()));

        paymentDatePicker.setValue(LocalDate.now());

        setupAutoCalculation();
        recalcTotal();
    }

    private void setupAutoCalculation() {
        electricityField.textProperty().addListener((a,b,c)->recalcTotal());
        waterField.textProperty().addListener((a,b,c)->recalcTotal());
        gasField.textProperty().addListener((a,b,c)->recalcTotal());
        otherField.textProperty().addListener((a,b,c)->recalcTotal());
        fineField.textProperty().addListener((a,b,c)->recalcTotal());
        discountField.textProperty().addListener((a,b,c)->recalcTotal());
    }

    private void recalcTotal() {
        double total =
                row.getHouseRent()
                        + d(electricityField)
                        + d(waterField)
                        + d(gasField)
                        + d(otherField)
                        + d(fineField)
                        - d(discountField);

        totalLabel.setText("৳ " + String.format("%.2f", total));
        paidAmountField.setText(String.valueOf(total));
    }

    private double d(TextField f) {
        try { return Double.parseDouble(f.getText().trim()); }
        catch (Exception e) { return 0; }
    }

    @FXML
    private void save() {

        RentDAO.applyPayment(
                row.getId(),
                d(electricityField),
                d(waterField),
                d(gasField),
                d(otherField),
                d(fineField),
                d(discountField),
                Double.parseDouble(paidAmountField.getText()),
                paymentDatePicker.getValue()
        );

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Payment Saved");
        confirm.setHeaderText("Print receipt?");
        confirm.setContentText("Do you want to print the receipt now?");

        Optional<ButtonType> res = confirm.showAndWait();

        // YES → receipt logic later
        // NO  → just close
        close();
    }

    @FXML
    private void close() {
        ((Stage) totalLabel.getScene().getWindow()).close();
    }
}