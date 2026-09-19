package com.beamillionaire.testsupport;

import com.beamillionaire.domain.*;
import java.util.ArrayList;
import java.util.List;

public final class QuestionFixtures {
    private QuestionFixtures() {}

    public static Question question(String id, Category category, Difficulty difficulty) {
        return new Question(id, category, difficulty, "Fixture question",
                List.of(new Choice("A", "First"), new Choice("B", "Second"),
                        new Choice("C", "Third"), new Choice("D", "Fourth")), "A", "Fixture clue");
    }

    public static List<Question> questions(Category category, int perDifficulty) {
        var questions = new ArrayList<Question>();
        for (Difficulty difficulty : Difficulty.values()) {
            for (int i = 0; i < perDifficulty; i++) {
                questions.add(question(category + "-" + difficulty + "-" + i, category, difficulty));
            }
        }
        return questions;
    }
}
