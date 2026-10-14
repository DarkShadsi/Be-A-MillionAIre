package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/** Native circuit frame and small teaching accents; the illustration itself remains stationary. */
public final class InstructionArtwork {
    private final Pane host;
    private final MotionScope motion;
    private final List<Shape> strokes=new ArrayList<>();
    private final List<Circle> dots=new ArrayList<>();
    private final Group emphasis=new Group();
    private final Rectangle halo=new Rectangle(),accent=new Rectangle();

    public InstructionArtwork(Pane host,MotionScope motion){this.host=host;this.motion=motion;}

    public void frame(){
        line(2,48,306,48,72,252,72,272,51,414,51);
        line(1,61,250,61,84,247,84,260,72,338,72);
        line(2,1506,65,1856,65,1856,1000,1450,1000);
        line(1,1600,80,1839,80,1839,260);
        line(3,62,774,62,1000,1140,1000);
        line(1.5,78,804,78,984,390,984);
        line(2,104,224,104,190,126,168,1165,168,1187,190,1187,780,1165,802,126,802,104,780,104,684);
        line(1.5,1240,267,1780,267);
        dot(62,1000,6);dot(1140,1000,6);dot(1856,65,4);
        trace(7,0,48,306,48,72,252,72,272,51,414,51);
        trace(8,.7,62,774,62,1000,1140,1000);
        trace(9,1.4,1506,65,1856,65,1856,1000,1450,1000);
    }

    public void addEmphasis(){
        for(var rectangle:new Rectangle[]{halo,accent}){
            rectangle.setFill(null);rectangle.setArcWidth(10);rectangle.setArcHeight(10);
            rectangle.setMouseTransparent(true);rectangle.setManaged(false);
            strokes.add(rectangle);
        }
        halo.setStrokeWidth(8);halo.setOpacity(.18);accent.setStrokeWidth(2.5);
        emphasis.getChildren().addAll(halo,accent);emphasis.setManaged(false);emphasis.setMouseTransparent(true);
        emphasis.setId("instructions-emphasis");host.getChildren().add(emphasis);
        var pulse=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(emphasis.opacityProperty(),.58)),
                new KeyFrame(Duration.seconds(1.5),new KeyValue(emphasis.opacityProperty(),1,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(3),new KeyValue(emphasis.opacityProperty(),.58,Interpolator.EASE_BOTH)));
        pulse.setCycleCount(Animation.INDEFINITE);motion.register(pulse,()->emphasis.setOpacity(1));
    }

    public void setTopic(int topic){
        emphasis.setVisible(topic!=0);
        double[] area=switch(topic){
            case 1 -> new double[]{400,190,1120,560};
            case 2 -> new double[]{80,380,260,400};
            case 3 -> new double[]{1600,130,280,650};
            case 4 -> new double[]{555,350,796,342};
            default -> new double[]{0,0,0,0};
        };
        double scale=1050.0/1920;
        for(var rectangle:new Rectangle[]{halo,accent}){
            rectangle.setX(120+area[0]*scale);rectangle.setY(190+area[1]*scale);
            rectangle.setWidth(area[2]*scale);rectangle.setHeight(area[3]*scale);
        }
    }

    public void setLightTheme(boolean light){
        Color cyan=Color.web(light?"#007e9e":"#009ac6");
        strokes.forEach(shape->shape.setStroke(cyan));dots.forEach(dot->dot.setFill(cyan));
        Color teachingAccent=Color.web(light?"#b51f32":"#ff5266");
        halo.setStroke(teachingAccent);accent.setStroke(teachingAccent);
    }

    private Polyline line(double width,double...points){
        var path=new Polyline(points);path.setFill(null);path.setStrokeWidth(width);
        path.setStrokeLineCap(StrokeLineCap.ROUND);path.setManaged(false);path.setMouseTransparent(true);
        path.setStroke(Color.web("#009ac6"));strokes.add(path);host.getChildren().add(path);return path;
    }
    private void dot(double x,double y,double radius){
        var dot=new Circle(x,y,radius,Color.web("#009ac6"));dot.setMouseTransparent(true);dot.setManaged(false);
        dots.add(dot);host.getChildren().add(dot);
    }
    private void trace(double seconds,double delay,double...points){
        var path=line(3.2,points);double length=0;
        for(int i=2;i<points.length;i+=2)length+=Math.hypot(points[i]-points[i-2],points[i+1]-points[i-1]);
        double segment=Math.min(110,length*.2);path.getStrokeDashArray().setAll(segment,length+segment);
        path.setOpacity(0);
        var timeline=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(path.strokeDashOffsetProperty(),segment),
                        new KeyValue(path.opacityProperty(),0)),
                new KeyFrame(Duration.seconds(.3),new KeyValue(path.opacityProperty(),.9,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(seconds-.6),new KeyValue(path.opacityProperty(),.9)),
                new KeyFrame(Duration.seconds(seconds),new KeyValue(path.strokeDashOffsetProperty(),-length,Interpolator.LINEAR),
                        new KeyValue(path.opacityProperty(),0,Interpolator.EASE_BOTH)));
        timeline.setCycleCount(Animation.INDEFINITE);timeline.setDelay(Duration.seconds(delay));
        motion.register(timeline,()->{path.setOpacity(0);path.setStrokeDashOffset(segment);});
    }
}
