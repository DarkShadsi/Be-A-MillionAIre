package com.beamillionaire.engine;

import com.beamillionaire.domain.Category;
import com.beamillionaire.domain.Question;
import com.beamillionaire.domain.QuestionBank;

import java.util.List;
import java.util.Random;

/** Holds a player's progress through one game round. */
public final class GameRound {
    public enum Status { ANSWERING, CORRECT, WON, LOST, WALKED_AWAY }

    private final List<Question> questions;
    private Status status = Status.ANSWERING;
    private int index;
    private int correctAnswers;
    private int payout;
    private String selected;

    public GameRound(QuestionBank bank, Category category, Random random) {
        questions = new QuestionSelector().select(bank, category, random);
    }

    public Question question() { return questions.get(index); }
    public int questionNumber() { return index + 1; }
    public int correctAnswers() { return correctAnswers; }
    public int credits() { return GameRules.earnedCredits(correctAnswers); }
    public int payout() { return payout; }
    public Status status() { return status; }
    public String selectedAnswer() { return selected; }
    public boolean selectionEnabled() { return status == Status.ANSWERING; }
    public boolean lockEnabled() { return selectionEnabled() && selected != null; }
    public boolean walkAwayEnabled() { return selectionEnabled(); }
    public boolean finished() {
        return status == Status.WON || status == Status.LOST || status == Status.WALKED_AWAY;
    }

    public void select(String answer) {
        if (question().choices().stream().noneMatch(choice -> choice.id().equals(answer))) {
            throw new IllegalArgumentException("Unknown answer: " + answer);
        }
        if (selectionEnabled()) {
            selected = answer;
        }
    }

    public void lockAnswer() {
        if (!lockEnabled()) {
            return;
        }
        if (question().correctChoiceId().equals(selected)) {
            correctAnswers++;
            if (correctAnswers == GameRules.QUESTIONS_PER_ROUND) {
                status = Status.WON;
                payout = credits();
            } else {
                status = Status.CORRECT;
            }
        } else {
            status = Status.LOST;
            payout = GameRules.guaranteedCredits(correctAnswers);
        }
    }

    public void nextQuestion() {
        if (status != Status.CORRECT) {
            return;
        }
        index++;
        selected = null;
        status = Status.ANSWERING;
    }

    public void walkAway() {
        if (!walkAwayEnabled()) {
            return;
        }
        payout = credits();
        status = Status.WALKED_AWAY;
    }
}
