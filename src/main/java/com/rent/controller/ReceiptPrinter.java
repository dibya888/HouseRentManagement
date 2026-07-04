package com.rent.controller;

import com.rent.model.RentRow;
import com.rent.util.DBUtil;
import com.rent.util.FileOpenUtil;
import com.rent.util.FileSaveUtil;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReceiptPrinter {

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public static void printReceipt(
            RentRow row,
            boolean includePropertyName,
            boolean includePropertyAddress
    ) {
        if (row == null) {
            new Alert(Alert.AlertType.ERROR, "Receipt data missing.").show();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Receipt PDF");
        FileSaveUtil.defaultToDownloads(chooser);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        String safeMonth = row.getBillMonth() == null ? "receipt" : row.getBillMonth();
        String safeFlat = row.getFlatNo() == null ? "flat" : row.getFlatNo().replaceAll("[\\\\/:*?\"<>|]", "_");

        chooser.setInitialFileName("Receipt_" + safeFlat + "_" + safeMonth + ".pdf");

        File file = chooser.showSaveDialog(null);

        if (file == null) {
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            createReceiptPdf(row, includePropertyName, includePropertyAddress, file);

            AuditLogDAO.log(
                    AuditActions.RECEIPT_PRINTED,
                    "Receipt printed/saved. Receipt No: "
                            + receiptNo(row)
                            + ", Flat: "
                            + row.getFlatNo()
                            + ", Tenant: "
                            + row.getTenantName()
                            + ", Month: "
                            + row.getBillMonth()
                            + ", File: "
                            + file.getAbsolutePath()
            );

            FileOpenUtil.showSavedAlertWithOpen(
                    "Receipt PDF saved successfully:\n" + file.getAbsolutePath(),
                    file
            );

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to save receipt PDF.").show();
        }
    }

    private static void createReceiptPdf(
            RentRow row,
            boolean includePropertyName,
            boolean includePropertyAddress,
            File outputFile
    ) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A5);
            doc.addPage(page);

            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float margin = 34;
                float contentWidth = pageWidth - (margin * 2);
                float y = pageHeight - 28;

                drawPaidWatermark(doc, cs, pageWidth / 2f, pageHeight / 2f);

                y = drawLogo(doc, cs, pageWidth, y);

                PropertyInfo property = fetchPropertyForFlat(row.getFlatNo());

                if (includePropertyName && !isBlank(property.name)) {
                    y -= 6;
                    y = drawCenteredText(cs, property.name, FONT_BOLD, 12.5f, pageWidth / 2f, y);
                }

                if (includePropertyAddress && !isBlank(property.address)) {
                    y -= 4;
                    List<String> addressLines = wrapText(property.address, FONT_REGULAR, 9.5f, contentWidth);
                    for (String line : addressLines) {
                        y = drawCenteredText(cs, line, FONT_REGULAR, 9.5f, pageWidth / 2f, y);
                    }
                }

                y -= 8;
                drawLine(cs, margin, y, pageWidth - margin, y);
                y -= 18;

                String billMonthPretty = YearMonth.parse(row.getBillMonth())
                        .format(DateTimeFormatter.ofPattern("MMMM, yyyy"));

                String paymentDate = formatReceiptDate(row.getPaymentDate());

                float leftX = margin;
                float rightX = pageWidth / 2f + 12;

                float metaYStart = y;

                y = drawKeyValue(cs, "Tenant:", row.getTenantName(), leftX, y);
                y = drawKeyValue(cs, "Mobile:", row.getPhone(), leftX, y);
                y = drawKeyValue(cs, "Flat No:", row.getFlatNo(), leftX, y);
                y = drawKeyValue(cs, "Meter No:", row.getMeterNo(), leftX, y);

                float rightY = metaYStart;
                rightY = drawKeyValue(cs, "Bill Month:", billMonthPretty, rightX, rightY);
                rightY = drawKeyValue(cs, "Payment Date:", paymentDate, rightX, rightY);
                rightY = drawKeyValue(cs, "Receipt No:", receiptNo(row), rightX, rightY);

                y = Math.min(y, rightY) - 10;

                drawLine(cs, margin, y, pageWidth - margin, y);
                y -= 18;

                y = drawBillRow(cs, "House Rent", row.getHouseRent(), margin, pageWidth - margin, y);
                y = drawBillRow(cs, "Electricity", row.getElectricity(), margin, pageWidth - margin, y);
                y = drawBillRow(cs, "Water", row.getWater(), margin, pageWidth - margin, y);
                y = drawBillRow(cs, "Gas", row.getGas(), margin, pageWidth - margin, y);
                y = drawBillRow(cs, "Other Bills", row.getOtherBills(), margin, pageWidth - margin, y);
                y = drawBillRow(cs, "Fine", row.getFine(), margin, pageWidth - margin, y);
                y = drawBillRow(cs, "Discount", -row.getDiscount(), margin, pageWidth - margin, y);

                y -= 6;
                drawLine(cs, margin, y, pageWidth - margin, y);
                y -= 18;

                String total = "TOTAL AMOUNT: Tk. " + formatMoney(row.getTotal());
                drawText(cs, total, FONT_BOLD, 13.5f, margin, y);
                y -= 18;

                String words = "In Words: " + amountToWordsBDT(row.getTotal());
                List<String> wordLines = wrapText(words, FONT_REGULAR, 9.5f, contentWidth);

                for (String line : wordLines) {
                    drawText(cs, line, FONT_REGULAR, 9.5f, margin, y);
                    y -= 12;
                }

                float footerY = 28;
                drawLine(cs, margin, footerY + 12, pageWidth - margin, footerY + 12);

                String footer = "This is a system-generated receipt. No signature required. Thank you.";
                drawCenteredText(cs, footer, FONT_REGULAR, 7.8f, pageWidth / 2f, footerY);
            }

            doc.save(outputFile);
        }
    }

    private static float drawLogo(
            PDDocument doc,
            PDPageContentStream cs,
            float pageWidth,
            float y
    ) {
        try (InputStream in = ReceiptPrinter.class.getResourceAsStream("/images/app-logo.png")) {
            if (in == null) {
                return y;
            }

            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                return y;
            }

            PDImageXObject logo = LosslessFactory.createFromImage(doc, img);

            float logoHeight = 42;
            float logoWidth = logoHeight * ((float) img.getWidth() / img.getHeight());
            float x = (pageWidth - logoWidth) / 2f;

            cs.drawImage(logo, x, y - logoHeight, logoWidth, logoHeight);

            return y - logoHeight - 8;

        } catch (Exception e) {
            e.printStackTrace();
            return y;
        }
    }

    private static void drawPaidWatermark(
            PDDocument doc,
            PDPageContentStream cs,
            float centerX,
            float centerY
    ) throws Exception {

        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setStrokingAlphaConstant(0.16f);
        gs.setNonStrokingAlphaConstant(0.16f);

        cs.saveGraphicsState();
        cs.setGraphicsStateParameters(gs);
        cs.transform(Matrix.getRotateInstance(Math.toRadians(-12), centerX, centerY));

        cs.setStrokingColor(new Color(34, 197, 94));
        cs.setNonStrokingColor(new Color(34, 197, 94));

        drawCircle(cs, centerX, centerY, 58);
        drawCircle(cs, centerX, centerY, 44);

        String paid = "PAID";
        float fontSize = 30;
        float textWidth = textWidth(paid, FONT_BOLD, fontSize);

        cs.beginText();
        cs.setFont(FONT_BOLD, fontSize);
        cs.newLineAtOffset(centerX - textWidth / 2f, centerY - 10);
        cs.showText(paid);
        cs.endText();

        cs.restoreGraphicsState();
    }

    private static void drawCircle(
            PDPageContentStream cs,
            float cx,
            float cy,
            float r
    ) throws Exception {
        float k = 0.552284749831f;
        float c = r * k;

        cs.moveTo(cx + r, cy);
        cs.curveTo(cx + r, cy + c, cx + c, cy + r, cx, cy + r);
        cs.curveTo(cx - c, cy + r, cx - r, cy + c, cx - r, cy);
        cs.curveTo(cx - r, cy - c, cx - c, cy - r, cx, cy - r);
        cs.curveTo(cx + c, cy - r, cx + r, cy - c, cx + r, cy);
        cs.closePath();
        cs.stroke();
    }

    private static float drawKeyValue(
            PDPageContentStream cs,
            String key,
            String value,
            float x,
            float y
    ) throws Exception {
        drawText(cs, key + " " + nullSafe(value), FONT_REGULAR, 9.5f, x, y);
        return y - 13;
    }

    private static float drawBillRow(
            PDPageContentStream cs,
            String label,
            double amount,
            float leftX,
            float rightX,
            float y
    ) throws Exception {
        drawText(cs, label, FONT_REGULAR, 10f, leftX, y);

        String money = "Tk. " + formatMoney(amount);
        float moneyWidth = textWidth(money, FONT_REGULAR, 10f);
        drawText(cs, money, FONT_REGULAR, 10f, rightX - moneyWidth, y);

        return y - 13;
    }

    private static void drawText(
            PDPageContentStream cs,
            String text,
            PDType1Font font,
            float size,
            float x,
            float y
    ) throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(safePdfText(text));
        cs.endText();
    }

    private static float drawCenteredText(
            PDPageContentStream cs,
            String text,
            PDType1Font font,
            float size,
            float centerX,
            float y
    ) throws Exception {
        String safe = safePdfText(text);
        float width = textWidth(safe, font, size);
        drawText(cs, safe, font, size, centerX - width / 2f, y);
        return y - (size + 3);
    }

    private static void drawLine(
            PDPageContentStream cs,
            float x1,
            float y1,
            float x2,
            float y2
    ) throws Exception {
        cs.setStrokingColor(new Color(209, 213, 219));
        cs.setLineWidth(0.6f);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
        cs.setStrokingColor(Color.BLACK);
    }

    private static List<String> wrapText(
            String text,
            PDType1Font font,
            float size,
            float maxWidth
    ) throws Exception {
        List<String> lines = new ArrayList<>();

        if (isBlank(text)) {
            return lines;
        }

        String[] words = safePdfText(text).split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;

            if (textWidth(candidate, font, size) <= maxWidth) {
                line = new StringBuilder(candidate);
            } else {
                if (!line.isEmpty()) {
                    lines.add(line.toString());
                }
                line = new StringBuilder(word);
            }
        }

        if (!line.isEmpty()) {
            lines.add(line.toString());
        }

        return lines;
    }

    private static float textWidth(
            String text,
            PDType1Font font,
            float size
    ) throws Exception {
        return font.getStringWidth(safePdfText(text)) / 1000f * size;
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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safePdfText(String text) {
        if (text == null) return "";

        return text
                .replace("৳", "Tk.")
                .replace("–", "-")
                .replace("—", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("‘", "'")
                .replace("’", "'");
    }

    private static String formatReceiptDate(String date) {
        try {
            if (date == null || date.isBlank()) {
                return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }

            return LocalDate.parse(date)
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        } catch (Exception e) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
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
        if (number == 0) return "";
        if (number < 20) return BELOW_TWENTY[(int) number];

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
    private static String receiptNo(RentRow row) {
        if (row == null || row.getReceiptNo() == null || row.getReceiptNo().isBlank()) {
            return "N/A";
        }

        return row.getReceiptNo();
    }

}