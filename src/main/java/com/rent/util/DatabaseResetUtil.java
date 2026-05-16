package com.rent.util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

import com.rent.dao.AuditLogDAO;
import com.rent.dao.UserSecurityDAO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class DatabaseResetUtil {

    private static final String CONFIRM_TEXT = "RESET";

    public static boolean confirmFactoryReset(Window ownerWindow) {
        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.initOwner(ownerWindow);
        warning.setTitle("Factory Reset");
        warning.setHeaderText("This will permanently delete all app data.");
        warning.setContentText("""
                Factory reset will delete:
                - tenants
                - flats
                - rent records
                - archive records
                - receipts
                - repairs
                - reports data
                - audit logs
                - users and recovery data

                A fresh database will be created with default login:
                Username: admin
                Password: 1234

                Continue?
                """);

        Optional<javafx.scene.control.ButtonType> warningResult =
                warning.showAndWait();

        if (warningResult.isEmpty()
                || warningResult.get() != javafx.scene.control.ButtonType.OK) {
            return false;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(ownerWindow);
        dialog.setTitle("Confirm Factory Reset");
        dialog.setHeaderText("Type RESET to confirm factory reset");
        dialog.setContentText("Confirmation:");

        Optional<String> result = dialog.showAndWait();

        return result.isPresent()
                && CONFIRM_TEXT.equals(result.get().trim());
    }

    public static boolean factoryReset() {
        try {
            Path dbPath = DBUtil.getDatabasePath();

            Files.deleteIfExists(dbPath);

            DBUtil.init();

            UserSecurityDAO.migratePlainPasswordsIfNeeded();

            AuditLogDAO.log(
                    AuditActions.FACTORY_RESET,
                    "Factory reset completed. Fresh database recreated."
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Factory reset failed."
            ).showAndWait();

            return false;
        }
    }
}