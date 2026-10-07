package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.*;
import com.beamillionaire.engine.GameRound;
import com.beamillionaire.engine.GameRules;
import com.beamillionaire.ui.design.*;
import com.beamillionaire.ui.state.GameplayViewState;

/** Builds gameplay once; subsequent refreshes update display data without restarting motion. */
final class GameplayScreen {
    private GameplayScreen() {}
    static void render(GamePresentation game,Pane pane){
        game.background(pane,"gameplay");
        var category=new MarqueeLabel();category.resizeRelocate(100,170,330,85);category.setPrefSize(330,85);
        pane.getChildren().add(category);game.onMotionChanged(category::setMotionState);
        game.onMotionRunning(category::motionRunning);game.onDispose(category::close);
        var number=game.text(pane,"",750,76,420,62,34,true);
        var initial=game.gameplayState();
        var content=new QuestionContent(initial.question(),initial.choices(),size->game.assets.font("body",size),
                size->game.assets.font("body",size),game.ink(),36,30,30);
        content.resizeRelocate(559,269,812,354);content.setPrefSize(812,354);
        content.setOnReadFullQuestion(game::showFullQuestion);pane.getChildren().add(content);
        var feedbackPane=new AnswerFeedback();pane.getChildren().add(feedbackPane);
        var answers=new LinkedHashMap<String,GameButton>();
        for(String id:List.of("A","B","C","D"))answers.put(id,game.original(pane,"answer."+id.toLowerCase(Locale.ROOT),id,()->game.selectAnswer(id)));
        var lock=game.action(pane,"Lock Answer",840,678,246,()->{
            if(game.round!=null&&(game.round.status()==GameRound.Status.CORRECT||game.round.finished()))game.continueGame();else game.lockAnswer();
        });
        var helps=new EnumMap<GameplayViewState.Help,GameButton>(GameplayViewState.Help.class);
        String[] names={"50:50","Clue","Second Chance"};int index=0;
        for(var help:GameplayViewState.Help.values()){
            var button=game.original(pane,"help."+(index+1),names[index],()->game.useHelp(help));
            button.setCaption(names[index],game.assets.font("body",index==2?27:34),game.ink());helps.put(help,button);index++;
        }
        var walk=game.original(pane,"walk-away","Walk away",game::walkAway);
        var credits=game.text(pane,"",1690,930,180,64,39,false);
        var notice=game.text(pane,"",620,710,680,34,24,true);
        var ladder=new PrizeLadderEffects();pane.getChildren().add(ladder);
        game.onMotionChanged(ladder::setMotionState);game.onMotionRunning(ladder::motionRunning);game.onDispose(ladder::close);
        for(int checkpoint:GameRules.CHECKPOINT_QUESTIONS){
            var marker=new javafx.scene.shape.Circle(1504,197+(15-checkpoint)*37.9590363153,5,Color.web("#dec33f"));
            marker.setMouseTransparent(true);pane.getChildren().add(marker);
        }
        game.onRefresh(()->{
            if(game.round==null)return;
            var state=game.gameplayState();
            category.setText(state.category().toUpperCase(Locale.ROOT),game.assets.font("heading",33),game.ink());
            number.setText(String.format("QUESTION %02d",state.questionNumber()));
            content.update(game.roundId+"/"+game.round.question().id(),state.question(),state.choices());
            content.setColors(game.ink(),Color.web(game.themeKey().equals("light")?"#087b89":"#008fbd"));
            boolean advancing=game.round.status()==GameRound.Status.CORRECT||game.round.finished();
            content.setVisible(!advancing);feedbackPane.setVisible(advancing);
            if(advancing)feedbackPane.update(game,state);
            answers.forEach((id,button)->{
                button.setDisable(!state.selectionEnabled()||state.eliminatedOptions().contains(id));
                var feedback=switch(state.feedback().getOrDefault(id,GameplayViewState.Feedback.NORMAL)){
                    case CORRECT->GameButton.Feedback.CORRECT;case INCORRECT->GameButton.Feedback.INCORRECT;
                    case NORMAL->id.equals(state.selectedAnswer())?GameButton.Feedback.SELECTED:GameButton.Feedback.NORMAL;
                };
                button.setFeedback(feedback);
            });
            String nextLabel=game.round.finished()?"View results":"Continue";
            lock.setId(advancing?"continueGame":"lockAnswer");lock.setAccessibleText(advancing?nextLabel:"Lock Answer");
            lock.setCaption(advancing?nextLabel:"Lock Answer",game.assets.font("body",27.5),game.ink());
            lock.setDisable(!advancing&&!state.lockEnabled());
            helps.forEach((help,button)->{
                var availability=state.helps().get(help);
                boolean active=availability==GameplayViewState.HelpState.ACTIVE;
                String caption=active?(help==GameplayViewState.Help.CLUE?"View clue":"Second Chance\nACTIVE"):names[help.ordinal()];
                button.setCaption(caption,game.assets.font("body",active?22:help==GameplayViewState.Help.SECOND_CHANCE?27:34),game.ink());
                button.setAccessibleText(caption.replace('\n',' '));
                button.setDisable(availability!=GameplayViewState.HelpState.AVAILABLE&&!(help==GameplayViewState.Help.CLUE&&active));
                button.setFeedback(active?GameButton.Feedback.SELECTED:GameButton.Feedback.NORMAL);
            });
            walk.setDisable(!state.walkAwayEnabled());
            credits.setText(String.format(Locale.US,"%,d",state.credits()));credits.setTextFill(Color.web(game.themeKey().equals("light")?"#927000":"#dec33f"));
            notice.setText(state.secondChanceRetry()?"Second Chance: choose again":game.round.secondChanceActive()?"Second Chance ACTIVE — one retry available":"");
            ladder.update(state.ladderPosition(),state.earnedLadderPosition(),game.themeKey().equals("light"));
        });
    }

