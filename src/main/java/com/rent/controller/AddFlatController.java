package com.rent.controller;

import com.rent.dao.FlatDAO;
import com.rent.model.Flat;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddFlatController {

    @FXML private TextField flatNoField;
    @FXML private TextField rentField;

    @FXML private Spinner<Integer> bedroomSpinner;
    @FXML private Spinner<Integer> bathroomSpinner;
    @FXML private Spinner<Integer> kitchenSpinner;
    @FXML private Spinner<Integer> balconySpinner;
    @FXML private Spinner<Integer> diningSpinner;
    @FXML private Spinner<Integer> livingSpinner;

    @FXML
    public void initialize() {
        bedroomSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 1));
        bathroomSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 1));
        kitchenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 1));
        balconySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        diningSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0));
        livingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 1));
    }

    @FXML
    private void handleSave() {

        if (flatNoField.getText().isBlank() || rentField.getText().isBlank()) {
            System.out.println("Flat No and Rent are required!");
            return;
        }

        Flat flat = new Flat(
                flatNoField.getText().trim(),
                bedroomSpinner.getValue(),
                bathroomSpinner.getValue(),
                kitchenSpinner.getValue(),
                balconySpinner.getValue(),
                diningSpinner.getValue(),
                livingSpinner.getValue(),
                Double.parseDouble(rentField.getText()),
                "Available"
        );

        boolean success = FlatDAO.saveFlat(flat);

        if (success) {
            System.out.println("✅ Flat saved successfully.");
            close();
        } else {
            System.out.println("❌ Failed to save flat.");
        }
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