package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.List;
import java.util.Locale;
import com.beamillionaire.engine.GameRound;
import com.beamillionaire.engine.GameRules;
import com.beamillionaire.ui.state.GameplayViewState;
import com.beamillionaire.ui.design.GameButton;
import com.beamillionaire.ui.design.QuestionContent;

/** Renders the gameplay screen with the existing artwork and layout. */
final class GameplayScreen {
    private GameplayScreen() {}
    static void render(GamePresentation game, Pane pane) {
        if(game.round==null)return;
        game.background(pane,"gameplay");
        GameplayViewState state=game.gameplayState();
        String category=state.category().toUpperCase(Locale.ROOT);
        // Fit the full heading, including the longest categories, within the artwork.
        var measure=new Text(category);
        measure.setWrappingWidth(326);
        double headingSize=33;
        measure.setFont(game.assets.font("heading",headingSize));
        while(headingSize>18&&measure.getLayoutBounds().getHeight()>81){
            measure.setFont(game.assets.font("heading",--headingSize));
        }
        var heading=game.text(pane,category,100,170,330,85,headingSize,true);
        heading.setId("categoryHeading");
        game.text(pane,String.format("QUESTION %02d",state.questionNumber()),750,76,420,62,34,true);
        var content=new QuestionContent(state.question(),state.choices(),size->game.assets.font("body",size),game.ink(),34.9178,28);
        content.resizeRelocate(559,269,812,354);content.setPrefSize(812,354);pane.getChildren().add(content);
        for(String answer:List.of("A","B","C","D")){
            var button=game.original(pane,"answer."+answer.toLowerCase(Locale.ROOT),answer,()->game.selectAnswer(answer));
            button.setDisable(!state.selectionEnabled()||state.eliminatedOptions().contains(answer));
            var feedback=state.feedback().getOrDefault(answer,GameplayViewState.Feedback.NORMAL);
            if(feedback==GameplayViewState.Feedback.CORRECT)button.setFeedback(GameButton.Feedback.CORRECT);
            else if(feedback==GameplayViewState.Feedback.INCORRECT)button.setFeedback(GameButton.Feedback.INCORRECT);
            else if(answer.equals(state.selectedAnswer()))button.setFeedback(GameButton.Feedback.SELECTED);
        }
        boolean advancing=game.round!=null&&game.round.status()==GameRound.Status.CORRECT;
        var lock=game.action(pane,advancing?"Continue":"Lock Answer",840,678,246,advancing?game::continueGame:game::lockAnswer);
        lock.setId(advancing?"continueGame":"lockAnswer");
        lock.setDisable(!advancing&&!state.lockEnabled());
        String[] labels={"50:50","Clue","Second Chance"};int index=0;
        for(var help:GameplayViewState.Help.values()){
            var button=game.original(pane,"help."+(index+1),labels[index],()->game.useHelp(help));
            button.setCaption(labels[index],game.assets.font("body",index==2?27:34),game.ink());
            var availability=state.helps().get(help);
            button.setDisable(availability!=GameplayViewState.HelpState.AVAILABLE);
            if(availability==GameplayViewState.HelpState.ACTIVE)button.setFeedback(GameButton.Feedback.SELECTED);
            index++;
        }
        var walk=game.original(pane,"walk-away","Walk away",game::walkAway);
        walk.setDisable(!state.walkAwayEnabled());
        game.text(pane,String.format(Locale.US,"%,d",state.credits()),1690,930,180,64,39,false).setTextFill(Color.web("#dec33f"));
        if(state.ladderPosition()>0){
            var highlight=new Rectangle(1518,180+(15-state.ladderPosition())*37.9590363153,270,35);
            highlight.setFill(Color.TRANSPARENT);highlight.setStroke(Color.web("#00d6ff"));
            highlight.setStrokeWidth(2);highlight.setMouseTransparent(true);pane.getChildren().add(highlight);
        }
        if(state.secondChanceRetry()) game.text(pane,"Second Chance: choose again",645,625,660,45,27,true);
        else if(advancing) game.text(pane,"Correct!",645,625,660,45,27,true);
        for(int checkpoint:GameRules.CHECKPOINT_QUESTIONS){
            var marker=new javafx.scene.shape.Circle(1504,197+(15-checkpoint)*37.9590363153,5,Color.web("#dec33f"));
            marker.setMouseTransparent(true);pane.getChildren().add(marker);
        }
    }
}
