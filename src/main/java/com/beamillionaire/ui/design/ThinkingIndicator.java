package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.geometry.VPos;
import javafx.scene.AccessibleRole;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import java.util.List;

/** Font-independent thinking symbol, playful captions, and a stable accessible loading state. */
public final class ThinkingIndicator extends Pane implements AutoCloseable {
    private static final List<String> PHRASES=List.of(
            "Warming up…","Thinking big…","Chasing a million…","Setting the stage…");
    private static final Color INK=Color.web("#85b8cc"),ACCENT=Color.web("#35d7f3");
    private final MotionScope motion=new MotionScope();
    private final Text label=new Text();
    private final Group symbol=new Group();
    private final Scale symbolScale=new Scale();
    private final Shape[] frames=new Shape[7];
    private final boolean animated;
    private boolean ready,closed;

    public ThinkingIndicator(Font font,boolean animated,Duration delay){
        this.animated=animated;setId("splash-thinking-indicator");setManaged(false);setMouseTransparent(true);
        setAccessibleRole(AccessibleRole.TEXT);setAccessibleText("Loading the game");
        label.setId("splash-loading-label");label.setManaged(false);label.setFont(font);
        label.setFill(INK);label.setTextOrigin(VPos.TOP);label.setAccessibleText("Loading the game");
        label.setText(animated?PHRASES.getFirst():"Preparing your game…");
        frames[0]=new Circle(18,18,3);
        int[] spokes={4,8,6,8,12};
        for(int index=1;index<=5;index++){
            var star=new Polygon();int count=spokes[index-1];
            for(int point=0;point<count*2;point++){
                double angle=-Math.PI/2+point*Math.PI/count;
                double radius=point%2==0?10+index*.6:3+index*.35;
                star.getPoints().addAll(18+Math.cos(angle)*radius,18+Math.sin(angle)*radius);
            }
            frames[index]=star;
        }
        var check=new Polyline(8,18,15,25,29,10);check.setStroke(ACCENT);check.setStrokeWidth(3);
        check.setStrokeLineCap(StrokeLineCap.ROUND);check.setStrokeLineJoin(StrokeLineJoin.ROUND);
        frames[6]=check;
        for(int index=0;index<frames.length;index++){
            var shape=frames[index];shape.setId("splash-thinking-frame-"+index);
            shape.setFill(index==6?null:ACCENT);shape.setMouseTransparent(true);symbol.getChildren().add(shape);
        }
        symbol.setManaged(false);symbol.getTransforms().add(symbolScale);
        getChildren().addAll(symbol,label);frame(animated?0:3);
        if(animated){
            int[] order={0,1,2,3,4,5,4,3,2,1};
            var spinner=new Timeline();
            for(int index=0;index<order.length;index++){
                int current=order[index];spinner.getKeyFrames().add(new KeyFrame(Duration.millis(index*140),e->frame(current)));
            }
            spinner.getKeyFrames().add(new KeyFrame(Duration.millis(order.length*140)));
            spinner.setCycleCount(Animation.INDEFINITE);spinner.setDelay(delay);
            motion.register(spinner,()->frame(0));
            var phrases=new Timeline();
            for(int index=0;index<PHRASES.size();index++){
                String phrase=PHRASES.get(index);
                phrases.getKeyFrames().add(new KeyFrame(Duration.seconds(index*2.6),e->{label.setText(phrase);requestLayout();}));
            }
            phrases.getKeyFrames().add(new KeyFrame(Duration.seconds(PHRASES.size()*2.6)));
            phrases.setCycleCount(Animation.INDEFINITE);phrases.setDelay(delay);
            motion.register(phrases,()->{label.setText(PHRASES.getFirst());requestLayout();});
            var shimmer=new Transition(){
                {setCycleDuration(Duration.seconds(1.8));setCycleCount(Animation.INDEFINITE);
                    setInterpolator(Interpolator.LINEAR);setDelay(delay);}
                @Override protected void interpolate(double fraction){
                    double band=100,start=-band+(label.getLayoutBounds().getWidth()+band)*fraction;
                    label.setFill(new LinearGradient(start,0,start+band,0,false,CycleMethod.NO_CYCLE,
                            new Stop(0,INK),new Stop(.35,INK),new Stop(.5,Color.web("#dcfaff")),new Stop(.65,INK),new Stop(1,INK)));
                }
            };
            motion.register(shimmer,()->label.setFill(INK));
        }
    }
    private void frame(int index){for(int i=0;i<frames.length;i++)frames[i].setVisible(i==index);}
    public void setFontSize(double size){label.setFont(new Font(label.getFont().getName(),size));requestLayout();}
    public void setActive(boolean available){
        if(closed||ready||!animated)return;
        if(available)motion.resume();else motion.pause();
    }
    public void finish(){
        if(closed||ready)return;
        ready=true;motion.stop();label.setText("Ready");label.setFill(ACCENT);frame(6);
        label.setAccessibleText("Ready to play");setAccessibleText("Ready to play");requestLayout();
    }
    public boolean motionRunning(){return motion.motionRunning();}
    @Override protected void layoutChildren(){
        double size=label.getFont().getSize(),scale=size/36;
        symbolScale.setX(scale);symbolScale.setY(scale);
        symbol.setLayoutY((getHeight()-size)/2);
        label.relocate(size+size*.35,(getHeight()-label.getLayoutBounds().getHeight())/2);
    }
    @Override public void close(){if(closed)return;closed=true;motion.dispose();}
}
