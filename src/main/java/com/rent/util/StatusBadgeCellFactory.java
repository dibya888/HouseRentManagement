package com.rent.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class StatusBadgeCellFactory {

    private StatusBadgeCellFactory() {
    }

    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> forStatus() {
        return column -> new TableCell<>() {

            private final Label badge = new Label();
            private final HBox box = new HBox(badge);

            {
                box.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null || status.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(status);
                badge.setStyle(baseStyle() + statusColor(status));

                setGraphic(box);
                setText(null);
            }
        };
    }

    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> forPaidBy() {
        return column -> new TableCell<>() {

            private final Label badge = new Label();
            private final HBox box = new HBox(badge);

            {
                box.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null || value.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(value);
                badge.setStyle(baseStyle() + paidByColor(value));

                setGraphic(box);
                setText(null);
            }
        };
    }

    private static String baseStyle() {
        return """
                -fx-padding:4 10;
                -fx-background-radius:10;
                -fx-font-weight:bold;
                -fx-font-size:11px;
                """;
    }

    private static String statusColor(String status) {
        String s = status.toUpperCase();

        if (s.equals("PAID") || s.equals("COMPLETED") || s.equals("SETTLED")) {
            return "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
        }

        if (s.equals("DUE") || s.equals("PAYABLE")) {
            return "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
        }

        if (s.equals("LATE")) {
            return "-fx-background-color:#fecaca; -fx-text-fill:#7f1d1d;";
        }

        if (s.equals("PARTIAL") || s.equals("PENDING")) {
            return "-fx-background-color:#ffedd5; -fx-text-fill:#9a3412;";
        }

        if (s.equals("REFUND")) {
            return "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
        }

        if (s.equals("MOVED OUT")) {
            return "-fx-background-color:#e0e7ff; -fx-text-fill:#3730a3;";
        }

        return "-fx-background-color:#e5e7eb; -fx-text-fill:#374151;";
    }

    private static String paidByColor(String value) {
        String v = value.toUpperCase();

        if (v.equals("OWNER")) {
            return "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
        }

        if (v.equals("TENANT")) {
            return "-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6;";
        }

        return "-fx-background-color:#e5e7eb; -fx-text-fill:#374151;";
    }
}