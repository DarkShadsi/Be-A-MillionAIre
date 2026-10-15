package com.beamillionaire.ui.design;

import javafx.scene.layout.Region;
import javafx.scene.shape.Polygon;

/** Shared design coordinates for the wider gameplay composition and its feedback. */
public final class GameplayLayout {
    public record Box(double x,double y,double width,double height) {
        public void place(Region node){
            node.setManaged(false);node.resizeRelocate(x,y,width,height);
            node.setMinSize(width,height);node.setPrefSize(width,height);node.setMaxSize(width,height);
        }
    }
    public static final Box READING=new Box(400,190,1120,560);
    public static final Box CATEGORY=new Box(94,205,232,88);
    public static final Box LADDER_PANEL=new Box(1580,130,260,650);
    public static final Box LADDER_HEADING=new Box(1598,151,224,42);
    public static final Box WALK_AWAY=new Box(80,870,400,152);
    public static final Box CREDITS=new Box(1480,898,360,124);
    public static final Box CREDITS_HEADING=new Box(1480,850,360,40);
    public static final Box CREDITS_VALUE=new Box(1498,922,324,76);
    public static final double LADDER_X=1598,LADDER_Y=218,LADDER_WIDTH=224,LADDER_ROW_HEIGHT=32,LADDER_STEP=36;
    public static final double CHECKPOINT_X=1588;
    private GameplayLayout(){}
    public static double prizeY(int position){return LADDER_Y+(15-position)*LADDER_STEP;}
    /** Shared native outline keeps the structural frame and earned-credit accent aligned. */
    public static Polygon creditsOutline(){
        double x=CREDITS.x(),y=CREDITS.y(),right=x+CREDITS.width(),bottom=y+CREDITS.height();
        return new Polygon(x,y+18,x+18,y,right-18,y,right,y+18,right,bottom,x,bottom);
    }
}
