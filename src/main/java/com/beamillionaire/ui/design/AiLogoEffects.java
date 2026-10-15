package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;
import javafx.util.Duration;
import java.util.function.Supplier;

/** Local strokes animate the AI contours; the existing title pixels stay stationary. */
public final class AiLogoEffects {
    private AiLogoEffects() {}

    public static Group create(MotionScope scope,double titleWidth,double titleHeight) {
        var result=new Group();result.setId("home-ai-glow");
        result.setManaged(false);result.setMouseTransparent(true);
        // The 1375x335 title export places the original 250x205 logo at (780,55), at 4/3 scale.
        // Match ImageView's preserveRatio fit, rather than rounding the exported dimensions.
        double imageScale=Math.min(titleWidth/1375,titleHeight/335);
        double logoScale=imageScale*4/3;
        result.getTransforms().add(new Affine(logoScale,0,780*imageScale,0,logoScale,55*imageScale));
        addLetter(result,scope,"a",AiLogoGeometry::letterA,6,8,0);
        addLetter(result,scope,"i",AiLogoGeometry::letterI,8,10,1.6);
        return result;
    }

    private static void addLetter(Group host,MotionScope scope,String id,Supplier<SVGPath> geometry,
                                  double breathingSeconds,double pulseSeconds,double delay) {
        var breath=stroke(geometry.get(),4.2,"home-ai-"+id+"-breath");
        var halo=stroke(geometry.get(),5,"home-ai-"+id+"-halo");
        var pulse=stroke(geometry.get(),1.4,"home-ai-"+id+"-pulse");
        host.getChildren().addAll(breath,halo,pulse);
        var breathing=new Timeline(
                new KeyFrame(Duration.ZERO,new KeyValue(breath.opacityProperty(),.10)),
                new KeyFrame(Duration.seconds(breathingSeconds/2),
                        new KeyValue(breath.opacityProperty(),.28,Interpolator.EASE_BOTH)));
        breathing.setAutoReverse(true);breathing.setCycleCount(Animation.INDEFINITE);
        breathing.setDelay(Duration.seconds(delay/2));
        scope.register(breathing,()->breath.setOpacity(0));

        double segment=55,period=pulseSeconds*100;
        for(var path:new SVGPath[]{halo,pulse}){
            path.getStrokeDashArray().setAll(segment,period-segment);
            path.setStrokeDashOffset(segment);
        }
        var traveling=new Timeline(
                new KeyFrame(Duration.ZERO,new KeyValue(pulse.strokeDashOffsetProperty(),segment),
                        new KeyValue(halo.strokeDashOffsetProperty(),segment),
                        new KeyValue(pulse.opacityProperty(),0),new KeyValue(halo.opacityProperty(),0)),
                new KeyFrame(Duration.seconds(.25),new KeyValue(pulse.opacityProperty(),.95,Interpolator.EASE_BOTH),
                        new KeyValue(halo.opacityProperty(),.24,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(pulseSeconds-.5),new KeyValue(pulse.opacityProperty(),.85),
                        new KeyValue(halo.opacityProperty(),.20)),
                new KeyFrame(Duration.seconds(pulseSeconds),
                        new KeyValue(pulse.strokeDashOffsetProperty(),segment-period,Interpolator.LINEAR),
                        new KeyValue(halo.strokeDashOffsetProperty(),segment-period,Interpolator.LINEAR),
                        new KeyValue(pulse.opacityProperty(),0,Interpolator.EASE_BOTH),
                        new KeyValue(halo.opacityProperty(),0,Interpolator.EASE_BOTH)));
        traveling.setDelay(Duration.seconds(delay));traveling.setCycleCount(Animation.INDEFINITE);
        scope.register(traveling,()->{
            pulse.setOpacity(0);halo.setOpacity(0);
            pulse.setStrokeDashOffset(segment);halo.setStrokeDashOffset(segment);
        });
    }

    private static SVGPath stroke(SVGPath path,double width,String id){
        path.setId(id);path.setManaged(false);path.setMouseTransparent(true);
        path.setFill(null);path.setStroke(Color.web("#8cedff"));path.setStrokeWidth(width);
        path.setStrokeLineCap(StrokeLineCap.ROUND);path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setOpacity(0);path.getStyleClass().add("ai-logo-trace");return path;
    }
}
