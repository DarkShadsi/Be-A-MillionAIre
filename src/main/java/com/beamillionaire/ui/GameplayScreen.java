package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;
import com.beamillionaire.application.Theme;
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
        boolean answered=game.round.status()==GameRound.Status.CORRECT||game.round.finished();
        if(answered) answerFeedback(game,pane,state);
        else {
            var content=new QuestionContent(state.question(),state.choices(),size->game.assets.font("body",size),game.ink(),34.9178,28);
            content.resizeRelocate(559,269,812,354);content.setPrefSize(812,354);pane.getChildren().add(content);
        }
        for(String answer:List.of("A","B","C","D")){
            var button=game.original(pane,"answer."+answer.toLowerCase(Locale.ROOT),answer,()->game.selectAnswer(answer));
            button.setDisable(!state.selectionEnabled()||state.eliminatedOptions().contains(answer));
            var feedback=state.feedback().getOrDefault(answer,GameplayViewState.Feedback.NORMAL);
            if(feedback==GameplayViewState.Feedback.CORRECT)button.setFeedback(GameButton.Feedback.CORRECT);
            else if(feedback==GameplayViewState.Feedback.INCORRECT)button.setFeedback(GameButton.Feedback.INCORRECT);
            else if(answer.equals(state.selectedAnswer()))button.setFeedback(GameButton.Feedback.SELECTED);
        }
        boolean advancing=answered;
        String nextLabel=game.round.finished()?"View results":"Continue";
        var lock=game.action(pane,advancing?nextLabel:"Lock Answer",840,678,246,advancing?game::continueGame:game::lockAnswer);
        lock.setId(advancing?"continueGame":"lockAnswer");
        lock.setDisable(!advancing&&!state.lockEnabled());
        String[] labels={"50:50","Clue","Second Chance"};int index=0;
        for(var help:GameplayViewState.Help.values()){
            var button=game.original(pane,"help."+(index+1),labels[index],()->game.useHelp(help));
            var availability=state.helps().get(help);
            boolean active=availability==GameplayViewState.HelpState.ACTIVE;
            String caption=active?(help==GameplayViewState.Help.CLUE?"View clue":"Second Chance\nACTIVE"):labels[index];
            button.setCaption(caption,game.assets.font("body",active?22:index==2?27:34),game.ink());
            button.setAccessibleText(caption.replace('\n',' '));
            button.setDisable(availability!=GameplayViewState.HelpState.AVAILABLE
                    &&!(help==GameplayViewState.Help.CLUE&&active));
            if(active)button.setFeedback(GameButton.Feedback.SELECTED);
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
        String notice=state.secondChanceRetry()?"Second Chance used - choose again"
                :game.round.secondChanceActive()?"Second Chance ACTIVE - one retry available":"";
        if(!notice.isEmpty()){
            var status=game.text(pane,notice,559,625,812,45,27,true);
            status.setId("secondChanceStatus");
            status.setTextFill(game.theme==Theme.DARK?Color.web("#ffd166"):Color.web("#775000"));
        }
        for(int checkpoint:GameRules.CHECKPOINT_QUESTIONS){
            var marker=new javafx.scene.shape.Circle(1504,197+(15-checkpoint)*37.9590363153,5,Color.web("#dec33f"));
            marker.setMouseTransparent(true);pane.getChildren().add(marker);
        }
    }
    private static void answerFeedback(GamePresentation game, Pane pane, GameplayViewState state) {
        boolean correct=game.round.status()!=GameRound.Status.LOST;
        Color accent=game.theme==Theme.DARK
                ?Color.web(correct?"#70e0ab":"#ff958f")
                :Color.web(correct?"#12633d":"#9b2020");
        String answer=state.choices().stream()
                .filter(choice->choice.id().equals(game.round.question().correctChoiceId()))
                .map(choice->choice.id()+". "+choice.text()).findFirst().orElse("");
        var title=new Label(correct?"CORRECT!":"INCORRECT");
        title.setId("answerFeedbackTitle");
        title.setFont(game.assets.font("heading",46));title.setTextFill(accent);
        title.setStyle("-fx-text-fill: #"+accent.toString().substring(2,8)+";");
        var detail=new Label((correct?"Your answer: ":"Correct answer: ")+answer);
        detail.setWrapText(true);detail.setMaxWidth(740);
        detail.setFont(game.assets.font("body",30));detail.setTextFill(game.ink());
        detail.setStyle("-fx-text-fill: #"+game.ink().toString().substring(2,8)+";");
        String summary=game.round.finished()
                ?String.format(Locale.US,"Round complete • Payout: %,d credits",game.round.payout())
                :String.format(Locale.US,"Current credits: %,d • Continue when ready",state.credits());
        var credits=new Label(summary);credits.setWrapText(true);credits.setMaxWidth(740);
        credits.setFont(game.assets.font("body",28));credits.setTextFill(game.ink());
        credits.setStyle("-fx-text-fill: #"+game.ink().toString().substring(2,8)+";");
        var box=new VBox(24,title,detail,credits);box.setAlignment(Pos.CENTER);
        box.setMinHeight(330);box.setStyle("-fx-padding: 12;");
        var feedback=new ScrollPane(box);feedback.setId("answerFeedback");
        feedback.setFitToWidth(true);feedback.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        feedback.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        feedback.resizeRelocate(559,269,812,354);feedback.setPrefSize(812,354);
        pane.getChildren().add(feedback);
    }

}
