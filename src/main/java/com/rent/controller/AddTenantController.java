package com.rent.controller;

import com.rent.util.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javafx.scene.control.ComboBox;
import com.rent.dao.FlatDAO;

public class AddTenantController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField nidField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> flatComboBox;
    @FXML private TextField rentField;

    @FXML private Label nidFileLabel;
    @FXML private Label docFileLabel;

    private File nidFile;
    private File docFile;

    @FXML
    public void saveTenant() {
        String selectedFlat = flatComboBox.getValue();

        if (selectedFlat == null) {
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "Please select a flat"
            ).show();
            return;
        }

        String sql = """
                INSERT INTO tenants
                (name, phone, email, nid, address, flat_no, rent, nid_path, doc_path)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nameField.getText());

            stmt.setString(2, phoneField.getText());

            stmt.setString(3, emailField.getText());

            stmt.setString(4, nidField.getText());

            stmt.setString(5, addressField.getText());

            stmt.setString(6, selectedFlat);

            stmt.setDouble(7,
                    Double.parseDouble(rentField.getText()));

            stmt.setString(8,
                    nidFile != null
                            ? nidFile.getAbsolutePath()
                            : null);

            stmt.setString(9,
                    docFile != null
                            ? docFile.getAbsolutePath()
                            : null);

            stmt.executeUpdate();
            FlatDAO.markFlatOccupied(selectedFlat);

            Stage stage =
                    (Stage) nameField.getScene().getWindow();

            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void uploadNidFile() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select NID File");

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {

            nidFile = file;

            nidFileLabel.setText(file.getName());
        }
    }

    @FXML
    public void uploadDocFile() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select Document");

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {

            docFile = file;

            docFileLabel.setText(file.getName());
        }
    }

    @FXML
    public void initialize() {
        flatComboBox.getItems().setAll(
                FlatDAO.getAvailableFlatNumbers()
        );
    }
}