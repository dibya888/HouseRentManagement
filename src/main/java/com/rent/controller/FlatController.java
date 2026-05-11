package com.rent.controller;

import com.rent.dao.FlatDAO;
import com.rent.model.Flat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import java.util.Optional;


public class FlatController {

    @FXML private TableView<Flat> flatTable;

    @FXML private TableColumn<Flat, String> colFlatNo;
    @FXML private TableColumn<Flat, Integer> colBeds;
    @FXML private TableColumn<Flat, Integer> colBaths;
    @FXML private TableColumn<Flat, Integer> colKitchen;
    @FXML private TableColumn<Flat, Integer> colBalcony;
    @FXML private TableColumn<Flat, Integer> colDining;
    @FXML private TableColumn<Flat, Integer> colLiving;
    @FXML private TableColumn<Flat, Double> colRent;
    @FXML private TableColumn<Flat, String> colStatus;
    @FXML private TableColumn<Flat, Void> colAction;

    private final ObservableList<Flat> flatList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
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

        loadFlatsFromDB(); // ✅ initial load
    }

    private void loadFlatsFromDB() {
        flatList.clear();
        flatList.addAll(FlatDAO.getAllFlats());
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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(8, viewBtn, editBtn, deleteBtn));
                }
            }
        });
    }

    private void openViewFlat(Flat f) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Flat Details");
        alert.setHeaderText("🏢 " + f.getFlatNo());
        alert.setContentText(
                "Bedrooms: " + f.getBedrooms() +
                        "\nBathrooms: " + f.getBathrooms() +
                        "\nKitchens: " + f.getKitchens() +
                        "\nBalconies: " + f.getBalconies() +
                        "\nDining Rooms: " + f.getDiningrooms() +
                        "\nLiving Rooms: " + f.getLivingrooms() +
                        "\nRent: ৳ " + f.getRent() +
                        "\nStatus: " + f.getStatus()
        );
        alert.showAndWait();
    }

    private void deleteFlat(Flat flat) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Flat");
        confirm.setHeaderText("Delete " + flat.getFlatNo() + "?");
        confirm.setContentText("Are you sure you want to delete this flat?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            boolean ok = FlatDAO.deleteFlat(flat.getFlatNo());

            if (ok) {
                loadFlatsFromDB();  // refresh
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete flat!");
                err.showAndWait();
            }
        }
    }

    private void openEditFlat(Flat flat) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pages/edit-flat.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Edit Flat");
            stage.setScene(new Scene(loader.load(), 420, 520));
            stage.initModality(Modality.APPLICATION_MODAL);

            EditFlatController controller = loader.getController();
            controller.setFlat(flat);

            stage.showAndWait();

            // ✅ refresh after update
            loadFlatsFromDB();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddFlat() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/pages/add-flat.fxml")
            );

            Stage stage = new Stage();
            stage.setTitle("Add Flat");
            stage.setScene(new Scene(loader.load(), 400, 480));
            stage.setResizable(false);

            // ✅ REFRESH TABLE WHEN WINDOW CLOSES
            stage.setOnHidden(event -> loadFlatsFromDB());

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}