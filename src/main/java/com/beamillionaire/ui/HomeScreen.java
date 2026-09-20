package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import com.beamillionaire.ui.design.ScreenRouter.Screen;

/** Renders the home screen with the existing artwork and layout. */
final class HomeScreen {
    private HomeScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"home");
        game.original(pane,"home.start","Start",()->game.show(Screen.CATEGORIES)).setDisable(true);
        game.original(pane,"home.menu","Menu",()->game.show(Screen.MENU)).setDisable(true);
        game.original(pane,"home.exit","Exit",game::requestExit);
    
    }
}
