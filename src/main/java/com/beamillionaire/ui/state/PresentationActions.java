package com.beamillionaire.ui.state;

/** Forward user intent to the presenter that owns the next display state. */
public interface PresentationActions {
    void selectAnswer(String answer);
    void lockAnswer();
    void useHelp(GameplayViewState.Help help);
    void walkAway();
    void continueGame();
    void replay();
    void openOverlay(Overlay overlay);
    boolean closeOverlay();
    enum Overlay { CLUE, WALK_AWAY, EXIT, ABANDON, UNAVAILABLE_CATEGORY, RESOURCE_ERROR, DATA_ERROR }
}
