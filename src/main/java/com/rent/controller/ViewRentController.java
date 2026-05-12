package com.rent.controller;

import com.rent.model.RentRow;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ViewRentController {

    @FXML private Label titleLabel;

    @FXML private Label flatNoLabel;
    @FXML private Label tenantNameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label billMonthLabel;

    @FXML private Label houseRentLabel;
    @FXML private Label electricityLabel;
    @FXML private Label waterLabel;
    @FXML private Label gasLabel;
    @FXML private Label otherBillsLabel;
    @FXML private Label fineLabel;
    @FXML private Label discountLabel;

    @FXML private Label totalLabel;
    @FXML private Label statusLabel;

    public void setRentRow(RentRow r) {
        titleLabel.setText("💰 Rent Details - " + r.getFlatNo() + " (" + r.getBillMonth() + ")");

        flatNoLabel.setText(nz(r.getFlatNo()));
        tenantNameLabel.setText(nz(r.getTenantName()));
        phoneLabel.setText(nz(r.getPhone()));
        billMonthLabel.setText(nz(r.getBillMonth()));

        houseRentLabel.setText(money(r.getHouseRent()));
        electricityLabel.setText(money(r.getElectricity()));
        waterLabel.setText(money(r.getWater()));
        gasLabel.setText(money(r.getGas()));
        otherBillsLabel.setText(money(r.getOtherBills()));
        fineLabel.setText(money(r.getFine()));
        discountLabel.setText(money(r.getDiscount()));

        totalLabel.setText(money(r.getTotal()));
        statusLabel.setText(nz(r.getStatus()));
    }

    private String money(double v) {
        return "৳ " + trimZero(v);
    }

    private String trimZero(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    @FXML
    private void close() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}