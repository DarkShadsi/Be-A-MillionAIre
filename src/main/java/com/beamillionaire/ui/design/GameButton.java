package com.beamillionaire.ui.design;

import javafx.geometry.Insets;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import java.util.Objects;

/** Original exported skin with alpha-aware picking and native keyboard activation. */
public final class GameButton extends Button {
    public enum Feedback { NORMAL, SELECTED, CORRECT, INCORRECT }
    private final Image image;
    private final ImageView artwork;
    private final TranslateTransition interaction=new TranslateTransition(Duration.millis(160),this);
    private boolean motionEnabled;
    private boolean rectangularHitArea;
    private Feedback feedback = Feedback.NORMAL;

    public GameButton(Image image, double width, double height, String accessibleName) {
        this.image = Objects.requireNonNull(image);
        if (image.isError() || image.getPixelReader() == null)
            throw new IllegalArgumentException("A decoded original button PNG is required.");
        setAccessibleText(Objects.requireNonNull(accessibleName));
        setFocusTraversable(true);
        setPadding(Insets.EMPTY);
        setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);
        artwork = new ImageView(image);
        artwork.fitWidthProperty().bind(widthProperty());
        artwork.fitHeightProperty().bind(heightProperty());
        artwork.setMouseTransparent(true);
        setGraphic(artwork);
        hoverProperty().addListener(ignored -> updateFeedback());
        focusedProperty().addListener(ignored -> updateFeedback());
        pressedProperty().addListener(ignored -> updateFeedback());
        disabledProperty().addListener(ignored -> updateFeedback());
        sceneProperty().addListener((observable,before,after)->{
            if(after==null){interaction.stop();setTranslateY(0);}
        });
        updateFeedback();
    }

    public Feedback feedback(){return feedback;}
    public boolean motionRunning(){return interaction.getStatus()==Animation.Status.RUNNING;}
    public void setMotionEnabled(boolean value){
        motionEnabled=value;
        if(!value){interaction.stop();setTranslateY(0);}
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = Objects.requireNonNull(feedback);
        updateFeedback();
    }

    public void setRectangularHitArea(boolean value) { rectangularHitArea = value; }
    public void setCaption(String text, javafx.scene.text.Font font, javafx.scene.paint.Color color) {
        var label = new javafx.scene.control.Label(text);
        label.setFont(font); label.setTextFill(color); label.setMouseTransparent(true);
        setGraphic(new javafx.scene.layout.StackPane(artwork, label));
    }
    @Override public boolean contains(double x, double y) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return false;
        if (rectangularHitArea) return true;
        int pixelX = Math.min((int) image.getWidth() - 1, (int) (x / getWidth() * image.getWidth()));
        int pixelY = Math.min((int) image.getHeight() - 1, (int) (y / getHeight() * image.getHeight()));
        return image.getPixelReader().getColor(pixelX, pixelY).getOpacity() > 0.05;
    }

    private void updateFeedback() {
        setOpacity(isDisabled() && feedback == Feedback.NORMAL ? 0.45 : 1);
        interaction.stop();
        if(motionEnabled&&!isDisabled()&&getScene()!=null){
            interaction.setToY(isPressed()?2:isHover()?-1:0);
            interaction.playFromStart();
        }else setTranslateY(0);
        
        Color glow = switch (feedback) {
            case SELECTED -> Color.CYAN;
            case CORRECT -> Color.LIMEGREEN;
            case INCORRECT -> Color.TOMATO;
            case NORMAL -> null;
        };
        if (glow != null) setEffect(new DropShadow(12, glow));
        else if (isFocused()) setEffect(new DropShadow(10, Color.CYAN));
        else if (isHover()) setEffect(new ColorAdjust(0, 0, 0.10, 0));
        else setEffect(null);
    }
}
