package com.beamillionaire.ui;
import com.beamillionaire.application.Theme;
import com.beamillionaire.ui.assets.AssetCatalog;
import com.beamillionaire.ui.design.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.*;
import java.util.function.Consumer;
import static com.beamillionaire.ui.design.ScreenRouter.Screen;
import com.beamillionaire.domain.*;
import com.beamillionaire.application.*;
public final class GamePresentation {
    final AssetCatalog assets;
    final Runnable fullscreen, exit;
    Theme theme=Theme.DARK;
    boolean reduceMotion;
    final BorderPane root=new BorderPane();
    final StackPane screens=new StackPane();
    final OverlayHost overlays=new OverlayHost(screens);
    final ScreenRouter router=new ScreenRouter(screens);
    final DesignViewport viewport=new DesignViewport(1920,1080);
    final QuestionBank bank;
    static final Map<String,Category> CATEGORIES=new LinkedHashMap<>();
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
    final AppService service;
    public GamePresentation(AppService service, StartupState startup, AssetCatalog assets, Runnable fullscreen, Runnable exit) {
        this.assets=assets;this.fullscreen=fullscreen;this.exit=exit;
        this.service=service;this.bank=startup.bank();this.theme=startup.preferences().theme();this.reduceMotion=startup.preferences().reduceMotion();
        screens.setPrefSize(1920,1080);screens.setMinSize(1920,1080);screens.setMaxSize(1920,1080);
        overlays.setPrefSize(1920,1080);viewport.canvas().getChildren().addAll(screens,overlays);root.setCenter(viewport);
        register(Screen.HOME,this::home);
        register(Screen.CATEGORIES,this::categories);
        register(Screen.MENU,this::menu);
        root.setStyle("-fx-background-color: "+(theme==Theme.DARK?"#02060a":"#f4fafa")+";");
        show(Screen.HOME);
        root.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,event->{
            if(event.getCode()==javafx.scene.input.KeyCode.BACK_SPACE&&!overlays.isVisible())show(Screen.HOME);
        });
    }
    public Parent root(){return root;}
    public void requestExit(){popup("Exit game?","Close Be a Millionaire?","Exit",exit,"Cancel",this::closeOverlay);}
    public boolean closeOverlay(){return overlays.close();}
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
    void popup(String title,String message,String primary,Runnable yes,String secondary,Runnable no){
        var pane=new Pane();pane.setPrefSize(1050,626);pane.setMaxSize(1050,626);
        panel(pane,0,0,1050);text(pane,title,85,65,880,70,44,true);
        text(pane,message,95,155,860,265,32,false);
        if(secondary==null)action(pane,primary,365,475,320,yes);
        else{action(pane,primary,165,475,300,yes);action(pane,secondary,580,475,300,no);}
        overlays.show(pane);
    }
    void categories(Pane pane){ CategoryScreen.render(this,pane); }
    void chooseCategory(Category category){
        boolean ready=Arrays.stream(Difficulty.values()).allMatch(d->bank.pool(category,d).size()>=QuestionBank.QUESTIONS_PER_DIFFICULTY);
        popup(category.displayName(),ready?"This category is ready.":"This category needs five easy, five medium, and five hard questions.","Close",this::closeOverlay,"Home",()->show(Screen.HOME));
    }
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
}
