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
import java.util.ArrayList;
import java.util.List;

public class ReportPdfExporter {

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final float FONT_SIZE = 8f;
    private static final float HEADER_FONT_SIZE = 8f;
    private static final float LINE_HEIGHT = 10f;
    private static final float HEADER_HEIGHT = 18f;

    private static final float[] COL_OFFSET = {
            0,    // Title
            100,  // Month
            150,  // Date
            220,  // Flat
            265,  // Tenant/Description
            455,  // Total
            515,  // Info
            575,  // Paid
            635,  // Due
            695   // Status
    };

    private static final float[] COL_WIDTH = {
            90,   // Title
            45,   // Month
            65,   // Date
            40,   // Flat
            180,  // Tenant/Description
            55,   // Total
            55,   // Info
            55,   // Paid
            55,   // Due
            75    // Status
    };

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

            PDPage page = new PDPage(landscapeA4());
            document.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(document, page);

            float margin = 35;
            float y = 540;

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

            drawText(cs, "Total Repair Cost: Tk. " + money(summary.getTotalRepairCost()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs, "Owner Paid Repairs: Tk. " + money(summary.getOwnerPaidRepairCost()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs, "Tenant Paid Repairs: Tk. " + money(summary.getTenantPaidRepairCost()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs, "This Month Repair: Tk. " + money(summary.getMonthRepairCost()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs, "This Year Repair: Tk. " + money(summary.getYearRepairCost()), FONT_REGULAR, 10, margin, y);
            y -= 14;

            drawText(cs, "Net Income: Tk. " + money(summary.getNetProfit()), FONT_BOLD, 10, margin, y);
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

            y -= 30;

            // Table title
            drawText(cs, "Report Details", FONT_BOLD, 13, margin, y);
            y -= 24;

            drawTableHeader(cs, margin, y);
            y -= 18;

            // Table rows with wrapping
            for (ReportRow row : rows) {

                float rowHeight = calculateRowHeight(row);

                if (y - rowHeight < 60) {
                    cs.close();

                    page = new PDPage(landscapeA4());
                    document.addPage(page);
                    cs = new PDPageContentStream(document, page);

                    y = 540;

                    drawTableHeader(cs, margin, y);
                    y -= 18;
                }

                drawWrappedRow(cs, row, margin, y);
                y -= rowHeight;
            }

            cs.close();
            document.save(file);
        }
    }

    private static void drawTableHeader(PDPageContentStream cs,
                                        float x,
                                        float y) throws Exception {

        drawCenteredText(cs, "Title", FONT_BOLD, HEADER_FONT_SIZE, x, y, 90);
        drawCenteredText(cs, "Month", FONT_BOLD, HEADER_FONT_SIZE, x + 100, y, 45);
        drawCenteredText(cs, "Date", FONT_BOLD, HEADER_FONT_SIZE, x + 150, y, 65);
        drawCenteredText(cs, "Flat", FONT_BOLD, HEADER_FONT_SIZE, x + 220, y, 40);
        drawCenteredText(cs, "Tenant/Description", FONT_BOLD, HEADER_FONT_SIZE, x + 265, y, 180);
        drawCenteredText(cs, "Total", FONT_BOLD, HEADER_FONT_SIZE, x + 455, y, 55);
        drawCenteredText(cs, "Info", FONT_BOLD, HEADER_FONT_SIZE, x + 515, y, 55);
        drawCenteredText(cs, "Paid", FONT_BOLD, HEADER_FONT_SIZE, x + 575, y, 55);
        drawCenteredText(cs, "Due", FONT_BOLD, HEADER_FONT_SIZE, x + 635, y, 55);
        drawCenteredText(cs, "Status", FONT_BOLD, HEADER_FONT_SIZE, x + 695, y, 75);
    }

    private static void drawWrappedRow(PDPageContentStream cs,
                                       ReportRow row,
                                       float x,
                                       float y) throws Exception {

        drawWrappedCenteredText(cs, row.getTitle(), FONT_REGULAR, FONT_SIZE, x, y, 90);
        drawWrappedCenteredText(cs, row.getMonth(), FONT_REGULAR, FONT_SIZE, x + 100, y, 45);
        drawWrappedCenteredText(cs, row.getDate(), FONT_REGULAR, FONT_SIZE, x + 150, y, 65);
        drawWrappedCenteredText(cs, row.getFlatNo(), FONT_REGULAR, FONT_SIZE, x + 220, y, 40);
        drawWrappedCenteredText(cs, row.getTenant(), FONT_REGULAR, FONT_SIZE, x + 265, y, 180);
        drawWrappedCenteredText(cs, money(row.getTotal()), FONT_REGULAR, FONT_SIZE, x + 455, y, 55);
        drawWrappedCenteredText(cs, row.getExtraInfo(), FONT_REGULAR, FONT_SIZE, x + 515, y, 55);
        drawWrappedCenteredText(cs, money(row.getPaid()), FONT_REGULAR, FONT_SIZE, x + 575, y, 55);
        drawWrappedCenteredText(cs, money(row.getDue()), FONT_REGULAR, FONT_SIZE, x + 635, y, 55);
        drawWrappedCenteredText(cs, row.getStatus(), FONT_REGULAR, FONT_SIZE, x + 695, y, 75);
    }

    private static float calculateRowHeight(ReportRow row) throws Exception {
        int maxLines = 1;

        maxLines = Math.max(maxLines, wrapText(row.getTitle(), FONT_REGULAR, FONT_SIZE, 90).size());
        maxLines = Math.max(maxLines, wrapText(row.getMonth(), FONT_REGULAR, FONT_SIZE, 45).size());
        maxLines = Math.max(maxLines, wrapText(row.getDate(), FONT_REGULAR, FONT_SIZE, 65).size());
        maxLines = Math.max(maxLines, wrapText(row.getFlatNo(), FONT_REGULAR, FONT_SIZE, 40).size());
        maxLines = Math.max(maxLines, wrapText(row.getTenant(), FONT_REGULAR, FONT_SIZE, 180).size());
        maxLines = Math.max(maxLines, wrapText(money(row.getTotal()), FONT_REGULAR, FONT_SIZE, 55).size());
        maxLines = Math.max(maxLines, wrapText(row.getExtraInfo(), FONT_REGULAR, FONT_SIZE, 55).size());
        maxLines = Math.max(maxLines, wrapText(money(row.getPaid()), FONT_REGULAR, FONT_SIZE, 55).size());
        maxLines = Math.max(maxLines, wrapText(money(row.getDue()), FONT_REGULAR, FONT_SIZE, 55).size());
        maxLines = Math.max(maxLines, wrapText(row.getStatus(), FONT_REGULAR, FONT_SIZE, 75).size());

        return (maxLines * LINE_HEIGHT) + 10;
    }

    private static void drawWrappedText(PDPageContentStream cs,
                                        String text,
                                        PDType1Font font,
                                        float size,
                                        float x,
                                        float y,
                                        float maxWidth) throws Exception {

        List<String> lines = wrapText(text, font, size, maxWidth);

        float currentY = y;

        for (String line : lines) {
            drawText(cs, line, font, size, x, currentY);
            currentY -= LINE_HEIGHT;
        }
    }


    private static List<String> wrapText(String text,
                                         PDType1Font font,
                                         float size,
                                         float maxWidth) throws Exception {

        List<String> lines = new ArrayList<>();

        String safeText = safe(text);

        if (safeText == null || safeText.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] words = safeText.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String candidate = line.length() == 0
                    ? word
                    : line + " " + word;

            if (textWidth(candidate, font, size) <= maxWidth) {
                line = new StringBuilder(candidate);
            } else {
                if (line.length() > 0) {
                    lines.add(line.toString());
                }

                // If one word is too long, cut it safely
                if (textWidth(word, font, size) > maxWidth) {
                    lines.add(cutLongWord(word, font, size, maxWidth));
                    line = new StringBuilder();
                } else {
                    line = new StringBuilder(word);
                }
            }
        }

        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines;
    }

    private static String cutLongWord(String word,
                                      PDType1Font font,
                                      float size,
                                      float maxWidth) throws Exception {

        StringBuilder result = new StringBuilder();

        for (char c : word.toCharArray()) {
            String candidate = result.toString() + c;

            if (textWidth(candidate + "...", font, size) > maxWidth) {
                return result + "...";
            }

            result.append(c);
        }

        return result.toString();
    }

    private static void drawCenteredText(PDPageContentStream cs,
                                         String text,
                                         PDType1Font font,
                                         float size,
                                         float x,
                                         float y,
                                         float width) throws Exception {

        String safeText = safe(text);
        float widthOfText = textWidth(safeText, font, size);
        float centeredX = x + ((width - widthOfText) / 2f);

        drawText(cs, safeText, font, size, centeredX, y);
    }

    private static void drawWrappedCenteredText(PDPageContentStream cs,
                                                String text,
                                                PDType1Font font,
                                                float size,
                                                float x,
                                                float y,
                                                float maxWidth) throws Exception {

        List<String> lines = wrapText(text, font, size, maxWidth - 4);

        float currentY = y;

        for (String line : lines) {
            String safeLine = safe(line);
            float lineWidth = textWidth(safeLine, font, size);
            float centeredX = x + ((maxWidth - lineWidth) / 2f);

            drawText(cs, safeLine, font, size, centeredX, currentY);
            currentY -= LINE_HEIGHT;
        }
    }

    private static float textWidth(String text,
                                   PDType1Font font,
                                   float size) throws Exception {

        return font.getStringWidth(safe(text)) / 1000f * size;
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

    private static PDRectangle landscapeA4() {
        return new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
    }
}