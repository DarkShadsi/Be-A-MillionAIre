package com.beamillionaire.domain;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Validated, immutable questions indexed once for display and round selection. */
public final class QuestionBank {
    public static final int QUESTIONS_PER_DIFFICULTY = 5;
    private final List<Question> questions;
    private final Map<Category, Map<Difficulty, List<Question>>> pools;

    public QuestionBank(List<Question> questions) {
        Objects.requireNonNull(questions, "Questions are required.");
        var ids = new HashSet<String>();
        for (int index = 0; index < questions.size(); index++) {
            Question question = Objects.requireNonNull(questions.get(index), "Question must not be null.");
            if (!ids.add(question.id())) {
                throw new IllegalArgumentException("Duplicate question ID at position " + (index + 1)
                        + ": " + question.id());
            }
        }
        this.questions = List.copyOf(questions);

        var index = new EnumMap<Category, Map<Difficulty, List<Question>>>(Category.class);
        for (Category category : Category.values()) {
            var difficulties = new EnumMap<Difficulty, List<Question>>(Difficulty.class);
            for (Difficulty difficulty : Difficulty.values()) {
                difficulties.put(difficulty, new ArrayList<>());
            }
            index.put(category, difficulties);
        }
        for (Question question : this.questions) {
            index.get(question.category()).get(question.difficulty()).add(question);
        }
        index.replaceAll((category, difficulties) -> {
            difficulties.replaceAll((difficulty, pool) -> List.copyOf(pool));
            return Map.copyOf(difficulties);
        });
        pools = Map.copyOf(index);
    }

    public static QuestionBank empty() {
        return new QuestionBank(List.of());
    }

    public List<Question> questions() {
        return questions;
    }

    public int size() {
        return questions.size();
    }

    public List<Question> pool(Category category, Difficulty difficulty) {
        return pools.get(Objects.requireNonNull(category, "Category is required."))
                .get(Objects.requireNonNull(difficulty, "Difficulty is required."));
    }
}
