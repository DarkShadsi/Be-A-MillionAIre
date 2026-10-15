package com.beamillionaire.ui.design;

import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/** Editable native geometry for the win composition; only small shapes carry motion. */
public final class WinArtwork {
    private record Ink(Shape shape,boolean fill,boolean gold,double opacity) {}
    private final List<Ink> inks=new ArrayList<>();
    private final List<Shape> goldHighlights=new ArrayList<>();
    private final MotionScope motion;
    private final Pane host;
    public WinArtwork(Pane host,MotionScope motion){this.host=host;this.motion=motion;}

    public void setLightTheme(boolean light){
        Color cyan=Color.web(light?"#007e9e":"#009ac6"),gold=Color.web(light?"#896600":"#d4bd16");
        for(var ink:inks){
            Color color=ink.gold()?gold:cyan;
            color=new Color(color.getRed(),color.getGreen(),color.getBlue(),ink.opacity());
            if(ink.fill())ink.shape().setFill(color);else ink.shape().setStroke(color);
        }
        goldHighlights.forEach(shape->shape.setStroke(Color.web(light?"#a17400":"#fff2b3")));
    }
    public void frame(){
        line(host,2,false,.85,48,322,48,48,244,48,262,34,330,34);
        line(host,2,false,.85,48,60,239,60,251,48,412,48);
        line(host,3,false,.9,78,740,78,1000,1140,1000);
        line(host,2,false,.8,1455,1000,1852,1000,1852,46);
        line(host,2,false,.5,1310,48,1852,48);
        line(host,2,false,.65,1390,63,1800,63);
        line(host,3,false,.9,61,750,61,936);
        polygon(host,false,2.5,.9,78,974,99,986,99,1010,78,1022,57,1010,57,986);
        dot(host,78,998,7);dot(host,1140,1000,8);
        trace(host,7,0,48,322,48,48,244,48,262,34,330,34);
        trace(host,8,.3,78,740,78,1000,1140,1000);
        trace(host,9,.6,1455,1000,1852,1000,1852,46,1310,46);
        star(1855,48,52,4,0);star(1784,55,26,6,.7);star(1860,140,25,8,1.2);
    }
    public void categoryFrame(){
        polygon(host,false,3,1,112,137,149,101,436,101,474,137,474,259,436,296,149,296,112,259);
        polygon(host,false,1,.9,185,122,391,122,401,132,401,162,391,172,185,172,175,162,175,132);
    }
    public void winningsFrame(){
        line(host,4,false,1,555,350,1351,350,1351,692,555,692,555,350);
        var corners=new Group();corners.setId("win-panel-gold-rest");decorative(corners);
        host.getChildren().add(corners);
        for(double[] points:new double[][]{{555,450,555,350,655,350},{1251,350,1351,350,1351,450},
                {555,592,555,692,655,692},{1251,692,1351,692,1351,592}})
            line(corners,5,true,1,points);
        goldOrbit("win-panel-gold-orbit",555,350,796,342,4,10,corners);
    }
    /** Continuous clockwise perimeter accents; the frame and its contents never rotate. */
    public Group goldOrbit(String id,double x,double y,double width,double height,int segments,double seconds){
        return goldOrbit(id,x,y,width,height,segments,seconds,null);
    }
    private Group goldOrbit(String id,double x,double y,double width,double height,int segments,double seconds,Node restingAccent){
        if(width<=0||height<=0||segments<=0||seconds<=0||!Double.isFinite(x+y+width+height+seconds))
            throw new IllegalArgumentException("Invalid gold perimeter motion.");
        double perimeter=2*(width+height),slot=perimeter/segments;
        double segment=Math.min(slot*.36,Math.min(200,Math.min(width,height)*.75));
        double coreWidth=width>=300?4.5:2.2;
        var orbit=new Group();orbit.setId(id);decorative(orbit);orbit.setOpacity(0);
        var timeline=new Timeline();
        var start=new ArrayList<KeyValue>();var end=new ArrayList<KeyValue>();
        start.add(new KeyValue(orbit.opacityProperty(),1));end.add(new KeyValue(orbit.opacityProperty(),1));
        if(restingAccent!=null){
            start.add(new KeyValue(restingAccent.opacityProperty(),0));end.add(new KeyValue(restingAccent.opacityProperty(),0));
        }
        for(int index=0;index<3;index++){
            var path=new Path(new MoveTo(x,y),new LineTo(x+width,y),new LineTo(x+width,y+height),
                    new LineTo(x,y+height),new ClosePath());
            path.setId(id+new String[]{"-halo","-glow","-core"}[index]);
            path.setFill(null);path.setStrokeWidth(coreWidth*new double[]{2.7,1.65,1}[index]);
            path.setStrokeLineCap(StrokeLineCap.ROUND);path.setStrokeLineJoin(StrokeLineJoin.ROUND);
            path.getStrokeDashArray().setAll(segment,slot-segment);
            ink(path,false,true,new double[]{.10,.28,1}[index]);
            if(index==2)goldHighlights.add(path);
            orbit.getChildren().add(path);
            start.add(new KeyValue(path.strokeDashOffsetProperty(),0,Interpolator.LINEAR));
            end.add(new KeyValue(path.strokeDashOffsetProperty(),-perimeter,Interpolator.LINEAR));
        }
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.ZERO,start.toArray(KeyValue[]::new)),
                new KeyFrame(Duration.seconds(seconds),end.toArray(KeyValue[]::new)));
        timeline.setCycleCount(Animation.INDEFINITE);
        motion.register(timeline,()->{
            orbit.setOpacity(0);if(restingAccent!=null)restingAccent.setOpacity(1);
            orbit.getChildren().forEach(node->((Path)node).setStrokeDashOffset(0));
        });
        host.getChildren().add(orbit);return orbit;
    }
    public void ladderFrame(){
        polygon(host,false,3,1,1440,137,1476,101,1766,101,1802,137,1802,752,1766,788,1476,788,1440,752);
        polygon(host,false,2,1,1476,129,1738,129,1757,140,1757,183,1490,183,1476,172);
        trace(host,9,.4,1440,510,1440,137,1476,101,1766,101,1802,137,1802,510);
    }
    /** Three independently rotating rings around stationary answer text. */
    public Group answeredRings(){
        var entrance=new Group();entrance.setId("win-answer-rings");
        var breathing=new Group();
        for(int index=0;index<3;index++){
            var ring=ring(166-index*24,index);ring.setId("win-answer-ring-"+index);
            breathing.getChildren().add(ring);UiEffects.spin(motion,ring,new double[]{24,-36,48}[index]);
        }
        breathing.setLayoutX(290);breathing.setLayoutY(571);entrance.getChildren().add(breathing);
        UiEffects.ambient(motion,breathing,new UiEffects.AmbientProfile(12,.99,1.01,0,0,.9,.15));
        circle(entrance,290,571,104,1.2,false,.75);
        circle(entrance,290,571,98,.7,false,.45);
        host.getChildren().add(entrance);decorative(entrance);return entrance;
    }
    /** Rotation occurs inside the fixed perspective transform, never on the ellipse wrapper. */
    public Group platform(){
        var entrance=new Group();entrance.setId("win-platform");
        var perspective=new Group();perspective.setLayoutX(953);perspective.setLayoutY(865);perspective.setScaleY(.38);
        for(int index=0;index<2;index++){
            var ring=ring(index==0?275:215,index==0?1:0,index==0?1:3);ring.setId("win-platform-ring-"+index);
            perspective.getChildren().add(ring);UiEffects.spin(motion,ring,index==0?30:-45);
        }
        circle(perspective,0,0,155,2,false,.6);
        var idle=new Group(perspective);entrance.getChildren().add(idle);
        UiEffects.ambient(motion,idle,new UiEffects.AmbientProfile(17,.99,1.01,0,0,.94,.55));
        host.getChildren().add(entrance);decorative(entrance);return entrance;
    }
    private Group ring(double radius,int style){return ring(radius,style,1);}
    private Group ring(double radius,int style,double weight){
        var group=new Group();decorative(group);
        circle(group,0,0,radius,1,false,.65);
        circle(group,0,0,radius-10,1,false,.5);
        int count=style==0?12:style==1?9:60;
        for(int index=0;index<count;index++){
            var arc=new Arc(0,0,radius-5,radius-5,index*360.0/count+3,style==2?2:360.0/count-12);
            arc.setType(ArcType.OPEN);arc.setFill(null);arc.setStrokeWidth((style==0?17:style==1?8:7)*weight);
            ink(arc,false,false,index%3==0?.95:.65);group.getChildren().add(arc);
        }
        for(int index=0;index<36;index++){
            double angle=index*Math.PI/18;
            double inner=radius-21,outer=radius-27;
            line(group,index%3==0?2:1,false,.6,Math.cos(angle)*inner,Math.sin(angle)*inner,
                    Math.cos(angle)*outer,Math.sin(angle)*outer);
        }
        return group;
    }
    private void star(double x,double y,double radius,double seconds,double delay){
        var star=polygon(host,true,0,.9,x,y-radius,x+radius*.18,y-radius*.18,x+radius,y,
                x+radius*.18,y+radius*.18,x,y+radius,x-radius*.18,y+radius*.18,x-radius,y,x-radius*.18,y-radius*.18);
        var pulse=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(star.opacityProperty(),.45),
                new KeyValue(star.scaleXProperty(),.92),new KeyValue(star.scaleYProperty(),.92)),
                new KeyFrame(Duration.seconds(seconds/2),new KeyValue(star.opacityProperty(),1,Interpolator.EASE_BOTH),
                        new KeyValue(star.scaleXProperty(),1.06,Interpolator.EASE_BOTH),
                        new KeyValue(star.scaleYProperty(),1.06,Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(seconds),new KeyValue(star.opacityProperty(),.45),
                        new KeyValue(star.scaleXProperty(),.92),new KeyValue(star.scaleYProperty(),.92)));
        pulse.setDelay(Duration.seconds(delay));pulse.setCycleCount(Animation.INDEFINITE);
        motion.register(pulse,()->{star.setOpacity(.8);star.setScaleX(1);star.setScaleY(1);});
    }
    private void trace(Pane parent,double seconds,double delay,double...points){
        CircuitEffects.addTrace(parent,motion,points,seconds,delay);
    }
    private void dot(Pane parent,double x,double y,double radius){circle(parent,x,y,radius,3,false,1);}
    private Circle circle(Object parent,double x,double y,double radius,double width,boolean fill,double opacity){
        var circle=new Circle(x,y,radius);circle.setFill(null);circle.setStrokeWidth(width);ink(circle,fill,false,opacity);add(parent,circle);return circle;
    }
    private Polygon polygon(Pane parent,boolean fill,double width,double opacity,double... points){
        var polygon=new Polygon(points);polygon.setFill(null);polygon.setStrokeWidth(width);ink(polygon,fill,false,opacity);add(parent,polygon);return polygon;
    }
    private Polyline line(Object parent,double width,boolean gold,double opacity,double... points){
        var line=new Polyline(points);line.setFill(null);line.setStrokeWidth(width);line.setStrokeLineCap(StrokeLineCap.BUTT);
        ink(line,false,gold,opacity);add(parent,line);return line;
    }
    private void ink(Shape shape,boolean fill,boolean gold,double opacity){
        inks.add(new Ink(shape,fill,gold,opacity));decorative(shape);
    }
    private static void add(Object parent,Node node){if(parent instanceof Pane pane)pane.getChildren().add(node);else ((Group)parent).getChildren().add(node);}
    private static void decorative(Node node){node.setManaged(false);node.setMouseTransparent(true);}
}
