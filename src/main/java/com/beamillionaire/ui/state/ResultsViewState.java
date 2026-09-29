package com.beamillionaire.ui.state;

import java.util.Objects;

public record ResultsViewState(Outcome outcome, String category, int correctAnswers, long payout,
                               boolean replayEnabled) {
    public enum Outcome { WIN, LOSS, WALK_AWAY }
    public ResultsViewState {
        Objects.requireNonNull(outcome);
        Objects.requireNonNull(category);
        if (correctAnswers < 0 || correctAnswers > 15 || payout < 0)
            throw new IllegalArgumentException("Invalid result presentation.");
    }
}
