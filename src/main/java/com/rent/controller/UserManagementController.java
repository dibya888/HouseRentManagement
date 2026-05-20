package com.rent.controller;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;
import com.rent.util.CurrentSession;
import com.rent.util.UserCreationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;

public class UserManagementController {

    @FXML
    private TableView<UserAccount> usersTable;

    @FXML
    private TableColumn<UserAccount, String> usernameColumn;

    @FXML
    private TableColumn<UserAccount, String> displayNameColumn;

    @FXML
    private TableColumn<UserAccount, String> roleColumn;

    @FXML
    private TableColumn<UserAccount, String> statusColumn;

    @FXML
    private TableColumn<UserAccount, String> createdAtColumn;

    @FXML
    private TableColumn<UserAccount, String> lastLoginColumn;

    @FXML
    private Button addUserButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    @FXML
    public void initialize() {
        if (!CurrentSession.isAdmin()) {
            showError("Only Admin can manage users.");
            closeWindow();
            return;
        }

        setupTable();
        loadUsers();
    }

    private void setupTable() {
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        usernameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullSafe(data.getValue().getUsername()))
        );

        displayNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullSafe(data.getValue().getDisplayName()))
        );

        roleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullSafe(data.getValue().getRole()))
        );

        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullSafe(data.getValue().getStatus()))
        );

        createdAtColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullSafe(data.getValue().getCreatedAt()))
        );

        lastLoginColumn.setCellValueFactory(data ->
                new SimpleStringProperty(nullSafe(data.getValue().getLastLoginAt()))
        );
    }

    private void loadUsers() {
        List<UserAccount> users = UserAccountDAO.getAllUsers();
        usersTable.setItems(FXCollections.observableArrayList(users));
    }

    @FXML
    private void addUser() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user with a separate secure database.");

        ButtonType createButtonType = new ButtonType("Create");
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField displayNameField = new TextField();
        displayNameField.setPromptText("Display name");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Temporary password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);

        grid.add(new Label("Display Name:"), 0, 1);
        grid.add(displayNameField, 1, 1);

        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);

        grid.add(new Label("Confirm:"), 0, 3);
        grid.add(confirmPasswordField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(result -> {
            if (result != createButtonType) {
                return;
            }

            String username = usernameField.getText();
            String displayName = displayNameField.getText();
            String password = passwordField.getText();
            String confirm = confirmPasswordField.getText();

            if (password == null || !password.equals(confirm)) {
                showError("Passwords do not match.");
                return;
            }

            try {
                UserCreationService.createUser(username, displayName, password);
                loadUsers();

                new Alert(
                        Alert.AlertType.INFORMATION,
                        "User created successfully. The user has a separate secure database."
                ).showAndWait();

            } catch (Exception e) {
                e.printStackTrace();

                Throwable root = e.getCause() != null ? e.getCause() : e;

                showError(root.getMessage() == null
                        ? "Failed to create user."
                        : root.getMessage());
            }
        });
    }

    @FXML
    private void refreshUsers() {
        loadUsers();
    }

    @FXML
    private void closeWindow() {
        if (closeButton != null && closeButton.getScene() != null) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}