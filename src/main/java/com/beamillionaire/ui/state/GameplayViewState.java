package com.beamillionaire.ui.state;

import com.beamillionaire.domain.Choice;
import java.util.*;

/** Display data only: no answer key, help rules, or payout calculations. */
public record GameplayViewState(String category, int questionNumber, String question, List<Choice> choices,
        String selectedAnswer, boolean selectionEnabled, boolean lockEnabled, boolean walkAwayEnabled,
        Map<Help, HelpState> helps, Set<String> eliminatedOptions, Map<String, Feedback> feedback,
        long credits, int ladderPosition, boolean secondChanceRetry, String notice) {
    public enum Help { FIFTY_FIFTY, CLUE, SECOND_CHANCE }
    public enum HelpState { AVAILABLE, ACTIVE, USED, DISABLED }
    public enum Feedback { NORMAL, CORRECT, INCORRECT }
    public GameplayViewState {
        Objects.requireNonNull(category);
        Objects.requireNonNull(question);
        Objects.requireNonNull(notice);
        choices = List.copyOf(choices);
        helps = Map.copyOf(helps);
        eliminatedOptions = Set.copyOf(eliminatedOptions);
        feedback = Map.copyOf(feedback);
        Set<String> ids = new HashSet<>();
        for (Choice choice : choices) ids.add(choice.id());
        if (choices.size() != 4 || !ids.equals(Set.of("A", "B", "C", "D")))
            throw new IllegalArgumentException("Four distinct A–D choices are required.");
        if (!ids.containsAll(eliminatedOptions) || !ids.containsAll(feedback.keySet()))
            throw new IllegalArgumentException("Unknown answer option.");
        if (selectedAnswer != null && (!ids.contains(selectedAnswer) || eliminatedOptions.contains(selectedAnswer)))
            throw new IllegalArgumentException("Selected option must be available.");
        if (lockEnabled && selectedAnswer == null)
            throw new IllegalArgumentException("Lock Answer requires a selection.");
        if (questionNumber < 1 || questionNumber > 15 || ladderPosition < 0 || ladderPosition > 15 || credits < 0)
            throw new IllegalArgumentException("Invalid displayed progress.");
        if (!helps.keySet().equals(EnumSet.allOf(Help.class)))
            throw new IllegalArgumentException("All helps require a displayed state.");
    }
}
