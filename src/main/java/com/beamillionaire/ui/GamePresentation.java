package com.beamillionaire.ui;

import com.beamillionaire.application.*;
import com.beamillionaire.domain.*;
import com.beamillionaire.engine.GameRules;
import com.beamillionaire.engine.GameRound;
import com.beamillionaire.ui.assets.AssetCatalog;
import com.beamillionaire.ui.design.*;
import com.beamillionaire.ui.state.*;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.*;
import com.beamillionaire.storage.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import static com.beamillionaire.ui.design.ScreenRouter.Screen;

/** Presentation coordinator: navigation and rendering, with engine decisions supplied as view state. */
public final class GamePresentation implements PresentationActions, AutoCloseable {
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
    final ScoreStore scoreStore;
    final GameAudio audio;
    boolean soundEnabled;
    String roundId;
    boolean roundRecorded;
    String scoreNotice;
    Theme theme;
    boolean reduceMotion;
    boolean motionSuppressed;
    private final Map<Screen,ScreenView> views = new EnumMap<>(Screen.class);
    private ScreenView renderingView;
    private Scene observedScene;
    private Window observedWindow;
    private boolean disposed;
    private final InvalidationListener availabilityListener=ignored->syncMotion();
    private final ChangeListener<Window> windowListener=(property,oldWindow,newWindow)->observeWindow(newWindow);
    private final ChangeListener<Scene> sceneListener=(property,oldScene,newScene)->observeScene(newScene);
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
                            AssetCatalog assets, ScoreStore scoreStore, GameAudio audio,
                            Runnable fullscreen, Runnable exit) {
        this.service=service; this.startup=startup; this.assets=assets;
        this.scoreStore=scoreStore; this.audio=audio;
        this.fullscreen=fullscreen; this.exit=exit;
        this.theme=startup.preferences().theme();
        this.reduceMotion=startup.preferences().reduceMotion();
        this.soundEnabled=startup.preferences().soundEnabled();
        screens.resizeRelocate(0,0,1920,1080);
        screens.setPrefSize(1920,1080);
        screens.setMinSize(1920,1080);
        screens.setMaxSize(1920,1080);
        overlays.resizeRelocate(0,0,1920,1080);
        overlays.setPrefSize(1920,1080);
        viewport.canvas().getChildren().addAll(screens,overlays);
        viewport.canvas().setClip(new Rectangle(1920,1080));
        root.setCenter(viewport);
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/ui/styles/game.css")).toExternalForm());
        root.sceneProperty().addListener(sceneListener);
        root.visibleProperty().addListener(availabilityListener);
        viewport.visibleProperty().addListener(availabilityListener);
        screens.visibleProperty().addListener(availabilityListener);
        overlays.visibleProperty().addListener(availabilityListener);
        register(Screen.HOME,this::home);
        register(Screen.CATEGORIES,this::categories);
        register(Screen.GAMEPLAY,this::gameplay);
        register(Screen.MENU,this::menu);
        register(Screen.RESULTS,this::results);
        register(Screen.SCORES,this::scores);
        root.addEventFilter(KeyEvent.KEY_PRESSED,this::keyPressed);
        applyTheme();
        show(Screen.HOME);
        if(!startup.notices().isEmpty()) popup("Content notice",String.join("\n",startup.notices()),"Close",this::closeOverlay,null,null);
    }
    public Parent root(){return root;}
    public boolean closeOverlay(){
        boolean closed=overlays.close();
        if(closed&&router.current()==Screen.CATEGORIES)
            buttons(screens).forEach(button->button.setFeedback(GameButton.Feedback.NORMAL));
        return closed;
    }
    public void requestExit(){openOverlay(Overlay.EXIT);}
    public Screen screen(){return router.current();}
    public GameplayViewState gameplayState(){return RoundPresenter.gameplay(round);}

    public boolean reducedMotion(){return reduceMotion;}
    public void setMotionSuppressed(boolean value){
        if(motionSuppressed==value)return;
        motionSuppressed=value;
        syncMotion();
    }
    boolean motionEnabled(){return !reduceMotion&&!motionSuppressed;}
    public boolean motionRunning(){
        return router.motionRunning()||overlays.motionRunning()||views.values().stream().anyMatch(view->view.motion.motionRunning()
                ||view.motionActivity.stream().anyMatch(BooleanSupplier::getAsBoolean))
                ||buttons(root).stream().anyMatch(GameButton::motionRunning);
    }

    String themeKey(){return theme.name().toLowerCase(Locale.ROOT);}
    Color ink(){return theme==Theme.DARK?Color.web("#f4fafa"):Color.web("#02060a");}
    void register(Screen screen, Consumer<Pane> render){
        router.register(screen,()->{
            var view=new ScreenView(screen,render);views.put(screen,view);return view;
        });
    }

    private final class ScreenView implements ScreenRouter.View {
        final Screen screen;
        final Consumer<Pane> render;
        final Pane pane=new Pane();
        final MotionScope motion=new MotionScope();
        final List<Runnable> appearanceUpdates=new ArrayList<>();
        final List<Runnable> refreshActions=new ArrayList<>();
        final List<Runnable> disposeActions=new ArrayList<>();
        final List<BiConsumer<Boolean,Boolean>> motionActions=new ArrayList<>();
        final List<BooleanSupplier> motionActivity=new ArrayList<>();
        boolean built;
        boolean active;
        boolean started;
        boolean entrancePending;
        ScreenView(Screen screen,Consumer<Pane> render){
            this.screen=screen;this.render=render;
            pane.setPrefSize(1920,1080);pane.setMinSize(1920,1080);pane.setMaxSize(1920,1080);
            pane.setClip(new Rectangle(1920,1080));
            pane.sceneProperty().addListener(availabilityListener);
            pane.visibleProperty().addListener(availabilityListener);
        }
        public Parent root(){return pane;}
        public void refresh(){
            if(!built){
                renderingView=this;
                try{render.accept(pane);built=true;}finally{renderingView=null;}
            }
            appearanceUpdates.forEach(Runnable::run);
            refreshActions.forEach(Runnable::run);
            sync();
        }
        public void onShown(){
            active=true;entrancePending=!started;
            if(screen==Screen.CATEGORIES)buttons(pane).forEach(button->button.setFeedback(GameButton.Feedback.NORMAL));
            sync();
        }
        public void onHidden(){active=false;entrancePending=false;sync();}
        public void dispose(){
            active=false;motion.dispose();buttons(pane).forEach(GameButton::dispose);
            disposeActions.forEach(Runnable::run);
            disposeActions.clear();motionActions.clear();motionActivity.clear();refreshActions.clear();appearanceUpdates.clear();
            pane.sceneProperty().removeListener(availabilityListener);
            pane.visibleProperty().removeListener(availabilityListener);
        }
        void sync(){
            boolean available=active&&!disposed&&windowActive()
                    &&pane.getScene()==root.getScene()&&pane.isVisible()&&screens.isVisible();
            boolean visible=available&&!overlays.isVisible();
            buttons(pane).forEach(button->button.setMotionEnabled(visible&&motionEnabled()));
            motionActions.forEach(action->action.accept(visible,motionEnabled()));
            if(!motionEnabled()){
                motion.stop();started=false;
                entrancePending=false;
                return;
            }
            if(!visible){motion.pause();return;}
            if(started)motion.resume();
            else{motion.start(entrancePending);started=true;entrancePending=false;}
        }
    }

    /** Retained view hooks; callbacks update data without rebuilding visual nodes. */
    void onRefresh(Runnable action){if(renderingView==null)action.run();else renderingView.refreshActions.add(action);}
    void onMotionChanged(BiConsumer<Boolean,Boolean> action){renderingView.motionActions.add(action);}
    void onDispose(Runnable action){renderingView.disposeActions.add(action);}
    void onMotionRunning(BooleanSupplier activity){renderingView.motionActivity.add(activity);}
    MotionScope motionScope(){return renderingView.motion;}
    private void onAppearance(Runnable action){if(renderingView!=null)renderingView.appearanceUpdates.add(action);}

    private void applyTheme(){
        root.getStyleClass().removeAll("theme-dark","theme-light");
        root.getStyleClass().add("theme-"+themeKey());
        root.setStyle("-fx-background-color: "+(theme==Theme.DARK?"#02060a":"#f4fafa")+";");
    }
    private void observeScene(Scene scene){
        if(observedScene!=null)observedScene.windowProperty().removeListener(windowListener);
        observedScene=scene;
        if(scene!=null)scene.windowProperty().addListener(windowListener);
        observeWindow(scene==null?null:scene.getWindow());
    }
    private void observeWindow(Window window){
        if(observedWindow!=null){
            observedWindow.showingProperty().removeListener(availabilityListener);
            if(observedWindow instanceof Stage stage)stage.iconifiedProperty().removeListener(availabilityListener);
        }
        observedWindow=window;
        if(window!=null){
            window.showingProperty().addListener(availabilityListener);
            if(window instanceof Stage stage)stage.iconifiedProperty().addListener(availabilityListener);
        }
        syncMotion();
    }
    private boolean windowActive(){
        return root.getScene()!=null&&observedWindow!=null&&observedWindow.isShowing()&&root.isVisible()&&viewport.isVisible()
                &&(!(observedWindow instanceof Stage stage)||!stage.isIconified());
    }
    private void syncMotion(){
        router.setMotionEnabled(!disposed&&windowActive()&&screens.isVisible()&&motionEnabled()&&!overlays.isVisible());
        views.values().forEach(ScreenView::sync);
        buttons(overlays).forEach(button->button.setMotionEnabled(!disposed&&windowActive()&&motionEnabled()));
        overlays.setMotionState(!disposed&&windowActive(),motionEnabled());
    }
    private static List<GameButton> buttons(Node node){
        var found=new ArrayList<GameButton>();collectButtons(node,found);return found;
    }
    private static void collectButtons(Node node,List<GameButton> found){
        if(node instanceof GameButton button){found.add(button);return;}
        if(node instanceof Parent parent)parent.getChildrenUnmodifiable().forEach(child->collectButtons(child,found));
    }
    @Override public void close(){
        if(disposed)return;
        disposed=true;overlays.close();router.dispose();views.clear();
        root.sceneProperty().removeListener(sceneListener);
        root.visibleProperty().removeListener(availabilityListener);
        viewport.visibleProperty().removeListener(availabilityListener);
        screens.visibleProperty().removeListener(availabilityListener);
        overlays.visibleProperty().removeListener(availabilityListener);
        observeScene(null);
    }

    void show(Screen screen){
        if(router.transitioning())return;
        overlays.close();router.show(screen);
    }
    void background(Pane pane,String screen){
        composeBackground(pane,screen,renderingView.motion,true);
    }
    void composeBackground(Pane pane,String screen,MotionScope scope,boolean categoryEntrance){
        Runnable backdrop=()->pane.setStyle("-fx-background-color: "+(theme==Theme.LIGHT?"#f4fafa":screen.equals("gameplay")?"#02060a":"#000000")+";");
        backdrop.run();onAppearance(backdrop);
        for(var asset:assets.layers(themeKey(),screen)){
            if(asset.id().equals("home.hexagons")){
                var hexagons=new AmbientHexagons(pane,scope);
                var wrapper=new Group(hexagons);wrapper.relocate(asset.left(),asset.top());
                wrapper.setId("home-hexagons");wrapper.setMouseTransparent(true);pane.getChildren().add(wrapper);
                onMotionChanged(hexagons::setActive);onDispose(hexagons::dispose);
                onMotionRunning(hexagons::motionRunning);
                onAppearance(()->hexagons.setLightTheme(theme==Theme.LIGHT));
                continue;
            }
            var image=assets.imageView(themeKey(),asset.id());
            image.setFitWidth(asset.width());image.setFitHeight(asset.height());
            onAppearance(()->image.setImage(assets.image(themeKey(),asset.id())));
            if(asset.id().endsWith(".base")){
                ControlArtwork.cleanBackground(image,screen);
                if(screen.equals("gameplay"))GameplayFrame.clearInterior(image);
            }
            if(asset.id().startsWith("gameplay.motif-")){image.setFitWidth(128);image.setFitHeight(128);}
            var idle=new Group(image);
            if(asset.id().equals("home.title"))idle.getChildren().add(AiLogoEffects.create(scope,asset.width(),asset.height()));
            var entrance=new Group(idle);entrance.relocate(asset.left(),asset.top());
            if(asset.id().startsWith("gameplay.motif-"))entrance.relocate(896,838);
            entrance.setId(asset.id().replace('.','-'));entrance.setMouseTransparent(true);
            pane.getChildren().add(entrance);
            if(asset.id().equals("gameplay.base"))GameplayFrame.add(pane);
            if(asset.id().startsWith("home.ring-")){
                int index=Integer.parseInt(asset.id().substring(asset.id().length()-1))-1;
                UiEffects.ambient(scope,idle,UiEffects.HOME_RINGS.get(index));
                UiEffects.spin(scope,idle,UiEffects.HOME_SPINS.get(index));
            }
            else if(asset.id().equals("home.hexagons"))UiEffects.ambient(scope,idle,UiEffects.HEXAGONS);
            else if(asset.id().startsWith("categories.ring-")){
                int index=Integer.parseInt(asset.id().substring(asset.id().length()-1))-1;
                UiEffects.ambient(scope,idle,UiEffects.CATEGORY_RINGS.get(index));
                UiEffects.spin(scope,idle,UiEffects.CATEGORY_SPINS.get(index));
                if(categoryEntrance)UiEffects.entrance(scope,entrance,0,0,.98,360,0);
            }else if(asset.id().equals("categories.title")){
                UiEffects.ambient(scope,idle,UiEffects.TITLE).setDelay(javafx.util.Duration.millis(320));
                if(categoryEntrance)UiEffects.entrance(scope,entrance,0,12,.96,320,0);
            }else if(asset.id().startsWith("gameplay.motif-")){
                int index=Integer.parseInt(asset.id().substring(asset.id().length()-1))-1;
                UiEffects.ambient(scope,idle,UiEffects.GAMEPLAY_MOTIFS.get(index));
                UiEffects.spin(scope,idle,UiEffects.GAMEPLAY_SPINS.get(index));
            }
        }
        CircuitEffects.addScreen(pane,screen,scope);
    }
    void categoryEntrance(Pane pane,GameButton button,int order){
        pane.getChildren().remove(button);var wrapper=new Group(button);pane.getChildren().add(wrapper);
        UiEffects.entrance(renderingView.motion,wrapper,button.getLayoutX()<960?-16:16,0,.98,240,80+45*order);
    }
    private void configure(GameButton button){
        button.setMotionEnabled(!disposed&&windowActive()&&motionEnabled());
        button.setLightTheme(theme==Theme.LIGHT);
        if(renderingView!=null&&renderingView.screen==Screen.GAMEPLAY){
            button.setCompactMotion(true);
        }
    }
    GameButton original(Pane pane,String id,String accessible,Runnable action){
        var a=assets.artwork(themeKey(),id);
        GameButton button;
        if(id.startsWith("category.")){
            button=ControlArtwork.createCategory(id,accessible,assets.font("heading",36),ink());
            onMotionChanged(button::setCaptionMotionState);
        }
        else if(id.equals("walk-away"))button=ControlArtwork.createWalkAway(accessible,assets.font("heading",44),Color.web("#ff777e"));
        else{
            button=new GameButton(assets.image(themeKey(),id),a.width(),a.height(),accessible);
            button.relocate(a.left(),a.top());
            button.setRectangularHitArea(a.rectangularHitArea());
        }
        configure(button);
        ControlArtwork.trimAnswerArtwork(button,id);
        onAppearance(()->{button.setArtwork(assets.image(themeKey(),id));button.setLightTheme(theme==Theme.LIGHT);
            button.setCaptionColor(id.equals("walk-away")?Color.web("#ff777e"):ink());});
        button.setId(id.replace('.','-'));
        button.setOnAction(event->{if(router.transitioning()&&!overlays.isVisible())return;action.run();});pane.getChildren().add(button);return button;
    }
    GameButton action(Pane pane,String text,double x,double y,double width,Runnable action){
        var a=assets.artwork(themeKey(),"button.blank");
        var button=new GameButton(assets.image(themeKey(),"button.blank"),width,width*a.height()/a.width(),text);
        configure(button);
        onAppearance(()->{button.setArtwork(assets.image(themeKey(),"button.blank"));button.setLightTheme(theme==Theme.LIGHT);button.setCaptionColor(ink());});
        button.setCaption(text,assets.font("body",Math.min(36,width/8)),ink());
        button.relocate(x,y);button.setId("action-"+text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+","-"));
        button.setOnAction(event->{if(router.transitioning()&&!overlays.isVisible())return;action.run();});pane.getChildren().add(button);return button;
    }
    Label text(Pane pane,String value,double x,double y,double width,double height,double size,boolean bold){
        var label=new Label(value);label.setWrapText(true);label.setTextFill(ink());
        label.setFont(assets.font(bold?"heading":"body",size));label.resizeRelocate(x,y,width,height);
        label.setMinSize(width,height);label.setPrefSize(width,height);label.setMaxSize(width,height);
        onAppearance(()->label.setTextFill(ink()));
        label.setAlignment(Pos.CENTER);pane.getChildren().add(label);return label;
    }
    void panel(Pane pane,double x,double y,double width){
        var a=assets.artwork(themeKey(),"panel.dialog");
        var image=assets.imageView(themeKey(),"panel.dialog");image.setFitWidth(width);
        onAppearance(()->image.setImage(assets.image(themeKey(),"panel.dialog")));
        image.setFitHeight(width*a.height()/a.width());image.relocate(x,y);pane.getChildren().add(image);
    }
    /** Match the wrapper to the artwork so standalone panels and modal panels center identically. */
    Pane panelContent(String id,double width){
        var artwork=assets.artwork(themeKey(),"panel.dialog");
        double height=width*artwork.height()/artwork.width();
        var pane=new AnimatedPanel(width,height);pane.setId(id);
        pane.setMinSize(width,height);pane.setPrefSize(width,height);pane.setMaxSize(width,height);
        panel(pane,0,0,width);
        // Artwork belongs below the native edge tracers created by AnimatedPanel.
        pane.getChildren().getLast().toBack();
        if(renderingView!=null){onMotionChanged(pane::setMotionState);onDispose(pane::close);onMotionRunning(pane::motionRunning);}
        return pane;
    }
    Pane centeredPanel(Pane host,String id,double width){
        var panel=panelContent(id,width);
        panel.relocate((1920-width)/2,(1080-panel.getPrefHeight())/2);
        host.getChildren().add(panel);return panel;
    }
    void home(Pane pane){ HomeScreen.render(this,pane); }
    void categories(Pane pane){ CategoryScreen.render(this,pane); }
    void chooseCategory(Category category){
        if(router.transitioning()||overlays.isVisible())return;
        boolean ready=startup.bankAvailable()&&Arrays.stream(Difficulty.values())
                .allMatch(d->startup.bank().pool(category,d).size()>=GameRules.QUESTIONS_PER_DIFFICULTY);
        if(ready){
            round=new GameRound(startup.bank(),category,new Random());
            roundId=UUID.randomUUID().toString();roundRecorded=false;scoreNotice=null;
            show(Screen.GAMEPLAY);return;
        }
        popup(category.displayName(),"This category is not ready yet. It needs five easy, five medium, and five hard questions.",
                "Close",this::closeOverlay,"Home",()->show(Screen.HOME));
    }
    void gameplay(Pane pane){ GameplayScreen.render(this,pane); }
    void menu(Pane pane){ MenuScreen.render(this,pane); }
    void toggleTheme(){
        theme=theme==Theme.DARK?Theme.LIGHT:Theme.DARK;
        applyTheme();
        router.refresh();
        savePreferences();
    }
    void toggleSound(){
        soundEnabled=!soundEnabled;audio.setEnabled(soundEnabled);router.refresh();savePreferences();
    }
    void toggleMotion(){
        reduceMotion=!reduceMotion;
        syncMotion();
        router.refresh();
        savePreferences();
    }
    void savePreferences(){
        service.savePreferences(new AppPreferences(theme,reduceMotion,soundEnabled))
                .ifPresent(message->popup("Settings",message,"Close",this::closeOverlay,null,null));
    }
    void results(Pane pane){ ResultsScreen.render(this,pane); }
    void scores(Pane pane){ ScoresScreen.render(this,pane); }
    void popup(String title,String message,String primary,Runnable yes,String secondary,Runnable no){
        var pane=panelContent("popup-panel",1050);
        text(pane,title,85,65,880,70,44,true);
        text(pane,message,95,155,860,265,32,false);
        if(secondary==null)action(pane,primary,365,475,320,yes);
        else{action(pane,primary,165,475,300,yes);action(pane,secondary,580,475,300,no);}
        overlays.show(pane);
    }
    void showFullQuestion(String question){
        if(!acceptingGameplayInput())return;
        var pane=panelContent("full-question-panel",1050);
        text(pane,"FULL QUESTION",85,45,880,65,44,true);
        var body=new Label(question);body.setWrapText(true);body.setTextFill(ink());
        body.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        body.setMinWidth(0);body.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        body.setFont(assets.font("question",30));body.setMaxWidth(830);
        var scroll=new ScrollPane(body);scroll.setFitToWidth(true);scroll.setId("fullQuestionScroll");
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.resizeRelocate(95,135,860,310);scroll.setPrefSize(860,310);pane.getChildren().add(scroll);
        action(pane,"Close",365,475,320,this::closeOverlay);overlays.show(pane);
    }
    boolean acceptingGameplayInput(){return router.current()==Screen.GAMEPLAY&&!router.transitioning()&&!overlays.isVisible();}
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
        if(round.finished()){
            results=RoundPresenter.results(round);
            if(!roundRecorded) {
                roundRecorded=true;
                try {
                    scoreStore.append(new ScoreEntry(roundId,Instant.now().toString(),
                            round.question().category().displayName(),round.status(),round.correctAnswers(),round.payout()));
                } catch(IOException error) { scoreNotice=error.getMessage(); }
            }
            router.refresh();
            if(round.status()==GameRound.Status.WALKED_AWAY)show(Screen.RESULTS);
        }else router.refreshCurrent();
    }
    @Override public void useHelp(GameplayViewState.Help help){
        if(!acceptingGameplayInput()||round==null)return;
        if(help==GameplayViewState.Help.CLUE&&round.clueRevealed()){
            openOverlay(Overlay.CLUE);return;
        }
        if(gameplayState().helps().get(help)!=GameplayViewState.HelpState.AVAILABLE)return;
        if(!round.useHelp(GameRound.Help.valueOf(help.name())))return;
        router.refreshCurrent();
        if(help==GameplayViewState.Help.CLUE)openOverlay(Overlay.CLUE);
    }
    @Override public void walkAway(){if(acceptingGameplayInput()&&gameplayState().walkAwayEnabled())openOverlay(Overlay.WALK_AWAY);}
    @Override public void continueGame(){
        if(!acceptingGameplayInput())return;
        if(round==null)return;
        if(round.finished()){show(Screen.RESULTS);return;}
        round.nextQuestion();renderRound();
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
        if(router.transitioning()&&!overlays.isVisible()&&event.getCode()!=KeyCode.F11&&event.getCode()!=KeyCode.ESCAPE){event.consume();return;}
        if(event.isAltDown()||event.isControlDown()||event.isMetaDown()||overlays.isVisible())return;
        Node focus=root.getScene()==null?null:root.getScene().getFocusOwner();
        if(focus instanceof TextInputControl||focus instanceof ComboBoxBase<?>||focus instanceof Hyperlink)return;
        if(event.getCode()==KeyCode.BACK_SPACE){
            if(router.current()==Screen.GAMEPLAY)openOverlay(Overlay.ABANDON);else show(Screen.HOME);
            event.consume();return;
        }
        if(router.current()==Screen.GAMEPLAY){
            String letter=event.getCode().name();
            if(Set.of("A","B","C","D").contains(letter)){selectAnswer(letter);event.consume();}
            else if(event.getCode()==KeyCode.ENTER&&gameplayState().lockEnabled()){lockAnswer();event.consume();}
            else if(event.getCode()==KeyCode.ENTER&&round!=null&&(round.status()==GameRound.Status.CORRECT||round.finished())){continueGame();event.consume();}
        }
    }
}
