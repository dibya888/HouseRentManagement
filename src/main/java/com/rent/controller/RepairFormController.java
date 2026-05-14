package com.rent.controller;

import com.rent.dao.FlatDAO;
import com.rent.dao.RepairDAO;
import com.rent.model.Repair;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class RepairFormController {

    @FXML private Label titleLabel;

    @FXML private ComboBox<String> flatCombo;
    @FXML private DatePicker repairDatePicker;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField costField;
    @FXML private ComboBox<String> paidByCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea notesArea;
    @FXML private TextField vendorNameField;
    @FXML private TextField vendorPhoneField;
    @FXML private TextField invoiceNoField;

    private Repair repair;

    @FXML
    public void initialize() {
        flatCombo.setItems(FXCollections.observableArrayList(
                FlatDAO.getOccupiedFlatNumbers()
        ));

        categoryCombo.setItems(FXCollections.observableArrayList(
                "Plumbing",
                "Electricity",
                "Painting",
                "Door/Window",
                "Floor",
                "Appliance",
                "Cleaning",
                "Other"
        ));

        paidByCombo.setItems(FXCollections.observableArrayList(
                "Owner",
                "Tenant"
        ));

        statusCombo.setItems(FXCollections.observableArrayList(
                "Pending",
                "Completed"
        ));

        repairDatePicker.setValue(LocalDate.now());
        categoryCombo.getSelectionModel().select("Other");
        paidByCombo.getSelectionModel().select("Owner");
        statusCombo.getSelectionModel().select("Completed");
    }

    public void setRepair(Repair repair) {
        this.repair = repair;

        if (repair == null) {
            titleLabel.setText("Add Repair");
            return;
        }

        titleLabel.setText("Edit Repair");

        flatCombo.setValue(repair.getFlatNo());

        if (repair.getRepairDate() != null && !repair.getRepairDate().isBlank()) {
            repairDatePicker.setValue(LocalDate.parse(repair.getRepairDate()));
        }

        categoryCombo.setValue(repair.getCategory());
        costField.setText(String.valueOf(repair.getCost()));
        paidByCombo.setValue(repair.getPaidBy());
        statusCombo.setValue(repair.getStatus());
        descriptionArea.setText(repair.getDescription());
        notesArea.setText(repair.getNotes());
        vendorNameField.setText(repair.getVendorName());
        vendorPhoneField.setText(repair.getVendorPhone());
        invoiceNoField.setText(repair.getInvoiceNo());
    }

    @FXML
    private void save() {
        if (!validate()) return;

        String flatNo = flatCombo.getValue();
        String repairDate = repairDatePicker.getValue().toString();
        String category = categoryCombo.getValue();
        String description = descriptionArea.getText();
        double cost = Double.parseDouble(costField.getText().trim());
        String paidBy = paidByCombo.getValue();
        String status = statusCombo.getValue();
        String notes = notesArea.getText();
        String vendorName = vendorNameField.getText() == null ? "" : vendorNameField.getText().trim();
        String vendorPhone = vendorPhoneField.getText() == null ? "" : vendorPhoneField.getText().trim();
        String invoiceNo = invoiceNoField.getText() == null ? "" : invoiceNoField.getText().trim();

        boolean success;

        if (repair == null) {
            Repair newRepair = new Repair(
                    flatNo,
                    repairDate,
                    category,
                    description,
                    cost,
                    paidBy,
                    status,
                    notes
            );

            newRepair.setVendorName(vendorName);
            newRepair.setVendorPhone(vendorPhone);
            newRepair.setInvoiceNo(invoiceNo);

            success = RepairDAO.addRepair(newRepair);
        } else {
            repair.setFlatNo(flatNo);
            repair.setRepairDate(repairDate);
            repair.setCategory(category);
            repair.setDescription(description);
            repair.setCost(cost);
            repair.setPaidBy(paidBy);
            repair.setStatus(status);
            repair.setNotes(notes);

            repair.setVendorName(vendorName);
            repair.setVendorPhone(vendorPhone);
            repair.setInvoiceNo(invoiceNo);

            success = RepairDAO.updateRepair(repair);
        }

        if (success) {
            close();
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to save repair.").show();
        }
    }

    private boolean validate() {
        if (flatCombo.getValue() == null || flatCombo.getValue().isBlank()) {
            new Alert(Alert.AlertType.WARNING,
                    "Please select a flat.").show();
            return false;
        }

        if (repairDatePicker.getValue() == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Please select repair date.").show();
            return false;
        }

        if (categoryCombo.getValue() == null || categoryCombo.getValue().isBlank()) {
            new Alert(Alert.AlertType.WARNING,
                    "Please select category.").show();
            return false;
        }

        try {
            double cost = Double.parseDouble(costField.getText().trim());

            if (cost < 0) {
                new Alert(Alert.AlertType.WARNING,
                        "Cost cannot be negative.").show();
                return false;
            }

        } catch (Exception e) {
            new Alert(Alert.AlertType.WARNING,
                    "Please enter valid repair cost.").show();
            return false;
        }

        return true;
    }

    @FXML
    private void close() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}