package com.rent.util;

import javafx.stage.FileChooser;

import java.io.File;

/**
 * Points every "Save As" FileChooser at the user's Downloads folder by
 * default, instead of the OS's usual last-used/default location, so
 * users don't have to navigate to a specific folder every time they
 * export a report, receipt, settlement, or recovery key PDF.
 */
public final class FileSaveUtil {

    private FileSaveUtil() {
    }

    public static void defaultToDownloads(FileChooser chooser) {
        File downloads = new File(System.getProperty("user.home"), "Downloads");

        if (downloads.exists() && downloads.isDirectory()) {
            chooser.setInitialDirectory(downloads);
            return;
        }

        // Downloads folder missing for some reason - fall back to the
        // user's home directory rather than leaving it unset.
        File home = new File(System.getProperty("user.home"));
        if (home.exists() && home.isDirectory()) {
            chooser.setInitialDirectory(home);
        }
    }
}
