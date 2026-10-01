package com.beamillionaire.ui;

import com.beamillionaire.ui.design.ScreenRouter.Screen;
import javafx.scene.layout.Pane;

import java.util.Locale;

/** Renders the results screen with the existing artwork and layout. */
final class ResultsScreen {
    private ResultsScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");game.panel(pane,830,448,1000);
        String heading=switch(game.results.outcome()){case WIN->"MILLIONAIRE!";case LOSS->"ROUND OVER";case WALK_AWAY->"WALKED AWAY";};
        game.text(pane,heading,915,480,825,65,46,true);
        game.text(pane,game.results.category()+"\n"+game.results.correctAnswers()+" correct answers\nPayout: "+String.format(Locale.US,"%,d",game.results.payout())+" credits",
                915,575,825,200,38,false);
        game.action(pane,"Play again",930,855,320,game::replay).setDisable(!game.results.replayEnabled());
        game.action(pane,"Home",1320,855,320,()->game.show(Screen.HOME));

    }
}
