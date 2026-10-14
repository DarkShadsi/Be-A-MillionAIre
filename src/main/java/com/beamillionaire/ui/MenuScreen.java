package com.beamillionaire.ui;

import com.beamillionaire.application.Theme;
import com.beamillionaire.ui.design.ScreenRouter.Screen;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Locale;

/** Spacious settings and navigation, with credits from completed rounds. */
final class MenuScreen {
    private MenuScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");
        var panel=game.centeredPanel(pane,"menu-panel",1320);
        double height=panel.getPrefHeight();
        game.text(panel,"MENU",112,38,1096,76,52,true);

        var card=new Polygon(460,190,860,190,876,206,876,454,860,470,460,470,444,454,444,206);
        card.setStrokeWidth(2);card.setMouseTransparent(true);
        var accents=new Group();accents.setMouseTransparent(true);
        var top=new Polyline(444,248,444,206,460,190,565,190);
        var bottom=new Polyline(755,470,860,470,876,454,876,412);
        top.setFill(null);bottom.setFill(null);top.setStrokeWidth(3);bottom.setStrokeWidth(3);
        accents.getChildren().addAll(top,bottom);panel.getChildren().addAll(card,accents);
        game.text(panel,"TOTAL CREDITS EARNED",462,211,396,55,30,true);
        var total=game.text(panel,"0",462,276,396,86,62,true);total.setId("menuTotalCredits");
        game.text(panel,"CREDITS",462,377,396,48,30,true);
        var notice=game.text(panel,"",434,483,452,58,23,false);notice.setId("menuCreditsNotice");

        var pulse=new Timeline(
                new KeyFrame(Duration.ZERO,new KeyValue(accents.opacityProperty(),.62,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(2.5),new KeyValue(accents.opacityProperty(),1,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(5),new KeyValue(accents.opacityProperty(),.62,Interpolator.EASE_BOTH)));
        pulse.setCycleCount(Animation.INDEFINITE);
        game.motionScope().register(pulse,()->accents.setOpacity(1));
        game.onAppearance(()->{
            boolean light=game.theme==Theme.LIGHT;
            var cyan=Color.web(light?"#007e9e":"#00a6d0");
            var gold=Color.web(light?"#855f00":"#e4c62c");
            card.setFill(Color.web(light?"#dcebf0":"#081922",.72));card.setStroke(cyan);
            top.setStroke(gold);bottom.setStroke(gold);total.setTextFill(gold);
        });

        var sound=game.action(panel,"Sound: On",145,200,260,game::toggleSound);sound.setId("soundButton");
        var motion=game.action(panel,"Reduce motion: Off",145,355,260,game::toggleMotion);motion.setId("motionButton");
        var fullscreen=game.action(panel,"Fullscreen",915,200,260,game.fullscreen);fullscreen.setId("fullscreenButton");
        var theme=game.action(panel,"Light theme",915,355,260,game::toggleTheme);theme.setId("themeButton");
        double footerY=height-170;
        game.action(panel,"Home",150,footerY,260,()->game.show(Screen.HOME)).setId("menuHomeButton");
        game.action(panel,"Score history",530,footerY,260,()->game.show(Screen.SCORES)).setId("historyButton");
        game.action(panel,"Instructions",910,footerY,260,()->game.show(Screen.INSTRUCTIONS)).setId("instructionsButton");
        game.onRefresh(()->{
            theme.setCaption(game.theme==Theme.DARK?"Light theme":"Dark theme",game.assets.font("body",30),game.ink());
            motion.setCaption("Reduce motion: "+(game.reduceMotion?"On":"Off"),game.assets.font("body",24),game.ink());
            sound.setCaption(game.soundEnabled?"Sound: On":"Sound: Off",game.assets.font("body",30),game.ink());
            theme.setAccessibleText(game.theme==Theme.DARK?"Light theme":"Dark theme");
            motion.setAccessibleText("Reduce motion: "+(game.reduceMotion?"On":"Off"));
            sound.setAccessibleText(game.soundEnabled?"Sound: On":"Sound: Off");
        });
        // Reading history belongs to screen entry, not preference or appearance refreshes.
        game.onShown(()->{
            try {
                long credits=game.scoreStore.load().stream().mapToLong(entry->entry.payout()).sum();
                setTotal(game,total,String.format(Locale.US,"%,d",credits));notice.setText("");
                total.setAccessibleText("Total credits earned: "+total.getText()+" credits");
            } catch(IOException error) {
                setTotal(game,total,"Unavailable");notice.setText("Score history could not be read.");
                total.setAccessibleText("Total credits earned unavailable. Score history could not be read.");
            }
        });
    }
    private static void setTotal(GamePresentation game,Label total,String value){
        var measured=new Text(value);measured.setFont(game.assets.font("heading",62));
        double size=Math.min(62,62*380/Math.max(1,measured.getLayoutBounds().getWidth()));
        total.setFont(game.assets.font("heading",size));total.setText(value);
    }
}
