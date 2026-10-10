package com.beamillionaire.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.Locale;
import com.beamillionaire.ui.design.ScreenRouter.Screen;
import com.beamillionaire.ui.state.ResultsViewState;
import javafx.scene.shape.Rectangle;

/** Centered result panel reached through View results or confirmed Walk Away. */
final class ResultsScreen {
    private ResultsScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");
        var panel=game.centeredPanel(pane,"results-panel",1000);
        if(game.results!=null&&game.results.outcome()==ResultsViewState.Outcome.WIN) populateWin(game,panel);
        else populate(game,panel);
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
    private static void populateWin(GamePresentation game,Pane pane){
        var cyan=Color.web("#00d6ff");
        var gold=Color.web("#dec33f");
        int x = 85; int y = 32;
        label(game,pane,"CONGRATULATIONS",x, y,825,65,46,game.ink(),true);
        y = 32 + 65;
        label(game,pane,"YOU ARE NOW A MILLIONAIRE",x, y,825,34,24,cyan,true);
        y = y + 32 + 34;
        x = 85 + (825 - 550) / 2;
        Rectangle rect = new Rectangle(x, y, 550, 211);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(cyan);
        rect.setStrokeWidth(4);
        pane.getChildren().add(rect);
        y += 32;
        x = 85;
        label(game,pane,"YOU WON",x,y,825,33,27,cyan,true);
        y += 32;
        var payout=label(game,pane,"",x,y,825,72,52,gold,true);
        y = y + 72;
        label(game,pane,"CREDITS",x,y,825,31,25,cyan,true);
        y = (int) rect.getY() + 211 + 16;
        var category=label(game,pane,"",x, y,825,40,26,game.ink(),false);
        y = y + 16 + 40;
        var notice=label(game,pane,"",x, y,825,28,20,gold,false);
        var replay=game.action(pane,"Play again",145,y,320,game::replay);
        game.action(pane,"Home",535,y,320,()->game.show(Screen.HOME));
        game.onRefresh(()->{
            payout.setText(String.format(Locale.US,"%,d",game.results.payout()));
            category.setText(game.results.category().toUpperCase(Locale.ROOT));
            notice.setText(game.scoreNotice!=null?"Score could not be saved.":"");
            replay.setDisable(!game.results.replayEnabled());
        });
    }
    private static Label label(GamePresentation game,Pane pane,String value,double x,double y,double width,double height,
                               double size,Color color,boolean bold){
        var label=new Label(value);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setTextFill(color);
        label.setFont(game.assets.font(bold?"heading":"body",size));
        label.resizeRelocate(x,y,width,height);
        label.setMinSize(width,height);label.setPrefSize(width,height);label.setMaxSize(width,height);
        pane.getChildren().add(label);
        return label;
    }
}
