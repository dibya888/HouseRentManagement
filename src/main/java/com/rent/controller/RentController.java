package com.rent.controller;

import com.rent.dao.RentDAO;
import com.rent.dao.TenantDAO;
import com.rent.dao.FlatDAO;
import com.rent.model.RentRow;
import com.rent.model.Tenant;
import com.rent.model.Flat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.util.StringConverter;
import javafx.scene.control.DateCell;
import java.time.YearMonth;


public class RentController {

    @FXML private TableView<RentRow> rentTable;

    @FXML private TableColumn<RentRow, String> colFlatNo;
    @FXML private TableColumn<RentRow, String> colTenantName;
    @FXML private TableColumn<RentRow, String> colPhone;
    @FXML private TableColumn<RentRow, String> colBillMonth;

    @FXML private TableColumn<RentRow, Double> colHouseRent;
    @FXML private TableColumn<RentRow, Double> colElectricity;
    @FXML private TableColumn<RentRow, Double> colWater;
    @FXML private TableColumn<RentRow, Double> colGas;
    @FXML private TableColumn<RentRow, Double> colOther;
    @FXML private TableColumn<RentRow, Double> colFine;
    @FXML private TableColumn<RentRow, Double> colDiscount;

    @FXML private TableColumn<RentRow, Double> colTotal;
    @FXML private TableColumn<RentRow, String> colStatus;
    @FXML private TableColumn<RentRow, Void> colAction;

    @FXML private TextField searchField;
    @FXML private DatePicker monthPicker;

    private ObservableList<RentRow> masterList = FXCollections.observableArrayList();
    private FilteredList<RentRow> filteredList;

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy-MM");

    @FXML
    public void initialize() {
        // month default
        // Month picker = current month (day forced to 1)
        monthPicker.setValue(LocalDate.now().withDayOfMonth(1));

// show only YYYY-MM, ignore day
        monthPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (date == null) return "";
                return YearMonth.from(date).toString(); // yyyy-MM
            }
            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) return null;
                YearMonth ym = YearMonth.parse(text.trim());
                return ym.atDay(1);
            }
        });

// allow selecting only day 1 (forces month selection behavior)
        monthPicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;
                setDisable(item.getDayOfMonth() != 1);
            }
        });

// when month changes -> filter table
        monthPicker.valueProperty().addListener((obs, oldV, newV) -> applyFilters());

        // column bindings
        colFlatNo.setCellValueFactory(new PropertyValueFactory<>("flatNo"));
        colTenantName.setCellValueFactory(new PropertyValueFactory<>("tenantName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colBillMonth.setCellValueFactory(new PropertyValueFactory<>("billMonth"));

        colHouseRent.setCellValueFactory(new PropertyValueFactory<>("houseRent"));
        colElectricity.setCellValueFactory(new PropertyValueFactory<>("electricity"));
        colWater.setCellValueFactory(new PropertyValueFactory<>("water"));
        colGas.setCellValueFactory(new PropertyValueFactory<>("gas"));
        colOther.setCellValueFactory(new PropertyValueFactory<>("otherBills"));
        colFine.setCellValueFactory(new PropertyValueFactory<>("fine"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));

        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // action buttons
        setupActionColumn();

        // table sizing (you told earlier: set in controller, not FXML)
        rentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        loadRentRows();
    }

    @FXML
    public void loadRentRows() {
        masterList.setAll(RentDAO.getCurrentRentRows());
        filteredList = new FilteredList<>(masterList, p -> true);
        rentTable.setItems(filteredList);
        applyFilters();
    }

    @FXML
    public void onSearch() {
        applyFilters();
    }

    @FXML
    public void generateMonth() {
        if (monthPicker.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a month.").show();
            return;
        }

        String month = YearMonth.from(monthPicker.getValue()).toString(); // yyyy-MM
        RentDAO.ensureMonthGenerated(month, 5);
        loadRentRows();
    }

    @FXML
    public void openBills() {
        openModal("/fxml/pages/bills-view.fxml", "Bills Defaults");
        loadRentRows();
    }

    @FXML
    public void clearMonthFilter() {
        monthPicker.setValue(null);
        applyFilters();
    }

    private void applyFilters() {
        if (filteredList == null) return;

        String q = (searchField.getText() == null) ? "" : searchField.getText().trim().toLowerCase();

        // selected month in YYYY-MM (or null)
        String selectedMonth = null;
        if (monthPicker != null && monthPicker.getValue() != null) {
            selectedMonth = YearMonth.from(monthPicker.getValue()).toString(); // yyyy-MM
        }

        final String monthFinal = selectedMonth;

        filteredList.setPredicate(r -> {
            // month filter
            if (monthFinal != null && (r.getBillMonth() == null || !r.getBillMonth().equals(monthFinal))) {
                return false;
            }

            // search filter
            if (q.isEmpty()) return true;

            return (r.getTenantName() != null && r.getTenantName().toLowerCase().contains(q))
                    || (r.getPhone() != null && r.getPhone().toLowerCase().contains(q))
                    || (r.getFlatNo() != null && r.getFlatNo().toLowerCase().contains(q));
        });
    }

    @FXML
    public void openArchive() {
        openModal("/fxml/pages/archive-view.fxml", "Rent Archive");
        loadRentRows();
    }

    private void setupActionColumn() {
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button payBtn = new Button("Payment");
            private final HBox box = new HBox(8, viewBtn, payBtn);

            {
                viewBtn.setStyle("-fx-background-color:#e5e7eb;");
                payBtn.setStyle("-fx-background-color:#16a34a; -fx-text-fill:white; -fx-font-weight:bold;");

                viewBtn.setOnAction(e -> {
                    RentRow row = getTableView().getItems().get(getIndex());
                    openRentView(row);
                });

                payBtn.setOnAction(e -> {
                    RentRow row = getTableView().getItems().get(getIndex());
                    openPayment(row);
                    loadRentRows();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void openRentView(RentRow row) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/view-rent.fxml"));
            Scene scene = new Scene(loader.load());

            ViewRentController c = loader.getController();
            c.setRentRow(row);

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle("Rent View");
            st.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );
            st.setScene(scene);
            st.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openPayment(RentRow row) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rent-payment.fxml"));
            Scene scene = new Scene(loader.load());
            RentPaymentController c = loader.getController();
            c.setRow(row);

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle("Payment");
            st.setScene(scene);
            st.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node root = loader.load();

            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle(title);
            st.setScene(new Scene((javafx.scene.Parent) root));
            st.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}