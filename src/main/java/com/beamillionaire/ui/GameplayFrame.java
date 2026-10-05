package com.beamillionaire.ui;

import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/** Native frame repairs free the reading area and provide clearance for the footer control. */
final class GameplayFrame {
    private GameplayFrame() {}
    static void clearInterior(ImageView image){
        Shape clip=image.getClip() instanceof Shape shape?shape:new Rectangle(1920,1080);
        clip=Shape.subtract(clip,new Rectangle(475,566,342,228));
        clip=Shape.subtract(clip,new Rectangle(812,977,296,84));
        image.setClip(clip);
    }
    static void add(Pane pane){
        line(pane,494,563,494,751,818,751);
        line(pane,810,1054,1110,1054);
        // Compact circuit ornament stays below the text rather than behind the choices.
        line(pane,503,708,503,723,529,742,594,742);
        line(pane,514,708,514,722,541,733,568,733);
        line(pane,517,753,540,762,620,762);
        for(double[] point:new double[][]{{503,708},{514,708},{594,742},{568,733},{620,762}}){
            var dot=new Circle(point[0],point[1],3,Color.web("#00a8d5"));dot.setMouseTransparent(true);pane.getChildren().add(dot);
        }
    }
    private static void line(Pane pane,double... points){
        var path=new Polyline(points);path.setFill(null);path.setStroke(Color.web("#008fbd"));
        path.setStrokeWidth(2.5);path.setMouseTransparent(true);pane.getChildren().add(path);
    }
}
