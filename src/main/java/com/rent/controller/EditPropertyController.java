package com.rent.controller;

import com.rent.util.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditPropertyController {

    private int propertyId = -1;

    @FXML private TextField nameField;
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;
    @FXML private CheckBox defaultCheck;

    public void loadProperty(int id) {
        this.propertyId = id;

        String sql = "SELECT name, address, phone, is_default FROM properties WHERE id=?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nameField.setText(rs.getString("name"));
                    addressField.setText(rs.getString("address"));
                    phoneField.setText(rs.getString("phone") == null ? "" : rs.getString("phone"));
                    defaultCheck.setSelected(rs.getInt("is_default") == 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateProperty() {
        if (propertyId <= 0) return;

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String address = addressField.getText() == null ? "" : addressField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        int makeDefault = defaultCheck.isSelected() ? 1 : 0;

        if (name.isBlank() || address.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Property Name and Address are required.").show();
            return;
        }

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            if (makeDefault == 1) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE properties SET is_default=0")) {
                    ps.executeUpdate();
                }
            }

            int rows;

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE properties SET name=?, address=?, phone=?, is_default=? WHERE id=?"
            )) {
                ps.setString(1, name);
                ps.setString(2, address);
                ps.setString(3, phone.isBlank() ? null : phone);
                ps.setInt(4, makeDefault);
                ps.setInt(5, propertyId);
                rows = ps.executeUpdate();
            }

            conn.commit();

            if (rows > 0) {
                AuditLogDAO.log(
                        AuditActions.PROPERTY_UPDATED,
                        "Property updated. ID: "
                                + propertyId
                                + ", Name: "
                                + name
                                + ", Phone: "
                                + (phone.isBlank() ? "-" : phone)
                                + ", Default: "
                                + (makeDefault == 1 ? "Yes" : "No")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to update property.").show();
            return;
        }

        close();
    }

    @FXML
    private void close() {
        Stage st = (Stage) nameField.getScene().getWindow();
        st.close();
    }
}