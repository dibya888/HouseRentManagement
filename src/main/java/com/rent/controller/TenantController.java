package com.rent.controller;

import com.rent.dao.TenantDAO;
import com.rent.model.Tenant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TenantController {

    @FXML private TableView<Tenant> tenantTable;

    @FXML private TableColumn<Tenant, Integer> colId;
    @FXML private TableColumn<Tenant, String> colName;
    @FXML private TableColumn<Tenant, String> colPhone;
    @FXML private TableColumn<Tenant, String> colEmail;
    @FXML private TableColumn<Tenant, String> colNid;
    @FXML private TableColumn<Tenant, String> colAddress;

    @FXML
    public void initialize() {

        // Bind columns to model
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNid.setCellValueFactory(new PropertyValueFactory<>("nid"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        loadTenants();
    }

    private void loadTenants() {

        ObservableList<Tenant> list =
                FXCollections.observableArrayList(TenantDAO.getAllTenants());

        tenantTable.setItems(list);
    }
}