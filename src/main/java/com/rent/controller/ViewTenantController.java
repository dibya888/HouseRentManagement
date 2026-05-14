package com.rent.controller;

import com.rent.dao.FlatDAO;
import com.rent.model.Tenant;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;

import javafx.scene.layout.VBox;

import javafx.scene.control.Button;



public class ViewTenantController {

    @FXML private Label nameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label nidLabel;
    @FXML private Label flatLabel;
    @FXML private Label rentLabel;
    @FXML private Label meterLabel;
    @FXML private Label addressLabel;
    @FXML private Label nidPathLabel;
    @FXML private Label docPathLabel;
    @FXML private VBox documentsBox;
    @FXML private VBox nidBox;
    @FXML private VBox docBox;
    @FXML private Button openNidBtn;
    @FXML private Button openDocBtn;
    @FXML private Label statusLabel;
    @FXML private Label moveInDateLabel;
    @FXML private Label moveOutDateLabel;
    @FXML private Label moveOutReasonLabel;
    @FXML private Label securityDepositLabel;
    @FXML private Label securityDepositDateLabel;
    @FXML private Label securityDepositNoteLabel;
    @FXML private VBox moveOutInfoBox;

    private Tenant tenant;

    public void setTenant(Tenant t) {
        this.tenant = t;

        nameLabel.setText(t.getName());
        phoneLabel.setText(t.getPhone());
        emailLabel.setText(t.getEmail());
        nidLabel.setText(t.getNid());
        flatLabel.setText(t.getFlatNo());
        meterLabel.setText(
                FlatDAO.getMeterNoByFlatNo(t.getFlatNo())
        );
        rentLabel.setText("৳ " + t.getRent());
        addressLabel.setText(t.getAddress());
        statusLabel.setText(nullSafe(t.getStatus()));
        moveInDateLabel.setText(nullSafe(t.getMoveInDate()));
        moveOutDateLabel.setText(nullSafe(t.getMoveOutDate()));
        moveOutReasonLabel.setText(nullSafe(t.getMoveOutReason()));

        securityDepositLabel.setText("৳ " + String.format("%,.2f", t.getSecurityDeposit()));
        securityDepositDateLabel.setText(nullSafe(t.getSecurityDepositDate()));
        securityDepositNoteLabel.setText(nullSafe(t.getSecurityDepositNote()));
        boolean movedOut = "Moved Out".equalsIgnoreCase(t.getStatus());

        moveOutInfoBox.setVisible(movedOut);
        moveOutInfoBox.setManaged(movedOut);

        if (movedOut) {
            moveOutDateLabel.setText(nullSafe(t.getMoveOutDate()));
            moveOutReasonLabel.setText(nullSafe(t.getMoveOutReason()));
        }

        // --- NID handling ---
        if (t.getNidPath() == null || t.getNidPath().isBlank()) {
            nidBox.setManaged(false);
            nidBox.setVisible(false);
        } else {
            nidPathLabel.setText(t.getNidPath());
        }

        // --- Document handling ---
        if (t.getDocPath() == null || t.getDocPath().isBlank()) {
            docBox.setManaged(false);
            docBox.setVisible(false);
        } else {
            docPathLabel.setText(t.getDocPath());
        }

        // --- Hide entire documents card if none exist ---
        if ((t.getNidPath() == null || t.getNidPath().isBlank())
                && (t.getDocPath() == null || t.getDocPath().isBlank())) {

            documentsBox.setManaged(false);
            documentsBox.setVisible(false);
        }
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

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}