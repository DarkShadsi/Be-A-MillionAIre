package com.beamillionaire.domain;

import java.util.List;

public record Question(String id, Category category, Difficulty difficulty, String text,
                       List<Choice> choices, String correctChoiceId, String clue) {
    public Question {
        if (id == null || id.isBlank() || text == null || text.isBlank()
                || clue == null || clue.isBlank()) {
            throw new IllegalArgumentException("Question ID, text, and clue are required.");
        }
        if (category == null || difficulty == null) {
            throw new IllegalArgumentException("A valid category and difficulty are required.");
        }
        if (choices == null || choices.size() != 4 || choices.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Exactly four non-null choices are required.");
        }
        choices = List.copyOf(choices);
        if (choices.stream().map(Choice::id).distinct().count() != 4) {
            throw new IllegalArgumentException("Choice IDs must be unique.");
        }
        if (choices.stream().noneMatch(choice -> choice.id().equals(correctChoiceId))) {
            throw new IllegalArgumentException("correctChoiceId must match a choice ID.");
        }
    }
}

