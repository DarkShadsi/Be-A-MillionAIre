package com.beamillionaire.domain;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static com.beamillionaire.testsupport.QuestionFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class QuestionBankTest {
    @Test
    void rejectsDuplicateIdsAcrossCategories() {
        var first = question("duplicate", Category.AI_FUNDAMENTALS, Difficulty.EASY);
        var second = question("duplicate", Category.MACHINE_LEARNING, Difficulty.HARD);
        var error = assertThrows(IllegalArgumentException.class,
                () -> new QuestionBank(List.of(first, second)));
        assertTrue(error.getMessage().contains("position 2"));
    }

    @Test
    void snapshotsTheSourceAndExposesImmutablePools() {
        var input = new ArrayList<>(questions(Category.AI_FUNDAMENTALS, 2));
        var bank = new QuestionBank(input);
        input.clear();
        assertEquals(6, bank.size());
        assertEquals(2, bank.pool(Category.AI_FUNDAMENTALS, Difficulty.EASY).size());
        assertThrows(UnsupportedOperationException.class, () -> bank.questions().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> bank.pool(Category.AI_FUNDAMENTALS, Difficulty.EASY).clear());
    }

    @Test
    void indexesEveryQuestionInItsOwnCategoryAndDifficulty() {
        var input = new ArrayList<>(questions(Category.AI_FUNDAMENTALS, 2));
        input.addAll(questions(Category.MACHINE_LEARNING, 3));
        var bank = new QuestionBank(input);
        assertEquals(15, bank.size());
        for (Question question : bank.questions()) {
            assertTrue(bank.pool(question.category(), question.difficulty()).contains(question));
        }
        assertEquals(3, bank.pool(Category.MACHINE_LEARNING, Difficulty.HARD).size());
        assertTrue(bank.pool(Category.DEEP_LEARNING, Difficulty.HARD).isEmpty());
    }

    @Test
    void permitsAnEmptyBankDuringAuthoring() {
        var bank = QuestionBank.empty();
        assertEquals(0, bank.size());
        for (Category category : Category.values()) {
            for (Difficulty difficulty : Difficulty.values()) {
                assertTrue(bank.pool(category, difficulty).isEmpty());
            }
        }
    }
}
