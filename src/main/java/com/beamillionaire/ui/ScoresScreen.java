package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import java.io.IOException;
import java.util.Locale;
import com.beamillionaire.ui.design.ScreenRouter.Screen;

/** Renders the scores screen with the existing artwork and layout. */
final class ScoresScreen {
    private ScoresScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");
        var panel=game.centeredPanel(pane,"scores-panel",1000);
        game.text(panel,"SCORE HISTORY",85,32,825,65,44,true);
        var content=new Label();content.setWrapText(true);content.setTextFill(game.ink());
        content.setFont(game.assets.font("body",28));content.setMaxWidth(790);
        var scroll=new ScrollPane(content);scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setId("scoreHistory");scroll.resizeRelocate(85,117,825,250);
        scroll.setPrefSize(825,250);panel.getChildren().add(scroll);
        game.action(panel,"Menu",340,407,320,()->game.show(Screen.MENU));
        game.onRefresh(()->{content.setText(history(game));content.setTextFill(game.ink());});
    }
    private static String history(GamePresentation game){
        String history;
        try {
            var entries=game.scoreStore.load();
            history=entries.isEmpty()?"No rounds recorded yet.":String.join("\n\n",entries.stream().map(e ->
                    e.playedAt().substring(0,10)+"  •  "+e.category()+"\n"+
                    e.correctAnswers()+" correct  •  "+String.format(Locale.US,"%,d",e.payout())+" credits  •  "+
                    e.outcome().name().replace('_',' ')).toList());
        } catch(IOException error) { history=error.getMessage(); }
        return history;
    }
}
