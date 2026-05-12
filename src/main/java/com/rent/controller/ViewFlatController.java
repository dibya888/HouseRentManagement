package com.rent.controller;

import com.rent.model.Flat;
import com.rent.util.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewFlatController {

    @FXML private Label titleLabel;

    // ✅ NEW (must exist in updated view-flat.fxml)
    @FXML private Label propertyNameLabel;
    @FXML private Label propertyAddressLabel;

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

        // ✅ Load property info by flat_no
        loadPropertyInfo(f.getFlatNo());

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

    private void loadPropertyInfo(String flatNo) {

        // default fallback if not linked
        propertyNameLabel.setText("-");
        propertyAddressLabel.setText("-");

        String sql = """
            SELECT p.name, p.address
            FROM flats f
            JOIN properties p ON f.property_id = p.id
            WHERE f.flat_no = ?
        """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flatNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    propertyNameLabel.setText(rs.getString("name"));
                    propertyAddressLabel.setText(rs.getString("address"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
