package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Objects;

/** Dissolves the splash over an opaque, already laid-out Home without reparenting Home afterward. */
public final class SplashHandoff implements AutoCloseable {
    private final StackPane composition=new StackPane();
    private final StackPane homeLayer;
    private final Timeline fade;
    private final InvalidationListener sizeListener=ignored->resizeSplash();
    private final EventHandler<InputEvent> blockInput=Event::consume;
    private Parent outgoing;
    private Runnable onFinished;
    private final boolean originalManaged;
    private final double originalOpacity;
    private boolean completed,closed;

    public SplashHandoff(Scene scene,Parent outgoingSplash,Parent incomingHome,boolean reducedMotion,Runnable onFinished){
        Objects.requireNonNull(scene);Objects.requireNonNull(outgoingSplash);Objects.requireNonNull(incomingHome);
        if(scene.getRoot()!=outgoingSplash)throw new IllegalArgumentException("The splash must be the current scene root.");
        this.outgoing=outgoingSplash;this.onFinished=Objects.requireNonNull(onFinished);
        originalManaged=outgoing.isManaged();originalOpacity=outgoing.getOpacity();
        homeLayer=new StackPane(incomingHome);homeLayer.setMinSize(0,0);
        composition.setMinSize(0,0);composition.setStyle("-fx-background-color: #070c12;");
        // Block activation without applying disabled styling to the incoming controls.
        composition.addEventFilter(InputEvent.ANY,blockInput);
        var clip=new Rectangle();clip.widthProperty().bind(composition.widthProperty());clip.heightProperty().bind(composition.heightProperty());
        composition.setClip(clip);
        // The splash's oversized HUD must not influence StackPane measurement or centering.
        outgoing.setManaged(false);outgoing.setLayoutX(0);outgoing.setLayoutY(0);outgoing.setOpacity(1);
        composition.resize(scene.getWidth(),scene.getHeight());
        scene.setRoot(composition);
        composition.getChildren().addAll(homeLayer,outgoing);
        composition.widthProperty().addListener(sizeListener);composition.heightProperty().addListener(sizeListener);
        resizeSplash();composition.applyCss();composition.layout();
        fade=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(outgoing.opacityProperty(),1)),
                new KeyFrame(Duration.millis(reducedMotion?180:1000),
                        new KeyValue(outgoing.opacityProperty(),0,Interpolator.SPLINE(.33,0,.67,1))));
        fade.setOnFinished(event->finish());
    }
    public Animation.Status status(){return fade.getStatus();}
    public void resume(){if(!closed&&!completed)fade.play();}
    public void pause(){if(fade.getStatus()==Animation.Status.RUNNING)fade.pause();}
    private void resizeSplash(){
        if(outgoing instanceof Region region)region.resize(composition.getWidth(),composition.getHeight());
    }
    private void finish(){
        if(closed||completed)return;
        completed=true;
        releaseSplash();
        var callback=onFinished;onFinished=null;callback.run();
        Node focus=composition.getScene()==null?null:composition.getScene().getFocusOwner();
        if(!hasHomeFocus(focus)){
            focus=firstFocusable(homeLayer);
            if(focus!=null)focus.requestFocus();
        }
    }
    private void releaseSplash(){
        fade.setOnFinished(null);fade.getKeyFrames().clear();
        composition.widthProperty().removeListener(sizeListener);composition.heightProperty().removeListener(sizeListener);
        composition.getChildren().remove(outgoing);
        outgoing.setManaged(originalManaged);outgoing.setOpacity(originalOpacity);outgoing=null;
        composition.removeEventFilter(InputEvent.ANY,blockInput);
    }
    private static Node firstFocusable(Node node){
        if(!node.isVisible()||node.isDisabled())return null;
        if(node instanceof Parent parent)for(Node child:parent.getChildrenUnmodifiable()){
            Node found=firstFocusable(child);if(found!=null)return found;
        }
        return node.isFocusTraversable()?node:null;
    }
    private boolean hasHomeFocus(Node node){
        if(node==null||!node.isFocusTraversable())return false;
        for(Node current=node;current!=null;current=current.getParent()){
            if(!current.isVisible()||current.isDisabled())return false;
            if(current==homeLayer)return true;
        }
        return false;
    }
    @Override public void close(){
        if(closed)return;
        closed=true;onFinished=null;
        if(!completed){fade.stop();releaseSplash();}
    }
}
