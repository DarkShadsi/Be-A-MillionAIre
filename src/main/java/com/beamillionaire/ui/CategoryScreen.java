package com.beamillionaire.ui;

import javafx.scene.layout.Pane;
import com.beamillionaire.ui.design.GameButton;
import java.util.Comparator;

/** Renders the categories screen with the existing artwork and layout. */
final class CategoryScreen {
    private CategoryScreen() {}
    static void render(GamePresentation game, Pane pane) {
        game.background(pane,"categories");
        var ordered=game.CATEGORIES.entrySet().stream().sorted(Comparator.comparingDouble(entry->
                game.assets.artwork(game.themeKey(),"category."+entry.getKey()).top())).toList();
        for(int index=0;index<ordered.size();index++){
            var entry=ordered.get(index);
            var button=game.original(pane,"category."+entry.getKey(),entry.getValue().displayName(),()->{});
            button.getStyleClass().add("category-card");
            button.setOnAction(event->{
                if(game.router.transitioning()||game.overlays.isVisible())return;
                button.setFeedback(GameButton.Feedback.SELECTED);
                game.audio.play("click");game.chooseCategory(entry.getValue());
            });
            game.categoryEntrance(pane,button,index);
        }
    }
}
