package com.rent.controller;

import com.rent.dao.RentDAO;
import com.rent.model.RentRow;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
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
        electricityField.textProperty().addListener((a, b, c) -> recalcTotal());
        waterField.textProperty().addListener((a, b, c) -> recalcTotal());
        gasField.textProperty().addListener((a, b, c) -> recalcTotal());
        otherField.textProperty().addListener((a, b, c) -> recalcTotal());
        fineField.textProperty().addListener((a, b, c) -> recalcTotal());
        discountField.textProperty().addListener((a, b, c) -> recalcTotal());
    }

    private void recalcTotal() {
        if (row == null) return;

        double total =
                row.getHouseRent()
                        + d(electricityField)
                        + d(waterField)
                        + d(gasField)
                        + d(otherField)
                        + d(fineField)
                        - d(discountField);

        totalLabel.setText("৳ " + String.format("%.2f", total));
        paidAmountField.setText(String.valueOf(total)); // not editable in FXML
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
        confirm.setHeaderText("✅ Payment saved successfully");
        confirm.setContentText("Do you want to print the receipt now?");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.YES) {
            openPrintOptions();
        }

        close();
    }

    private void openPrintOptions() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pages/print-options.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Print Receipt Options");
            stage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(loader.load(), 360, 260);
            stage.setScene(scene);

            PrintOptionsController controller = loader.getController();
            controller.setRentRow(row);

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void close() {
        ((Stage) totalLabel.getScene().getWindow()).close();
    }
}