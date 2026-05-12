package com.rent.controller;

import javafx.fxml.FXML;

public class SettingsController {

    @FXML
    public void openProperty() {
        // Ask parent DashboardController to navigate
        DashboardController.getInstance()
                .showProperty();
    }
}
