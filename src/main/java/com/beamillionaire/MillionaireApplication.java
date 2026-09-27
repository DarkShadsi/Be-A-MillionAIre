package com.beamillionaire;

import com.beamillionaire.application.*;
import com.beamillionaire.storage.*;
import com.beamillionaire.ui.*;
import com.beamillionaire.ui.assets.AssetCatalog;
import com.beamillionaire.ui.design.FullscreenSupport;
import javafx.animation.FadeTransition;
import javafx.application.*;
import javafx.concurrent.Task;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class MillionaireApplication extends Application {
    private AppService service;
    private StartupState startup;

    @Override public void init() {
        service=new AppService(new CsvQuestionRepository(AppPaths.questionDataDirectory()),
            new SettingsStore(AppPaths.userDataDirectory()));
        startup=service.load();
    }

    @Override public void start(Stage stage) {
        stage.setTitle("Be a Millionaire");stage.setMinWidth(960);stage.setMinHeight(540);
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");

        SplashScreen splash=new SplashScreen();
        StackPane transitionHost=new StackPane(splash.root());
        transitionHost.setStyle("-fx-background-color: #070c12;");
        Scene scene=new Scene(transitionHost,1280,720);
        scene.setFill(javafx.scene.paint.Color.web("#070c12"));
        Runnable resizeSplash=()->splash.resize(scene.getWidth(),scene.getHeight());
        scene.widthProperty().addListener((o,a,b)->resizeSplash.run());
        scene.heightProperty().addListener((o,a,b)->resizeSplash.run());

        stage.setScene(scene);
        stage.setOpacity(0);
        stage.show();
        stage.setFullScreen(true);
        Platform.runLater(() -> stage.setOpacity(1));
        resizeSplash.run();

        Task<GamePresentation> loadTask=new Task<>() {
            @Override protected GamePresentation call() throws Exception {
                var assets=AssetCatalog.load();
                return new GamePresentation(service,startup,assets,
                    ()->stage.setFullScreen(!stage.isFullScreen()),Platform::exit);
            }
        };
        loadTask.setOnSucceeded(event->{
            GamePresentation game=loadTask.getValue();
            splash.setOnReady(()->transitionToGame(stage,scene,transitionHost,splash,game));
            splash.notifyLoadingComplete();
        });
        loadTask.setOnFailed(event->{
            Throwable error=loadTask.getException();
            System.err.println("Startup failed: "+(error!=null?error.getMessage():"unknown error"));
            new Alert(Alert.AlertType.ERROR,"The game could not start: "+
                (error!=null?error.getMessage():"unknown error")).showAndWait();
            Platform.exit();
        });
        Thread loaderThread=new Thread(loadTask,"asset-loader");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }

    private void transitionToGame(Stage stage,Scene scene,StackPane transitionHost,SplashScreen splash,GamePresentation game){
        FullscreenSupport.install(stage,game::closeOverlay);
        stage.setOnCloseRequest(event->{event.consume();game.requestExit();});

        game.root().setStyle("-fx-background-color: #02060a;");
        game.root().setOpacity(0);
        transitionHost.getChildren().setAll(game.root());
        FadeTransition fadeIn=new FadeTransition(Duration.seconds(0.6),game.root());
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e->{
            game.root().requestFocus();
        });
        fadeIn.play();
    }

    public static void main(String[] args){launch(args);}
}
