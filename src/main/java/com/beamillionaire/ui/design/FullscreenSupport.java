package com.beamillionaire.ui.design;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.util.function.BooleanSupplier;

public final class FullscreenSupport {
    private FullscreenSupport() {}

    /** The overlay callback returns true only if it consumed Escape. */
    public static void install(Stage stage, BooleanSupplier closeOverlay) {
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                if (!closeOverlay.getAsBoolean()) stage.setFullScreen(false);
                event.consume();
            }
        });
    }
}
