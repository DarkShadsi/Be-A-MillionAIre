package com.beamillionaire.ui.state;

import com.beamillionaire.engine.GameRound;
import java.util.*;
import static com.beamillionaire.ui.state.GameplayViewState.*;

/** Maps engine decisions to immutable display data without evaluating game rules. */
public final class RoundPresenter {
    private RoundPresenter() {}

    public static GameplayViewState gameplay(GameRound round) {
        var helps = new EnumMap<Help, HelpState>(Help.class);
        for (Help help : Help.values()) {
            var engineHelp = GameRound.Help.valueOf(help.name());
            helps.put(help, round.usedHelps().contains(engineHelp) ? HelpState.USED
                    : round.helpAvailable(engineHelp) ? HelpState.AVAILABLE : HelpState.DISABLED);
            if (help == Help.SECOND_CHANCE && round.secondChanceActive()) helps.put(help, HelpState.ACTIVE);
        }
        var feedback = new HashMap<String, Feedback>();
        if (round.firstWrongAnswer() != null) feedback.put(round.firstWrongAnswer(), Feedback.INCORRECT);
        if (round.status() == GameRound.Status.CORRECT || round.status() == GameRound.Status.WON)
            feedback.put(round.selectedAnswer(), Feedback.CORRECT);
        if (round.status() == GameRound.Status.LOST) feedback.put(round.selectedAnswer(), Feedback.INCORRECT);
        String notice = switch (round.status()) {
            case RETRY -> "Second Chance: choose again. Credits are unchanged.";
            case CORRECT -> "Correct! Continue to the next question.";
            case ANSWERING -> round.secondChanceActive() ? "Second Chance activated for this question." : "Select an answer, then lock it.";
            default -> "Round complete.";
        };
        return new GameplayViewState(round.question().category().displayName(), round.questionNumber(),
                round.question().text(), round.question().choices(), round.selectedAnswer(),
                round.selectionEnabled(), round.lockEnabled(), round.walkAwayEnabled(), helps,
                round.eliminatedOptions(), feedback, round.credits(), round.questionNumber(),
                round.status() == GameRound.Status.RETRY, notice);
    }

    public static ResultsViewState results(GameRound round) {
        var outcome = switch (round.status()) {
            case WON -> ResultsViewState.Outcome.WIN;
            case LOST -> ResultsViewState.Outcome.LOSS;
            case WALKED_AWAY -> ResultsViewState.Outcome.WALK_AWAY;
            default -> throw new IllegalStateException("Round is not complete.");
        };
        return new ResultsViewState(outcome, round.question().category().displayName(),
                round.correctAnswers(), round.payout(), true);
    }
}
