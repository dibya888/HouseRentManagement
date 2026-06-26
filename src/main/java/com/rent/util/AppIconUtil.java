package com.rent.util;

import javafx.collections.ListChangeListener;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Ensures every window in the application — the main stage, popups,
 * modal dialogs, and Alert dialogs — consistently shows the app icon,
 * without requiring each call site to set it manually.
 */
public final class AppIconUtil {

    private static final Image APP_ICON =
            new Image(AppIconUtil.class.getResourceAsStream("/images/app-icon.png"));

    private static boolean installed = false;

    private AppIconUtil() {
    }

    /**
     * Call once, early in Application.start(), before any window is shown.
     * Watches every window the JVM opens (Stages opened via new Stage(),
     * Alert/Dialog internal stages, etc.) and applies the app icon to any
     * Stage that doesn't already have one set.
     */
    public static void installGlobalIcon() {
        if (installed) {
            return;
        }
        installed = true;

        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (!change.wasAdded()) {
                    continue;
                }
                for (Window w : change.getAddedSubList()) {
                    if (w instanceof Stage stage && stage.getIcons().isEmpty()) {
                        stage.getIcons().add(APP_ICON);
                    }
                }
            }
        });
    }

    /**
     * Manual/explicit application of the app icon to a specific stage.
     * Not required if installGlobalIcon() has been called, but kept
     * available for any code path that wants to be explicit.
     */
    public static void apply(Stage stage) {
        if (stage.getIcons().isEmpty()) {
            stage.getIcons().add(APP_ICON);
        }
    }
}
