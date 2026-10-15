package com.beamillionaire.ui;

import com.beamillionaire.engine.GameRules;
import com.beamillionaire.ui.design.CircuitEffects;
import com.beamillionaire.ui.design.GameplayLayout;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Native, stationary frame with room for every question and all four answers. */
final class GameplayFrame {
    private GameplayFrame(){}
    static void add(GamePresentation game,Pane pane){
        var shapes=new ArrayList<Shape>();
        line(pane,shapes,2.5,36,805,36,105,104,37,520,37,600,106,717,106);
        line(pane,shapes,2.5,1203,106,1320,106,1400,37,1816,37,1884,105,1884,805);
        line(pane,shapes,2,49,795,49,112,111,50,512,50,589,119,713,119);
        line(pane,shapes,2,1207,119,1331,119,1408,50,1809,50,1871,112,1871,795);
        line(pane,shapes,3,36,846,36,1054,810,1054);
        line(pane,shapes,3,1110,1054,1884,1054,1884,846);
        line(pane,shapes,3,36,807,112,807,151,830,296,830,340,795,543,795,570,816,1350,816,1377,795,1580,795,1624,830,1769,830,1808,807,1884,807);
        line(pane,shapes,1.5,48,820,108,820,146,843,300,843,346,808,537,808);
        line(pane,shapes,1.5,1383,808,1574,808,1620,843,1774,843,1812,820,1872,820);
        polygon(pane,shapes,2.5,368,140,1518,140,1552,174,1552,746,1518,780,402,780,368,746);
        polygon(pane,shapes,2,80,174,114,140,306,140,340,174,340,276,306,310,114,310,80,276);
        polygon(pane,shapes,2,80,414,114,380,306,380,340,414,340,746,306,780,114,780,80,746);
        var ladder=GameplayLayout.LADDER_PANEL;
        double lx=ladder.x(),ly=ladder.y(),lr=lx+ladder.width(),lb=ly+ladder.height();
        polygon(pane,shapes,2.5,lx,ly+34,lx+34,ly,lr-34,ly,lr,ly+34,lr,lb-34,lr-34,lb,lx+34,lb,lx,lb-34);
        var creditFrame=GameplayLayout.creditsOutline();creditFrame.setFill(Color.TRANSPARENT);creditFrame.setStrokeWidth(2);
        add(pane,shapes,creditFrame);
        polygon(pane,shapes,2,715,72,745,48,1175,48,1205,72,1175,96,745,96);
        polygon(pane,shapes,1.5,123,159,297,159,309,171,309,195,297,207,123,207,111,195,111,171);
        var ladderHeading=GameplayLayout.LADDER_HEADING;
        polygon(pane,shapes,1.5,ladderHeading.x(),ladderHeading.y(),ladderHeading.x()+ladderHeading.width(),ladderHeading.y(),
                ladderHeading.x()+ladderHeading.width(),ladderHeading.y()+ladderHeading.height(),ladderHeading.x(),ladderHeading.y()+ladderHeading.height());
        game.text(pane,"CATEGORY",111,159,198,45,27,false);
        game.text(pane,"USE HELP",94,389,232,43,30,true);
        game.text(pane,"PRIZE LADDER",ladderHeading.x(),ladderHeading.y(),ladderHeading.width(),ladderHeading.height(),28,true);
        var creditsHeading=GameplayLayout.CREDITS_HEADING;
        var creditsTitle=game.text(pane,"CREDITS",creditsHeading.x(),creditsHeading.y(),creditsHeading.width(),creditsHeading.height(),32,true);
        creditsTitle.setId("credits-heading");
        for(int position=15;position>=1;position--){
            int row=position;
            Color color=Color.web(position>=13?"#ef795c":position>=10?"#d6a253":position>=5?"#d6c15b":"#9bd492");
            var label=game.text(pane,String.format(Locale.US,"%02d   %,d",position,GameRules.PRIZE_LADDER.get(position-1)),
                    GameplayLayout.LADDER_X,GameplayLayout.prizeY(position),GameplayLayout.LADDER_WIDTH,32,27,false);
            label.setId("prize-label-"+position);
            game.onAppearance(()->label.setTextFill(game.themeKey().equals("light")?
                    Color.web(row>=13?"#9d341d":row>=10?"#866020":row>=5?"#716000":"#246b28"):color));
        }
        line(pane,shapes,2,370,690,354,706,354,773,382,801,478,801);
        line(pane,shapes,1.5,354,725,333,746,333,786,370,823,485,823);
        for(double[] p:new double[][]{{354,706},{478,801},{485,823},{1871,795}}){
            var dot=new Circle(p[0],p[1],3);dot.setMouseTransparent(true);pane.getChildren().add(dot);
            game.onAppearance(()->dot.setFill(accent(game)));
        }
        for(int i=0;i<5;i++){
            var bar=new Polygon(1375+i*22,129,1388+i*22,129,1380+i*22,140,1367+i*22,140);
            bar.setMouseTransparent(true);pane.getChildren().add(bar);game.onAppearance(()->bar.setFill(accent(game)));
        }
        game.onAppearance(()->shapes.forEach(shape->shape.setStroke(accent(game))));
        CircuitEffects.addPaths(pane,game.motionScope(),new double[][]{
                {36,805,36,105,104,37,520,37,600,106,717,106},
                {1884,805,1884,105,1816,37,1400,37,1320,106,1203,106},
                {36,846,36,1054,810,1054},{1884,846,1884,1054,1110,1054},
                {368,746,368,140,1518,140},{1552,174,1552,746,1518,780,402,780},
                {354,706,354,773,382,801,478,801}
        });
    }
    private static Color accent(GamePresentation game){return Color.web(game.themeKey().equals("light")?"#087b89":"#008fbd");}
    private static void line(Pane pane,List<Shape> shapes,double width,double...points){
        var path=new Polyline(points);path.setFill(null);path.setStrokeWidth(width);add(pane,shapes,path);
    }
    private static void polygon(Pane pane,List<Shape> shapes,double width,double...points){
        var path=new Polygon(points);path.setFill(Color.TRANSPARENT);path.setStrokeWidth(width);add(pane,shapes,path);
    }
    private static void add(Pane pane,List<Shape> shapes,Shape shape){
        shape.setStroke(Color.web("#008fbd"));shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
        shape.setManaged(false);shape.setMouseTransparent(true);pane.getChildren().add(shape);shapes.add(shape);
    }
}
