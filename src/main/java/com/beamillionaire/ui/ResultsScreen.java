package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import java.util.Locale;
import com.beamillionaire.ui.design.ScreenRouter.Screen;
import com.beamillionaire.ui.state.ResultsViewState;

/** Retained full-screen win composition and centered loss/walk-away panels. */
final class ResultsScreen {
    private ResultsScreen() {}
    static void render(GamePresentation game,Pane pane){
        game.resultVariant(pane,"win-results",()->game.results!=null
                &&game.results.outcome()==ResultsViewState.Outcome.WIN,view->WinResultsScreen.render(game,view));
        game.resultVariant(pane,"standard-results",()->game.results!=null
                &&game.results.outcome()!=ResultsViewState.Outcome.WIN,view->populate(game,view));
    }
    private static void populate(GamePresentation game,Pane host){
        game.background(host,"home");
        var pane=game.centeredPanel(host,"results-panel",1000);
        var heading=game.text(pane,"",85,32,825,65,46,true);
        var summary=game.text(pane,"",85,127,825,200,38,false);
        var notice=game.text(pane,"",85,340,825,55,24,false);
        var replay=game.action(pane,"Play again",145,407,320,game::replay);
        replay.setId("result-panel-replay");
        var home=game.action(pane,"Home",535,407,320,()->game.show(Screen.HOME));
        home.setId("result-panel-home");
        game.onRefresh(()->{
            if(game.results==null)return;
            heading.setText(game.results.outcome()==ResultsViewState.Outcome.LOSS?"ROUND OVER":"WALKED AWAY");
            summary.setText(game.results.category()+"\n"+game.results.correctAnswers()+" correct answers\nPayout: "
                    +String.format(Locale.US,"%,d",game.results.payout())+" credits");
            notice.setText(game.scoreNotice!=null?"Score could not be saved: "+game.scoreNotice:"");
            replay.setDisable(!game.results.replayEnabled());
        });
    }
}
