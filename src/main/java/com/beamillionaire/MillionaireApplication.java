package com.beamillionaire;

import com.beamillionaire.application.*;
import com.beamillionaire.storage.*;
import com.beamillionaire.ui.*;
import com.beamillionaire.ui.assets.AssetCatalog;
import com.beamillionaire.ui.design.FullscreenSupport;
import com.beamillionaire.ui.design.SplashHandoff;
import javafx.animation.*;
import javafx.application.*;
import javafx.beans.InvalidationListener;
import javafx.concurrent.Task;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
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
    private SplashHandoff startupHandoff;
    private PauseTransition smokePause;
    private Stage window;
    private Scene splashScene;
    private boolean closed;
    private final InvalidationListener windowListener=ignored->syncFade();
    private final InvalidationListener splashSizeListener=ignored->resizeSplash();
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
            audio=new GameAudio(!smoke&&initialPreferences.soundEnabled());
            if(!smoke)audio.setScene(GameAudio.Scene.SPLASH);
            stage.setTitle("Be a Millionaire");stage.setMinWidth(960);stage.setMinHeight(540);
            Scene scene;
            if(smoke){scene=new Scene(new Pane(),1280,720);stage.setOpacity(0);}
            else {
                splash=new SplashScreen();
                splash.setOnShimmerCue(splash.shimmerMidpoint().subtract(Duration.millis(GameAudio.LOGO_IMPACT_MILLIS)),
                        lateness->{
                            if(!closed&&stage.isShowing()&&!stage.isIconified())
                                audio.playSplashLogo(Math.round(lateness.toMillis()));
                        });
                scene=new Scene(splash.root(),1280,720);
            }
            stage.setScene(scene);
            if(!smoke){
                splashScene=scene;
                scene.widthProperty().addListener(splashSizeListener);
                scene.heightProperty().addListener(splashSizeListener);
                resizeSplash();
            }
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
        startupHandoff=new SplashHandoff(scene,splash.root(),game.root(),initialPreferences.reduceMotion(),()->{
            detachSplashLayout();splash.stopAnimations();splash=null;startupHandoff=null;
        });
        game.setAudioVisible(true);
        syncFade();
    }
    private void resizeSplash(){
        if(splash!=null&&splashScene!=null)splash.resize(splashScene.getWidth(),splashScene.getHeight());
    }
    private void detachSplashLayout(){
        if(splashScene==null)return;
        splashScene.widthProperty().removeListener(splashSizeListener);
        splashScene.heightProperty().removeListener(splashSizeListener);
        splashScene=null;
    }
    private void syncFade(){
        if(audio!=null&&window!=null)audio.setSuspended(!window.isShowing()||window.isIconified());
        if(closed||startupHandoff==null)return;
        if(window.isShowing()&&!window.isIconified()){
            startupHandoff.resume();
        }else startupHandoff.pause();
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
        if(startupHandoff!=null){startupHandoff.close();startupHandoff=null;}
        if(smokePause!=null)smokePause.stop();
        detachSplashLayout();
        if(splash!=null)splash.stopAnimations();
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
