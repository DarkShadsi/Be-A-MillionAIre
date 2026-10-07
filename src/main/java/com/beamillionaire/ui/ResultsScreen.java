package com.beamillionaire.ui;

import com.beamillionaire.ui.design.ScreenRouter.Screen;
import com.beamillionaire.application.Theme;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.Locale;

/** Shows the completed round in a centered panel. */
final class ResultsScreen {
    private ResultsScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");game.panel(pane,460,227,1000);
        String heading=switch(game.results.outcome()){
            case WIN->"MILLIONAIRE!";case LOSS->"ROUND OVER";case WALK_AWAY->"WALKED AWAY";
        };
        game.text(pane,heading,545,265,830,65,46,true);
        game.text(pane,game.results.category(),555,350,810,65,29,false);
        game.text(pane,"TOTAL PAYOUT",555,437,810,40,27,true);
        var payout=game.text(pane,String.format(Locale.US,"%,d credits",game.results.payout()),
                555,475,810,82,56,true);
        payout.setTextFill(game.theme==Theme.DARK?Color.web("#5cdbff"):Color.web("#006480"));
        payout.setId("resultPayout");
        game.text(pane,game.results.correctAnswers()+" / 15 correct answers",555,578,810,50,30,false);
        var replay=game.action(pane,"Play again",610,689,300,game::replay);
        replay.setId("replayButton");replay.setDisable(!game.results.replayEnabled());
        game.action(pane,"Home",1010,689,300,()->game.show(Screen.HOME)).setId("resultsHomeButton");
    }
}
