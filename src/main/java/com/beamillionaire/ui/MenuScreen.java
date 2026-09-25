package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import com.beamillionaire.application.Theme;
import com.beamillionaire.ui.design.ScreenRouter.Screen;

/** Renders the menu screen with the existing artwork and layout. */
final class MenuScreen {
    private MenuScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");game.panel(pane,830,448,1000);
        game.text(pane,"MENU",915,472,825,58,46,true);
        game.text(pane,"Choose a category. Select A–D, then Lock Answer.\nCheckpoints: Q4 / 1,000 • Q9 / 30,000 • Q12 / 100,000\nEach help once per game; combine helps on a question.\nSecond Chance: one retry, consumed on activation.\nWalk away before locking. F11: fullscreen • Esc: close popup",
                915,535,825,180,25,false);
        game.action(pane,game.theme==Theme.DARK?"Light theme":"Dark theme",930,745,240,game::toggleTheme).setId("themeButton");
        game.action(pane,"Fullscreen",1215,745,240,game.fullscreen);
        String motionText="Reduce motion: "+(game.reduceMotion?"On":"Off");
        var motion=game.action(pane,motionText,1500,745,240,game::toggleMotion);
        motion.setId("motionButton");
        motion.setCaption(motionText,game.assets.font("body",24),game.ink());
        game.action(pane,"Home",1500,865,240,()->game.show(Screen.HOME));
    
    }
}
