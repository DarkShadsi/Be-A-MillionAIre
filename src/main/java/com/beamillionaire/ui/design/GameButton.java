package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.Objects;

/** Exported skin with alpha-aware picking, stable hit bounds and native keyboard activation. */
public final class GameButton extends Button {
    public enum Feedback { NORMAL, SELECTED, CORRECT, INCORRECT }
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass CORRECT = PseudoClass.getPseudoClass("correct");
    private static final PseudoClass INCORRECT = PseudoClass.getPseudoClass("incorrect");
    private Image image;
    private final Node artwork;
    private final Shape hitShape;
    private final StackPane visual = new StackPane();
    private Label caption;
    private MarqueeLabel marquee;
    private final ColorAdjust color = new ColorAdjust();
    private final DropShadow shadow = new DropShadow();
    private final Timeline interaction = new Timeline();
    private final InvalidationListener hoverListener = ignored -> updateFeedback(isHover() ? 140 : 180);
    private final InvalidationListener focusListener = ignored -> updateFeedback(100);
    private final InvalidationListener armedListener = ignored -> updateFeedback(isArmed() ? 70 : 130);
    private final InvalidationListener disabledListener = ignored -> updateFeedback(0);
    private final ChangeListener<Scene> sceneListener = (observable, before, after) -> updateFeedback(0);
    private boolean motionEnabled;
    private boolean rectangularHitArea;
    private boolean compactMotion;
    private boolean lightTheme;
    private boolean disposed;
    private Feedback feedback = Feedback.NORMAL;

    public GameButton(Image image, double width, double height, String accessibleName) {
        this(new ImageView(requireDecoded(image)), width, height, accessibleName, null);
        this.image = image;
    }

    /** A complete vector skin and an independent, stationary shape for pointer picking. */
    public GameButton(Node artwork, double width, double height, String accessibleName, Shape hitShape) {
        this.artwork = Objects.requireNonNull(artwork);
        this.hitShape = hitShape;
        setAccessibleText(Objects.requireNonNull(accessibleName));
        setFocusTraversable(true);
        setPadding(Insets.EMPTY);
        getStyleClass().add("game-button");
        setMinSize(width, height); setPrefSize(width, height); setMaxSize(width, height);
        if (artwork instanceof ImageView imageView) {
            imageView.fitWidthProperty().bind(widthProperty());
            imageView.fitHeightProperty().bind(heightProperty());
        }
        artwork.setMouseTransparent(true);
        shadow.setInput(color);
        shadow.setRadius(0); shadow.setSpread(0); shadow.setColor(Color.TRANSPARENT);
        artwork.setEffect(shadow);
        visual.getStyleClass().add("button-visual");
        visual.setMouseTransparent(true);
        visual.getChildren().add(artwork);
        setGraphic(visual);
        hoverProperty().addListener(hoverListener);
        focusedProperty().addListener(focusListener);
        armedProperty().addListener(armedListener);
        disabledProperty().addListener(disabledListener);
        sceneProperty().addListener(sceneListener);
        updateFeedback(0);
    }

    private static Image requireDecoded(Image image) {
        Objects.requireNonNull(image);
        if (image.isError() || image.getPixelReader() == null)
            throw new IllegalArgumentException("A decoded original button PNG is required.");
        return image;
    }

    /** Swap theme pixels in place. Native vector skins keep their original geometry. */
    public void setArtwork(Image image) {
        if (!(artwork instanceof ImageView imageView) || this.image == image) return;
        this.image = requireDecoded(image);
        imageView.setImage(image);
    }

    public Feedback feedback() { return feedback; }
    public boolean motionRunning() { return interaction.getStatus() == Animation.Status.RUNNING
            ||marquee!=null&&marquee.motionRunning(); }
    public void setMotionEnabled(boolean value) {
        if (motionEnabled == value) return;
        motionEnabled = value;
        updateFeedback(0);
    }
    public void setCompactMotion(boolean value) {
        if (compactMotion == value) return;
        compactMotion = value;
        getStyleClass().remove("gameplay-control");
        if (value) getStyleClass().add("gameplay-control");
        updateFeedback(0);
    }
    public void setLightTheme(boolean value) {
        if (lightTheme == value) return;
        lightTheme = value;
        updateFeedback(0);
    }
    public void setFeedback(Feedback feedback) {
        Objects.requireNonNull(feedback);
        if (this.feedback == feedback) return;
        this.feedback = feedback;
        pseudoClassStateChanged(SELECTED, feedback == Feedback.SELECTED);
        pseudoClassStateChanged(CORRECT, feedback == Feedback.CORRECT);
        pseudoClassStateChanged(INCORRECT, feedback == Feedback.INCORRECT);
        updateFeedback(120);
    }
    public void setRectangularHitArea(boolean value) { rectangularHitArea = value; }
    public void setCaption(String text, Font font, Color color) {
        if (caption == null) {
            caption = new Label();
            caption.setAlignment(Pos.CENTER); caption.setWrapText(true);
            caption.setMouseTransparent(true);
            visual.getChildren().add(caption);
        }
        if (!Objects.equals(caption.getText(), text)) caption.setText(text);
        if (!Objects.equals(caption.getFont(), font)) caption.setFont(font);
        setCaptionColor(color);
    }
    public void setCaptionColor(Color color) {
        if (caption != null) caption.setTextFill(color);
        if (marquee != null) marquee.setColor(color);
    }
    /** Category names use the same readable, clipped marquee as the gameplay heading. */
    public void setMarqueeCaption(String text, Font font, Color color) {
        if(caption!=null){visual.getChildren().remove(caption);caption=null;}
        if(marquee==null){
            marquee=new MarqueeLabel(30);marquee.setManaged(false);
            marquee.setId(getId()+"-caption");visual.getChildren().add(marquee);
        }
        marquee.setText(text,font,color);
    }
    public void setCaptionMotionState(boolean available,boolean enabled){
        if(marquee!=null)marquee.setMotionState(available,enabled);
    }
    /** Position a caption inside its frame, excluding decorative connector wires. */
    public void setCaptionBounds(Rectangle2D bounds) {
        if(marquee!=null){
            marquee.resizeRelocate(bounds.getMinX(),bounds.getMinY(),bounds.getWidth(),bounds.getHeight());
            marquee.setPrefSize(bounds.getWidth(),bounds.getHeight());return;
        }
        if (caption == null) throw new IllegalStateException("Set the caption before its bounds.");
        caption.setManaged(false);
        caption.resizeRelocate(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }
    public void setArtworkClip(Shape clip) { artwork.setClip(clip); }
    @Override public boolean contains(double x, double y) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return false;
        if (rectangularHitArea) return true;
        if (hitShape != null) return hitShape.contains(x, y);
        if (image == null) return true;
        int pixelX = Math.min((int) image.getWidth() - 1, (int) (x / getWidth() * image.getWidth()));
        int pixelY = Math.min((int) image.getHeight() - 1, (int) (y / getHeight() * image.getHeight()));
        return image.getPixelReader().getColor(pixelX, pixelY).getOpacity() > 0.05;
    }

