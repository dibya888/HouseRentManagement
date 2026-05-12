package com.rent.controller;

import com.rent.model.Flat;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ViewFlatController {

    @FXML private Label titleLabel;
    @FXML private Label meterLabel;
    @FXML private Label bedLabel;
    @FXML private Label bathLabel;
    @FXML private Label kitchenLabel;
    @FXML private Label balconyLabel;
    @FXML private Label diningLabel;
    @FXML private Label livingLabel;
    @FXML private Label rentLabel;
    @FXML private Label statusLabel;

    public void setFlat(Flat f) {
        titleLabel.setText("🏢 Flat " + f.getFlatNo());

        meterLabel.setText(f.getMeterNo());
        bedLabel.setText(String.valueOf(f.getBedrooms()));
        bathLabel.setText(String.valueOf(f.getBathrooms()));
        kitchenLabel.setText(String.valueOf(f.getKitchens()));
        balconyLabel.setText(String.valueOf(f.getBalconies()));
        diningLabel.setText(String.valueOf(f.getDiningrooms()));
        livingLabel.setText(String.valueOf(f.getLivingrooms()));
        rentLabel.setText("৳ " + f.getRent());
        statusLabel.setText(f.getStatus());
    }

    @FXML
    private void close() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
