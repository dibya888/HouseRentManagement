package com.rent.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A month-only picker: shows "yyyy-MM" on a button, and opens a small
 * popup with a 4x3 grid of month names plus year navigation arrows —
 * no day-of-month grid is ever shown.
 *
 * Exposes a LocalDate valueProperty (always day 1 of the selected month)
 * so it's a drop-in replacement for code that previously read a
 * DatePicker's LocalDate value via YearMonth.from(picker.getValue()).
 */
public class MonthYearPicker extends Button {

    private final ObjectProperty<LocalDate> value = new SimpleObjectProperty<>(this, "value");

    private int popupYear;
    private Popup popup;
    private Label yearLabel;
    private GridPane monthGrid;

    public MonthYearPicker() {
        setText("Select Month");
        setOnAction(e -> togglePopup());
        value.addListener((obs, oldV, newV) -> refreshButtonText());
    }

    public ObjectProperty<LocalDate> valueProperty() {
        return value;
    }

    public LocalDate getValue() {
        return value.get();
    }

    /**
     * Accepts any LocalDate (matching DatePicker's API shape); only the
     * year and month are kept, day is normalized to 1.
     */
    public void setValue(LocalDate date) {
        value.set(date == null ? null : date.withDayOfMonth(1));
    }

    private void refreshButtonText() {
        LocalDate v = value.get();
        setText(v == null ? "Select Month" : YearMonth.from(v).toString());
    }

    private void togglePopup() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
            return;
        }
        showPopup();
    }

    private void showPopup() {
        LocalDate current = value.get();
        popupYear = current != null ? current.getYear() : Year.now().getValue();

        popup = new Popup();
        popup.setAutoHide(true);

        VBox root = new VBox(8);
        root.setPadding(new Insets(10));
        root.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: #d1d5db;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);"
        );

        HBox yearNav = new HBox(10);
        yearNav.setAlignment(Pos.CENTER);

        Button prevYear = new Button("◀");
        Button nextYear = new Button("▶");
        yearLabel = new Label(String.valueOf(popupYear));
        yearLabel.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");

        prevYear.setOnAction(e -> {
            popupYear--;
            rebuildMonthGrid();
        });
        nextYear.setOnAction(e -> {
            popupYear++;
            rebuildMonthGrid();
        });

        yearNav.getChildren().addAll(prevYear, yearLabel, nextYear);

        monthGrid = new GridPane();
        monthGrid.setHgap(6);
        monthGrid.setVgap(6);
        rebuildMonthGrid();

        root.getChildren().addAll(yearNav, monthGrid);
        popup.getContent().add(root);

        popup.show(this,
                getScene().getWindow().getX() + localToScene(0, 0).getX() + getScene().getX(),
                getScene().getWindow().getY() + localToScene(0, getHeight()).getY() + getScene().getY());
    }

    private void rebuildMonthGrid() {
        monthGrid.getChildren().clear();
        yearLabel.setText(String.valueOf(popupYear));

        ToggleGroup group = new ToggleGroup();
        LocalDate current = value.get();

        for (int month = 1; month <= 12; month++) {
            String label = java.time.Month.of(month)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            ToggleButton btn = new ToggleButton(label);
            btn.setToggleGroup(group);
            btn.setPrefWidth(60);

            boolean isSelected = current != null
                    && current.getYear() == popupYear
                    && current.getMonthValue() == month;
            btn.setSelected(isSelected);

            int finalMonth = month;
            btn.setOnAction(e -> {
                setValue(LocalDate.of(popupYear, finalMonth, 1));
                if (popup != null) popup.hide();
            });

            monthGrid.add(btn, (month - 1) % 4, (month - 1) / 4);
        }
    }
}