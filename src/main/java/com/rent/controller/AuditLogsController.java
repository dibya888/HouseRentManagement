package com.rent.controller;

import com.rent.dao.AuditLogDAO;
import com.rent.model.AuditLog;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogsController {

    @FXML private TableView<AuditLog> auditTable;

    @FXML private TableColumn<AuditLog, String> colCreatedAt;
    @FXML private TableColumn<AuditLog, String> colUsername;
    @FXML private TableColumn<AuditLog, String> colActionType;
    @FXML private TableColumn<AuditLog, String> colDetails;
    @FXML private TableColumn<AuditLog, Void> colAction;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> actionCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    private final ObservableList<AuditLog> masterList =
            FXCollections.observableArrayList();

    private FilteredList<AuditLog> filteredList;

    private static final DateTimeFormatter DB_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DISPLAY_TS =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        loadLogs();
    }

    private void setupTable() {
        colCreatedAt.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatDateTime(cellData.getValue().getCreatedAt()))
        );

        colUsername.setCellValueFactory(cellData ->
                new SimpleStringProperty(nullSafe(cellData.getValue().getUsername()))
        );

        colActionType.setCellValueFactory(cellData ->
                new SimpleStringProperty(nullSafe(cellData.getValue().getAction()))
        );
        setupActionBadgeColumn();

        colDetails.setCellValueFactory(cellData ->
                new SimpleStringProperty(nullSafe(cellData.getValue().getDetails()))
        );

        setupActionColumn();

        filteredList = new FilteredList<>(masterList, p -> true);
        auditTable.setItems(filteredList);

        auditTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void setupFilters() {
        actionCombo.setOnAction(e -> applyFilters());

        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    @FXML
    public void loadLogs() {
        masterList.setAll(AuditLogDAO.getAllLogs());

        actionCombo.setItems(FXCollections.observableArrayList());
        actionCombo.getItems().add("All");
        actionCombo.getItems().addAll(AuditLogDAO.getActionTypes());
        actionCombo.getSelectionModel().select("All");

        applyFilters();
    }

    @FXML
    private void applyFilters() {
        if (filteredList == null) return;

        String q = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String selectedAction = actionCombo == null
                ? "All"
                : actionCombo.getValue();

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        filteredList.setPredicate(log -> {

            if (!q.isEmpty()) {
                boolean matched =
                        contains(log.getUsername(), q)
                                || contains(log.getAction(), q)
                                || contains(log.getDetails(), q)
                                || contains(log.getCreatedAt(), q);

                if (!matched) return false;
            }

            if (selectedAction != null && !"All".equals(selectedAction)) {
                if (log.getAction() == null
                        || !log.getAction().equals(selectedAction)) {
                    return false;
                }
            }

            if (fromDate != null || toDate != null) {
                LocalDate logDate = parseLogDate(log.getCreatedAt());

                if (logDate == null) {
                    return false;
                }

                if (fromDate != null && logDate.isBefore(fromDate)) {
                    return false;
                }

                if (toDate != null && logDate.isAfter(toDate)) {
                    return false;
                }
            }

            return true;
        });
    }

    @FXML
    private void clearFilters() {
        searchField.clear();

        if (actionCombo != null) {
            actionCombo.getSelectionModel().select("All");
        }

        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);

        applyFilters();
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String formatDateTime(String value) {
        try {
            if (value == null || value.isBlank()) return "";

            return LocalDateTime.parse(value, DB_TS)
                    .format(DISPLAY_TS);

        } catch (Exception e) {
            return value;
        }
    }

    private LocalDate parseLogDate(String value) {
        try {
            if (value == null || value.isBlank()) return null;

            return LocalDateTime.parse(value, DB_TS).toLocalDate();

        } catch (Exception e) {
            return null;
        }
    }

    private void setupActionColumn() {
        colAction.setCellFactory(tc -> new TableCell<>() {

            private final Button viewBtn = new Button("View");
            private final javafx.scene.layout.HBox box =
                    new javafx.scene.layout.HBox(viewBtn);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);

                viewBtn.setStyle(
                        "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                viewBtn.setOnAction(e -> {
                    AuditLog log = getTableView().getItems().get(getIndex());
                    showLogDetails(log);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }

                setText(null);
            }
        });
    }

    private void showLogDetails(AuditLog log) {
        if (log == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Audit Log Details");
        alert.setHeaderText(log.getAction());

        String text = """
            Date/Time: %s
            User: %s
            Action: %s

            Details:
            %s
            """.formatted(
                formatDateTime(log.getCreatedAt()),
                nullSafe(log.getUsername()),
                nullSafe(log.getAction()),
                nullSafe(log.getDetails())
        );

        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(520);
        area.setPrefHeight(260);

        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    private void setupActionBadgeColumn() {
        colActionType.setCellFactory(column -> new TableCell<>() {

            private final Label badge = new Label();
            private final StackPane wrapper = new StackPane(badge);

            {
                badge.setStyle("-fx-padding:4 8; -fx-background-radius:8; -fx-font-weight:bold; -fx-font-size:11px;");
            }

            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);

                if (empty || action == null || action.isBlank()) {
                    setGraphic(null);
                    return;
                }

                badge.setText(action);
                badge.setStyle(
                        "-fx-padding:4 8;"
                                + "-fx-background-radius:8;"
                                + "-fx-font-weight:bold;"
                                + "-fx-font-size:11px;"
                                + actionColorStyle(action)
                );

                setGraphic(wrapper);
            }
        });
    }

    private String actionColorStyle(String action) {
        if (action == null) {
            return "-fx-background-color:#e5e7eb; -fx-text-fill:#374151;";
        }

        String a = action.toUpperCase();

        if (a.contains("FAILED")
                || a.contains("DELETED")
                || a.contains("FACTORY_RESET")) {
            return "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
        }

        if (a.contains("SUCCESS")
                || a.contains("ADDED")
                || a.contains("PAYMENT")) {
            return "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
        }

        if (a.contains("BACKUP")
                || a.contains("PRINTED")
                || a.contains("PDF")
                || a.contains("EXCEL")) {
            return "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
        }

        if (a.contains("RESTORE")
                || a.contains("UPDATED")
                || a.contains("CHANGED")) {
            return "-fx-background-color:#ffedd5; -fx-text-fill:#9a3412;";
        }

        if (a.contains("RECOVERY")
                || a.contains("EMERGENCY")
                || a.contains("PASSWORD")) {
            return "-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6;";
        }

        return "-fx-background-color:#e5e7eb; -fx-text-fill:#374151;";
    }
}