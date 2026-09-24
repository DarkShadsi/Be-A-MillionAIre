package com.beamillionaire.ui;

import javafx.scene.layout.Pane;

/** Renders the categories screen with the existing artwork and layout. */
final class CategoryScreen {
    private CategoryScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"categories");
        game.CATEGORIES.forEach((id,category)->game.original(pane,"category."+id,category.displayName(),()->game.chooseCategory(category)));
    
    }
}
