package com.rent.controller;

import com.rent.dao.TenantDAO;
import com.rent.model.Tenant;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditTenantController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField nidField;
    @FXML private TextField addressField;
    @FXML private TextField flatNoField;
    @FXML private TextField rentField;

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
    }

    @FXML
    private void updateTenant() {
        tenant.setName(nameField.getText());
        tenant.setPhone(phoneField.getText());
        tenant.setEmail(emailField.getText());
        tenant.setNid(nidField.getText());
        tenant.setAddress(addressField.getText());
        tenant.setFlatNo(flatNoField.getText());
        tenant.setRent(Double.parseDouble(rentField.getText()));

        TenantDAO.updateTenant(tenant);

        close();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}