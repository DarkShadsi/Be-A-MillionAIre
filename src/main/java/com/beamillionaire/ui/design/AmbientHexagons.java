package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/** Original hexagon geometry, with independent drift and non-blocking pointer reactions. */
public final class AmbientHexagons extends Pane implements AutoCloseable {
    private record Hexagon(Group idle, Group pointer, Polygon outline, double stroke) {}
    private final Pane pointerHost;
    private final List<Hexagon> hexagons = new ArrayList<>();
    private final List<Line> connectors = new ArrayList<>();
    private final Timeline interaction = new Timeline();
    private final EventHandler<MouseEvent> moved = this::pointerMoved;
    private final EventHandler<MouseEvent> exited = this::pointerExited;
    private boolean active, enabled, disposed, lightTheme;

    public AmbientHexagons(Pane pointerHost, MotionScope scope) {
        this.pointerHost = pointerHost;
        setMinSize(500,610);setPrefSize(500,610);setMaxSize(500,610);
        setMouseTransparent(true);setPickOnBounds(false);setClip(new Rectangle(500,610));
        double[][] geometry = {{290,86,80,5},{130,238,82,5},{198,454,90,5},{245,322,58,1.5},
                {69,428,60,1.5},{380,249,58,1.5},{279,550,50,1.5},{80,125,43,1.5}};
        double[] periods = {13,17,23,19,21,16,22,18};
        for (int index=0;index<geometry.length;index++) {
            double[] shape = geometry[index];
            var polygon = new Polygon();
            for (int vertex=0;vertex<6;vertex++) {
                double angle = Math.toRadians(vertex*60-15);
                polygon.getPoints().addAll(shape[2]*Math.cos(angle),shape[2]*Math.sin(angle));
            }
            polygon.setFill(Color.TRANSPARENT);polygon.setStroke(accent());polygon.setStrokeWidth(shape[3]);
            var pointer = new Group(polygon);
            var idle = new Group(pointer);idle.setLayoutX(shape[0]);idle.setLayoutY(shape[1]);
            idle.setId("home-hexagon-"+(index+1));getChildren().add(idle);
            hexagons.add(new Hexagon(idle,pointer,polygon,shape[3]));
            UiEffects.ambient(scope,idle,new UiEffects.AmbientProfile(periods[index],.995,1.015,
                    5+index%3,4+index%4,.78,(index*.137)%1));
            double angle=2+index%3;
            var rotation=new Timeline(new KeyFrame(Duration.ZERO,new KeyValue(idle.rotateProperty(),-angle)),
                    new KeyFrame(Duration.seconds(periods[index]/2),new KeyValue(idle.rotateProperty(),angle,Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.seconds(periods[index]),new KeyValue(idle.rotateProperty(),-angle,Interpolator.EASE_BOTH)));
            rotation.setCycleCount(Animation.INDEFINITE);rotation.setDelay(Duration.millis(index*170));
            scope.register(rotation,()->idle.setRotate(0));
        }
        connector(0,-83,30,1,30,-78);
        connector(1,34,76,2,-28,-89);
        pointerHost.addEventHandler(MouseEvent.MOUSE_MOVED,moved);
        pointerHost.addEventHandler(MouseEvent.MOUSE_DRAGGED,moved);
        pointerHost.addEventHandler(MouseEvent.MOUSE_EXITED,exited);
    }

    private Color accent() { return Color.web(lightTheme?"#087f9d":"#008fbd"); }
    public void setLightTheme(boolean value) {
        lightTheme=value;
        hexagons.forEach(hexagon->hexagon.outline().setStroke(accent()));
        connectors.forEach(line->line.setStroke(accent()));
    }
    private void connector(int first,double ax,double ay,int second,double bx,double by) {
        Hexagon a=hexagons.get(first),b=hexagons.get(second);
        var line = new Line();line.setStroke(accent());line.setStrokeWidth(5);line.setOpacity(.8);
        line.startXProperty().bind(coordinate(a,ax,ay,true));line.startYProperty().bind(coordinate(a,ax,ay,false));
        line.endXProperty().bind(coordinate(b,bx,by,true));line.endYProperty().bind(coordinate(b,bx,by,false));
        connectors.add(line);getChildren().addFirst(line);
    }
    private javafx.beans.binding.DoubleBinding coordinate(Hexagon hexagon,double x,double y,boolean horizontal){
        return javafx.beans.binding.Bindings.createDoubleBinding(()->{
            var point=hexagon.idle().localToParent(hexagon.pointer().localToParent(x,y));
            return horizontal?point.getX():point.getY();
        },hexagon.idle().localToParentTransformProperty(),hexagon.pointer().localToParentTransformProperty());
    }
    private void pointerExited(MouseEvent event){if(event.getTarget()==pointerHost)retarget(null);}
    private void pointerMoved(MouseEvent event) {
        if (!disposed && active && enabled && getScene()!=null)
            retarget(sceneToLocal(event.getSceneX(),event.getSceneY()));
    }
    private void retarget(Point2D point) {
        if (disposed || !active || !enabled) return;
        // Stopping a timeline rewinds it; retain the visible transforms before retargeting.
        double[] positions = new double[hexagons.size()*3];
        for(int index=0;index<hexagons.size();index++) {
            var hexagon=hexagons.get(index);positions[index*3]=hexagon.pointer().getTranslateX();
            positions[index*3+1]=hexagon.pointer().getTranslateY();positions[index*3+2]=hexagon.outline().getStrokeWidth();
        }
        interaction.stop();
        var values=new ArrayList<KeyValue>();
        for(int index=0;index<hexagons.size();index++) {
            var hexagon=hexagons.get(index);hexagon.pointer().setTranslateX(positions[index*3]);
            hexagon.pointer().setTranslateY(positions[index*3+1]);hexagon.outline().setStrokeWidth(positions[index*3+2]);
            double dx=point==null?0:point.getX()-hexagon.idle().getLayoutX();
            double dy=point==null?0:point.getY()-hexagon.idle().getLayoutY();
            double proximity=point==null?0:Math.max(0,1-Math.hypot(dx,dy)/210);
            double distance=Math.max(1,Math.hypot(dx,dy));
            values.add(new KeyValue(hexagon.pointer().translateXProperty(),-dx/distance*12*proximity,Interpolator.EASE_OUT));
            values.add(new KeyValue(hexagon.pointer().translateYProperty(),-dy/distance*12*proximity,Interpolator.EASE_OUT));
            values.add(new KeyValue(hexagon.outline().strokeWidthProperty(),hexagon.stroke()+1.4*proximity,Interpolator.EASE_OUT));
        }
        interaction.getKeyFrames().setAll(new KeyFrame(Duration.millis(point==null?600:220),values.toArray(KeyValue[]::new)));
        interaction.playFromStart();
    }
    public void setActive(boolean available,boolean motionEnabled) {
        active=available;enabled=motionEnabled;
        if(disposed)return;
        if(!enabled) {
            interaction.stop();
            hexagons.forEach(hexagon->{hexagon.pointer().setTranslateX(0);hexagon.pointer().setTranslateY(0);
                hexagon.outline().setStrokeWidth(hexagon.stroke());});
        } else if(!active) interaction.pause();
        else if(interaction.getStatus()==Animation.Status.PAUSED) interaction.play();
    }
    public boolean motionRunning() { return interaction.getStatus()==Animation.Status.RUNNING; }
    public void dispose() {
        if(disposed)return;
        setActive(false,false);disposed=true;interaction.getKeyFrames().clear();
        pointerHost.removeEventHandler(MouseEvent.MOUSE_MOVED,moved);
        pointerHost.removeEventHandler(MouseEvent.MOUSE_DRAGGED,moved);
        pointerHost.removeEventHandler(MouseEvent.MOUSE_EXITED,exited);
        connectors.forEach(line->{line.startXProperty().unbind();line.startYProperty().unbind();
            line.endXProperty().unbind();line.endYProperty().unbind();});
    }
    @Override public void close() { dispose(); }
}
