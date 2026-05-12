package com.rent.controller;

import com.rent.dao.FlatDAO;
import com.rent.model.Flat;
import com.rent.util.DBUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddFlatController {

    @FXML private ComboBox<PropertyOption> propertyComboBox;

    @FXML private TextField flatNoField;
    @FXML private TextField meterNoField;
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

        loadProperties();
    }

    private void loadProperties() {
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, name, is_default FROM properties ORDER BY is_default DESC, id DESC"
             );
             ResultSet rs = ps.executeQuery()) {

            var items = FXCollections.<PropertyOption>observableArrayList();
            PropertyOption defaultOne = null;

            while (rs.next()) {
                PropertyOption p = new PropertyOption(rs.getInt("id"), rs.getString("name"));
                items.add(p);
                if (rs.getInt("is_default") == 1 && defaultOne == null) {
                    defaultOne = p;
                }
            }

            propertyComboBox.setItems(items);

            if (defaultOne != null) {
                propertyComboBox.getSelectionModel().select(defaultOne);
            } else if (!items.isEmpty()) {
                propertyComboBox.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {

        if (propertyComboBox.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a Property first.").show();
            return;
        }

        if (flatNoField.getText().isBlank()
                || meterNoField.getText().isBlank()
                || rentField.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Flat No, Meter No and Rent are required!").show();
            return;
        }

        double rent;
        try {
            rent = Double.parseDouble(rentField.getText().trim());
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Invalid rent value!").show();
            return;
        }

        Flat flat = new Flat(
                flatNoField.getText().trim(),
                meterNoField.getText().trim(),
                bedroomSpinner.getValue(),
                bathroomSpinner.getValue(),
                kitchenSpinner.getValue(),
                balconySpinner.getValue(),
                diningSpinner.getValue(),
                livingSpinner.getValue(),
                rent,
                "Available"
        );

        int propertyId = propertyComboBox.getValue().id;

        boolean success = FlatDAO.saveFlatWithProperty(flat, propertyId);

        if (success) {
            close();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to save flat.").show();
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

    // ComboBox item
    public static class PropertyOption {
        final int id;
        final String name;

        PropertyOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
