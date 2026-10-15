package com.beamillionaire.ui;

import com.beamillionaire.application.Theme;
import com.beamillionaire.engine.GameRules;
import com.beamillionaire.ui.design.*;
import com.beamillionaire.ui.design.ScreenRouter.Screen;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Locale;

/** Win-only layout based on the team's Canva results composition. */
final class WinResultsScreen {
    private WinResultsScreen() {}
    static void render(GamePresentation game,Pane pane){
        var scope=game.motionScope();
        var art=new WinArtwork(pane,scope);
        art.frame();art.categoryFrame();art.winningsFrame();art.ladderFrame();
        var rings=art.answeredRings();var platform=art.platform();
        var title=game.text(pane,"CONGRATULATIONS",545,122,820,86,68,true);
        title.setId("win-title");
        var subtitle=game.text(pane,"YOU ARE NOW A MILLIONAIRE",545,218,820,54,38,true);
        var categoryHeading=game.text(pane,"CATEGORY",174,126,228,42,29,false);
        var category=new MarqueeLabel(22);category.resizeRelocate(137,181,312,91);category.setId("win-category");
        category.setPrefSize(312,91);category.setMinSize(312,91);category.setMaxSize(312,91);
        pane.getChildren().add(category);
        game.onMotionChanged(category::setMotionState);game.onDispose(category::close);game.onMotionRunning(category::motionRunning);
        var answered=game.text(pane,"",177,533,226,54,40,false);answered.setId("win-answered");
        var answeredHeading=game.text(pane,"ANSWERED",180,587,220,32,24,true);
        var won=game.text(pane,"YOU WON",610,390,686,62,44,true);
        var payout=game.text(pane,"",590,462,726,108,86,true);payout.setId("win-payout");
        var credits=game.text(pane,"CREDITS",610,595,686,62,44,true);
        var notice=game.text(pane,"",570,712,766,48,25,false);notice.setId("win-score-notice");
        var ladderHeading=game.text(pane,"PRIZE LADDER",1481,134,270,44,27,true);
        var activeRow=ladder(game,pane,scope,art);
        var home=game.action(pane,"HOME",157,868,328,()->game.show(Screen.HOME));home.setId("win-home");
        var replay=game.action(pane,"PLAY AGAIN",1421,874,338,game::replay);replay.setId("win-replay");
        game.onAppearance(()->{
            boolean light=game.theme==Theme.LIGHT;art.setLightTheme(light);
            Color cyan=Color.web(light?"#007e9e":"#009ac6"),gold=Color.web(light?"#896600":"#d4bd16");
            for(var label:new Label[]{subtitle,categoryHeading,answeredHeading,won,credits,ladderHeading})label.setTextFill(cyan);
            payout.setTextFill(gold);notice.setTextFill(gold);category.setColor(game.ink());
            activeRow.setFill(Color.web(light?"#e1c35b":"#e3c658"));
        });
        game.onRefresh(()->{
            if(game.results==null)return;
            category.setText(game.results.category().toUpperCase(Locale.ROOT),game.assets.font("heading",34),game.ink());
            answered.setText(game.results.correctAnswers()+" / "+GameRules.QUESTIONS_PER_ROUND);
            payout.setText(String.format(Locale.US,"%,d",game.results.payout()));
            notice.setText(game.scoreNotice==null?"":"Score could not be saved. You can still play again.");
            replay.setDisable(!game.results.replayEnabled());
        });
        entrance(pane,scope,title,0);entrance(pane,scope,subtitle,60);
        UiEffects.entrance(scope,rings,0,12,1,320,120);
        entrance(pane,scope,won,180);entrance(pane,scope,payout,240);entrance(pane,scope,credits,300);
        UiEffects.entrance(scope,platform,0,12,1,320,360);
    }
    private static Rectangle ladder(GamePresentation game,Pane pane,MotionScope scope,WinArtwork art){
        var highlight=new Rectangle(1539,191,165,43);highlight.setId("win-ladder-highlight");
        highlight.setMouseTransparent(true);pane.getChildren().add(highlight);
        var glow=new DropShadow(8,Color.web("#d4b928"));highlight.setEffect(glow);
        var pulse=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(highlight.opacityProperty(),.88),new KeyValue(glow.radiusProperty(),4)),
                new KeyFrame(Duration.seconds(1.4),new KeyValue(highlight.opacityProperty(),1,Interpolator.EASE_BOTH),
                        new KeyValue(glow.radiusProperty(),12,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(2.8),new KeyValue(highlight.opacityProperty(),.88),new KeyValue(glow.radiusProperty(),4)));
        pulse.setCycleCount(Animation.INDEFINITE);scope.register(pulse,()->{highlight.setOpacity(1);glow.setRadius(0);});
        art.goldOrbit("win-prize-gold-orbit",highlight.getX(),highlight.getY(),highlight.getWidth(),highlight.getHeight(),2,5);
        for(int row=15;row>=1;row--){
            double y=191+(15-row)*37.5;
            var label=game.text(pane,String.format(Locale.US,"%02d    %,d",row,GameRules.PRIZE_LADDER.get(row-1)),
                    1477,y,285,36,24,row==15);label.setId("win-prize-"+row);
            int level=row;
            game.onAppearance(()->label.setTextFill(level==15?Color.web("#131916"):ladderColor(level,game.theme==Theme.LIGHT)));
            if(row==4||row==9||row==12||row==15){
                var divider=new Rectangle(1478,y+37,285,1.5);divider.setMouseTransparent(true);pane.getChildren().add(divider);
                game.onAppearance(()->divider.setFill(ladderColor(level,game.theme==Theme.LIGHT)));
            }
        }
        return highlight;
    }
    private static Color ladderColor(int row,boolean light){
        return Color.web(row>=13?(light?"#b52b15":"#fb704d"):row>=10?(light?"#9a4e00":"#e9a74f"):
                row>=5?(light?"#786000":"#eee169"):(light?"#287333":"#b3e59c"));
    }
    private static void entrance(Pane pane,MotionScope scope,Label label,double delay){
        pane.getChildren().remove(label);var wrapper=new Group(label);pane.getChildren().add(wrapper);
        UiEffects.entrance(scope,wrapper,0,12,1,320,delay);
    }
}
