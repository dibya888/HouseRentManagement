package com.rent.controller;

import com.rent.model.RentRow;
import javafx.scene.control.Alert;
import com.rent.util.DBUtil;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;


public class ReceiptPrinter {

    public static void printReceipt(
            RentRow row,
            boolean includePropertyName,
            boolean includePropertyAddress
    ) {
        if (row == null) {
            new Alert(Alert.AlertType.ERROR, "Receipt data missing.").show();
            return;
        }

        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            new Alert(Alert.AlertType.ERROR, "No printer found.").show();
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob(printer);
        if (job == null) {
            new Alert(Alert.AlertType.ERROR, "Failed to create print job.").show();
            return;
        }

        PageLayout layout = createA5Layout(printer);
        Node receiptNode = buildReceiptNode(row, layout, includePropertyName, includePropertyAddress);

        boolean printed = job.printPage(layout, receiptNode);

        if (printed) {
            job.endJob();
        } else {
            job.cancelJob();
        }
    }

    private static PageLayout createA5Layout(Printer printer) {
        try {
            return printer.createPageLayout(
                    Paper.A5,
                    PageOrientation.PORTRAIT,
                    Printer.MarginType.HARDWARE_MINIMUM
            );
        } catch (Exception e) {
            return printer.getDefaultPageLayout();
        }
    }

    private static Node buildReceiptNode(
            RentRow row,
            PageLayout layout,
            boolean includePropertyName,
            boolean includePropertyAddress
    ) {
        double width = layout.getPrintableWidth();
        double height = layout.getPrintableHeight();

        StackPane root = new StackPane();
        root.setPrefSize(width, height);
        root.setMinSize(width, height);
        root.setMaxSize(width, height);
        root.setStyle("-fx-background-color:white;");

        Node paidStamp = buildPaidStamp();
        StackPane.setAlignment(paidStamp, Pos.CENTER);

        VBox content = new VBox(9);
        content.setPadding(new Insets(12));
        content.setPrefSize(width, height);
        content.setFillWidth(true);

        VBox header = buildHeader(row, width, includePropertyName, includePropertyAddress);

        Separator sep1 = new Separator();

        GridPane meta = buildMetaBlock(row, width);

        Separator sep2 = new Separator();

        GridPane billTable = buildBillTable(row, width);

        Separator sep3 = new Separator();

        Text totalText = new Text("TOTAL AMOUNT: ৳ " + formatMoney(row.getTotal()));
        totalText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalText.setFill(Color.web("#111827"));

        Text wordsText = new Text("In Words: " + amountToWordsBDT(row.getTotal()));
        wordsText.setFont(Font.font("Segoe UI", 10.5));
        wordsText.setFill(Color.web("#374151"));
        wordsText.setWrappingWidth(width - 24);

        Separator sep4 = new Separator();

        VBox footer = buildFooter();

        content.getChildren().addAll(
                header,
                sep1,
                meta,
                sep2,
                billTable,
                sep3,
                totalText,
                wordsText,
                sep4,
                footer
        );

        root.getChildren().addAll(paidStamp, content);
        return root;
    }

    private static VBox buildHeader(
            RentRow row,
            double width,
            boolean includePropertyName,
            boolean includePropertyAddress
    ) {
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);

        Image logoImage = new Image(
                ReceiptPrinter.class.getResourceAsStream("/images/app-logo.png")
        );

        ImageView logo = new ImageView(logoImage);
        logo.setPreserveRatio(true);
        logo.setFitHeight(52);

        header.getChildren().add(logo);

        PropertyInfo propertyInfo = fetchPropertyForFlat(row.getFlatNo());

        if (includePropertyName) {
            Text name = new Text(nullSafe(propertyInfo.name));
            name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            name.setTextAlignment(TextAlignment.CENTER);
            header.getChildren().add(name);
        }

        if (includePropertyAddress) {
            Text address = new Text(nullSafe(propertyInfo.address));
            address.setFont(Font.font("Segoe UI", 10.5));
            address.setFill(Color.web("#4b5563"));
            address.setTextAlignment(TextAlignment.CENTER);
            address.setWrappingWidth(width - 24);
            header.getChildren().add(address);
        }

