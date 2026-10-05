package com.beamillionaire.ui.design;

import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** Earned progress is gold; the active question's potential prize remains a separate cyan outline. */
public final class PrizeLadderEffects extends Group implements AutoCloseable {
    private final Rectangle active=new Rectangle(1518,180,270,35);
    private final Rectangle earned=new Rectangle(1518,180,270,35);
    private final Polygon credits=new Polygon(1503,937,1607,937,1660,897,1856,897,1856,1013,1503,1013);
    private final Group gold=new Group(earned,credits);
    private final DropShadow rowGlow=new DropShadow(),creditGlow=new DropShadow();
    private final Timeline pulse=new Timeline();
    private final ScaleTransition checkpoint=new ScaleTransition(Duration.millis(160),gold);
    private boolean available,enabled,closed;
    private int earnedPosition=-1;
    public PrizeLadderEffects(){
        setId("prizeLadderEffects");setMouseTransparent(true);
        active.setId("activePrizeRow");active.setFill(Color.TRANSPARENT);active.setStroke(Color.web("#00d6ff"));active.setStrokeWidth(2);
        earned.setId("earnedPrizeRow");credits.setId("earnedCreditsFrame");earned.setEffect(rowGlow);credits.setEffect(creditGlow);
        getChildren().addAll(active,gold);
        checkpoint.setFromX(1);checkpoint.setFromY(1);checkpoint.setToX(1.008);checkpoint.setToY(1.008);
        checkpoint.setCycleCount(2);checkpoint.setAutoReverse(true);checkpoint.setInterpolator(Interpolator.EASE_BOTH);
    }
    public void update(int activePosition,int earnedPosition,boolean lightTheme){
        active.setY(180+(15-activePosition)*37.9590363153);active.setVisible(activePosition>0);
        Color accent=Color.web(lightTheme?"#927000":"#dec33f");
        double progress=earnedPosition/15.0;
        for(var shape:java.util.List.of(earned,credits)){
            shape.setStroke(accent);shape.setStrokeWidth(2+progress);
            shape.setFill(accent.deriveColor(0,1,1,.06+.10*progress));
        }
        for(var glow:java.util.List.of(rowGlow,creditGlow)){glow.setColor(accent.deriveColor(0,1,1,.65));glow.setRadius(4+8*progress);}
        earned.setY(180+(15-earnedPosition)*37.9590363153);earned.setVisible(earnedPosition>0);
        if(this.earnedPosition!=earnedPosition){
            int previous=this.earnedPosition;this.earnedPosition=earnedPosition;
            pulse.stop();gold.setOpacity(1);
            double period=2.8-progress;
            pulse.getKeyFrames().setAll(new KeyFrame(Duration.ZERO,new KeyValue(gold.opacityProperty(),1)),
                    new KeyFrame(Duration.seconds(period/2),new KeyValue(gold.opacityProperty(),.70,Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.seconds(period),new KeyValue(gold.opacityProperty(),1,Interpolator.EASE_BOTH)));
            pulse.setCycleCount(Animation.INDEFINITE);
            checkpoint.stop();gold.setScaleX(1);gold.setScaleY(1);
            if(previous>=0&&earnedPosition>previous&&(earnedPosition==4||earnedPosition==9||earnedPosition==12)&&available&&enabled)checkpoint.playFromStart();
            sync();
        }
    }
    public int earnedPosition(){return earnedPosition;}
    public void setMotionState(boolean available,boolean enabled){this.available=available;this.enabled=enabled;sync();}
    private void sync(){
        if(closed)return;
        if(!enabled){pulse.stop();checkpoint.stop();gold.setOpacity(1);gold.setScaleX(1);gold.setScaleY(1);}
        else if(available){pulse.play();if(checkpoint.getStatus()==Animation.Status.PAUSED)checkpoint.play();}
        else{pulse.pause();checkpoint.pause();}
    }
    public boolean motionRunning(){return pulse.getStatus()==Animation.Status.RUNNING||checkpoint.getStatus()==Animation.Status.RUNNING;}
    @Override public void close(){closed=true;pulse.stop();pulse.getKeyFrames().clear();checkpoint.stop();}
}
