package com.rent.controller;

import com.rent.model.Tenant;
import com.rent.dao.TenantDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class EditTenantController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField flatNoField;
    @FXML private TextField rentField;
    @FXML private TextField addressField;
    @FXML private Label nidFileLabel;
    @FXML private Label docFileLabel;

    private File nidFile;
    private File docFile;
    private Tenant tenant; // current tenant

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;

        // Pre-fill fields
        nameField.setText(tenant.getName());
        phoneField.setText(tenant.getPhone());
        emailField.setText(tenant.getEmail());
        flatNoField.setText(tenant.getFlatNo());
        rentField.setText(String.valueOf(tenant.getRent()));
        addressField.setText(tenant.getAddress());

        // Optionally show previous file names
        nidFileLabel.setText(tenant.getNid() != null ? tenant.getNid() : "No file selected");
        docFileLabel.setText(tenant.getDocPath() != null ? tenant.getDocPath() : "No file selected");
    }

    @FXML
    public void saveTenant() {
        try {
            tenant.setName(nameField.getText());
            tenant.setPhone(phoneField.getText());
            tenant.setEmail(emailField.getText());
            tenant.setFlatNo(flatNoField.getText());
            tenant.setRent(Double.parseDouble(rentField.getText()));
            tenant.setAddress(addressField.getText());

            if (nidFile != null) tenant.setNid(nidFile.getAbsolutePath());
            if (docFile != null) tenant.setDocPath(docFile.getAbsolutePath());

            TenantDAO.updateTenant(tenant);

            // Close window
            Stage stage = (Stage) nameField.getScene().getWindow();
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
}