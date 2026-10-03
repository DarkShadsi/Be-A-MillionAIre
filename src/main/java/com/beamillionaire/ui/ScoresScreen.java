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
        game.background(pane,"home");game.panel(pane,830,448,1000);
        game.text(pane,"SCORE HISTORY",915,480,825,65,44,true);
        String history;
        try {
            var entries=game.scoreStore.load();
            history=entries.isEmpty()?"No rounds recorded yet.":String.join("\n\n",entries.stream().map(e ->
                    e.playedAt().substring(0,10)+"  •  "+e.category()+"\n"+
                    e.correctAnswers()+" correct  •  "+String.format(Locale.US,"%,d",e.payout())+" credits  •  "+
                    e.outcome().name().replace('_',' ')).toList());
        } catch(IOException error) { history=error.getMessage(); }
        var content=new Label(history);content.setWrapText(true);content.setTextFill(game.ink());
        content.setFont(game.assets.font("body",28));content.setMaxWidth(790);
        var scroll=new ScrollPane(content);scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setId("scoreHistory");scroll.resizeRelocate(915,565,825,250);
        scroll.setPrefSize(825,250);pane.getChildren().add(scroll);
        game.action(pane,"Menu",1110,855,320,()->game.show(Screen.MENU));
    
    }
}
