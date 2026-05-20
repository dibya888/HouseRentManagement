package com.rent.util;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public final class PortableBackupPasswordDialog {

    private PortableBackupPasswordDialog() {
    }

    public static Optional<String> askBackupPasswordForCreate() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Backup Password");
        dialog.setHeaderText("Set a password for this portable backup.");

        ButtonType createButton = new ButtonType("Create Backup");
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Backup password");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm backup password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));

        grid.add(new Label("Password:"), 0, 0);
        grid.add(passwordField, 1, 0);

        grid.add(new Label("Confirm:"), 0, 1);
        grid.add(confirmField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != createButton) {
                return null;
            }

            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Backup password is required.");
            }

            if (password.length() < 4) {
                throw new IllegalArgumentException("Backup password must be at least 4 characters.");
            }

            if (!password.equals(confirm)) {
                throw new IllegalArgumentException("Backup passwords do not match.");
            }

            return password;
        });

        try {
            return dialog.showAndWait();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static Optional<String> askBackupPasswordForRestore() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Backup Password");
        dialog.setHeaderText("Enter the password used when this backup was created.");

        ButtonType restoreButton = new ButtonType("Continue");
        dialog.getDialogPane().getButtonTypes().addAll(restoreButton, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Backup password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));

        grid.add(new Label("Backup Password:"), 0, 0);
        grid.add(passwordField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != restoreButton) {
                return null;
            }

            String password = passwordField.getText();

            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Backup password is required.");
            }

            return password;
        });

        try {
            return dialog.showAndWait();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static Optional<String> askCurrentAccountPassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Account Password");
        dialog.setHeaderText("Enter your current account password to complete restore.");

        ButtonType continueButton = new ButtonType("Restore");
        dialog.getDialogPane().getButtonTypes().addAll(continueButton, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Current account password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));

        grid.add(new Label("Account Password:"), 0, 0);
        grid.add(passwordField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != continueButton) {
                return null;
            }

            String password = passwordField.getText();

            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Current account password is required.");
            }

            return password;
        });

        try {
            return dialog.showAndWait();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}