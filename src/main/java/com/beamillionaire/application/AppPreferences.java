package com.beamillionaire.application;

import java.util.Objects;

/** Preferences shared by startup, settings persistence, and the presentation. */
public record AppPreferences(Theme theme, boolean reduceMotion, boolean soundEnabled) {
    public AppPreferences {
        Objects.requireNonNull(theme, "Theme is required.");
    }

    public AppPreferences(Theme theme, boolean reduceMotion) {
        this(theme, reduceMotion, true);
    }

    public static AppPreferences defaults() {
        return new AppPreferences(Theme.DARK, false);
    }
}