    /** Retained Milly answer summary; only its text and theme change between submissions. */
    private static final class AnswerFeedback extends ScrollPane {
        final Label title=new Label();
        final Label detail=new Label();
        final Label summary=new Label();
        String displayed;
        AnswerFeedback(){
            setId("answerFeedback");title.setId("answerFeedbackTitle");
            for(var label:List.of(title,detail,summary)){
                label.setWrapText(true);label.setMaxWidth(740);label.setMinHeight(Region.USE_PREF_SIZE);
                label.setAlignment(Pos.CENTER);
            }
            var box=new VBox(24,title,detail,summary);box.setAlignment(Pos.CENTER);box.setMinHeight(330);
            box.setStyle("-fx-padding: 12;");setContent(box);setFitToWidth(true);
            setHbarPolicy(ScrollBarPolicy.NEVER);
            setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            resizeRelocate(559,269,812,354);setPrefSize(812,354);setVisible(false);
        }
        void update(GamePresentation game,GameplayViewState state){
            boolean correct=game.round.status()!=GameRound.Status.LOST;
            Color accent=Color.web(game.themeKey().equals("dark")?(correct?"#70e0ab":"#ff958f"):(correct?"#12633d":"#9b2020"));
            String answer=state.choices().stream().filter(choice->choice.id().equals(game.round.question().correctChoiceId()))
                    .map(choice->choice.id()+". "+choice.text()).findFirst().orElse("");
            title.setText(correct?"CORRECT!":"INCORRECT");title.setFont(game.assets.font("heading",46));title.setTextFill(accent);
            title.setStyle("-fx-text-fill: #"+accent.toString().substring(2,8)+";");
            detail.setText((correct?"Your answer: ":"Correct answer: ")+answer);
            detail.setFont(game.assets.font("choices",30));detail.setTextFill(game.ink());
            detail.setStyle("-fx-text-fill: #"+game.ink().toString().substring(2,8)+";");
            summary.setText(game.round.finished()?String.format(Locale.US,"Round complete • Payout: %,d credits",game.round.payout())
                    :String.format(Locale.US,"Current credits: %,d • Continue when ready",state.credits()));
            summary.setFont(game.assets.font("body",28));summary.setTextFill(game.ink());
            summary.setStyle("-fx-text-fill: #"+game.ink().toString().substring(2,8)+";");
            String key=game.roundId+"/"+game.round.question().id()+"/"+game.round.status();
            if(!Objects.equals(displayed,key)){displayed=key;setVvalue(0);}
        }
    }
}
