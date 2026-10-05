package com.beamillionaire.ui.design;

import javafx.animation.Animation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Owns one view's motion: pause preserves phase, while explicit stop/disposal restores the resting composition. */
public final class MotionScope implements AutoCloseable {
    private record Entry(Animation animation, Runnable reset, Runnable prepare, boolean entrance) {}
    private final List<Entry> entries = new ArrayList<>();
    private final List<Animation> paused = new ArrayList<>();
    private boolean started;
    private boolean disposed;

    public <T extends Animation> T register(T animation, Runnable reset) {
        return register(animation, reset, () -> {}, false);
    }
    public <T extends Animation> T registerEntrance(T animation, Runnable reset) {
        return register(animation, reset, () -> {}, true);
    }
    <T extends Animation> T registerEntrance(T animation, Runnable reset, Runnable prepare) {
        return register(animation, reset, prepare, true);
    }
    private <T extends Animation> T register(T animation, Runnable reset, Runnable prepare, boolean entrance) {
        if (disposed) throw new IllegalStateException("Motion scope has been disposed.");
        Objects.requireNonNull(animation); Objects.requireNonNull(reset); Objects.requireNonNull(prepare);
        if (entries.stream().anyMatch(entry -> entry.animation() == animation))
            throw new IllegalArgumentException("Animation is already registered.");
        entries.add(new Entry(animation, reset, prepare, entrance));
        return animation;
    }

    /** Starts ambient motion without replaying entrances after an ordinary view refresh. */
    public void start() { start(false); }
    public void start(boolean entrance) {
        if (disposed || started) return;
        started = true;
        for (var entry : entries) {
            if (!entry.entrance() || entrance) {
                entry.prepare().run();
                entry.animation().playFromStart();
            }
        }
    }
    /** Suspends current motion so a modal can resume the same phase when it closes. */
    public void pause() {
        if (disposed) return;
        for (var entry : entries) {
            if (entry.animation().getStatus() == Animation.Status.RUNNING) {
                entry.animation().pause();
                paused.add(entry.animation());
            }
        }
    }
    public void resume() {
        if (disposed) return;
        if (!started) { start(); return; }
        paused.forEach(Animation::play);
        paused.clear();
    }
    public void stop() {
        for (var entry : entries) {
            entry.animation().stop();
            entry.reset().run();
        }
        paused.clear();
        started = false;
    }
    public boolean motionRunning() {
        return entries.stream().anyMatch(entry -> entry.animation().getStatus() == Animation.Status.RUNNING);
    }
    public int animationCount() { return entries.size(); }
    public void dispose() {
        if (disposed) return;
        stop();
        entries.forEach(entry -> entry.animation().setOnFinished(null));
        entries.clear();
        disposed = true;
    }
    @Override public void close() { dispose(); }
}
