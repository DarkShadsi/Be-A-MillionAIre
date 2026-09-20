package com.beamillionaire;

/** Separate launcher allows JavaFX to run from the packaged classpath. */
public final class Launcher {
    private Launcher() {}

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--cli")) {
            CLIMode.main(args);
        } else {
            MillionaireApplication.main(args);
        }
    }
}
