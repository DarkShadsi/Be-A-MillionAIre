package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** Small traveling highlights follow circuit edges; screen-sized effects are deliberately avoided. */
public final class CircuitEffects {
    private CircuitEffects() {}

    /** Native frames supply their own paths so highlights always follow the visible outline. */
    public static void addPaths(Pane host,MotionScope scope,double[][] paths){
        for(int index=0;index<paths.length;index++)
            addTrace(host,scope,paths[index],6+index%3,index*.13%1);
    }

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
            addTrace(host,scope,paths[index],6+index%3,index*.13%1);
            addTrace(host,scope,mirror(paths[index],1920),7+index%3,(.43+index*.17)%1);
        }
    }

    private record TraceProfile(double speed,double travelSeconds,double cycleSeconds,boolean breathing) {}
    private static final double CORE_WIDTH=3.4,HALO_WIDTH=9;

    /** The same compact vector trace is used by screen frames, Instructions and win artwork. */
    public static Group addTrace(Pane host,MotionScope scope,double[] points,double minimumCycle,double phase){
        return trace(host,scope,points,new TraceProfile(150,0,minimumCycle,true),phase);
    }

    public static void addPanel(Pane host,MotionScope scope,double width,double height) {
        var profile=new TraceProfile(0,3,5.5,false);
        trace(host,scope,scaled(new double[]{.019,.663,.019,.037,.588,.054},width,height),profile,0);
        trace(host,scope,scaled(new double[]{.594,.088,.707,.088,.720,.051,.934,.051},width,height),profile,.35);
        trace(host,scope,scaled(new double[]{.938,.099,.938,.963,.338,.963},width,height),profile,.58);
        trace(host,scope,scaled(new double[]{.046,.948,.097,.948,.111,.932,.156,.932},width,height),profile,.17);
    }

    /** Layered strokes create the glow without a blur over the large containing node. */
    private static Group trace(Pane host,MotionScope scope,double[] points,TraceProfile profile,double phase){
        double length=0;
        for(int index=2;index<points.length;index+=2)
            length+=Math.hypot(points[index]-points[index-2],points[index+1]-points[index-1]);
        double segment=Math.min(120,length*.35);
        double travel=profile.travelSeconds()>0?profile.travelSeconds():Math.max(.55,(length+segment)/profile.speed());
        double cycle=Math.max(profile.cycleSeconds(),travel+1.2);
        var group=new Group();group.setManaged(false);group.setMouseTransparent(true);
        group.setId("circuit-trace-"+host.getChildren().size());
        var breath=stroke(points,4.5,"circuit-breath");breath.setOpacity(0);
        var halo=stroke(points,HALO_WIDTH,"circuit-pulse-halo");halo.setOpacity(0);
        var pulse=stroke(points,CORE_WIDTH,"circuit-pulse");pulse.setOpacity(0);
        pulse.setId("circuit-pulse-"+host.getChildren().size());
        for(var path:new Polyline[]{halo,pulse}){
            path.getStrokeDashArray().setAll(segment,length+segment);
            path.setStrokeDashOffset(segment);
        }
        var endpoint=new Rectangle(points[points.length-2]-3,points[points.length-1]-3,6,6);
        endpoint.setManaged(false);endpoint.setMouseTransparent(true);endpoint.setFill(Color.web("#b9f8ff"));
        endpoint.getStyleClass().add("circuit-endpoint");endpoint.setOpacity(0);
        if(profile.breathing())group.getChildren().add(breath);
        group.getChildren().addAll(halo,pulse,endpoint);host.getChildren().add(group);
        var timeline=new Timeline(
                new KeyFrame(Duration.ZERO,new KeyValue(pulse.strokeDashOffsetProperty(),segment),
                        new KeyValue(halo.strokeDashOffsetProperty(),segment),new KeyValue(pulse.opacityProperty(),0),
                        new KeyValue(halo.opacityProperty(),0),new KeyValue(endpoint.opacityProperty(),0)),
                new KeyFrame(Duration.seconds(.18),new KeyValue(pulse.opacityProperty(),.95,Interpolator.EASE_BOTH),
                        new KeyValue(halo.opacityProperty(),.35,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(travel-.30),new KeyValue(pulse.opacityProperty(),.95),
                        new KeyValue(halo.opacityProperty(),.35),new KeyValue(endpoint.opacityProperty(),0)),
                new KeyFrame(Duration.seconds(travel-.15),new KeyValue(endpoint.opacityProperty(),.9,Interpolator.EASE_OUT)),
                new KeyFrame(Duration.seconds(travel),new KeyValue(pulse.strokeDashOffsetProperty(),-length,Interpolator.LINEAR),
                        new KeyValue(halo.strokeDashOffsetProperty(),-length,Interpolator.LINEAR),
                        new KeyValue(pulse.opacityProperty(),0,Interpolator.EASE_BOTH),new KeyValue(halo.opacityProperty(),0,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(travel+.15),new KeyValue(endpoint.opacityProperty(),0,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(cycle),new KeyValue(endpoint.opacityProperty(),0)));
        if(profile.breathing())timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,new KeyValue(breath.opacityProperty(),.12)),
                new KeyFrame(Duration.seconds(cycle/2),new KeyValue(breath.opacityProperty(),.28,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(cycle),new KeyValue(breath.opacityProperty(),.12,Interpolator.EASE_BOTH)));
        timeline.setDelay(Duration.seconds(phase*cycle));timeline.setCycleCount(Animation.INDEFINITE);
        scope.register(timeline,()->{
            breath.setOpacity(0);pulse.setOpacity(0);halo.setOpacity(0);endpoint.setOpacity(0);
            pulse.setStrokeDashOffset(segment);halo.setStrokeDashOffset(segment);
        });
        return group;
    }
    private static Polyline stroke(double[] points,double width,String style){
        var path=new Polyline(points);path.setManaged(false);path.setMouseTransparent(true);path.setFill(null);
        path.setStroke(Color.web(style.equals("circuit-pulse")?"#b9f8ff":"#23d9f2"));
        path.setStrokeWidth(width);path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.getStyleClass().addAll("circuit-trace",style);return path;
    }
    private static double[] mirror(double[] points,double width) {
        double[] result=points.clone();for(int index=0;index<result.length;index+=2)result[index]=width-result[index];return result;
    }
    private static double[] scaled(double[] points,double width,double height) {
        double[] result=points.clone();for(int index=0;index<result.length;index++)result[index]*=index%2==0?width:height;return result;
    }
}
