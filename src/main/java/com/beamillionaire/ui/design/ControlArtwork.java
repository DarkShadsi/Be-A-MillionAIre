package com.beamillionaire.ui.design;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import java.util.Locale;

/** Complete control frames: the outline, circuitry and caption share one animated visual. */
public final class ControlArtwork {
    private static final Color CYAN = Color.web("#008fbd");
    private static final Color BLUE = Color.web("#0067bb");
    private static final Rectangle2D[] BOUNDS = {
            new Rectangle2D(140,174,538,194), new Rectangle2D(132,393,510,169),
            new Rectangle2D(137,567,505,179), new Rectangle2D(159,781,580,195)
    };
    private static final Rectangle2D[] CAPTIONS = {
            new Rectangle2D(166,207,438,78), new Rectangle2D(160,413,395,78),
            new Rectangle2D(170,661,372,66), new Rectangle2D(192,869,444,86)
    };
    private static final double[][] OUTLINES = {
            {147,181,296,181,322,206,638,206,638,284,619,303,524,303,501,286,147,286},
            {139,400,544,400,570,426,570,461,535,495,515,495,480,530,382,530,349,497,175,497,139,461},
            {144,657,159,642,213,642,230,658,550,658,557,679,511,725,232,725,215,738,159,738,144,724},
            {168,897,209,856,406,856,444,818,554,818,595,858,658,858,658,937,629,967,168,967}
    };

    private ControlArtwork() {}

    public static GameButton createCategory(String id, String caption, Font font, Color ink) {
        int row = switch (id) {
            case "category.ai-fundamentals", "category.machine-learning" -> 0;
            case "category.neural-networks", "category.deep-learning" -> 1;
            case "category.future-of-ai", "category.research-in-ai" -> 2;
            case "category.search-game-playing", "category.knowledge-problem-representation" -> 3;
            default -> throw new IllegalArgumentException("Unknown category control: " + id);
        };
        boolean right = switch (id) {
            case "category.machine-learning", "category.deep-learning", "category.research-in-ai",
                    "category.knowledge-problem-representation" -> true;
            default -> false;
        };
        Rectangle2D bounds = mirrored(BOUNDS[row], right);
        var artwork = canvas(bounds);
        var frame = polygon(OUTLINES[row], bounds, right);
        frame.setFill(Color.TRANSPARENT); frame.setStroke(CYAN); frame.setStrokeWidth(2.3);
        frame.setStrokeLineJoin(StrokeLineJoin.ROUND);
        artwork.getChildren().add(frame);
        switch (row) {
            case 0 -> {
                for (int i=0;i<5;i++) bar(artwork, bounds, right, 164+i*70,298,59,12,BLUE);
                wire(artwork,bounds,right,530,310,553,310,603,360,669,360);
                dot(artwork,bounds,right,669,360,6.5);
            }
            case 1 -> {
                bar(artwork,bounds,right,375,497,99,20,CYAN);
                wire(artwork,bounds,right,515,495,575,495,633,553);
                dot(artwork,bounds,right,633,553,8);
            }
            case 2 -> {
                for (int i=0;i<5;i++) bar(artwork,bounds,right,232+i*66,636,59,12,CYAN);
                wire(artwork,bounds,right,550,658,633,575);
                dot(artwork,bounds,right,633,575,8);
            }
            case 3 -> {
                bar(artwork,bounds,right,436,834,112,23,CYAN);
                wire(artwork,bounds,right,595,858,664,858,729,793);
                dot(artwork,bounds,right,729,793,9);
            }
            default -> throw new AssertionError(row);
        }
        // This copy never enters the scene graph and therefore never receives visual transforms.
        var hit = polygon(OUTLINES[row], bounds, right);
        hit.setFill(Color.BLACK); hit.setStroke(null);
        var button = new GameButton(artwork,bounds.getWidth(),bounds.getHeight(),caption,hit);
        button.relocate(bounds.getMinX(),bounds.getMinY());
        button.setId(id.replace('.','-'));
        String title = caption.toUpperCase(Locale.ROOT);
        button.setMarqueeCaption(title,font,ink);
        var captionBounds = mirrored(CAPTIONS[row],right);
        button.setCaptionBounds(new Rectangle2D(captionBounds.getMinX()-bounds.getMinX(),
                captionBounds.getMinY()-bounds.getMinY(),captionBounds.getWidth(),captionBounds.getHeight()));
        return button;
    }

