package com.beamillionaire.ui.design;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Retains screens and owns navigation motion independently of their ambient motion. */
public final class ScreenRouter {
    public enum Screen { HOME, CATEGORIES, GAMEPLAY, MENU, RESULTS, SCORES }
    public interface View {
        Parent root();
        default void onShown() {}
        default void refresh() {}
        default void onHidden() {}
        default void dispose() { onHidden(); }
    }
    private final StackPane host;
    private final Map<Screen, Supplier<View>> factories = new EnumMap<>(Screen.class);
    private final Map<Screen, View> views = new EnumMap<>(Screen.class);
    private Screen current;
    private boolean motionEnabled;
    private Animation transition;
    private View incoming;
    private Parent outgoing;
    private boolean incomingShown;

    public ScreenRouter(StackPane host) {
        this.host = Objects.requireNonNull(host);
        host.addEventFilter(MouseEvent.ANY,event->{if(transitioning())event.consume();});
        host.addEventFilter(KeyEvent.ANY,event->{
            if(transitioning()&&event.getCode()!=KeyCode.F11&&event.getCode()!=KeyCode.ESCAPE)event.consume();
        });
    }
    public void register(Screen screen, Supplier<View> factory) {
        if (factories.putIfAbsent(screen, Objects.requireNonNull(factory)) != null)
            throw new IllegalArgumentException("Screen already registered: " + screen);
    }
    public void setMotionEnabled(boolean enabled) {
        motionEnabled=enabled;
        if(!enabled)finishTransition();
    }
    public boolean transitioning() { return transition!=null; }
    public boolean motionRunning() { return transition!=null&&transition.getStatus()==Animation.Status.RUNNING; }
    public void show(Screen screen) {
        if (screen == current || transitioning()) return;
        var factory = factories.get(screen);
        if (factory == null) throw new IllegalArgumentException("Screen not registered: " + screen);
        View next = views.computeIfAbsent(screen, ignored -> Objects.requireNonNull(factory.get()));
        Screen previous=current;
        View old=previous==null?null:views.get(previous);
        if(old!=null)old.onHidden();
        current=screen;
        next.refresh();
        boolean animate=motionEnabled&&old!=null&&(previous==Screen.CATEGORIES||screen==Screen.CATEGORIES);
        if(!animate) {
            host.getChildren().setAll(next.root());
            next.onShown();
            return;
        }
        outgoing=old.root();incoming=next;incomingShown=false;
        var exit=new Timeline(new KeyFrame(Duration.millis(180),
                new KeyValue(outgoing.opacityProperty(),0,Interpolator.EASE_BOTH),
                new KeyValue(outgoing.translateYProperty(),-8,Interpolator.EASE_BOTH)));
        var enter=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(next.root().opacityProperty(),0)),
                new KeyFrame(Duration.millis(220),new KeyValue(next.root().opacityProperty(),1,Interpolator.EASE_OUT)));
        exit.setOnFinished(event->{
            reset(outgoing);
            host.getChildren().setAll(next.root());
            incomingShown=true;
            next.onShown();
        });
        transition=new SequentialTransition(exit,enter);
        transition.setOnFinished(event->finishTransition());
        transition.play();
    }
    /** Settles the committed destination when motion is suppressed or a refresh interrupts navigation. */
    private void finishTransition() {
        if(transition==null)return;
        transition.stop();transition=null;
        reset(outgoing);reset(incoming.root());
        if(host.getChildren().size()!=1||host.getChildren().getFirst()!=incoming.root())
            host.getChildren().setAll(incoming.root());
        if(!incomingShown)incoming.onShown();
        incoming=null;outgoing=null;incomingShown=false;
    }
    private static void reset(Node node) { node.setOpacity(1);node.setTranslateY(0); }
    public Screen current() { return current; }
    public void refresh() {
        finishTransition();
        Node focus=host.getScene()==null?null:host.getScene().getFocusOwner();
        String id=focus==null?null:focus.getId();
        views.values().forEach(View::refresh);
        restoreFocus(id);
    }
    public void refreshCurrent() {
        if (current == null) return;
        finishTransition();
        var focus = host.getScene() == null ? null : host.getScene().getFocusOwner();
        String id = focus == null ? null : focus.getId();
        views.get(current).refresh();
        restoreFocus(id);
    }
    private void restoreFocus(String id) {
        if(id==null)return;
        var replacement=host.lookup("#"+id);
        if(replacement!=null&&!replacement.isDisabled())replacement.requestFocus();
    }
    public void dispose() {
        if(transition!=null){transition.stop();transition=null;}
        if(outgoing!=null)reset(outgoing);
        if(incoming!=null)reset(incoming.root());
        views.values().forEach(View::dispose);
        host.getChildren().clear();views.clear();current=null;incoming=null;outgoing=null;
    }
}
