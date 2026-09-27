package com.beamillionaire.storage;

import com.beamillionaire.engine.GameRound;
import java.time.Instant;
import java.util.Objects;

/** One completed round. The ID makes repeated save requests harmless. */
public record ScoreEntry(String id, String playedAt, String category,
                         GameRound.Status outcome, int correctAnswers, int payout) {
    public ScoreEntry {
        if (id == null || id.isBlank() || category == null || category.isBlank())
            throw new IllegalArgumentException("Round ID and category are required.");
        Instant.parse(Objects.requireNonNull(playedAt));
        if (outcome != GameRound.Status.WON && outcome != GameRound.Status.LOST
                && outcome != GameRound.Status.WALKED_AWAY)
            throw new IllegalArgumentException("Only completed rounds can be saved.");
        if (correctAnswers < 0 || correctAnswers > 15 || payout < 0 || payout > 1_000_000)
            throw new IllegalArgumentException("Invalid round result.");
    }
}
