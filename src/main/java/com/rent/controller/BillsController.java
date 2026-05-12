package com.rent.controller;

import com.rent.util.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import com.rent.dao.RentDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BillsController {

    @FXML private TextField electricityField;
    @FXML private TextField gasField;
    @FXML private TextField waterField;

    @FXML
    public void initialize() {
        loadDefaults();
    }

    private void loadDefaults() {
        String sql = "SELECT electricity, gas, water FROM bill_defaults WHERE id=1";
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                electricityField.setText(String.valueOf(rs.getDouble("electricity")));
                gasField.setText(String.valueOf(rs.getDouble("gas")));
                waterField.setText(String.valueOf(rs.getDouble("water")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void save() {
        try {
            double electricity = parse(electricityField);
            double gas = parse(gasField);
            double water = parse(waterField);

            String sql = """
                UPDATE bill_defaults
                   SET electricity=?, gas=?, water=?
                 WHERE id=1
            """;

            try (Connection conn = DBUtil.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setDouble(1, electricity);
                ps.setDouble(2, gas);
                ps.setDouble(3, water);
                ps.executeUpdate();
            }

            RentDAO.applyGlobalDefaultsToUnpaidRows();
            close();

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Please enter valid numbers.").show();
        }
    }

    private double parse(TextField tf) {
        String s = tf.getText() == null ? "" : tf.getText().trim();
        if (s.isEmpty()) return 0;
        return Double.parseDouble(s);
    }

    @FXML
    private void close() {
        Stage stage = (Stage) electricityField.getScene().getWindow();
        stage.close();
    }
}