package com.beamillionaire.ui.design;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

/** A fixed design canvas in a centered, uniformly scaled viewport. */
public final class DesignViewport extends Region {
    private final Pane canvas = new Pane();
    private final Group scaledCanvas = new Group(canvas);
    private final Scale transform = new Scale();
    private final double sourceWidth;
    private final double sourceHeight;

    public DesignViewport(double sourceWidth, double sourceHeight) {
        DesignMetrics.fit(0, 0, sourceWidth, sourceHeight);
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        canvas.setMinSize(sourceWidth, sourceHeight);
        canvas.setPrefSize(sourceWidth, sourceHeight);
        canvas.setMaxSize(sourceWidth, sourceHeight);
        canvas.resize(sourceWidth, sourceHeight);
        scaledCanvas.setManaged(false);
        scaledCanvas.getTransforms().add(transform);
        getChildren().add(scaledCanvas);
        setMinSize(0, 0);
    }

    public Pane canvas() { return canvas; }

    @Override protected void layoutChildren() {
        var metrics = DesignMetrics.fit(getWidth(), getHeight(), sourceWidth, sourceHeight);
        transform.setX(metrics.scale());
        transform.setY(metrics.scale());
        scaledCanvas.setLayoutX(metrics.offsetX());
        scaledCanvas.setLayoutY(metrics.offsetY());
        canvas.resize(sourceWidth, sourceHeight);
    }

    @Override protected double computePrefWidth(double height) { return sourceWidth; }
    @Override protected double computePrefHeight(double width) { return sourceHeight; }
}
