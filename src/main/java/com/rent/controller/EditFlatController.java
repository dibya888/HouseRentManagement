package com.rent.controller;

import com.rent.dao.FlatDAO;
import com.rent.model.Flat;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

public class EditFlatController {

    @FXML private TextField flatNoField;
    @FXML private TextField meterNoField;
    @FXML private TextField rentField;

    @FXML private Spinner<Integer> bedroomSpinner;
    @FXML private Spinner<Integer> bathroomSpinner;
    @FXML private Spinner<Integer> kitchenSpinner;
    @FXML private Spinner<Integer> balconySpinner;
    @FXML private Spinner<Integer> diningSpinner;
    @FXML private Spinner<Integer> livingSpinner;

    @FXML private ComboBox<String> statusBox;

    private Flat flat;

    @FXML
    public void initialize() {
        bedroomSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 1));
        bathroomSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 1));
        kitchenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 1));
        balconySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        diningSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0));
        livingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 1));

        statusBox.getItems().addAll("Available", "Occupied");
    }

    public void setFlat(Flat f) {
        this.flat = f;

        flatNoField.setText(f.getFlatNo());
        meterNoField.setText(f.getMeterNo());
        bedroomSpinner.getValueFactory().setValue(f.getBedrooms());
        bathroomSpinner.getValueFactory().setValue(f.getBathrooms());
        kitchenSpinner.getValueFactory().setValue(f.getKitchens());
        balconySpinner.getValueFactory().setValue(f.getBalconies());
        diningSpinner.getValueFactory().setValue(f.getDiningrooms());
        livingSpinner.getValueFactory().setValue(f.getLivingrooms());
        rentField.setText(String.valueOf(f.getRent()));
        statusBox.setValue(f.getStatus());
    }

    @FXML
    private void handleUpdate() {
        Flat updated = new Flat(
                flatNoField.getText(),
                meterNoField.getText(),
                bedroomSpinner.getValue(),
                bathroomSpinner.getValue(),
                kitchenSpinner.getValue(),
                balconySpinner.getValue(),
                diningSpinner.getValue(),
                livingSpinner.getValue(),
                Double.parseDouble(rentField.getText()),
                statusBox.getValue()
        );

        boolean ok = FlatDAO.updateFlat(updated);
        if (ok) close();
        else new Alert(Alert.AlertType.ERROR, "Update failed!").showAndWait();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) flatNoField.getScene().getWindow();
        stage.close();
    }
}