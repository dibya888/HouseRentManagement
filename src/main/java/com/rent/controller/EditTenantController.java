package com.rent.controller;

import com.rent.dao.TenantDAO;
import com.rent.model.Tenant;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class EditTenantController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField nidField;
    @FXML private TextField addressField;
    @FXML private TextField flatNoField;
    @FXML private TextField rentField;

    @FXML private CheckBox depositCheckBox;
    @FXML private TextField depositAmountField;
    @FXML private DatePicker depositDatePicker;
    @FXML private TextArea depositNoteArea;

    private Tenant tenant;

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;

        nameField.setText(tenant.getName());
        phoneField.setText(tenant.getPhone());
        emailField.setText(tenant.getEmail());
        nidField.setText(tenant.getNid());
        addressField.setText(tenant.getAddress());
        flatNoField.setText(tenant.getFlatNo());
        rentField.setText(String.valueOf(tenant.getRent()));

        setupDepositFields();

        double deposit = tenant.getSecurityDeposit();

        if (deposit > 0) {
            depositCheckBox.setSelected(true);
            depositAmountField.setText(String.valueOf(deposit));

            if (tenant.getSecurityDepositDate() != null
                    && !tenant.getSecurityDepositDate().isBlank()) {
                try {
                    depositDatePicker.setValue(LocalDate.parse(tenant.getSecurityDepositDate()));
                } catch (Exception e) {
                    depositDatePicker.setValue(null);
                }
            }

            depositNoteArea.setText(
                    tenant.getSecurityDepositNote() == null
                            ? ""
                            : tenant.getSecurityDepositNote()
            );

        } else {
            depositCheckBox.setSelected(false);
            depositAmountField.clear();
            depositDatePicker.setValue(null);
            depositNoteArea.clear();
        }
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

    @FXML
    private void updateTenant() {
        if (tenant == null) {
            showError("Tenant data missing.");
            return;
        }

        String name = text(nameField);
        String phone = text(phoneField);

        if (name.isBlank() || phone.isBlank()) {
            showWarning("Name and phone are required.");
            return;
        }

        double rent;

        try {
            rent = Double.parseDouble(text(rentField));
        } catch (Exception e) {
            showWarning("Please enter a valid rent amount.");
            return;
        }

        boolean depositTaken = depositCheckBox != null && depositCheckBox.isSelected();

        double depositAmount = 0;
        String depositDate = null;
        String depositNote = null;

        if (depositTaken) {
            try {
                depositAmount = Double.parseDouble(text(depositAmountField));
            } catch (Exception e) {
                showWarning("Please enter a valid security deposit amount.");
                return;
            }

            if (depositAmount <= 0) {
                showWarning("Security deposit amount must be greater than 0.");
                return;
            }

            if (depositDatePicker.getValue() == null) {
                showWarning("Please select security deposit date.");
                return;
            }

            depositDate = depositDatePicker.getValue().toString();

            depositNote = depositNoteArea.getText() == null
                    ? null
                    : depositNoteArea.getText().trim();
        }

        tenant.setName(name);
        tenant.setPhone(phone);
        tenant.setEmail(text(emailField));
        tenant.setNid(text(nidField));
        tenant.setAddress(text(addressField));
        tenant.setFlatNo(text(flatNoField));
        tenant.setRent(rent);

        tenant.setSecurityDeposit(depositAmount);
        tenant.setSecurityDepositDate(depositDate);
        tenant.setSecurityDepositNote(depositNote);

        TenantDAO.updateTenant(tenant);

        close();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}