package com.rent.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.awt.Desktop;
import java.io.File;
import java.util.Optional;

/**
 * Shared helper for "file saved successfully" alerts across the app
 * (report exports, receipts, settlements, recovery keys PDF). Adds an
 * "Open File" button alongside OK so the user can open the generated
 * file immediately instead of navigating to it manually.
 */
public final class FileOpenUtil {

    private FileOpenUtil() {
    }

    /**
     * Shows an information alert with the given message plus an
     * "Open File" button. If clicked, opens the file with the OS's
     * default associated application.
     */
    public static void showSavedAlertWithOpen(String message, File file) {
        ButtonType openButton = new ButtonType("Open File", ButtonBar.ButtonData.LEFT);
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, openButton, okButton);
        alert.setTitle("Saved Successfully");
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == openButton) {
            openFile(file);
        }
    }

    private static void openFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                showOpenFailedAlert(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showOpenFailedAlert(file);
        }
    }

    private static void showOpenFailedAlert(File file) {
        Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,
                "Could not open the file automatically.\nYou can find it at:\n" + file.getAbsolutePath())
                .showAndWait());
    }
}