    private void updateFeedback(double millis) {
        if (disposed) return;
        stopAtCurrentValues();
        setCursor(isDisabled() ? Cursor.DEFAULT : Cursor.HAND);
        setOpacity(isDisabled() && feedback == Feedback.NORMAL ? .45 : 1);
        boolean enabled = !isDisabled();
        boolean animated = motionEnabled && enabled && getScene() != null;
        boolean armed = enabled && isArmed();
        boolean hovered = enabled && isHover();
        boolean focused = enabled && isFocused();
        double scaleX = animated ? armed ? .99 : hovered ? compactMotion ? 1.02 : 1.035 : 1 : 1;
        double scaleY = animated ? armed ? .96 : scaleX : 1;
        double offsetY = animated ? armed ? 2 : hovered ? compactMotion ? -1 : -2 : 0 : 0;
        Color accent = lightTheme ? Color.web("#087b89") : Color.CYAN;
        Color glow = switch (feedback) {
            case SELECTED -> Color.CYAN;
            case CORRECT -> Color.LIMEGREEN;
            case INCORRECT -> Color.TOMATO;
            case NORMAL -> focused ? accent : hovered ? accent.deriveColor(0, 1, 1, .55) : Color.TRANSPARENT;
        };
        double radius = feedback != Feedback.NORMAL ? 12 : focused ? 10 : hovered ? armed ? 5 : 12 : 0;
        double brightness = hovered ? armed ? .025 : .08 : 0;
        if (!animated || millis == 0) {
            visual.setScaleX(scaleX); visual.setScaleY(scaleY); visual.setTranslateY(offsetY);
            color.setBrightness(brightness); shadow.setRadius(radius); shadow.setColor(glow);
            return;
        }
        var easing = millis == 180 ? Interpolator.EASE_BOTH : Interpolator.EASE_OUT;
        interaction.getKeyFrames().setAll(new KeyFrame(Duration.millis(millis),
                new KeyValue(visual.scaleXProperty(), scaleX, easing),
                new KeyValue(visual.scaleYProperty(), scaleY, easing),
                new KeyValue(visual.translateYProperty(), offsetY, easing),
                new KeyValue(color.brightnessProperty(), brightness, easing),
                new KeyValue(shadow.radiusProperty(), radius, easing),
                new KeyValue(shadow.colorProperty(), glow, easing)));
        interaction.playFromStart();
    }

    private void stopAtCurrentValues() {
        double scaleX = visual.getScaleX(), scaleY = visual.getScaleY(), y = visual.getTranslateY();
        double brightness = color.getBrightness(), radius = shadow.getRadius();
        Color glow = shadow.getColor();
        interaction.stop();
        // Timeline.stop rewinds to its starting values; retarget from what the player last saw.
        visual.setScaleX(scaleX); visual.setScaleY(scaleY); visual.setTranslateY(y);
        color.setBrightness(brightness); shadow.setRadius(radius); shadow.setColor(glow);
    }

    /** Detach presentation listeners when a screen rebuild discards this control. */
    public void dispose() {
        if (disposed) return;
        disposed = true;
        if(marquee!=null)marquee.close();
        interaction.stop(); interaction.getKeyFrames().clear();
        hoverProperty().removeListener(hoverListener);
        focusedProperty().removeListener(focusListener);
        armedProperty().removeListener(armedListener);
        disabledProperty().removeListener(disabledListener);
        sceneProperty().removeListener(sceneListener);
        if (artwork instanceof ImageView imageView) {
            imageView.fitWidthProperty().unbind(); imageView.fitHeightProperty().unbind();
        }
        visual.setScaleX(1); visual.setScaleY(1); visual.setTranslateY(0);
    }
}
