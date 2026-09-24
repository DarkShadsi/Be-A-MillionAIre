package com.beamillionaire.storage;

import com.beamillionaire.application.AppPreferences;
import com.beamillionaire.application.Theme;
import com.beamillionaire.application.port.SettingsRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class SettingsStore implements SettingsRepository {
    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SettingsStore(Path directory) {
        file = directory.resolve("settings.json");
    }

    @Override
    public AppPreferences load() throws IOException {
        if (!Files.exists(file)) return AppPreferences.defaults();
        try {
            var settings = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if (!settings.has("theme") || !settings.get("theme").isJsonPrimitive()
                    || !settings.getAsJsonPrimitive("theme").isString()) {
                throw new IllegalArgumentException("Missing or unknown theme.");
            }
            Theme theme = Theme.valueOf(settings.get("theme").getAsString());
            boolean reduceMotion = false;
            if (settings.has("reduceMotion")) {
                if (!settings.get("reduceMotion").isJsonPrimitive()
                        || !settings.getAsJsonPrimitive("reduceMotion").isBoolean()) {
                    throw new IllegalArgumentException("reduceMotion must be a boolean.");
                }
                reduceMotion = settings.get("reduceMotion").getAsBoolean();
            }
            boolean sound = true;
            if (settings.has("soundEnabled")) {
                var value = settings.get("soundEnabled");
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
                    throw new IllegalArgumentException("soundEnabled must be a boolean.");
                sound = value.getAsBoolean();
            }
            return new AppPreferences(theme, reduceMotion, sound);
        } catch (RuntimeException exception) {
            throw new IOException("Could not read settings from " + file + ".", exception);
        }
    }

    @Override
    public void save(AppPreferences preferences) throws IOException {
        Objects.requireNonNull(preferences, "Preferences are required.");
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(file.toAbsolutePath().getParent(), "settings-", ".tmp");
        try {
            Files.writeString(temporary, gson.toJson(preferences) + System.lineSeparator());
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
