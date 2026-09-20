package com.beamillionaire;

/** Separate launcher allows JavaFX to run from the packaged classpath. */
public final class Launcher {
    private Launcher() {}

    public static void main(String[] args) {
        MillionaireApplication.main(args);
    }
}
