package com.rent.controller;

import com.rent.model.Tenant;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;

public class ViewTenantController {

    @FXML private Label nameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label nidLabel;
    @FXML private Label flatLabel;
    @FXML private Label rentLabel;
    @FXML private Label addressLabel;
    @FXML private Label nidPathLabel;
    @FXML private Label docPathLabel;

    private Tenant tenant;

    public void setTenant(Tenant t) {
        this.tenant = t;

        nameLabel.setText(t.getName());
        phoneLabel.setText(t.getPhone());
        emailLabel.setText(t.getEmail());
        nidLabel.setText(t.getNid());
        flatLabel.setText(t.getFlatNo());
        rentLabel.setText("৳ " + t.getRent());
        addressLabel.setText(t.getAddress());

        nidPathLabel.setText(t.getNidPath());
        docPathLabel.setText(t.getDocPath());
    }

    @FXML
    private void openNidFile() {
        openFile(tenant.getNidPath());
    }

    @FXML
    private void openDocFile() {
        openFile(tenant.getDocPath());
    }

    private void openFile(String path) {
        try {
            if (path != null && !path.isBlank()) {
                Desktop.getDesktop().open(new File(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}