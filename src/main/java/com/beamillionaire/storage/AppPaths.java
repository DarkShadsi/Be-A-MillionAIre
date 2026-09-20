package com.beamillionaire.storage;

import java.nio.file.Path;

public final class AppPaths {
    private AppPaths() {}

    public static Path userDataDirectory() {
        String override = System.getProperty("millionaire.dataDir");
        if (override != null && !override.isBlank()) return Path.of(override);
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Path.of(localAppData, "BeAMillionaire");
        }
        return Path.of(System.getProperty("user.home"), ".be-a-millionaire");
    }

    public static Path questionDataDirectory() {
        String override = System.getProperty("millionaire.questionDataDir");
        if (override != null && !override.isBlank()) return Path.of(override);
        String launcher = System.getProperty("jpackage.app-path");
        if (launcher != null && !launcher.isBlank()) {
            return Path.of(launcher).toAbsolutePath().getParent().resolve("MCQ");
        }
        return Path.of("MCQ");
    }
}
