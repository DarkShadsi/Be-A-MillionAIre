package com.beamillionaire.ui.design;

import javafx.scene.layout.Region;

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
    public static final Box CREDITS=new Box(1600,920,280,82);
    public static final double LADDER_X=1622,LADDER_Y=218,LADDER_WIDTH=236,LADDER_ROW_HEIGHT=32,LADDER_STEP=36;
    private GameplayLayout(){}
    public static double prizeY(int position){return LADDER_Y+(15-position)*LADDER_STEP;}
}
