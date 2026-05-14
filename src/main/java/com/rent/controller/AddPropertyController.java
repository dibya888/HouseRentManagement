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

public class AddPropertyController {

    @FXML private TextField nameField;
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;
    @FXML private CheckBox defaultCheck;

    @FXML
    private void saveProperty() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String address = addressField.getText() == null ? "" : addressField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();

        if (name.isBlank() || address.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Property Name and Address are required.").show();
            return;
        }

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            boolean hasAny;
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM properties LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                hasAny = rs.next();
            }

            int makeDefault = (defaultCheck.isSelected() || !hasAny) ? 1 : 0;

            if (makeDefault == 1) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE properties SET is_default=0")) {
                    ps.executeUpdate();
                }
            }

            int rows;

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO properties(name, address, phone, is_default) VALUES(?,?,?,?)"
            )) {
                ps.setString(1, name);
                ps.setString(2, address);
                ps.setString(3, phone.isBlank() ? null : phone);
                ps.setInt(4, makeDefault);
                rows = ps.executeUpdate();
            }

            conn.commit();

            if (rows > 0) {
                AuditLogDAO.log(
                        AuditActions.PROPERTY_ADDED,
                        "Property added. Name: "
                                + name
                                + ", Phone: "
                                + (phone.isBlank() ? "-" : phone)
                                + ", Default: "
                                + (makeDefault == 1 ? "Yes" : "No")
                );
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to save property.").show();
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