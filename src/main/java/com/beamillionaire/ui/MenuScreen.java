package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import com.beamillionaire.application.Theme;
import com.beamillionaire.ui.design.ScreenRouter.Screen;

/** Renders the menu screen with the existing artwork and layout. */
final class MenuScreen {
    private MenuScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");
        var content=game.centeredPanel(pane,"menu-panel",1000);
        game.text(content,"MENU",85,24,825,58,46,true);
        game.text(content,"Choose a category. Select A–D, then Lock Answer.\nCheckpoints: Q4 / 1,000 • Q9 / 30,000 • Q12 / 100,000\nEach help once per game; one help per question.\nSecond Chance: one retry, consumed on activation.\nWalk away before locking. F11: fullscreen • Esc: close popup",
                85,87,825,180,25,false);
        var theme=game.action(content,"Light theme",100,297,240,game::toggleTheme);theme.setId("themeButton");
        game.action(content,"Fullscreen",380,297,240,game.fullscreen);
        String motionText="Reduce motion: "+(game.reduceMotion?"On":"Off");
        var motion=game.action(content,motionText,660,297,240,game::toggleMotion);
        motion.setId("motionButton");
        motion.setCaption(motionText,game.assets.font("body",24),game.ink());
        var sound=game.action(content,"Sound: On",100,417,240,game::toggleSound);sound.setId("soundButton");
        game.action(content,"Score history",380,417,240,()->game.show(Screen.SCORES)).setId("historyButton");
        game.action(content,"Home",660,417,240,()->game.show(Screen.HOME));
        game.onRefresh(()->{
            theme.setCaption(game.theme==Theme.DARK?"Light theme":"Dark theme",game.assets.font("body",30),game.ink());
            motion.setCaption("Reduce motion: "+(game.reduceMotion?"On":"Off"),game.assets.font("body",24),game.ink());
            sound.setCaption(game.soundEnabled?"Sound: On":"Sound: Off",game.assets.font("body",30),game.ink());
        });
    
    }
}
