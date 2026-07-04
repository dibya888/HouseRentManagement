package com.rent.util;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class EmergencyKeyUtil {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final PDType1Font FONT_REGULAR =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public static List<String> generatePlainKeys(int count) {
        List<String> keys = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            keys.add(generateOneKey());
        }

        return keys;
    }

    private static String generateOneKey() {
        return randomBlock(4) + "-" +
                randomBlock(4) + "-" +
                randomBlock(4);
    }

    private static String randomBlock(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARS.length());
            sb.append(CHARS.charAt(index));
        }

        return sb.toString();
    }

    public static void saveKeysAsPdf(Window ownerWindow, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "No recovery keys to save.").showAndWait();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Emergency Recovery Keys");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        chooser.setInitialFileName("Emergency_Recovery_Keys.pdf");

        File file = chooser.showSaveDialog(ownerWindow);

        if (file == null) {
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        try {
            createPdf(keys, file);

            FileOpenUtil.showSavedAlertWithOpen(
                    "Recovery keys saved successfully:\n" + file.getAbsolutePath(),
                    file
            );

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    """
                    Failed to save recovery keys PDF.
        
                    If a PDF with the same name is already open,
                    close it and try again, or choose a different file name.
                    """).showAndWait();
        }
    }

    private static void createPdf(List<String> keys, File file) throws Exception {
        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                float margin = 50;
                float y = 770;

                drawText(cs, "Emergency Recovery Keys", FONT_BOLD, 18, margin, y);
                y -= 30;

                drawText(cs, "House Rent Management System", FONT_REGULAR, 11, margin, y);
                y -= 24;

                drawText(cs, "Important:", FONT_BOLD, 12, margin, y);
                y -= 18;

                drawText(cs, "1. Keep these keys safe.", FONT_REGULAR, 10, margin, y);
                y -= 15;

                drawText(cs, "2. Anyone with these keys can reset your password.", FONT_REGULAR, 10, margin, y);
                y -= 15;

                drawText(cs, "3. Each key can be used only once.", FONT_REGULAR, 10, margin, y);
                y -= 15;

                drawText(cs, "4. If you lose password, PIN, and all keys, only factory reset is possible.", FONT_REGULAR, 10, margin, y);
                y -= 30;

                drawText(cs, "Recovery Keys:", FONT_BOLD, 13, margin, y);
                y -= 22;

                int index = 1;

                for (String key : keys) {
                    drawText(cs, index + ". " + key, FONT_BOLD, 12, margin, y);
                    y -= 22;
                    index++;
                }
            }

            document.save(file);
        }
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