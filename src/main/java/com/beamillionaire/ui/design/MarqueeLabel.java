package com.beamillionaire.ui.design;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.Objects;

/** A clipped, seamless leftward marquee with a fully wrapped reduced-motion alternative. */
public final class MarqueeLabel extends Pane implements AutoCloseable {
    private final Pane moving=new Pane();
    private final Text first=new Text(),second=new Text();
    private final Label stationary=new Label();
    private final Rectangle clip=new Rectangle();
    private final Timeline timeline=new Timeline();
    private boolean available,enabled,closed;
    private double measuredWidth=-1;
    private String text="";
    private Font font=Font.getDefault();
    private final double minimumSize;
    public MarqueeLabel(){this(22);}
    public MarqueeLabel(double minimumSize){
        this.minimumSize=minimumSize;
        setId("categoryMarquee");setClip(clip);setMouseTransparent(true);
        moving.setManaged(false);stationary.setManaged(false);stationary.setWrapText(true);
        stationary.setAlignment(javafx.geometry.Pos.CENTER);
        stationary.setPadding(javafx.geometry.Insets.EMPTY);stationary.setMinWidth(0);
        stationary.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        moving.getChildren().addAll(first,second);getChildren().addAll(moving,stationary);
    }
    public void setColor(Color ink){first.setFill(ink);second.setFill(ink);stationary.setTextFill(ink);}
    public void setText(String value,Font font,Color ink){
        if(!Objects.equals(text,value)||!Objects.equals(this.font,font)){
            text=value;this.font=font;measuredWidth=-1;requestLayout();
        }
        first.setText(value);second.setText(value);stationary.setText(value);setAccessibleText(value);
        first.setFont(font);second.setFont(font);first.setFill(ink);second.setFill(ink);stationary.setTextFill(ink);
    }
    @Override protected void layoutChildren(){
        clip.setWidth(getWidth());clip.setHeight(getHeight());
        stationary.resizeRelocate(0,0,getWidth(),getHeight());
        double size=font.getSize();
        stationary.applyCss();
        while(true){
            stationary.setFont(new Font(font.getName(),size));
            if(Math.ceil(stationary.prefHeight(getWidth()))<=getHeight()||size<=minimumSize)break;
            size=Math.max(minimumSize,size-1);
        }
        double baseline=(getHeight()-first.getLayoutBounds().getHeight())/2-first.getLayoutBounds().getMinY();
        first.setY(baseline);second.setY(baseline);
        if(measuredWidth!=getWidth()){
            timeline.stop();moving.setTranslateX(0);measuredWidth=getWidth();
            double distance=first.getLayoutBounds().getWidth()+48;
            second.setX(distance);
            timeline.getKeyFrames().clear();
            if(first.getLayoutBounds().getWidth()>getWidth()){
                timeline.getKeyFrames().setAll(new KeyFrame(Duration.ZERO,new KeyValue(moving.translateXProperty(),0)),
                        new KeyFrame(Duration.seconds(1.2),new KeyValue(moving.translateXProperty(),0)),
                        new KeyFrame(Duration.seconds(1.2+distance/30),new KeyValue(moving.translateXProperty(),-distance,Interpolator.LINEAR)));
                timeline.setCycleCount(Animation.INDEFINITE);
            }
            sync();
        }
    }
    public void setMotionState(boolean available,boolean enabled){this.available=available;this.enabled=enabled;sync();}
    private void sync(){
        if(closed)return;
        boolean overflowing=!timeline.getKeyFrames().isEmpty();
        moving.setVisible(enabled&&overflowing);stationary.setVisible(!enabled||!overflowing);
        if(!enabled){timeline.stop();moving.setTranslateX(0);}
        else if(available&&overflowing)timeline.play();else timeline.pause();
    }
    public double offset(){return moving.getTranslateX();}
    public boolean motionRunning(){return timeline.getStatus()==Animation.Status.RUNNING;}
    @Override public void close(){closed=true;timeline.stop();timeline.getKeyFrames().clear();}
}
