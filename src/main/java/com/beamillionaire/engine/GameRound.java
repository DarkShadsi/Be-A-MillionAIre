package com.beamillionaire.engine;

import com.beamillionaire.domain.*;
import java.util.*;

/** A single round. All answer evaluation, help eligibility and payouts live here. */
public final class GameRound {
    public enum Help { FIFTY_FIFTY, CLUE, SECOND_CHANCE }
    public enum Status { ANSWERING, RETRY, CORRECT, WON, LOST, WALKED_AWAY }

    private final List<Question> questions;
    private final Random random;
    private final EnumSet<Help> usedHelps = EnumSet.noneOf(Help.class);
    private final Set<String> eliminated = new HashSet<>();
    private Status status = Status.ANSWERING;
    private int index;
    private int correctAnswers;
    private int payout;
    private String selected;
    private String firstWrongAnswer;
    private boolean secondChanceActive;
    private boolean clueVisible;
    private boolean helpUsedThisQuestion;

    public GameRound(QuestionBank bank, Category category, Random random) {
        this.questions = new QuestionSelector().select(bank, category, random);
        this.random = random;
    }

    public Question question() { return questions.get(index); }
    public int questionNumber() { return index + 1; }
    public int correctAnswers() { return correctAnswers; }
    public int credits() { return GameRules.earnedCredits(correctAnswers); }
    public int payout() { return payout; }
    public Status status() { return status; }
    public String selectedAnswer() { return selected; }
    public String firstWrongAnswer() { return firstWrongAnswer; }
    public Set<String> eliminatedOptions() { return Set.copyOf(eliminated); }
    public Set<Help> usedHelps() { return Set.copyOf(usedHelps); }
    public boolean secondChanceActive() { return secondChanceActive; }
    public boolean clueRevealed() { return clueVisible; }
    public String clue() { return clueVisible ? question().clue() : ""; }
    public boolean selectionEnabled() { return status == Status.ANSWERING || status == Status.RETRY; }
    public boolean lockEnabled() { return selectionEnabled() && selected != null; }
    public boolean walkAwayEnabled() { return status == Status.ANSWERING; }
    public boolean finished() { return status == Status.WON || status == Status.LOST || status == Status.WALKED_AWAY; }

    public void select(String answer) {
        if (question().choices().stream().noneMatch(choice -> choice.id().equals(answer)))
            throw new IllegalArgumentException("Unknown answer: " + answer);
        if (selectionEnabled() && !eliminated.contains(answer)) selected = answer;
    }

    public void lockAnswer() {
        if (!lockEnabled()) return;
        if (question().correctChoiceId().equals(selected)) {
            correctAnswers++;
            secondChanceActive = false;
            if (correctAnswers == GameRules.QUESTIONS_PER_ROUND) {
                status = Status.WON;
                payout = credits();
            } else status = Status.CORRECT;
        } else if (secondChanceActive && status == Status.ANSWERING) {
            secondChanceActive = false;
            firstWrongAnswer = selected;
            eliminated.add(selected);
            selected = null;
            status = Status.RETRY;
        } else {
            status = Status.LOST;
            payout = GameRules.guaranteedCredits(correctAnswers);
        }
    }

    public boolean helpAvailable(Help help) {
        Objects.requireNonNull(help);
        // Each help is available once per game, with one activation per question.
        return selectionEnabled() && !helpUsedThisQuestion && !usedHelps.contains(help)
            && (help != Help.SECOND_CHANCE || status == Status.ANSWERING);
    }

    public boolean useHelp(Help help) {
        if (!helpAvailable(help)) return false;
        usedHelps.add(help);
        helpUsedThisQuestion = true;
        switch (help) {
            case CLUE -> clueVisible = true;
            case SECOND_CHANCE -> secondChanceActive = true;
            case FIFTY_FIFTY -> {
                var wrong = new ArrayList<>(question().choices().stream()
                    .map(Choice::id).filter(id -> !id.equals(question().correctChoiceId())).toList());
                Collections.shuffle(wrong, random);
                // Remove two wrong options without removing the correct answer.
                for (String id : wrong) {
                    if (eliminated.size() >= 2) break;
                    eliminated.add(id);
                }
                if (eliminated.contains(selected)) selected = null;
            }
        }
        return true;
    }

    public void nextQuestion() {
        if (status != Status.CORRECT) return;
        index++;
        selected = null;
        firstWrongAnswer = null;
        secondChanceActive = false;
        clueVisible = false;
        helpUsedThisQuestion = false;
        eliminated.clear();
        status = Status.ANSWERING;
    }

    public void walkAway() {
        if (!walkAwayEnabled()) return;
        payout = credits();
        status = Status.WALKED_AWAY;
        secondChanceActive = false;
    }
}