        return header;
    }

    private static GridPane buildMetaBlock(RentRow row, double width) {
        GridPane meta = new GridPane();
        meta.setHgap(16);
        meta.setVgap(6);

        ColumnConstraints leftCol = new ColumnConstraints((width - 24) * 0.55);
        ColumnConstraints rightCol = new ColumnConstraints((width - 24) * 0.45);

        meta.getColumnConstraints().addAll(leftCol, rightCol);

        String billMonth = YearMonth.parse(row.getBillMonth())
                .format(DateTimeFormatter.ofPattern("MMMM, yyyy"));

        String paymentDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        VBox left = new VBox(4);
        left.getChildren().add(line("Tenant:", row.getTenantName()));
        left.getChildren().add(line("Mobile:", row.getPhone()));
        left.getChildren().add(line("Flat No:", row.getFlatNo()));
        left.getChildren().add(line("Meter No:", row.getMeterNo()));

        VBox right = new VBox(4);
        right.getChildren().add(line("Bill Month:", billMonth));
        right.getChildren().add(line("Payment Date:", paymentDate));

        meta.add(left, 0, 0);
        meta.add(right, 1, 0);

        return meta;
    }

    private static GridPane buildBillTable(RentRow row, double width) {
        GridPane table = new GridPane();
        table.setHgap(10);
        table.setVgap(6);

        ColumnConstraints labelCol = new ColumnConstraints((width - 24) * 0.60);
        ColumnConstraints amountCol = new ColumnConstraints((width - 24) * 0.40);
        amountCol.setHalignment(HPos.RIGHT);

        table.getColumnConstraints().addAll(labelCol, amountCol);

        int r = 0;
        r = addBillRow(table, r, "House Rent", row.getHouseRent());
        r = addBillRow(table, r, "Electricity", row.getElectricity());
        r = addBillRow(table, r, "Water", row.getWater());
        r = addBillRow(table, r, "Gas", row.getGas());
        r = addBillRow(table, r, "Other Bills", row.getOtherBills());
        r = addBillRow(table, r, "Fine", row.getFine());
        addBillRow(table, r, "Discount", -row.getDiscount());

        return table;
    }

    private static Node buildPaidStamp() {
        StackPane stamp = new StackPane();
        stamp.setMouseTransparent(true);

        Circle circle = new Circle(118);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#22c55e"));
        circle.setStrokeWidth(7);
        circle.setOpacity(0.16);

        Text paidText = new Text("PAID");
        paidText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 58));
        paidText.setFill(Color.web("#22c55e"));
        paidText.setOpacity(0.14);

        stamp.getChildren().addAll(circle, paidText);
        stamp.getTransforms().add(new Rotate(-12));

        return stamp;
    }

    private static VBox buildFooter() {
        Text line1 = new Text("This is a system-generated receipt.");
        line1.setFont(Font.font("Segoe UI", 10));
        line1.setFill(Color.web("#6b7280"));

        Text line2 = new Text("No signature required. Thank you.");
        line2.setFont(Font.font("Segoe UI", 10));
        line2.setFill(Color.web("#6b7280"));

        VBox footer = new VBox(2, line1, line2);
        footer.setAlignment(Pos.CENTER);

        return footer;
    }

    private static VBox line(String key, String value) {
        Text k = new Text(key + " ");
        k.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
        k.setFill(Color.web("#111827"));

        Text v = new Text(nullSafe(value));
        v.setFont(Font.font("Segoe UI", 10.5));
        v.setFill(Color.web("#111827"));

        return new VBox(new TextFlow(k, v));
    }

    private static int addBillRow(GridPane table, int rowIndex, String label, double amount) {
        Text labelText = new Text(label);
        labelText.setFont(Font.font("Segoe UI", 10.5));
        labelText.setFill(Color.web("#111827"));

        Text amountText = new Text("৳ " + formatMoney(amount));
        amountText.setFont(Font.font("Segoe UI", 10.5));
        amountText.setFill(Color.web("#111827"));

        table.add(labelText, 0, rowIndex);
        table.add(amountText, 1, rowIndex);

        return rowIndex + 1;
    }

    private static PropertyInfo fetchPropertyForFlat(String flatNo) {
        PropertyInfo info = new PropertyInfo();

        String sql = """
                SELECT p.name, p.address
                FROM flats f
                JOIN properties p ON f.property_id = p.id
                WHERE f.flat_no = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flatNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    info.name = rs.getString("name");
                    info.address = rs.getString("address");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    private static String formatMoney(double value) {
        return String.format("%,.2f", value);
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static String amountToWordsBDT(double amount) {
        long taka = (long) Math.floor(amount);
        long paisa = Math.round((amount - taka) * 100);

        String takaWords = numberToWords(taka);
        if (takaWords.isBlank()) {
            takaWords = "Zero";
        }

        if (paisa > 0) {
            return takaWords + " Taka and " + numberToWords(paisa) + " Paisa Only";
        }

        return takaWords + " Taka Only";
    }

    private static String numberToWords(long number) {
        if (number == 0) {
            return "";
        }

        if (number < 20) {
            return BELOW_TWENTY[(int) number];
        }

        if (number < 100) {
            return TENS[(int) number / 10]
                    + (number % 10 != 0 ? " " + numberToWords(number % 10) : "");
        }

        if (number < 1000) {
            return numberToWords(number / 100)
                    + " Hundred"
                    + (number % 100 != 0 ? " " + numberToWords(number % 100) : "");
        }

        if (number < 100000) {
            return numberToWords(number / 1000)
                    + " Thousand"
                    + (number % 1000 != 0 ? " " + numberToWords(number % 1000) : "");
        }

        if (number < 10000000) {
            return numberToWords(number / 100000)
                    + " Lakh"
                    + (number % 100000 != 0 ? " " + numberToWords(number % 100000) : "");
        }

        return numberToWords(number / 10000000)
                + " Crore"
                + (number % 10000000 != 0 ? " " + numberToWords(number % 10000000) : "");
    }

    private static final String[] BELOW_TWENTY = {
            "",
            "One",
            "Two",
            "Three",
            "Four",
            "Five",
            "Six",
            "Seven",
            "Eight",
            "Nine",
            "Ten",
            "Eleven",
            "Twelve",
            "Thirteen",
            "Fourteen",
            "Fifteen",
            "Sixteen",
            "Seventeen",
            "Eighteen",
            "Nineteen"
    };

    private static final String[] TENS = {
            "",
            "",
            "Twenty",
            "Thirty",
            "Forty",
            "Fifty",
            "Sixty",
            "Seventy",
            "Eighty",
            "Ninety"
    };

    private static class PropertyInfo {
        String name = "";
        String address = "";
    }
}
