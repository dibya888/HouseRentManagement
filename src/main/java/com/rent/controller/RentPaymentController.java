package com.rent.controller;

import com.rent.dao.RentDAO;
import com.rent.model.RentRow;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class RentPaymentController {

    @FXML private Label titleLabel;

    @FXML private TextField electricityField;
    @FXML private TextField waterField;
    @FXML private TextField gasField;

    @FXML private TextField otherField;
    @FXML private TextField fineField;
    @FXML private TextField discountField;

    @FXML private TextField paidAmountField;
    @FXML private DatePicker paymentDatePicker;

    private RentRow row;

    public void setRow(RentRow row) {
        this.row = row;
        titleLabel.setText("Payment - " + row.getFlatNo() + " (" + row.getTenantName() + ") - " + row.getBillMonth());

        electricityField.setText(String.valueOf(row.getElectricity()));
        waterField.setText(String.valueOf(row.getWater()));
        gasField.setText(String.valueOf(row.getGas()));

        otherField.setText(String.valueOf(row.getOtherBills()));
        fineField.setText(String.valueOf(row.getFine()));
        discountField.setText(String.valueOf(row.getDiscount()));

        // default paid amount = total
        paidAmountField.setText(String.valueOf(row.getTotal()));
        paymentDatePicker.setValue(LocalDate.now());
    }

    @FXML
    public void save() {
        if (row == null) return;

        try {
            double e = parseDouble(electricityField);
            double w = parseDouble(waterField);
            double g = parseDouble(gasField);

            double other = parseDouble(otherField);
            double fine = parseDouble(fineField);
            double disc = parseDouble(discountField);

            double paid = parseDouble(paidAmountField);
            LocalDate payDate = paymentDatePicker.getValue() == null ? LocalDate.now() : paymentDatePicker.getValue();

            RentDAO.applyPayment(row.getId(), e, w, g, other, fine, disc, paid, payDate);
            close();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Invalid input. Please enter numbers only.").show();
        }
    }

    private double parseDouble(TextField tf) {
        String s = tf.getText() == null ? "" : tf.getText().trim();
        if (s.isEmpty()) return 0;
        return Double.parseDouble(s);
    }

    @FXML
    public void close() {
        Stage st = (Stage) titleLabel.getScene().getWindow();
        st.close();
    }
}