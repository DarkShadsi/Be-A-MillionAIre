package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** Small traveling highlights follow circuit edges; screen-sized effects are deliberately avoided. */
public final class CircuitEffects {
    private CircuitEffects() {}

    public static void addScreen(Pane host,String screen,MotionScope scope) {
        if("categories".equals(screen))return;
        if("gameplay".equals(screen)) {
            double[][] paths={
                    {613,106,604,106,524,37,111,34,40,107,40,568},
                    {51,566,64,583,64,662,18,700,18,788,122,788,151,822,288,841},
                    {18,732,18,768,61,811,124,811,153,822},
                    {288,841,193,841,171,822,151,822,124,844,18,844},
                    {37,869,37,1054,813,1054}
            };
            corners(host,scope,paths);
        }else {
            double[][] paths={
                    {192,62,373,62},
                    {46,93,46,65,62,48,156,48,185,77,270,77},
                    {78,80,78,107,130,160,130,183},
                    {220,89,153,89,125,61,74,61,60,75,60,112,111,160,111,225},
                    {95,105,95,76,120,76,149,103,187,103,196,118,261,118,275,94,321,94},
                    {33,48,33,119,74,153,74,252,100,279,100,323},
                    {53,160,53,389},
                    {102,198,95,202,95,239},
                    {57,732,57,867,32,892,32,970},
                    {37,793,37,867},
                    {57,918,57,1007,147,1007},
                    {91,1032,174,1032,199,1007,332,1007},
                    {199,1028,271,1028}
            };
            corners(host,scope,paths);
        }
    }

    private static void corners(Pane host,MotionScope scope,double[][] paths){
        for(int index=0;index<paths.length;index++){
            screenTrace(host,scope,paths[index],7+index%5,index*.13%1);
            screenTrace(host,scope,mirror(paths[index],1920),8+index%4,(.43+index*.17)%1);
        }
    }