    public static GameButton createWalkAway(String caption, Font font, Color ink) {
        var bounds = new Rectangle2D(52,889,361,130);
        var artwork = canvas(bounds);
        double[] points = {57,894,255,894,303,935,407,935,407,1012,57,1012};
        var frame = polygon(points,bounds,false);
        frame.setFill(Color.TRANSPARENT); frame.setStroke(CYAN); frame.setStrokeWidth(3.5);
        frame.setStrokeLineJoin(StrokeLineJoin.ROUND);
        artwork.getChildren().add(frame);
        var hit = polygon(points,bounds,false); hit.setFill(Color.BLACK); hit.setStroke(null);
        var button = new GameButton(artwork,bounds.getWidth(),bounds.getHeight(),caption,hit);
        button.relocate(bounds.getMinX(),bounds.getMinY()); button.setId("walk-away");
        button.setCaption(caption.toUpperCase(Locale.ROOT),font,ink);
        button.setCaptionBounds(new Rectangle2D(18,45,315,68));
        return button;
    }

    /** Remove the split, baked frame pixels before complete native controls are placed above them. */
    public static void cleanBackground(ImageView image, String screen) {
        Shape clip = new Rectangle(0,0,1920,1080);
        if (screen.equals("categories")) {
            double[][] regions = {
                    // The light export has older frame fragments above and beside the dark crops.
                    // Clear both compositions before drawing the complete native card visuals.
                    {132,150,620,220},{120,365,550,205},{120,540,550,220},{140,758,620,226},
                    {1168,150,620,220},{1250,365,550,205},{1250,540,550,220},{1160,758,620,226}
            };
            for (var r : regions) clip = Shape.subtract(clip,new Rectangle(r[0],r[1],r[2],r[3]));
        } else if (screen.equals("gameplay")) {
            clip = Shape.subtract(clip,new Rectangle(48,886,368,133));
        } else return;
        image.setClip(clip);
    }

    /** The source crop includes a stray fragment outside the answer frame's lower right edge. */
    public static void trimAnswerArtwork(GameButton button, String id) {
        if (id.equals("answer.a")) button.setArtworkClip(new Polygon(
                18.0,0.0,240.0,0.0,258.0,37.875,240.0,75.75,18.0,75.75,0.0,37.875));
    }

    private static Pane canvas(Rectangle2D bounds) {
        var pane = new Pane();
        pane.setMinSize(bounds.getWidth(),bounds.getHeight());
        pane.setPrefSize(bounds.getWidth(),bounds.getHeight());
        pane.setMaxSize(bounds.getWidth(),bounds.getHeight());
        return pane;
    }
    private static Rectangle2D mirrored(Rectangle2D bounds, boolean right) {
        return right ? new Rectangle2D(1920-bounds.getMaxX(),bounds.getMinY(),bounds.getWidth(),bounds.getHeight()) : bounds;
    }
    private static Polygon polygon(double[] points, Rectangle2D bounds, boolean right) {
        var polygon = new Polygon();
        for (int i=0;i<points.length;i+=2) polygon.getPoints().addAll(
                (right ? 1920-points[i] : points[i])-bounds.getMinX(),points[i+1]-bounds.getMinY());
        return polygon;
    }
    private static void wire(Pane pane, Rectangle2D bounds, boolean right, double... points) {
        var wire = new Polyline();
        for (int i=0;i<points.length;i+=2) wire.getPoints().addAll(
                (right ? 1920-points[i] : points[i])-bounds.getMinX(),points[i+1]-bounds.getMinY());
        wire.setFill(null); wire.setStroke(CYAN); wire.setStrokeWidth(2.3);
        wire.setStrokeLineJoin(StrokeLineJoin.ROUND); pane.getChildren().add(wire);
    }
    private static void dot(Pane pane, Rectangle2D bounds, boolean right, double x, double y, double radius) {
        pane.getChildren().add(new Circle((right?1920-x:x)-bounds.getMinX(),y-bounds.getMinY(),radius,CYAN));
    }
    private static void bar(Pane pane, Rectangle2D bounds, boolean right, double x, double y,
                            double width, double height, Color color) {
        var bar = polygon(new double[]{x,y,x+width-6,y,x+width,y+6,x+width,y+height,
                x+6,y+height,x,y+height-6},bounds,right);
        bar.setFill(color); bar.setStroke(null); pane.getChildren().add(bar);
    }
}
