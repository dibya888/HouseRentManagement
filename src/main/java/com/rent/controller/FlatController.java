package com.rent.controller;

import com.rent.dao.FlatDAO;
import com.rent.model.Flat;
import com.rent.util.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class FlatController {

    @FXML private TableView<Flat> flatTable;

    @FXML private TableColumn<Flat, String> colFlatNo;
    @FXML private TableColumn<Flat, String> colProperty; // ✅ NEW
    @FXML private TableColumn<Flat, String> colMeterNo;
    @FXML private TableColumn<Flat, Integer> colBeds;
    @FXML private TableColumn<Flat, Integer> colBaths;
    @FXML private TableColumn<Flat, Integer> colKitchen;
    @FXML private TableColumn<Flat, Integer> colBalcony;
    @FXML private TableColumn<Flat, Integer> colDining;
    @FXML private TableColumn<Flat, Integer> colLiving;
    @FXML private TableColumn<Flat, Double> colRent;
    @FXML private TableColumn<Flat, String> colStatus;
    @FXML private TableColumn<Flat, Void> colAction;

    private final ObservableList<Flat> flatList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        colMeterNo.setCellValueFactory(new PropertyValueFactory<>("meterNo"));

        // ✅ Property column via DB lookup (no model change)
        colProperty.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Flat f = getTableRow().getItem();
                    setText(fetchPropertyName(f.getFlatNo()));
                }
            }
        });

        colBeds.setCellValueFactory(new PropertyValueFactory<>("bedrooms"));
        colBaths.setCellValueFactory(new PropertyValueFactory<>("bathrooms"));
        colKitchen.setCellValueFactory(new PropertyValueFactory<>("kitchens"));
        colBalcony.setCellValueFactory(new PropertyValueFactory<>("balconies"));
        colDining.setCellValueFactory(new PropertyValueFactory<>("diningrooms"));
        colLiving.setCellValueFactory(new PropertyValueFactory<>("livingrooms"));
        colRent.setCellValueFactory(new PropertyValueFactory<>("rent"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        flatTable.setItems(flatList);
        addActionButtons();
        loadFlatsFromDB();
    }

    private void loadFlatsFromDB() {
        flatList.clear();
        flatList.addAll(FlatDAO.getAllFlats());
    }

    // ✅ Lookup property name by flat_no
    private String fetchPropertyName(String flatNo) {
        String sql = """
            SELECT p.name
            FROM flats f
            JOIN properties p ON f.property_id = p.id
            WHERE f.flat_no = ?
        """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flatNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-";
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
                    Flat flat = getTableRow().getItem();
                    if (flat != null) openViewFlat(flat);
                });

                editBtn.setOnAction(e -> {
                    Flat flat = getTableRow().getItem();
                    if (flat != null) openEditFlat(flat);
                });

                deleteBtn.setOnAction(e -> {
                    Flat flat = getTableRow().getItem();
                    if (flat != null) deleteFlat(flat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(8, viewBtn, editBtn, deleteBtn));
            }
        });
    }

    private void openViewFlat(Flat flat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pages/view-flat.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Flat Details");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
            stage.setScene(new Scene(loader.load(), 460, 520));
            stage.setMinWidth(420);
            stage.setMinHeight(380);
            stage.initModality(Modality.APPLICATION_MODAL);
            ViewFlatController controller = loader.getController();
            controller.setFlat(flat);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditFlat(Flat flat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pages/edit-flat.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Edit Flat");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
            stage.setScene(new Scene(loader.load(), 460, 560));
            stage.setMinWidth(420);
            stage.setMinHeight(420);
            stage.initModality(Modality.APPLICATION_MODAL);
            EditFlatController controller = loader.getController();
            controller.setFlat(flat);
            stage.showAndWait();
            loadFlatsFromDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteFlat(Flat flat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Flat");
        confirm.setHeaderText("Delete " + flat.getFlatNo() + "?");
        confirm.setContentText("Are you sure?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (FlatDAO.deleteFlat(flat.getFlatNo())) {
                loadFlatsFromDB();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete flat!").show();
            }
        }
    }

    @FXML
    private void handleAddFlat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pages/add-flat.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Add Flat");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));
            stage.setScene(new Scene(loader.load(), 440, 560));
            stage.setMinWidth(420);
            stage.setMinHeight(420);
            stage.setOnHidden(e -> loadFlatsFromDB());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}