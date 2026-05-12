package com.rent.controller;

import com.rent.util.DBUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditFlatController {

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

    @FXML private ComboBox<String> statusBox;

    private String flatNo; // store key only

    @FXML
    public void initialize() {
        bedroomSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 1));
        bathroomSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 1));
        kitchenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 1));
        balconySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        diningSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0));
        livingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 1));

        statusBox.setItems(FXCollections.observableArrayList("Available", "Occupied"));

        loadProperties();
    }

    // called from FlatController after loading fxml
    public void setFlat(com.rent.model.Flat f) {
        this.flatNo = f.getFlatNo();

        flatNoField.setText(f.getFlatNo());
        meterNoField.setText(f.getMeterNo());
        rentField.setText(String.valueOf(f.getRent()));

        bedroomSpinner.getValueFactory().setValue(f.getBedrooms());
        bathroomSpinner.getValueFactory().setValue(f.getBathrooms());
        kitchenSpinner.getValueFactory().setValue(f.getKitchens());
        balconySpinner.getValueFactory().setValue(f.getBalconies());
        diningSpinner.getValueFactory().setValue(f.getDiningrooms());
        livingSpinner.getValueFactory().setValue(f.getLivingrooms());

        statusBox.setValue(f.getStatus());

        selectPropertyForFlat(f.getFlatNo());
    }

    private void loadProperties() {
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM properties ORDER BY name");
             ResultSet rs = ps.executeQuery()) {

            var list = FXCollections.<PropertyOption>observableArrayList();
            while (rs.next()) {
                list.add(new PropertyOption(rs.getInt("id"), rs.getString("name")));
            }
            propertyComboBox.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectPropertyForFlat(String flatNo) {
        String sql = "SELECT property_id FROM flats WHERE flat_no=?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flatNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int pid = rs.getInt("property_id");
                    for (PropertyOption p : propertyComboBox.getItems()) {
                        if (p.id == pid) {
                            propertyComboBox.getSelectionModel().select(p);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {

        if (flatNo == null || flatNo.isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Flat reference missing.").show();
            return;
        }

        if (propertyComboBox.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a Property.").show();
            return;
        }

        double rent;
        try {
            rent = Double.parseDouble(rentField.getText().trim());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Invalid rent.").show();
            return;
        }

        String meterNo = meterNoField.getText() == null ? "" : meterNoField.getText().trim();
        if (meterNo.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Meter No is required.").show();
            return;
        }

        String status = statusBox.getValue();
        if (status == null || status.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Please select Status.").show();
            return;
        }

        int beds = bedroomSpinner.getValue();
        int baths = bathroomSpinner.getValue();
        int kitchens = kitchenSpinner.getValue();
        int balconies = balconySpinner.getValue();
        int dining = diningSpinner.getValue();
        int living = livingSpinner.getValue();

        int propertyId = propertyComboBox.getValue().id;

        // ✅ Single DB update (flat fields + property_id)
        String sql = """
            UPDATE flats SET
              meter_no=?,
              bedrooms=?,
              bathrooms=?,
              kitchens=?,
              balconies=?,
              dining_rooms=?,
              living_rooms=?,
              rent=?,
              status=?,
              property_id=?
            WHERE flat_no=?
        """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, meterNo);
            ps.setInt(2, beds);
            ps.setInt(3, baths);
            ps.setInt(4, kitchens);
            ps.setInt(5, balconies);
            ps.setInt(6, dining);
            ps.setInt(7, living);
            ps.setDouble(8, rent);
            ps.setString(9, status);
            ps.setInt(10, propertyId);
            ps.setString(11, flatNo);

            int updated = ps.executeUpdate();
            if (updated <= 0) {
                new Alert(Alert.AlertType.ERROR, "Failed to update flat.").show();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to update flat.").show();
            return;
        }

        close();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) flatNoField.getScene().getWindow();
        stage.close();
    }

    private static class PropertyOption {
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