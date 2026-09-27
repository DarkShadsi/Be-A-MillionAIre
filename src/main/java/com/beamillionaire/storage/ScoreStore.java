package com.beamillionaire.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

/** Local history; a damaged file is reported and never silently overwritten. */
public final class ScoreStore {
    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ScoreStore(Path directory) { file = directory.resolve("scores.json"); }

    public List<ScoreEntry> load() throws IOException {
        if (!Files.exists(file)) return List.of();
        try {
            var entries = gson.fromJson(Files.readString(file), ScoreEntry[].class);
            if (entries == null || Arrays.stream(entries).anyMatch(Objects::isNull))
                throw new IllegalArgumentException("Expected a list of completed rounds.");
            var ids = new HashSet<String>();
            for (var entry : entries) if (!ids.add(entry.id()))
                throw new IllegalArgumentException("Duplicate round ID.");
            return Arrays.stream(entries)
                    .sorted(Comparator.comparing((ScoreEntry e) -> Instant.parse(e.playedAt())).reversed()).toList();
        } catch (RuntimeException error) {
            throw new IOException("Score history could not be read. The existing file has been preserved.", error);
        }
    }

    public void append(ScoreEntry entry) throws IOException {
        Objects.requireNonNull(entry);
        var entries = new ArrayList<>(load());
        if (entries.stream().anyMatch(saved -> saved.id().equals(entry.id()))) return;
        entries.add(entry);
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "scores-", ".tmp");
        try {
            Files.writeString(temporary, gson.toJson(entries) + System.lineSeparator());
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }
}
