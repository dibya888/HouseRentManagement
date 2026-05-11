package com.rent.controller;

import com.rent.dao.TenantDAO;
import com.rent.model.Tenant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.util.Optional;

import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TextField;


public class TenantController {

    @FXML
    private TableView<Tenant> tenantTable;

    @FXML
    private TableColumn<Tenant, Integer> colId;

    @FXML
    private TableColumn<Tenant, String> colName;

    @FXML
    private TableColumn<Tenant, String> colPhone;

    @FXML
    private TableColumn<Tenant, String> colEmail;

    @FXML
    private TableColumn<Tenant, String> colNid;

    @FXML
    private TableColumn<Tenant, String> colAddress;

    @FXML
    private TableColumn<Tenant, String> colFlatNo;

    @FXML
    private TableColumn<Tenant, Double> colRent;

    @FXML
    private TableColumn<Tenant, Void> colAction;

    @FXML
    private TextField searchField;

    private ObservableList<Tenant> masterList;
    private FilteredList<Tenant> filteredList;

    @FXML
    public void initialize() {

        // table bindings
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        colName.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        colPhone.setCellValueFactory(
                new PropertyValueFactory<>("phone")
        );

        colEmail.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );

        colNid.setCellValueFactory(
                new PropertyValueFactory<>("nid")
        );

        colAddress.setCellValueFactory(
                new PropertyValueFactory<>("address")
        );

        colFlatNo.setCellValueFactory(
                new PropertyValueFactory<>("flatNo")
        );

        colRent.setCellValueFactory(
                new PropertyValueFactory<>("rent")
        );

        // load data
        loadTenants();
        addActionButtons();
    }

    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {

            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                viewBtn.setStyle("-fx-background-color:#22c55e; -fx-text-fill:white;");
                editBtn.setStyle("-fx-background-color:#3b82f6; -fx-text-fill:white;");
                deleteBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white;");

                viewBtn.setOnAction(e -> {
                    Tenant tenant = getTableView().getItems().get(getIndex());
                    openViewTenant(tenant);
                });

                editBtn.setOnAction(e -> {
                    Tenant tenant = getTableView().getItems().get(getIndex());
                    openEditForm(tenant);
                });

                deleteBtn.setOnAction(e -> {
                    Tenant tenant = getTableView().getItems().get(getIndex());
                    TenantDAO.deleteTenantAndFreeFlat(tenant.getId());
                    loadTenants();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(8, viewBtn, editBtn, deleteBtn));
                }
            }
        });
    }

    private void openEditForm(Tenant tenant) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pages/edit-tenant.fxml")
            );

            Parent root = loader.load();

            EditTenantController controller = loader.getController();
            controller.setTenant(tenant);

            Stage stage = new Stage();
            stage.setTitle("Edit Tenant");
            stage.setScene(new Scene(root, 700, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadTenants();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openViewTenant(Tenant tenant) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pages/view-tenant.fxml")
            );
            Parent root = loader.load();

            ViewTenantController controller = loader.getController();
            controller.setTenant(tenant);

            Stage stage = new Stage();
            stage.setTitle("Tenant Details");
            stage.setScene(new Scene(root, 700, 600));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openAddTenantForm(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pages/add-tenant.fxml")
            );

            Parent root = loader.load();

            Scene scene = new Scene(root, 850, 650);

            // popup css
            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/add-tenant.css")
                            .toExternalForm()
            );

            Stage stage = new Stage();

            stage.setTitle("Add New Tenant");

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(scene);

            stage.setResizable(false);

            stage.showAndWait();

            // refresh table after popup closes
            loadTenants();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadTenants() {
        masterList = FXCollections.observableArrayList(
                TenantDAO.getAllTenants()
        );

        filteredList = new FilteredList<>(masterList, p -> true);

        SortedList<Tenant> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(
                tenantTable.comparatorProperty()
        );

        tenantTable.setItems(sortedList);
    }

    @FXML
    private void onSearch() {
        String keyword = searchField.getText().toLowerCase();

        filteredList.setPredicate(tenant -> {
            if (keyword.isEmpty()) return true;

            return tenant.getName().toLowerCase().contains(keyword)
                    || tenant.getPhone().toLowerCase().contains(keyword)
                    || tenant.getNid().toLowerCase().contains(keyword)
                    || tenant.getFlatNo().toLowerCase().contains(keyword);
        });
    }

    @FXML
    public void deleteTenant() {

        Tenant selectedTenant =
                tenantTable.getSelectionModel().getSelectedItem();

        if (selectedTenant == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a tenant first.");

            alert.showAndWait();

            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirm.setTitle("Delete Tenant");

        confirm.setHeaderText("Delete Selected Tenant");

        confirm.setContentText(
                "Are you sure you want to delete this tenant?"
        );

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent()
                && result.get() == ButtonType.OK) {

            TenantDAO.deleteTenant(selectedTenant.getId());

            loadTenants();
        }
    }
}