package com.rent.controller;

import com.rent.model.MoveOutSettlement;
import com.rent.util.FileOpenUtil;

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

public class SettlementPdfExporter {

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public static boolean exportSettlement(MoveOutSettlement s) {
        if (s == null) {
            new Alert(Alert.AlertType.ERROR, "Settlement data missing.").showAndWait();
            return false;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Settlement PDF");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        String safeName = safeFilePart(s.getTenantName());
        String safeFlat = safeFilePart(s.getFlatNo());

        chooser.setInitialFileName(
                "Settlement_" + safeFlat + "_" + safeName + ".pdf"
        );

        File file = chooser.showSaveDialog(null);

        if (file == null) {
            return false;
        }

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            createPdf(s, file);

            FileOpenUtil.showSavedAlertWithOpen(
                    "Settlement PDF saved successfully:\n" + file.getAbsolutePath(),
                    file
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Failed to save settlement PDF."
            ).showAndWait();

            return false;
        }
    }

    private static void createPdf(MoveOutSettlement s, File file) throws Exception {
        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float margin = 60;
                float y = 780;

                drawText(cs, "Move-Out Settlement", FONT_BOLD, 18, margin, y);
                y -= 28;

                drawText(cs, "Generated: "
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")),
                        FONT_REGULAR, 10, margin, y);
                y -= 30;

                drawText(cs, "Tenant Information", FONT_BOLD, 13, margin, y);
                y -= 20;

                y = drawLine(cs, "Tenant:", s.getTenantName(), margin, y);
                y = drawLine(cs, "Phone:", s.getTenantPhone(), margin, y);
                y = drawLine(cs, "Flat No:", s.getFlatNo(), margin, y);
                y = drawLine(cs, "Move Out Date:", s.getMoveOutDate(), margin, y);

                y -= 10;

                drawText(cs, "Settlement Summary", FONT_BOLD, 13, margin, y);
                y -= 20;

                y = drawLine(cs, "Unpaid Due:", money(s.getUnpaidDue()), margin, y);
                y = drawLine(cs, "Security Deposit:", money(s.getSecurityDeposit()), margin, y);
                y = drawLine(cs, "Refund Amount:", money(s.getRefundAmount()), margin, y);
                y = drawLine(cs, "Payable Amount:", money(s.getPayableAmount()), margin, y);
                y = drawLine(cs, "Result:", safe(s.getResult()), margin, y);

                y -= 10;

                drawText(cs, "Reason / Notes", FONT_BOLD, 13, margin, y);
                y -= 20;

                drawText(cs, safe(s.getReason()), FONT_REGULAR, 10, margin, y);

                y -= 50;

                drawText(cs,
                        "This is a system-generated settlement document.",
                        FONT_REGULAR,
                        9,
                        margin,
                        y
                );
            }

            doc.save(file);
        }
    }

    private static float drawLine(PDPageContentStream cs,
                                  String label,
                                  String value,
                                  float x,
                                  float y) throws Exception {

        drawText(cs, label + " " + safe(value), FONT_REGULAR, 11, x, y);
        return y - 18;
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
        return "Tk. " + String.format("%,.2f", value);
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

    private static String safeFilePart(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}