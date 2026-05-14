package com.rent.controller;

import com.rent.util.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import java.time.LocalDate;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
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
    @FXML private CheckBox depositCheckBox;
    @FXML private TextField depositAmountField;
    @FXML private DatePicker depositDatePicker;
    @FXML private TextArea depositNoteArea;

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

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String nid = nidField.getText() == null ? "" : nidField.getText().trim();
        String address = addressField.getText() == null ? "" : addressField.getText().trim();

        if (name.isBlank() || phone.isBlank()) {
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "Name and phone are required."
            ).show();
            return;
        }
        if (!validateTenantIdentity(phone, email, nid)) {
            return;
        }

        boolean depositTaken = depositCheckBox != null && depositCheckBox.isSelected();

        double depositAmount = 0;
        String depositDate = null;
        String depositNote = null;

        if (depositTaken) {
            try {
                depositAmount = Double.parseDouble(depositAmountField.getText().trim());
            } catch (Exception e) {
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING,
                        "Please enter a valid security deposit amount."
                ).show();
                return;
            }

            if (depositAmount <= 0) {
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING,
                        "Security deposit amount must be greater than 0."
                ).show();
                return;
            }

            if (depositDatePicker.getValue() == null) {
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING,
                        "Please select security deposit date."
                ).show();
                return;
            }

            depositDate = depositDatePicker.getValue().toString();

            depositNote = depositNoteArea.getText() == null
                    ? null
                    : depositNoteArea.getText().trim();
        }

        String insertTenantSql = """
            INSERT INTO tenants
            (name, phone, email, nid, address, flat_no, rent,
             nid_path, doc_path, status, move_in_date,
             move_out_date, move_out_reason,
             security_deposit, security_deposit_date, security_deposit_note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active', ?, NULL, NULL, ?, ?, ?)
            """;

        String occupyFlatSql = """
            UPDATE flats
            SET status = 'Occupied'
            WHERE flat_no = ?
            """;

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            double rent = FlatDAO.getRentByFlatNo(selectedFlat);

            int rows;

            try (PreparedStatement stmt = conn.prepareStatement(insertTenantSql)) {
                stmt.setString(1, name);
                stmt.setString(2, phone);
                stmt.setString(3, email);
                stmt.setString(4, nid);
                stmt.setString(5, address);
                stmt.setString(6, selectedFlat);
                stmt.setDouble(7, rent);

                stmt.setString(8,
                        nidFile != null
                                ? nidFile.getAbsolutePath()
                                : null);

                stmt.setString(9,
                        docFile != null
                                ? docFile.getAbsolutePath()
                                : null);

                stmt.setString(10, LocalDate.now().toString());

                stmt.setDouble(11, depositAmount);
                stmt.setString(12, depositDate);
                stmt.setString(13, depositNote);

                rows = stmt.executeUpdate();
            }

            if (rows > 0) {
                try (PreparedStatement ps = conn.prepareStatement(occupyFlatSql)) {
                    ps.setString(1, selectedFlat);
                    ps.executeUpdate();
                }
            }

            conn.commit();

            if (rows > 0) {
                AuditLogDAO.log(
                        AuditActions.TENANT_ADDED,
                        "Tenant added. Name: "
                                + name
                                + ", Phone: "
                                + phone
                                + ", Flat: "
                                + selectedFlat
                                + ", Rent: "
                                + rent
                                + ", Security Deposit: "
                                + depositAmount
                );
            }

            Stage stage =
                    (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();

            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Failed to save tenant."
            ).show();
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

        flatComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double rent = FlatDAO.getRentByFlatNo(newVal);
                rentField.setText(String.valueOf(rent));
            } else {
                rentField.clear();
            }
        });

        setupDepositFields();
    }

    private void setupDepositFields() {
        if (depositCheckBox == null) {
            return;
        }

        depositAmountField.setDisable(true);
        depositDatePicker.setDisable(true);
        depositNoteArea.setDisable(true);

        depositCheckBox.selectedProperty().addListener((obs, oldVal, selected) -> {
            depositAmountField.setDisable(!selected);
            depositDatePicker.setDisable(!selected);
            depositNoteArea.setDisable(!selected);

            if (selected) {
                if (depositDatePicker.getValue() == null) {
                    depositDatePicker.setValue(LocalDate.now());
                }
            } else {
                depositAmountField.clear();
                depositDatePicker.setValue(null);
                depositNoteArea.clear();
            }
        });
    }

    private boolean validateTenantIdentity(String phone, String email, String nid) {

        if (!isValidBdMobile(phone)) {
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "Please enter a valid Bangladeshi mobile number.\nExample: 01XXXXXXXXX"
            ).show();
            return false;
        }

        if (email != null && !email.isBlank() && !isValidEmail(email)) {
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "Please enter a valid email address."
            ).show();
            return false;
        }

        if (nid != null && !nid.isBlank() && !isValidNid(nid)) {
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "NID must be exactly 10 or 17 digits."
            ).show();
            return false;
        }

        return true;
    }

    private boolean isValidBdMobile(String phone) {
        if (phone == null) return false;
        return phone.trim().matches("^01[0-9]{9}$");
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return true;
        return email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidNid(String nid) {
        if (nid == null || nid.isBlank()) return true;
        return nid.trim().matches("^(\\d{10}|\\d{17})$");
    }
}