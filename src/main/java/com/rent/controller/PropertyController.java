package com.rent.controller;

import com.rent.util.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PropertyController {

    @FXML private VBox emptyStateBox;
    @FXML private FlowPane propertyContainer;

    @FXML
    public void initialize() {
        loadProperties();
    }

    @FXML
    private void addProperty() {
        openAddPopup();
    }

    private void openAddPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pages/add-property-view.fxml"));
            Parent root = loader.load();

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle("Add Property");
            st.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            st.setScene(scene);
            st.setResizable(false);

            st.showAndWait();
            loadProperties(); // refresh after popup closes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditPopup(int propertyId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/pages/edit-property-view.fxml"));
            Parent root = loader.load();

            EditPropertyController c = loader.getController();
            c.loadProperty(propertyId);

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle("Edit Property");
            st.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            st.setScene(scene);
            st.setResizable(false);

            st.showAndWait();
            loadProperties(); // refresh after popup closes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() {
        propertyContainer.getChildren().clear();

        String sql = """
            SELECT id, name, address, phone, is_default
            FROM properties
            ORDER BY is_default DESC, id DESC
        """;

        int count = 0;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                count++;

                int id = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                int isDefault = rs.getInt("is_default");

                VBox card = new VBox(10);
                card.setPrefWidth(280);
                card.setStyle("""
                        -fx-background-color:white;
                        -fx-padding:14;
                        -fx-background-radius:12;
                        -fx-border-color:#e5e7eb;
                        -fx-border-radius:12;
                        """);

                Label nameLbl = new Label(name);
                nameLbl.setStyle("-fx-font-size:15px; -fx-font-weight:bold;");

                Label addrLbl = new Label(address);
                addrLbl.setWrapText(true);
                addrLbl.setStyle("-fx-text-fill:#6b7280;");

                card.getChildren().addAll(nameLbl, addrLbl);

                if (phone != null && !phone.isBlank()) {
                    Label phoneLbl = new Label("📞 " + phone);
                    phoneLbl.setStyle("-fx-text-fill:#6b7280;");
                    card.getChildren().add(phoneLbl);
                }

                HBox bottomRow = new HBox(8);
                bottomRow.setStyle("-fx-padding:6 0 0 0;");

                if (isDefault == 1) {
                    Label badge = new Label("DEFAULT");
                    badge.setStyle("""
                            -fx-background-color:#dcfce7;
                            -fx-text-fill:#166534;
                            -fx-padding:2 8;
                            -fx-background-radius:6;
                            -fx-font-size:11px;
                            """);
                    bottomRow.getChildren().add(badge);
                }

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button editBtn = new Button("Edit");
                editBtn.setOnAction(e -> openEditPopup(id));

                Button delBtn = new Button("Delete");
                delBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:bold;");
                delBtn.setOnAction(e -> deleteProperty(id));

                bottomRow.getChildren().addAll(spacer, editBtn, delBtn);
                card.getChildren().add(bottomRow);

                propertyContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean empty = (count == 0);
        emptyStateBox.setVisible(empty);
        emptyStateBox.setManaged(empty);

        propertyContainer.setVisible(!empty);
        propertyContainer.setManaged(!empty);
    }

    private void deleteProperty(int propertyId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Property");
        confirm.setHeaderText("Delete this property?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM properties WHERE id=?")) {
            ps.setInt(1, propertyId);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        loadProperties();
    }
}