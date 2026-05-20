package com.rent.controller;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;
import com.rent.util.CurrentSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
        /*
         * Full add-user form will be added in the next step.
         */
        new Alert(
                Alert.AlertType.INFORMATION,
                "Add User form will be added in the next step."
        ).showAndWait();
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