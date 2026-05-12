package com.rent.controller;

import com.rent.dao.RentDAO;
import com.rent.model.RentRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class ArchiveController {

    @FXML private TableView<RentRow> archiveTable;

    @FXML private TableColumn<RentRow, String> colFlatNo;
    @FXML private TableColumn<RentRow, String> colTenantName;
    @FXML private TableColumn<RentRow, String> colPhone;
    @FXML private TableColumn<RentRow, String> colBillMonth;
    @FXML private TableColumn<RentRow, Double> colTotal;
    @FXML private TableColumn<RentRow, String> colStatus;
    @FXML private TableColumn<RentRow, Void> colAction;

    private final ObservableList<RentRow> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        colTenantName.setCellValueFactory(new PropertyValueFactory<>("tenantName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colBillMonth.setCellValueFactory(new PropertyValueFactory<>("billMonth"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupAction();

        archiveTable.setItems(list);
        archiveTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        loadArchive();
    }

    @FXML
    public void loadArchive() {
        list.setAll(RentDAO.getArchiveRows());
    }

    private void setupAction() {
        colAction.setCellFactory(tc -> new TableCell<>() {

            private final Button restoreBtn = new Button("Restore");
            private final HBox box = new HBox(8, restoreBtn);

            {
                restoreBtn.setStyle(
                        "-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:bold;"
                );

                restoreBtn.setOnAction(e -> {
                    RentRow row = getTableView().getItems().get(getIndex());
                    RentDAO.restoreFromArchive(row.getId());
                    loadArchive();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
}