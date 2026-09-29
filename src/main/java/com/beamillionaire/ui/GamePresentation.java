package com.beamillionaire.ui;

import com.beamillionaire.application.*;
import com.beamillionaire.domain.*;
import com.beamillionaire.engine.GameRules;
import com.beamillionaire.engine.GameRound;
import com.beamillionaire.ui.assets.AssetCatalog;
import com.beamillionaire.ui.design.*;
import com.beamillionaire.ui.state.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.*;
import com.beamillionaire.storage.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import static com.beamillionaire.ui.design.ScreenRouter.Screen;

/** Presentation coordinator: navigation and rendering, with engine decisions supplied as view state. */
public final class GamePresentation implements PresentationActions {
    final AppService service;
    final StartupState startup;
    final AssetCatalog assets;
    final Runnable fullscreen;
    final Runnable exit;
    final BorderPane root = new BorderPane();
    final DesignViewport viewport = new DesignViewport(1920,1080);
    final StackPane screens = new StackPane();
    final OverlayHost overlays = new OverlayHost(screens);
    final ScreenRouter router = new ScreenRouter(screens);
    GameRound round;
    ResultsViewState results;
    Theme theme;
    boolean reduceMotion;
    static final Map<String,Category> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("ai-fundamentals",Category.AI_FUNDAMENTALS);
        CATEGORIES.put("neural-networks",Category.NEURAL_NETWORKS);
        CATEGORIES.put("future-of-ai",Category.FUTURE_OF_AI);
        CATEGORIES.put("search-game-playing",Category.SEARCH_AND_GAME_PLAYING);
        CATEGORIES.put("machine-learning",Category.MACHINE_LEARNING);
        CATEGORIES.put("deep-learning",Category.DEEP_LEARNING);
        CATEGORIES.put("research-in-ai",Category.RESEARCH_IN_AI);
        CATEGORIES.put("knowledge-problem-representation",Category.KNOWLEDGE_AND_PROBLEM_REPRESENTATION);
    }

    public GamePresentation(AppService service, StartupState startup,
                            AssetCatalog assets,
                            Runnable fullscreen, Runnable exit) {
        this.service=service; this.startup=startup; this.assets=assets;
        this.fullscreen=fullscreen; this.exit=exit;
        this.theme=startup.preferences().theme();
        this.reduceMotion=startup.preferences().reduceMotion();
        screens.resizeRelocate(0,0,1920,1080);
        screens.setPrefSize(1920,1080);
        screens.setMinSize(1920,1080);
        screens.setMaxSize(1920,1080);
        overlays.resizeRelocate(0,0,1920,1080);
        overlays.setPrefSize(1920,1080);
        viewport.canvas().getChildren().addAll(screens,overlays);
        root.setCenter(viewport);
        register(Screen.HOME,this::home);
        register(Screen.CATEGORIES,this::categories);
        register(Screen.GAMEPLAY,this::gameplay);
        register(Screen.MENU,this::menu);
        root.addEventFilter(KeyEvent.KEY_PRESSED,this::keyPressed);
        root.setStyle("-fx-background-color: "+(theme==Theme.DARK?"#02060a":"#f4fafa")+";");
        show(Screen.HOME);
        if(!startup.notices().isEmpty()) popup("Content notice",String.join("\n",startup.notices()),"Close",this::closeOverlay,null,null);
    }
    public Parent root(){return root;}
    public boolean closeOverlay(){return overlays.close();}
    public void requestExit(){openOverlay(Overlay.EXIT);}
    public Screen screen(){return router.current();}
    public GameplayViewState gameplayState(){return RoundPresenter.gameplay(round);}

    public boolean reducedMotion(){return reduceMotion;}
    boolean motionEnabled(){return !reduceMotion;}
    public boolean motionRunning(){
        return root.lookupAll(".button").stream().filter(GameButton.class::isInstance)
                .map(GameButton.class::cast).anyMatch(GameButton::motionRunning);
    }

    String themeKey(){return theme.name().toLowerCase(Locale.ROOT);}
    Color ink(){return theme==Theme.DARK?Color.web("#f4fafa"):Color.web("#02060a");}
    void register(Screen screen, Consumer<Pane> render){
        router.register(screen,()->new ScreenRouter.View() {
            final Pane pane = new Pane();
            { pane.setPrefSize(1920,1080); refresh(); }
            public Parent root() { return pane; }
            public void refresh() { pane.getChildren().clear(); render.accept(pane); }
        });
    }

    void show(Screen screen){
        overlays.close();router.show(screen);
    }
    void background(Pane pane,String screen){
        var image=assets.imageView(themeKey(),screen+".background");
        image.setFitWidth(1920);image.setFitHeight(1080);pane.getChildren().add(image);
    }
    GameButton original(Pane pane,String id,String accessible,Runnable action){
        var a=assets.artwork(themeKey(),id);
        var button=new GameButton(assets.image(themeKey(),id),a.width(),a.height(),accessible);
        button.setMotionEnabled(motionEnabled());
        button.setRectangularHitArea(a.rectangularHitArea());
        button.relocate(a.left(),a.top());button.setId(id.replace('.','-'));
        button.setOnAction(event->{action.run();});pane.getChildren().add(button);return button;
    }
    GameButton action(Pane pane,String text,double x,double y,double width,Runnable action){
        var a=assets.artwork(themeKey(),"button.blank");
        var button=new GameButton(assets.image(themeKey(),"button.blank"),width,width*a.height()/a.width(),text);
        button.setMotionEnabled(motionEnabled());
        button.setCaption(text,assets.font("body",Math.min(36,width/8)),ink());
        button.relocate(x,y);button.setOnAction(event->{action.run();});pane.getChildren().add(button);return button;
    }
    Label text(Pane pane,String value,double x,double y,double width,double height,double size,boolean bold){
        var label=new Label(value);label.setWrapText(true);label.setTextFill(ink());
        label.setFont(assets.font(bold?"heading":"body",size));label.resizeRelocate(x,y,width,height);
        label.setMinSize(width,height);label.setPrefSize(width,height);label.setMaxSize(width,height);
        label.setAlignment(Pos.CENTER);pane.getChildren().add(label);return label;
    }
    void panel(Pane pane,double x,double y,double width){
        var a=assets.artwork(themeKey(),"panel.dialog");
        var image=assets.imageView(themeKey(),"panel.dialog");image.setFitWidth(width);
        image.setFitHeight(width*a.height()/a.width());image.relocate(x,y);pane.getChildren().add(image);
    }
    void home(Pane pane){ HomeScreen.render(this,pane); }
    void categories(Pane pane){ CategoryScreen.render(this,pane); }
    void chooseCategory(Category category){
        boolean ready=startup.bankAvailable()&&Arrays.stream(Difficulty.values())
                .allMatch(d->startup.bank().pool(category,d).size()>=GameRules.QUESTIONS_PER_DIFFICULTY);
        if(ready){
            round=new GameRound(startup.bank(),category,new Random());
            router.refresh();show(Screen.GAMEPLAY);return;
        }
        popup(category.displayName(),"This category is not ready yet. It needs five easy, five medium, and five hard questions.",
                "Close",this::closeOverlay,"Home",()->show(Screen.HOME));
    }
    void gameplay(Pane pane){ GameplayScreen.render(this,pane); }
    void menu(Pane pane){ MenuScreen.render(this,pane); }
    void toggleTheme(){
        theme=theme==Theme.DARK?Theme.LIGHT:Theme.DARK;
        root.setStyle("-fx-background-color: "+(theme==Theme.DARK?"#02060a":"#f4fafa")+";");
        router.refresh();
        savePreferences();
    }

    void toggleMotion(){
        reduceMotion=!reduceMotion;
        router.refresh();
        savePreferences();
    }
    void savePreferences(){
        service.savePreferences(new AppPreferences(theme,reduceMotion))
                .ifPresent(message->popup("Settings",message,"Close",this::closeOverlay,null,null));
    }


    void popup(String title,String message,String primary,Runnable yes,String secondary,Runnable no){
        var pane=new Pane();pane.setPrefSize(1050,626);pane.setMaxSize(1050,626);
        panel(pane,0,0,1050);text(pane,title,85,65,880,70,44,true);
        text(pane,message,95,155,860,265,32,false);
        if(secondary==null)action(pane,primary,365,475,320,yes);
        else{action(pane,primary,165,475,300,yes);action(pane,secondary,580,475,300,no);}
        overlays.show(pane);
    }
    boolean acceptingGameplayInput(){return router.current()==Screen.GAMEPLAY&&!overlays.isVisible();}
    @Override public void selectAnswer(String answer){
        if(!acceptingGameplayInput())return;
        if(round!=null)round.select(answer);
        router.refreshCurrent();
    }
    @Override public void lockAnswer(){
        if(!acceptingGameplayInput())return;
        if(round!=null && round.lockEnabled()) {
            round.lockAnswer();
            renderRound();
        }
    }
    void renderRound(){
        router.refreshCurrent();
        if(round.finished())popup("Round complete",String.format(Locale.US,"Payout: %,d credits",round.payout()),"Choose a category",this::replay,null,null);
    }
    @Override public void useHelp(GameplayViewState.Help help){
        if(!acceptingGameplayInput()||gameplayState().helps().get(help)!=GameplayViewState.HelpState.AVAILABLE)return;
        if(round==null||!round.useHelp(GameRound.Help.valueOf(help.name())))return;
        router.refreshCurrent();
        if(help==GameplayViewState.Help.CLUE)openOverlay(Overlay.CLUE);
    }
    @Override public void walkAway(){if(acceptingGameplayInput()&&gameplayState().walkAwayEnabled())openOverlay(Overlay.WALK_AWAY);}
    @Override public void continueGame(){
        if(!acceptingGameplayInput())return;
        if(round!=null){round.nextQuestion();renderRound();}
    }
    @Override public void replay(){round=null;show(Screen.CATEGORIES);}
    @Override public void openOverlay(Overlay overlay){
        switch(overlay){
            case CLUE -> popup("Clue",round==null?"":round.clue(),"Close",this::closeOverlay,null,null);
            case WALK_AWAY -> {
                if(router.current()!=Screen.GAMEPLAY||!gameplayState().walkAwayEnabled())return;
                popup("Walk away?","Leave the round with the displayed credits?","Walk away",()->{
                    if(round!=null&&round.walkAwayEnabled()){round.walkAway();renderRound();}
                },"Keep playing",this::closeOverlay);
            }
            case EXIT -> popup("Exit game?","Close Be a Millionaire?","Exit",exit,"Cancel",this::closeOverlay);
            case ABANDON -> popup("Leave this round?","Return to the home screen?","Home",()->{round=null;show(Screen.HOME);},"Cancel",this::closeOverlay);
            case UNAVAILABLE_CATEGORY -> popup("Category unavailable","This category needs more questions before a round can start.","Close",this::closeOverlay,null,null);
            case RESOURCE_ERROR,DATA_ERROR -> popup("Unable to load content","A required resource or question file could not be loaded.","Close",this::closeOverlay,null,null);
        }
    }


    void keyPressed(KeyEvent event){
        if(event.isAltDown()||event.isControlDown()||event.isMetaDown()||overlays.isVisible())return;
        Node focus=root.getScene()==null?null:root.getScene().getFocusOwner();
        if(focus instanceof TextInputControl||focus instanceof ComboBoxBase<?>)return;
        if(event.getCode()==KeyCode.BACK_SPACE){
            if(router.current()==Screen.GAMEPLAY)openOverlay(Overlay.ABANDON);else show(Screen.HOME);
            event.consume();return;
        }
        if(router.current()==Screen.GAMEPLAY){
            String letter=event.getCode().name();
            if(Set.of("A","B","C","D").contains(letter)){selectAnswer(letter);event.consume();}
            else if(event.getCode()==KeyCode.ENTER&&gameplayState().lockEnabled()){lockAnswer();event.consume();}
            else if(event.getCode()==KeyCode.ENTER&&round!=null&&round.status()==GameRound.Status.CORRECT){continueGame();event.consume();}
        }
    }
}
