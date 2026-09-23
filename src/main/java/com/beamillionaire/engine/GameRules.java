package com.beamillionaire.engine;

import java.util.List;

/** Current project rules shared by the round engine and presentation. */
public final class GameRules {
    public static final int QUESTIONS_PER_DIFFICULTY = com.beamillionaire.domain.QuestionBank.QUESTIONS_PER_DIFFICULTY;
    public static final int QUESTIONS_PER_ROUND = 15;
    public static final List<Integer> CHECKPOINT_QUESTIONS = List.of(4, 9, 12);
    public static final List<Integer> PRIZE_LADDER = List.of(
            100, 300, 500, 1_000, 2_000, 5_000, 10_000, 15_000,
            30_000, 50_000, 75_000, 100_000, 250_000, 500_000, 1_000_000);

    private GameRules() {}

    public static int earnedCredits(int correctAnswers) {
        checkProgress(correctAnswers);
        return correctAnswers == 0 ? 0 : PRIZE_LADDER.get(correctAnswers - 1);
    }

    public static int guaranteedCredits(int correctAnswers) {
        checkProgress(correctAnswers);
        if (correctAnswers == 15) return 1_000_000;
        if (correctAnswers >= 12) return 100_000;
        if (correctAnswers >= 9) return 30_000;
        if (correctAnswers >= 4) return 1_000;
        return 0;
    }

    private static void checkProgress(int correctAnswers) {
        if (correctAnswers < 0 || correctAnswers > QUESTIONS_PER_ROUND) {
            throw new IllegalArgumentException("Correct answer count must be between 0 and 15.");
        }
    }
}
