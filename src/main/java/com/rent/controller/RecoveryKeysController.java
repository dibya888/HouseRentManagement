package com.rent.controller;

import com.rent.dao.EmergencyKeyDAO;
import com.rent.util.EmergencyKeyUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class RecoveryKeysController {

    @FXML private Label remainingKeysLabel;
    @FXML private TextArea keysArea;

    private List<String> currentGeneratedKeys = new ArrayList<>();

    @FXML
    public void initialize() {
        updateRemainingKeysLabel();
    }

    @FXML
    private void generateKeys() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Generate Emergency Keys");
        confirm.setHeaderText("Generate new emergency keys?");
        confirm.setContentText("""
                This will replace existing unused emergency keys.
                
                Old unused keys will no longer work.
                Continue?
                """);

        if (confirm.showAndWait().isEmpty()
                || confirm.getResult().getButtonData().isCancelButton()) {
            return;
        }

        currentGeneratedKeys = EmergencyKeyUtil.generatePlainKeys(10);

        EmergencyKeyDAO.replaceKeys(currentGeneratedKeys);

        keysArea.setText(formatKeys(currentGeneratedKeys));
        updateRemainingKeysLabel();

        new Alert(Alert.AlertType.INFORMATION,
                "10 emergency recovery keys generated. Save or print them now.")
                .showAndWait();
    }

    @FXML
    private void saveAsPdf() {
        if (currentGeneratedKeys == null || currentGeneratedKeys.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "Generate keys first. Keys are shown only once.")
                    .showAndWait();
            return;
        }

        EmergencyKeyUtil.saveKeysAsPdf(
                keysArea.getScene().getWindow(),
                currentGeneratedKeys
        );
    }

    private String formatKeys(List<String> keys) {
        StringBuilder sb = new StringBuilder();

        int index = 1;

        for (String key : keys) {
            sb.append(index)
                    .append(". ")
                    .append(key)
                    .append("\n");

            index++;
        }

        return sb.toString();
    }

    private void updateRemainingKeysLabel() {
        int remaining = EmergencyKeyDAO.countUnusedKeys();
        remainingKeysLabel.setText("Unused keys: " + remaining);
    }

    @FXML
    private void close() {
        Stage stage = (Stage) keysArea
                .getScene()
                .getWindow();

        stage.close();
    }
}