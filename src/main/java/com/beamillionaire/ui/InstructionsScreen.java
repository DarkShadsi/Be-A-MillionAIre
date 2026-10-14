package com.beamillionaire.ui;

import com.beamillionaire.application.Theme;
import com.beamillionaire.ui.design.InstructionArtwork;
import com.beamillionaire.ui.design.ScreenRouter.Screen;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/** A retained, read-only guide. Illustrations never create rounds or write scores. */
final class InstructionsScreen {
    private InstructionsScreen() {}

    private record Page(String title,String asset,String text) {}
    private static final Page[] PAGES={
            new Page("CATEGORIES","instructions.categories",
                    "Choose from eight AI topics:\n\n"
                    +"Artificial Intelligence Fundamentals\n"
                    +"Knowledge and Problem Representation\n"
                    +"Search and Game Playing\n"
                    +"Machine Learning · Neural Networks\n"
                    +"Deep Learning · Future of AI\n"
                    +"Research in AI\n\n"
                    +"A category opens when it has at least five Easy, five Medium, and five Hard questions."),
            new Page("QUESTIONS","instructions.gameplay",
                    "Read the whole question and all four choices. Everything stays visible without scrolling.\n\n"
                    +"Select A, B, C, or D, then choose Lock Answer. You can change your selection before locking it.\n\n"
                    +"A correct answer earns the next prize. Read the feedback, then choose Continue.\n\n"
                    +"Keyboard: A–D selects. Tab moves between controls; Enter or Space activates the focused button."),
            new Page("HELPS","instructions.gameplay",
                    "Use each help once per round. Activate only one help on each question.\n\n"
                    +"50:50 removes two wrong choices.\n\n"
                    +"Clue reveals a hint. You can reopen it while that question is active.\n\n"
                    +"Second Chance is used as soon as you activate it. If your next locked answer is wrong, choose again on the same question. A second wrong answer ends the round."),
            new Page("LADDER","instructions.gameplay",
                    "15 questions: 5 Easy → 5 Medium → 5 Hard.\n\n"
                    +"Cyan marks the current prize. Gold marks credits already earned.\n\n"
                    +"Guaranteed checkpoints:\n"
                    +"Q4 — 1,000 credits\n"
                    +"Q9 — 30,000 credits\n"
                    +"Q12 — 100,000 credits\n\n"
                    +"A wrong answer pays your last checkpoint, or zero before Q4."),
            new Page("WINNING","instructions.winning",
                    "Answer all 15 questions correctly to win 1,000,000 credits.\n\n"
                    +"After a win or loss, choose View results from the feedback screen. Play Again opens Categories.\n\n"
                    +"Walk Away before your first locked attempt on a question to keep your earned credits. Confirm to finish the round and view your payout.")
    };

    static void render(GamePresentation game,Pane pane){
        var art=new InstructionArtwork(pane,game.motionScope());art.frame();
        var heading=game.text(pane,"HOW TO PLAY",560,62,800,88,68,true);
        heading.setId("instructions-heading");
        var topic=game.text(pane,"",1240,188,540,70,49,true);topic.setId("instructions-page-title");
        topic.setAlignment(Pos.CENTER_LEFT);
        var body=game.text(pane,"",1240,286,540,550,29,false);body.setId("instructions-body");
        body.setManaged(false);body.setMinHeight(0);body.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        body.setAlignment(Pos.TOP_LEFT);body.setTextOverrun(OverrunStyle.CLIP);
        body.setLineSpacing(5);
        var illustration=new ImageView();illustration.setId("instructions-illustration");
        illustration.setFitWidth(1050);illustration.setFitHeight(1050.0*1080/1920);illustration.setPreserveRatio(true);
        illustration.setMouseTransparent(true);illustration.setManaged(false);illustration.relocate(120,190);
        pane.getChildren().add(illustration);
        art.addEmphasis();
        var pageNumber=game.text(pane,"",820,816,280,34,27,false);pageNumber.setId("instructions-page-number");
        int[] current={0};
        Runnable[] update={null};
        var home=game.action(pane,"HOME",200,874,300,()->game.show(Screen.HOME));home.setId("instructions-home");
        var next=game.action(pane,"NEXT",810,874,300,()->{
            if(current[0]==PAGES.length-1){game.show(Screen.HOME);return;}
            current[0]++;update[0].run();
        });next.setId("instructions-next");
        var previous=game.action(pane,"PREVIOUS",1420,874,300,()->{
            if(current[0]>0){current[0]--;update[0].run();}
        });previous.setId("instructions-previous");
        update[0]=()->{
            var page=PAGES[current[0]];
            topic.setText(page.title());body.setText(page.text());fitBody(game,body,page.text());
            illustration.setImage(game.assets.image(game.themeKey(),page.asset()));
            illustration.setAccessibleText("Illustration for "+page.title().toLowerCase(java.util.Locale.ROOT));
            pageNumber.setText((current[0]+1)+" / "+PAGES.length);
            previous.setDisable(current[0]==0);
            next.setCaption(current[0]==PAGES.length-1?"DONE":"NEXT",game.assets.font("body",36),game.ink());
            next.setAccessibleText(current[0]==PAGES.length-1?"Done, return Home":"Next instruction page");
            art.setTopic(current[0]);
        };
        game.onAppearance(()->{
            boolean light=game.theme==Theme.LIGHT;art.setLightTheme(light);
            pane.setStyle("-fx-background-color: "+(light?"#f4fafa":"#02060a")+";");
            topic.setTextFill(javafx.scene.paint.Color.web(light?"#007e9e":"#00a6d4"));
            illustration.setImage(game.assets.image(game.themeKey(),PAGES[current[0]].asset()));
            next.setCaptionColor(game.ink());
        });
        game.onShown(()->{current[0]=0;update[0].run();});
        game.onRefresh(()->{
            if(body.getText().isEmpty())update[0].run();
        });
        update[0].run();
    }

    /** Measure with the same verified face and spacing, so no page needs scrolling or ellipsis. */
    private static void fitBody(GamePresentation game,Label label,String value){
        label.applyCss();
        for(int size=29;size>=25;size--){
            var font=game.assets.font("choices",size);
            label.setFont(font);
            if(Math.ceil(label.prefHeight(540))+4<=550)return;
        }
        throw new IllegalStateException("Instruction text exceeds its stationary reading area: "+value.substring(0,Math.min(40,value.length()))+" ("+label.prefHeight(540)+" px)");
    }
}
