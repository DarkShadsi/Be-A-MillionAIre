package com.beamillionaire.ui.design;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

/** A panel owns its edge traces for exactly as long as its visible composition exists. */
public final class AnimatedPanel extends Pane implements AutoCloseable {
    private final MotionScope motion=new MotionScope();
    private final Pane traces=new Pane();
    private boolean closed;

    public AnimatedPanel(double width,double height) {
        setMinSize(width,height);setPrefSize(width,height);setMaxSize(width,height);
        traces.setManaged(false);traces.setMouseTransparent(true);traces.resize(width,height);
        CircuitEffects.addPanel(traces,motion,width,height);getChildren().add(traces);
    }
    public void setMotionState(boolean available,boolean enabled) {
        if(closed)return;
        traces.toFront();
        if(!enabled)motion.stop();else if(available)motion.resume();else motion.pause();
    }
    public boolean motionRunning() { return motion.motionRunning(); }
    @Override public void close() {
        if(closed)return;
        closed=true;motion.dispose();getChildren().forEach(AnimatedPanel::disposeButtons);
    }
    private static void disposeButtons(Node node) {
        if(node instanceof GameButton button)button.dispose();
        else if(node instanceof Parent parent)parent.getChildrenUnmodifiable().forEach(AnimatedPanel::disposeButtons);
    }
}