    /** Three narrow strokes provide a soft glow without filtering a large image. */
    private static void screenTrace(Pane host,MotionScope scope,double[] points,double cycle,double phase){
        double length=0;
        for(int i=2;i<points.length;i+=2)length+=Math.hypot(points[i]-points[i-2],points[i+1]-points[i-1]);
        double segment=Math.max(35,Math.min(65,length*.2)),travel=(length+segment)/100;
        cycle=Math.max(cycle,travel+.7);
        var breath=stroke(points,4.5,"circuit-breath");breath.setOpacity(.10);
        var halo=stroke(points,6,"circuit-pulse-halo");halo.setOpacity(0);
        var pulse=stroke(points,2.1,"circuit-pulse");pulse.setOpacity(0);
        for(var path:new Polyline[]{halo,pulse}){
            path.getStrokeDashArray().setAll(segment,length+segment);
            path.setStrokeDashOffset(segment);
        }
        var endpoint=new Rectangle(points[points.length-2]-2.5,points[points.length-1]-2.5,5,5);
        endpoint.setManaged(false);endpoint.setMouseTransparent(true);endpoint.setFill(Color.web("#a5f5ff"));
        endpoint.getStyleClass().add("circuit-endpoint");endpoint.setOpacity(0);
        host.getChildren().addAll(breath,halo,pulse,endpoint);
        pulse.setId("circuit-pulse-"+host.getChildren().size());
        var idle=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(breath.opacityProperty(),.10)),
                new KeyFrame(Duration.seconds(3+phase*1.5),new KeyValue(breath.opacityProperty(),.28,Interpolator.EASE_BOTH)));
        idle.setAutoReverse(true);idle.setCycleCount(Animation.INDEFINITE);idle.setDelay(Duration.seconds(phase*2));
        scope.register(idle,()->breath.setOpacity(0));
        var moving=new Timeline(
                new KeyFrame(Duration.ZERO,new KeyValue(pulse.strokeDashOffsetProperty(),segment),
                        new KeyValue(halo.strokeDashOffsetProperty(),segment),new KeyValue(pulse.opacityProperty(),0),
                        new KeyValue(halo.opacityProperty(),0),new KeyValue(endpoint.opacityProperty(),0)),
                new KeyFrame(Duration.seconds(.18),new KeyValue(pulse.opacityProperty(),.92,Interpolator.EASE_BOTH),
                        new KeyValue(halo.opacityProperty(),.24,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(Math.max(.2,travel-.35)),new KeyValue(endpoint.opacityProperty(),0)),
                new KeyFrame(Duration.seconds(Math.max(.3,travel-.175)),new KeyValue(endpoint.opacityProperty(),.85,Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(travel),new KeyValue(pulse.strokeDashOffsetProperty(),-length,Interpolator.LINEAR),
                        new KeyValue(halo.strokeDashOffsetProperty(),-length,Interpolator.LINEAR),
                        new KeyValue(pulse.opacityProperty(),0,Interpolator.EASE_BOTH),new KeyValue(halo.opacityProperty(),0,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(travel+.175),new KeyValue(endpoint.opacityProperty(),0,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(cycle),new KeyValue(endpoint.opacityProperty(),0)));
        moving.setDelay(Duration.seconds(phase*cycle));moving.setCycleCount(Animation.INDEFINITE);
        scope.register(moving,()->{pulse.setOpacity(0);halo.setOpacity(0);endpoint.setOpacity(0);});
    }
    private static Polyline stroke(double[] points,double width,String style){
        var path=new Polyline(points);path.setManaged(false);path.setMouseTransparent(true);path.setFill(null);
        path.setStroke(Color.web("#67e4f5"));path.setStrokeWidth(width);path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.getStyleClass().addAll("circuit-trace",style);return path;
    }

    public static void addPanel(Pane host,MotionScope scope,double width,double height) {
        panelTrace(host,scope,scaled(new double[]{.019,.663,.019,.037,.588,.054},width,height),0,.76);
        panelTrace(host,scope,scaled(new double[]{.594,.088,.707,.088,.720,.051,.934,.051},width,height),.35,.76);
        panelTrace(host,scope,scaled(new double[]{.938,.099,.938,.963,.338,.963},width,height),.58,.76);
        panelTrace(host,scope,scaled(new double[]{.046,.948,.097,.948,.111,.932,.156,.932},width,height),.17,.64);
    }

    private static void panelTrace(Pane host,MotionScope scope,double[] points,double phase,double intensity){
        trace(host,scope,points,7,phase,intensity,true);
    }

    private static void trace(Pane host,MotionScope scope,double[] points,double seconds,double phase,double intensity) {
        trace(host,scope,points,seconds,phase,intensity,false);
    }
    private static void trace(Pane host,MotionScope scope,double[] points,double seconds,double phase,double intensity,boolean panel) {
        var path=new Polyline(points);path.setManaged(false);path.setMouseTransparent(true);
        path.setFill(null);path.setStroke(Color.web("#67e4f5"));path.setStrokeWidth(2.2);
        path.setStrokeLineCap(StrokeLineCap.ROUND);path.setOpacity(0);
        path.getStyleClass().add("circuit-trace");
        double length=0;
        for(int index=2;index<points.length;index+=2)
            length+=Math.hypot(points[index]-points[index-2],points[index+1]-points[index-1]);
        double segment=Math.min(90,length*.2),cycle=length+segment;
        path.getStrokeDashArray().setAll(segment,length);
        path.setStrokeDashOffset(-phase*cycle);
        path.setId("circuit-trace-"+host.getChildren().size());host.getChildren().add(path);
        double travel=panel?2.4:seconds;
        var timeline=new Timeline(new KeyFrame(Duration.ZERO,
                new KeyValue(path.strokeDashOffsetProperty(),-phase*cycle,Interpolator.LINEAR),
                new KeyValue(path.opacityProperty(),0,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(travel*.14),new KeyValue(path.opacityProperty(),intensity,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(travel*.72),new KeyValue(path.opacityProperty(),intensity*.56,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(travel),
                        new KeyValue(path.strokeDashOffsetProperty(),-(phase+1)*cycle,Interpolator.LINEAR),
                        new KeyValue(path.opacityProperty(),panel?intensity*.12:0,Interpolator.EASE_BOTH)));
        if(panel)timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.seconds(3.2),new KeyValue(path.opacityProperty(),0,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(seconds),new KeyValue(path.strokeDashOffsetProperty(),-(phase+1)*cycle),
                        new KeyValue(path.opacityProperty(),0)));
        timeline.setCycleCount(Animation.INDEFINITE);
        scope.register(timeline,()->{path.setOpacity(0);path.setStrokeDashOffset(-phase*cycle);});
    }
    private static double[] mirror(double[] points,double width) {
        double[] result=points.clone();for(int index=0;index<result.length;index+=2)result[index]=width-result[index];return result;
    }
    private static double[] scaled(double[] points,double width,double height) {
        double[] result=points.clone();for(int index=0;index<result.length;index++)result[index]*=index%2==0?width:height;return result;
    }
}
