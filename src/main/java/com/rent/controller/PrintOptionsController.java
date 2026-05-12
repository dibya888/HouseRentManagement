package com.rent.controller;

import com.rent.model.RentRow;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

public class PrintOptionsController {

    @FXML private CheckBox propertyNameCheck;
    @FXML private CheckBox propertyAddressCheck;

    private RentRow row;

    public void setRentRow(RentRow row) {
        this.row = row;
    }

    @FXML
    public void initialize() {
        propertyNameCheck.setSelected(true);
        propertyAddressCheck.setSelected(true);
    }

    @FXML
    private void print() {
        boolean includePropertyName = propertyNameCheck.isSelected();
        boolean includePropertyAddress = propertyAddressCheck.isSelected();

        ReceiptPrinter.printReceipt(
                row,
                includePropertyName,
                includePropertyAddress
        );

        close();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) propertyNameCheck.getScene().getWindow();
        stage.close();
    }
}