package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;
import java.util.List;

/** Shared design-coordinate motion profiles, independent of game state and rendering. */
public final class UiEffects {
    public record AmbientProfile(double fullCycleSeconds, double minScale, double maxScale,
                                 double driftX, double driftY, double minOpacity, double phase) {
        public AmbientProfile {
            if (!Double.isFinite(fullCycleSeconds) || fullCycleSeconds <= 0
                    || !Double.isFinite(minScale) || !Double.isFinite(maxScale)
                    || minScale <= 0 || maxScale < minScale
                    || !Double.isFinite(driftX) || !Double.isFinite(driftY)
                    || !Double.isFinite(minOpacity) || minOpacity < 0 || minOpacity > 1
                    || !Double.isFinite(phase))
                throw new IllegalArgumentException("Invalid ambient motion profile.");
        }
    }
    public static final List<AmbientProfile> HOME_RINGS = List.of(
            new AmbientProfile(12, .985, 1.025, 6, 4, .94, 0),
            new AmbientProfile(17, .990, 1.020, 4, 6, .96, .27),
            new AmbientProfile(23, .980, 1.030, 5, 3, .92, .61));
    public static final List<AmbientProfile> CATEGORY_RINGS = List.of(
            new AmbientProfile(14, .990, 1.015, 2, 2, .94, 0),
            new AmbientProfile(19, .990, 1.015, 2, 2, .94, .27),
            new AmbientProfile(23, .990, 1.015, 2, 2, .94, .61));
    public static final AmbientProfile HEXAGONS = new AmbientProfile(19, .995, 1.010, 4, 4, .95, .38);
    public static final List<AmbientProfile> GAMEPLAY_MOTIFS = List.of(
            new AmbientProfile(18, .995, 1.010, 1, 1, .97, .12),
            new AmbientProfile(25, .995, 1.010, 1, 1, .97, .61));
    public static final AmbientProfile TITLE = new AmbientProfile(8, 1, 1.008, 0, 2, 1, .25);
    /** Signed seconds per revolution: positive clockwise, negative counterclockwise. */
    public static final List<Double> HOME_SPINS = List.of(32.0, -44.0, 60.0);
    public static final List<Double> CATEGORY_SPINS = List.of(-40.0, 56.0, -72.0);
    public static final List<Double> GAMEPLAY_SPINS = List.of(70.0, -95.0);
    private UiEffects() {}

    /** Rotation owns only the angle, so it can run alongside ambient scale and drift. */
    public static RotateTransition spin(MotionScope scope, Node node, double signedPeriodSeconds) {
        if (!Double.isFinite(signedPeriodSeconds) || signedPeriodSeconds == 0)
            throw new IllegalArgumentException("Invalid rotation period.");
        double restingAngle = node.getRotate();
        var rotation = new RotateTransition(Duration.seconds(Math.abs(signedPeriodSeconds)), node);
        rotation.setFromAngle(restingAngle);
        rotation.setToAngle(restingAngle + Math.copySign(360, signedPeriodSeconds));
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.setCycleCount(Animation.INDEFINITE);
        return scope.register(rotation, () -> node.setRotate(restingAngle));
    }

