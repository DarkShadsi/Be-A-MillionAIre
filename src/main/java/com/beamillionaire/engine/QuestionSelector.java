package com.beamillionaire.engine;

import com.beamillionaire.domain.Category;
import com.beamillionaire.domain.Difficulty;
import com.beamillionaire.domain.Question;
import com.beamillionaire.domain.QuestionBank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Selects questions and shuffles choices using the supplied random source. */
public final class QuestionSelector {
    private Question shuffleChoices(Question question, Random random) {
        var shuffled = new ArrayList<>(question.choices());
        Collections.shuffle(shuffled, random);
        var displayed = new ArrayList<com.beamillionaire.domain.Choice>();
        String correct = null;
        for (int index = 0; index < shuffled.size(); index++) {
            var original = shuffled.get(index);
            String label = String.valueOf((char) ('A' + index));
            displayed.add(new com.beamillionaire.domain.Choice(label, original.text()));
            if (original.id().equals(question.correctChoiceId())) correct = label;
        }
        return new Question(question.id(), question.category(), question.difficulty(),
                question.text(), displayed, correct, question.clue());
    }

    public List<Question> select(QuestionBank bank, Category category, Random random) {
        Objects.requireNonNull(bank, "Question bank is required.");
        Objects.requireNonNull(category, "Category is required.");
        Objects.requireNonNull(random, "Random source is required.");
        List<Question> selected = new ArrayList<>();
        for (Difficulty difficulty : Difficulty.values()) {
            var pool = new ArrayList<>(bank.pool(category, difficulty));
            if (pool.size() < QuestionBank.QUESTIONS_PER_DIFFICULTY) {
                throw new IllegalArgumentException(category.displayName() + " needs at least "
                        + QuestionBank.QUESTIONS_PER_DIFFICULTY + " " + difficulty + " questions.");
            }
            Collections.shuffle(pool, random);
            for (Question question : pool.subList(0, QuestionBank.QUESTIONS_PER_DIFFICULTY)) {
                selected.add(shuffleChoices(question, random));
            }
        }
        return List.copyOf(selected);
    }
}
