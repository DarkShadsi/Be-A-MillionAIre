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
    private record LoadedStartup(StartupState startup, AssetCatalog assets) {}

    @Override public void init() {
        service=new AppService(new CsvQuestionRepository(AppPaths.questionDataDirectory()),
            new SettingsStore(AppPaths.userDataDirectory()));
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

        Task<LoadedStartup> loadTask=new Task<>() {
            @Override protected LoadedStartup call() throws Exception {
                var startup=service.load();
                var assets=AssetCatalog.load();
                return new LoadedStartup(startup,assets);
            }
        };
        loadTask.setOnSucceeded(event->{
            try {
                var loaded=loadTask.getValue();
                var game=new GamePresentation(service,loaded.startup(),loaded.assets(),
                    ()->stage.setFullScreen(!stage.isFullScreen()),Platform::exit);
                splash.setOnReady(()->transitionToGame(stage,scene,transitionHost,splash,game));
                splash.notifyLoadingComplete();
            }catch(Exception error){
                startupFailed(splash,error);
            }
        });
        loadTask.setOnFailed(event->startupFailed(splash,loadTask.getException()));
        stage.setOnCloseRequest(event->{
            loadTask.cancel();
            splash.stopAnimations();
        });
        Thread loaderThread=new Thread(loadTask,"asset-loader");
        loaderThread.setDaemon(true);
        loaderThread.start();
    }

    private void transitionToGame(Stage stage,Scene scene,StackPane transitionHost,SplashScreen splash,GamePresentation game){
        splash.stopAnimations();
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

    private void startupFailed(SplashScreen splash,Throwable error){
        splash.stopAnimations();
        String message=error!=null?error.getMessage():"unknown error";
        System.err.println("Startup failed: "+message);
        new Alert(Alert.AlertType.ERROR,"The game could not start: "+message).showAndWait();
        Platform.exit();
    }

    public static void main(String[] args){launch(args);}
}