    /** A seamless timeline with independent phase offsets for drift, scale and opacity. */
    public static Timeline ambient(MotionScope scope, Node node, AmbientProfile profile) {
        var rest = new RestingState(node);
        var timeline = new Timeline();
        for (int step = 0; step <= 8; step++) {
            double position = step / 8.0;
            double phase = position + profile.phase();
            double scaleCenter = (profile.minScale() + profile.maxScale()) / 2;
            double scaleAmplitude = (profile.maxScale() - profile.minScale()) / 2;
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(profile.fullCycleSeconds() * position),
                    cyclic(node.scaleXProperty(), rest.scaleX * scaleCenter, rest.scaleX * scaleAmplitude, phase, profile),
                    cyclic(node.scaleYProperty(), rest.scaleY * scaleCenter, rest.scaleY * scaleAmplitude, phase, profile),
                    cyclic(node.translateXProperty(), rest.x, profile.driftX(), phase + .17, profile),
                    cyclic(node.translateYProperty(), rest.y, profile.driftY(), phase + .43, profile),
                    cyclic(node.opacityProperty(), rest.opacity * (1 + profile.minOpacity()) / 2,
                            rest.opacity * (1 - profile.minOpacity()) / 2, phase + .31, profile)));
        }
        timeline.setCycleCount(Animation.INDEFINITE);
        return scope.register(timeline, rest::restore);
    }

    /** Register on a wrapper separate from any ambient or button interaction transforms. */
    public static Timeline entrance(MotionScope scope, Node node, double fromX, double fromY,
                                    double fromScale, double durationMillis, double delayMillis) {
        if (durationMillis <= 0 || delayMillis < 0 || fromScale <= 0
                || !Double.isFinite(durationMillis + delayMillis + fromScale + fromX + fromY))
            throw new IllegalArgumentException("Invalid entrance motion.");
        var rest = new RestingState(node);
        Runnable prepare = () -> {
            node.setTranslateX(rest.x + fromX); node.setTranslateY(rest.y + fromY);
            node.setScaleX(rest.scaleX * fromScale); node.setScaleY(rest.scaleY * fromScale);
            node.setOpacity(0);
        };
        var timeline = new Timeline(
                new KeyFrame(Duration.ZERO, entranceStart(node, rest, fromX, fromY, fromScale)),
                new KeyFrame(Duration.millis(delayMillis + durationMillis),
                        out(node.translateXProperty(), rest.x), out(node.translateYProperty(), rest.y),
                        out(node.scaleXProperty(), rest.scaleX), out(node.scaleYProperty(), rest.scaleY),
                        out(node.opacityProperty(), rest.opacity)));
        if (delayMillis > 0)
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delayMillis),
                    entranceStart(node, rest, fromX, fromY, fromScale)));
        timeline.setOnFinished(event -> rest.restore());
        return scope.registerEntrance(timeline, rest::restore, prepare);
    }
    private static KeyValue[] entranceStart(Node node, RestingState rest, double x, double y, double scale) {
        return new KeyValue[]{out(node.translateXProperty(), rest.x + x), out(node.translateYProperty(), rest.y + y),
                out(node.scaleXProperty(), rest.scaleX * scale), out(node.scaleYProperty(), rest.scaleY * scale),
                out(node.opacityProperty(), 0)};
    }
    private static KeyValue cyclic(javafx.beans.value.WritableValue<Number> property,
                                   double center, double amplitude, double turns, AmbientProfile profile) {
        double radians = 2 * Math.PI * (turns % 1);
        double value = center + amplitude * Math.sin(radians);
        double tangentDelta = amplitude * 2 * Math.PI * Math.cos(radians) / 24;
        var tangentTime = Duration.seconds(profile.fullCycleSeconds() / 24);
        // Shared cyclic tangents ease at extrema, without pausing at each sampled keyframe.
        return new KeyValue(property, value, Interpolator.TANGENT(
                tangentTime, value - tangentDelta, tangentTime, value + tangentDelta));
    }
    private static KeyValue out(javafx.beans.value.WritableValue<Number> property, double value) {
        return new KeyValue(property, value, Interpolator.EASE_OUT);
    }
    public static void reset(Node node) {
        node.setTranslateX(0); node.setTranslateY(0);
        node.setScaleX(1); node.setScaleY(1); node.setOpacity(1);
    }
    private static final class RestingState {
        final Node node;
        final double x, y, scaleX, scaleY, opacity;
        RestingState(Node node) {
            this.node = node; x = node.getTranslateX(); y = node.getTranslateY();
            scaleX = node.getScaleX(); scaleY = node.getScaleY(); opacity = node.getOpacity();
        }
        void restore() {
            node.setTranslateX(x); node.setTranslateY(y);
            node.setScaleX(scaleX); node.setScaleY(scaleY); node.setOpacity(opacity);
        }
    }
}
