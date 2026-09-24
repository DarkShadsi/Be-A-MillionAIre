package com.beamillionaire.application;

import com.beamillionaire.domain.QuestionBank;
import java.util.List;
import java.util.Objects;

/** Startup failures are independent: a failed bank must not discard saved preferences. */
public record StartupState(AppPreferences preferences, QuestionBank bank, boolean bankAvailable, List<String> notices) {
    public StartupState {
        Objects.requireNonNull(preferences, "Preferences are required.");
        Objects.requireNonNull(bank, "Question bank is required.");
        notices = List.copyOf(notices);
    }

    public Theme theme() {
        return preferences.theme();
    }
}
