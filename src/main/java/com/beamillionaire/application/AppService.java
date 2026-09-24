package com.beamillionaire.application;

import com.beamillionaire.application.port.QuestionRepository;
import com.beamillionaire.application.port.SettingsRepository;
import com.beamillionaire.domain.QuestionBank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/** Application use cases; independent of JavaFX and the storage implementation. */
public final class AppService {
    private final QuestionRepository questions;
    private final SettingsRepository settings;

    public AppService(QuestionRepository questions, SettingsRepository settings) {
        this.questions = Objects.requireNonNull(questions);
        this.settings = Objects.requireNonNull(settings);
    }

    public StartupState load() {
        var notices = new ArrayList<String>();
        AppPreferences preferences = AppPreferences.defaults();
        try {
            preferences = settings.load();
        } catch (IOException exception) {
            notices.add("Saved settings could not be read. Using the default preferences.");
        }

        QuestionBank bank = QuestionBank.empty();
        boolean available = false;
        try {
            bank = questions.load();
            available = true;
        } catch (IOException exception) {
            String detail = exception.getMessage();
            notices.add(detail == null || detail.isBlank() ? "Question bank could not be read." : detail);
        }
        return new StartupState(preferences, bank, available, notices);
    }

    /** A save failure must not undo the preferences already applied by the presentation. */
    public Optional<String> savePreferences(AppPreferences preferences) {
        Objects.requireNonNull(preferences);
        try {
            settings.save(preferences);
            return Optional.empty();
        } catch (IOException exception) {
            return Optional.of("The preferences changed, but they could not be saved for the next launch.");
        }
    }
}
