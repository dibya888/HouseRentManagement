package com.rent.controller;

import com.rent.model.ReportRow;
import com.rent.model.ReportSummary;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportPdfExporter {

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public static void exportReport(ReportSummary summary,
                                    ObservableList<ReportRow> rows) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        chooser.setInitialFileName("Reports_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".pdf");

        File file = chooser.showSaveDialog(null);

        if (file == null) {
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            createPdf(summary, rows, file);

            new Alert(Alert.AlertType.INFORMATION,
                    "PDF report exported successfully:\n" + file.getAbsolutePath()
            ).show();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to export PDF report."
            ).show();
        }
    }

    private static void createPdf(ReportSummary summary,
                                  ObservableList<ReportRow> rows,
                                  File file) throws Exception {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(document, page);

            float margin = 40;
            float y = 790;

            // Title
            drawText(cs, "House Rent Management - Reports", FONT_BOLD, 16, margin, y);
            y -= 20;

            drawText(cs,
                    "Generated: " + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")),
                    FONT_REGULAR,
                    9,
                    margin,
                    y
            );

            y -= 25;

            // Summary
            drawText(cs, "Summary", FONT_BOLD, 13, margin, y);
            y -= 18;

            drawText(cs, "Total Income: Tk. " + money(summary.getTotalIncome()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs, "This Month Income: Tk. " + money(summary.getMonthIncome()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs, "This Year Income: Tk. " + money(summary.getYearIncome()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs, "Total Due: Tk. " + money(summary.getTotalDue()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs,
                    "Flats: " + summary.getTotalFlats()
                            + " | Occupied: " + summary.getOccupiedFlats()
                            + " | Available: " + summary.getAvailableFlats()
                            + " | Tenants: " + summary.getTotalTenants(),
                    FONT_REGULAR,
                    10,
                    margin,
                    y
            );

            y -= 28;

            // Table Header
            drawText(cs, "Report Details", FONT_BOLD, 13, margin, y);
            y -= 18;

            drawTableHeader(cs, margin, y);
            y -= 16;

            for (ReportRow row : rows) {

                if (y < 70) {
                    cs.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);

                    y = 790;
                    drawTableHeader(cs, margin, y);
                    y -= 16;
                }

                drawRow(cs, row, margin, y);
                y -= 15;
            }

            cs.close();
            document.save(file);
        }
    }

    private static void drawTableHeader(PDPageContentStream cs,
                                        float x,
                                        float y) throws Exception {

        drawText(cs, "Title", FONT_BOLD, 8, x, y);
        drawText(cs, "Month", FONT_BOLD, 8, x + 65, y);
        drawText(cs, "Date", FONT_BOLD, 8, x + 110, y);
        drawText(cs, "Flat", FONT_BOLD, 8, x + 165, y);
        drawText(cs, "Tenant", FONT_BOLD, 8, x + 210, y);
        drawText(cs, "Total", FONT_BOLD, 8, x + 315, y);
        drawText(cs, "Paid", FONT_BOLD, 8, x + 370, y);
        drawText(cs, "Due", FONT_BOLD, 8, x + 425, y);
        drawText(cs, "Status", FONT_BOLD, 8, x + 480, y);
    }

    private static void drawRow(PDPageContentStream cs,
                                ReportRow row,
                                float x,
                                float y) throws Exception {

        drawText(cs, cut(row.getTitle(), 10), FONT_REGULAR, 8, x, y);
        drawText(cs, cut(row.getMonth(), 8), FONT_REGULAR, 8, x + 65, y);
        drawText(cs, cut(row.getDate(), 10), FONT_REGULAR, 8, x + 110, y);
        drawText(cs, cut(row.getFlatNo(), 8), FONT_REGULAR, 8, x + 165, y);
        drawText(cs, cut(row.getTenant(), 16), FONT_REGULAR, 8, x + 210, y);
        drawText(cs, money(row.getTotal()), FONT_REGULAR, 8, x + 315, y);
        drawText(cs, money(row.getPaid()), FONT_REGULAR, 8, x + 370, y);
        drawText(cs, money(row.getDue()), FONT_REGULAR, 8, x + 425, y);
        drawText(cs, cut(row.getStatus(), 8), FONT_REGULAR, 8, x + 480, y);
    }

    private static void drawText(PDPageContentStream cs,
                                 String text,
                                 PDType1Font font,
                                 float size,
                                 float x,
                                 float y) throws Exception {

        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(safe(text));
        cs.endText();
    }

    private static String money(double value) {
        return String.format("%,.2f", value);
    }

    private static String cut(String value, int max) {
        if (value == null) return "";
        if (value.length() <= max) return value;
        return value.substring(0, max - 3) + "...";
    }

    private static String safe(String text) {
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
}