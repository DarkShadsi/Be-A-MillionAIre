package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import java.util.Locale;
import com.beamillionaire.ui.design.ScreenRouter.Screen;

/** Centered result panel reached through View results or confirmed Walk Away. */
final class ResultsScreen {
    private ResultsScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");
        populate(game,game.centeredPanel(pane,"results-panel",1000));
    }
    private static void populate(GamePresentation game,Pane pane){
        var heading=game.text(pane,"",85,32,825,65,46,true);
        var summary=game.text(pane,"",85,127,825,200,38,false);
        var notice=game.text(pane,"",85,340,825,55,24,false);
        var replay=game.action(pane,"Play again",145,407,320,game::replay);
        game.action(pane,"Home",535,407,320,()->game.show(Screen.HOME));
        game.onRefresh(()->{
            heading.setText(switch(game.results.outcome()){case WIN->"MILLIONAIRE!";case LOSS->"ROUND OVER";case WALK_AWAY->"WALKED AWAY";});
            summary.setText(game.results.category()+"\n"+game.results.correctAnswers()+" correct answers\nPayout: "+String.format(Locale.US,"%,d",game.results.payout())+" credits");
            notice.setText(game.scoreNotice!=null?"Score could not be saved: "+game.scoreNotice:"");
            replay.setDisable(!game.results.replayEnabled());
        });

    }
}
