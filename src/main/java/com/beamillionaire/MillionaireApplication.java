package com.beamillionaire;

import com.beamillionaire.application.Theme;
import com.beamillionaire.storage.*;
import com.beamillionaire.ui.*;
import com.beamillionaire.ui.assets.AssetCatalog;
import com.beamillionaire.ui.design.FullscreenSupport;
import javafx.animation.PauseTransition;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class MillionaireApplication extends Application {

    @Override public void start(Stage stage) {
        try {
            stage.setTitle("Be a Millionaire");stage.setMinWidth(960);stage.setMinHeight(540);
            var assets=AssetCatalog.load();
            var game=new GamePresentation(new CsvQuestionRepository(AppPaths.questionDataDirectory()).load(),assets,
                    ()->stage.setFullScreen(!stage.isFullScreen()),Platform::exit);
            var scene=new Scene(game.root(),1280,720);
            stage.setScene(scene);FullscreenSupport.install(stage,game::closeOverlay);
            stage.setOnCloseRequest(event->{event.consume();game.requestExit();});
            String theme=Theme.DARK.name().toLowerCase(java.util.Locale.ROOT);
            ImageView title=assets.imageView(theme,"home.background");
            title.fitWidthProperty().bind(scene.widthProperty());title.fitHeightProperty().bind(scene.heightProperty());
            var splash=new StackPane(title);splash.setStyle("-fx-background-color: #02060a;");
            var pause=new PauseTransition(Duration.seconds(1));
            Runnable finish=()->{pause.stop();scene.setRoot(game.root());};
            splash.setOnMouseClicked(event->finish.run());splash.setOnKeyPressed(event->finish.run());
            pause.setOnFinished(event->finish.run());scene.setRoot(splash);splash.requestFocus();pause.play();
            stage.show();
            stage.setFullScreen(true);
        }catch(Exception error){
            System.err.println("Startup failed: "+error.getMessage());
            new Alert(Alert.AlertType.ERROR,"The game could not start: "+error.getMessage()).showAndWait();Platform.exit();
        }
    }
    public static void main(String[] args){launch(args);}
}
