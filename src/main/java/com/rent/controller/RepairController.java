package com.rent.controller;

import com.rent.dao.RepairDAO;
import com.rent.model.Repair;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Optional;


public class RepairController {

    @FXML private TableView<Repair> repairTable;

    @FXML private TableColumn<Repair, Integer> colId;
    @FXML private TableColumn<Repair, String> colFlatNo;
    @FXML private TableColumn<Repair, String> colRepairDate;
    @FXML private TableColumn<Repair, String> colCategory;
    @FXML private TableColumn<Repair, String> colDescription;
    @FXML private TableColumn<Repair, Double> colCost;
    @FXML private TableColumn<Repair, String> colPaidBy;
    @FXML private TableColumn<Repair, String> colStatus;
    @FXML private TableColumn<Repair, Void> colAction;

    @FXML private TextField searchField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    @FXML private Label totalRepairLabel;
    @FXML private Label monthRepairLabel;
    @FXML private Label yearRepairLabel;
    @FXML private Label ownerPaidLabel;
    @FXML private Label tenantPaidLabel;
    @FXML private ComboBox<String> paidByFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;

    private final ObservableList<Repair> masterList =
            FXCollections.observableArrayList();

    private FilteredList<Repair> filteredList;

    @FXML
    public void initialize() {
        setupTable();
        setupFilterCombos();
        setupFilters();
        loadRepairs();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        colRepairDate.setCellValueFactory(new PropertyValueFactory<>("repairDate"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
        colPaidBy.setCellValueFactory(new PropertyValueFactory<>("paidBy"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupActionColumn();

        filteredList = new FilteredList<>(masterList, p -> true);
        repairTable.setItems(filteredList);

        repairTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void setupFilters() {
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    @FXML
    public void loadRepairs() {
        masterList.setAll(RepairDAO.getAllRepairs());
        applyFilters();
        updateSummary();
    }

    @FXML
    private void onSearch() {
        applyFilters();
    }

    private void applyFilters() {
        if (filteredList == null) return;

        String q = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String paidByFilter = paidByFilterCombo == null
                ? "All"
                : paidByFilterCombo.getValue();

        String statusFilter = statusFilterCombo == null
                ? "All"
                : statusFilterCombo.getValue();

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        filteredList.setPredicate(repair -> {

            // Search filter
            if (!q.isEmpty()) {
                boolean matched =
                        contains(repair.getFlatNo(), q)
                                || contains(repair.getCategory(), q)
                                || contains(repair.getDescription(), q)
                                || contains(repair.getPaidBy(), q)
                                || contains(repair.getStatus(), q)
                                || contains(repair.getNotes(), q);

                if (!matched) return false;
            }

            // Paid By filter
            if (paidByFilter != null && !paidByFilter.equals("All")) {
                if (repair.getPaidBy() == null
                        || !repair.getPaidBy().equalsIgnoreCase(paidByFilter)) {
                    return false;
                }
            }

            // Status filter
            if (statusFilter != null && !statusFilter.equals("All")) {
                if (repair.getStatus() == null
                        || !repair.getStatus().equalsIgnoreCase(statusFilter)) {
                    return false;
                }
            }

            // Date range filter
            if (fromDate != null || toDate != null) {
                if (repair.getRepairDate() == null || repair.getRepairDate().isBlank()) {
                    return false;
                }

                try {
                    LocalDate repairDate = LocalDate.parse(repair.getRepairDate());

                    if (fromDate != null && repairDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null && repairDate.isAfter(toDate)) {
                        return false;
                    }

                } catch (Exception e) {
                    return false;
                }
            }

            return true;
        });

        updateSummary();
    }

    private void updateSummary() {
        if (filteredList == null) return;

        double total = 0;
        double month = 0;
        double year = 0;
        double ownerPaid = 0;
        double tenantPaid = 0;

        String currentMonth = YearMonth.now().toString();
        String currentYear = String.valueOf(Year.now().getValue());

        for (Repair repair : filteredList) {
            total += repair.getCost();

            if ("Owner".equalsIgnoreCase(repair.getPaidBy())) {
                ownerPaid += repair.getCost();
            }

            if ("Tenant".equalsIgnoreCase(repair.getPaidBy())) {
                tenantPaid += repair.getCost();
            }

            if (repair.getRepairDate() != null) {
                if (repair.getRepairDate().startsWith(currentMonth)) {
                    month += repair.getCost();
                }

                if (repair.getRepairDate().startsWith(currentYear + "-")) {
                    year += repair.getCost();
                }
            }
        }

        totalRepairLabel.setText(money(total));
        monthRepairLabel.setText(money(month));
        yearRepairLabel.setText(money(year));
        ownerPaidLabel.setText(money(ownerPaid));
        tenantPaidLabel.setText(money(tenantPaid));
    }

    @FXML
    private void clearFilters() {
        searchField.clear();

        if (paidByFilterCombo != null) {
            paidByFilterCombo.getSelectionModel().select("All");
        }

        if (statusFilterCombo != null) {
            statusFilterCombo.getSelectionModel().select("All");
        }

        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);

        applyFilters();
    }

    @FXML
    private void openAddRepair() {
        openRepairForm(null);
    }

    private void openEditRepair(Repair repair) {
        openRepairForm(repair);
    }

    private void openRepairForm(Repair repair) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/repair-form.fxml")
            );

            Scene scene = new Scene(loader.load());

            RepairFormController controller = loader.getController();
            controller.setRepair(repair);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(repair == null ? "Add Repair" : "Edit Repair");
            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

            loadRepairs();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupActionColumn() {
        colAction.setCellFactory(tc -> new TableCell<>() {

            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#3b82f6; -fx-text-fill:white;");
                deleteBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white;");

                editBtn.setOnAction(e -> {
                    Repair repair = getTableView().getItems().get(getIndex());
                    openEditRepair(repair);
                });

                deleteBtn.setOnAction(e -> {
                    Repair repair = getTableView().getItems().get(getIndex());
                    deleteRepair(repair);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void deleteRepair(Repair repair) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Repair");
        confirm.setHeaderText("Delete this repair record?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = RepairDAO.deleteRepair(repair.getId());

            if (success) {
                AuditLogDAO.log(
                        AuditActions.REPAIR_DELETED,
                        "Repair deleted. ID: "
                                + repair.getId()
                                + ", Flat: "
                                + repair.getFlatNo()
                                + ", Date: "
                                + repair.getRepairDate()
                                + ", Category: "
                                + repair.getCategory()
                                + ", Cost: "
                                + repair.getCost()
                                + ", Paid By: "
                                + repair.getPaidBy()
                                + ", Status: "
                                + repair.getStatus()
                );
            }

            loadRepairs();
        }
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private String money(double value) {
        return "৳ " + String.format("%,.2f", value);
    }
    private void setupFilterCombos() {
        paidByFilterCombo.setItems(FXCollections.observableArrayList(
                "All",
                "Owner",
                "Tenant"
        ));

        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All",
                "Pending",
                "Completed"
        ));

        paidByFilterCombo.getSelectionModel().select("All");
        statusFilterCombo.getSelectionModel().select("All");

        paidByFilterCombo.setOnAction(e -> applyFilters());
        statusFilterCombo.setOnAction(e -> applyFilters());
    }
}