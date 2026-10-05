package com.beamillionaire;

import com.beamillionaire.application.*;
import com.beamillionaire.storage.*;
import com.beamillionaire.ui.*;
import com.beamillionaire.ui.assets.AssetCatalog;
import com.beamillionaire.ui.design.FullscreenSupport;
import javafx.animation.*;
import javafx.application.*;
import javafx.beans.InvalidationListener;
import javafx.concurrent.Task;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class MillionaireApplication extends Application {
    private record LoadedStartup(StartupState state, AssetCatalog assets) {}
    private AppService service;
    private AppPreferences initialPreferences;
    private GameAudio audio;
    private GamePresentation game;
    private SplashScreen splash;
    private Task<LoadedStartup> loader;
    private ParallelTransition startupFade;
    private PauseTransition smokePause;
    private Stage window;
    private boolean closed;
    private final InvalidationListener windowListener=ignored->syncFade();
    @Override public void init() {
        var settings=new SettingsStore(AppPaths.userDataDirectory());
        service=new AppService(new CsvQuestionRepository(AppPaths.questionDataDirectory()),settings);
        // Only small preferences are read before the splash; the bank and artwork load afterward.
        try{initialPreferences=settings.load();}
        catch(java.io.IOException error){initialPreferences=AppPreferences.defaults();}
    }
    @Override public void start(Stage stage) {
        boolean smoke=getParameters().getRaw().contains("--smoke-test");
        window=stage;
        try {
            stage.setTitle("Be a Millionaire");stage.setMinWidth(960);stage.setMinHeight(540);
            Scene scene;
            if(smoke){scene=new Scene(new Pane(),1280,720);stage.setOpacity(0);}
            else {
                splash=new SplashScreen(stage,!initialPreferences.reduceMotion());
                scene=new Scene(splash.root(),1280,720);
            }
            stage.setScene(scene);
            FullscreenSupport.install(stage,()->game!=null&&game.closeOverlay());
            stage.setOnCloseRequest(event->{
                if(!smoke&&game!=null&&splash==null){event.consume();game.requestExit();}
                else shutdown();
            });
            stage.showingProperty().addListener(windowListener);
            stage.iconifiedProperty().addListener(windowListener);
            stage.show();
            if(!smoke)stage.setFullScreen(true);
            loader=new Task<>() {
                @Override protected LoadedStartup call() throws Exception {
                    var state=service.load();
                    if(isCancelled())return null;
                    return new LoadedStartup(state,AssetCatalog.load());
                }
            };
            loader.setOnSucceeded(event->{
                if(closed)return;
                try{
                    var loaded=loader.getValue();
                    audio=new GameAudio(!smoke&&loaded.state().preferences().soundEnabled());
                    game=new GamePresentation(service,loaded.state(),loaded.assets(),new ScoreStore(AppPaths.userDataDirectory()),
                            audio,()->stage.setFullScreen(!stage.isFullScreen()),Platform::exit);
                    game.setMotionSuppressed(smoke);
                    if(smoke){
                        scene.setRoot(game.root());
                        smokePause=new PauseTransition(Duration.millis(500));
                        smokePause.setOnFinished(done->{
                            boolean ok=loaded.state().bankAvailable();
                            System.out.println(ok?"SMOKE_OK: interface and question file loaded":"SMOKE_FAILED: question file unavailable");
                            Platform.exit();if(!ok)System.exit(1);
                        });smokePause.play();
                    }else{
                        splash.setOnReady(()->revealHome(scene));
                        splash.notifyLoadingComplete();
                    }
                }catch(Exception error){startupFailed(error,smoke);}
            });
            loader.setOnFailed(event->{if(!closed)startupFailed(loader.getException(),smoke);});
            var thread=new Thread(loader,"millionaire-startup");thread.setDaemon(true);thread.start();
        }catch(Exception error){startupFailed(error,smoke);}
    }
    private void revealHome(Scene scene){
        if(closed||splash==null)return;
        if(initialPreferences.reduceMotion()){
            splash.close();splash=null;scene.setRoot(game.root());return;
        }
        game.root().setOpacity(0);game.root().setDisable(true);
        scene.setRoot(new Pane());
        var composition=new StackPane(game.root(),splash.root());scene.setRoot(composition);
        var fadeSplash=new FadeTransition(Duration.millis(600),splash.root());fadeSplash.setToValue(0);
        var fadeHome=new FadeTransition(Duration.millis(600),game.root());fadeHome.setToValue(1);
        startupFade=new ParallelTransition(fadeSplash,fadeHome);startupFade.setInterpolator(Interpolator.EASE_BOTH);
        startupFade.setOnFinished(event->{
            if(closed)return;
            splash.close();splash=null;composition.getChildren().clear();
            game.root().setDisable(false);game.root().setOpacity(1);scene.setRoot(game.root());
        });startupFade.play();syncFade();
    }
    private void syncFade(){
        if(closed||startupFade==null)return;
        if(window.isShowing()&&!window.isIconified()){
            if(startupFade.getStatus()==Animation.Status.PAUSED)startupFade.play();
        }else if(startupFade.getStatus()==Animation.Status.RUNNING)startupFade.pause();
    }
    private void startupFailed(Throwable error,boolean smoke){
        shutdown();
        System.err.println("Startup failed: "+error.getMessage());
        if(smoke){Platform.exit();System.exit(1);return;}
        new Alert(Alert.AlertType.ERROR,"The game could not start: "+error.getMessage()).showAndWait();Platform.exit();
    }
    private void shutdown(){
        if(closed)return;
        closed=true;
        if(loader!=null)loader.cancel();
        if(startupFade!=null){startupFade.stop();startupFade.setOnFinished(null);}
        if(smokePause!=null)smokePause.stop();
        if(splash!=null)splash.close();
        if(game!=null)game.close();
        if(audio!=null)audio.close();
        if(window!=null){
            window.showingProperty().removeListener(windowListener);
            window.iconifiedProperty().removeListener(windowListener);
        }
    }
    @Override public void stop(){shutdown();}
    public static void main(String[] args){launch(args);}
}
