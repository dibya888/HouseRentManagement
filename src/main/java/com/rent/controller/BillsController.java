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
    @FXML private TextField dueDayField;

    @FXML
    public void initialize() {
        loadDefaults();
    }

    private void loadDefaults() {
        String sql = "SELECT electricity, gas, water, due_day FROM bill_defaults WHERE id=1";
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                electricityField.setText(String.valueOf(rs.getDouble("electricity")));
                gasField.setText(String.valueOf(rs.getDouble("gas")));
                waterField.setText(String.valueOf(rs.getDouble("water")));

                int dueDay = rs.getInt("due_day");
                dueDayField.setText(String.valueOf(dueDay > 0 ? dueDay : 5));
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
            int dueDay = parseDueDay(dueDayField);

            String sql = """
                UPDATE bill_defaults
                   SET electricity=?, gas=?, water=?, due_day=?
                 WHERE id=1
            """;

            try (Connection conn = DBUtil.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setDouble(1, electricity);
                ps.setDouble(2, gas);
                ps.setDouble(3, water);
                ps.setInt(4, dueDay);
                ps.executeUpdate();
            }

            RentDAO.applyGlobalDefaultsToUnpaidRows();
            close();

        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Please enter valid numbers.").show();
        } catch (IllegalArgumentException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Please enter valid numbers.").show();
        }
    }

    /**
     * Validates the due day is between 1 and 28, so it's always a safe
     * day-of-month regardless of which month it's later applied to
     * (months with 28-30 days, and February in non-leap years).
     */
    private int parseDueDay(TextField tf) {
        String s = tf.getText() == null ? "" : tf.getText().trim();

        int day = s.isEmpty() ? 5 : Integer.parseInt(s);

        if (day < 1 || day > 28) {
            throw new IllegalArgumentException("Rent Due Day must be between 1 and 28.");
        }

        return day;
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