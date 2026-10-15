package com.beamillionaire.ui;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/** Non-blocking, scene-aware audio. Java Sound and state changes stay off the UI thread. */
public final class GameAudio implements AutoCloseable {
    public enum Scene { SPLASH, HOME, GAMEPLAY, SILENT }
    public enum Cue { HOVER, CORRECT, WRONG, WIN }
    /** Center of the strongest 50 ms RMS burst in the bundled logoSound.wav. */
    public static final long LOGO_IMPACT_MILLIS = 1425;

    public static final List<String> FILES = List.of("logoSound.wav", "win.wav", "correct.wav",
            "wrong.wav", "hover.wav", "homeScreenSound.wav", "inGameSound.wav");
    private static final Map<Cue, String> CUE_FILES = Map.of(Cue.HOVER, "hover.wav",
            Cue.CORRECT, "correct.wav", Cue.WRONG, "wrong.wav", Cue.WIN, "win.wav");
    private static final double HOME_MUSIC_VOLUME = 1.0, GAMEPLAY_MUSIC_VOLUME = 1.6;
    private static final double LOGO_VOLUME = 1.35, HOVER_VOLUME = .35, EFFECT_VOLUME = .95;
    private static final double DUCK_VOLUME = .20;
    private static final long HOVER_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(100);

    private final AudioBackend backend;
    private final Worker worker;
    private final LongSupplier nanoTime;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong splashCueVersion = new AtomicLong();
    private final Map<String, Voice> voices = new HashMap<>();
    private final EnumMap<Cue, Long> cueVersions = new EnumMap<>(Cue.class);
    private volatile boolean available;
    // Everything below is owned by worker.
    private boolean enabled, suspended, splashCueUsed;
    private Scene scene = Scene.SILENT;
    private Voice background;
    private Cue outcome;
    private Cue pendingCue;
    private long outcomeVersion, lastHoverNanos;
    private boolean hoverPlayed;

    public GameAudio(boolean enabled) {
        this(enabled, GameAudio::loadJavaSound, new AudioWorker(), System::nanoTime);
    }

    /** Package-private seams let checks exercise behavior without an audio device or sleeps. */
    GameAudio(boolean enabled, AudioBackend backend, Worker worker, LongSupplier nanoTime) {
        this.enabled = enabled;
        this.backend = Objects.requireNonNull(backend);
        this.worker = Objects.requireNonNull(worker);
        this.nanoTime = Objects.requireNonNull(nanoTime);
        worker.execute(() -> loadNext(0));
    }

    /** True when at least one audio resource has opened successfully; loading is asynchronous. */
    public boolean available() { return available && !closed.get(); }

    public void setScene(Scene value) {
        Objects.requireNonNull(value);
        if (value != Scene.SPLASH) splashCueVersion.incrementAndGet();
        submit(() -> {
            if (scene == value) return;
            if (scene == Scene.SPLASH) {
                Voice logo = voices.get("logoSound.wav");
                if (logo != null && logo.playing) fade(logo, 0, 200, () -> stop(logo));
            }
            scene = value;
            updateBackground();
        });
    }

    /** The splash calls this ahead of its visual midpoint by the clip's impact offset. */
    public void playSplashLogo(long cueLatenessMillis) {
        if (cueLatenessMillis < 0) throw new IllegalArgumentException("Negative cue lateness.");
        long requestedAt = nanoTime.getAsLong();
        long version = splashCueVersion.get();
        submit(() -> {
            if (scene != Scene.SPLASH || splashCueUsed) return;
            splashCueUsed = true;
            if (version != splashCueVersion.get()) return;
            Voice logo = voices.get("logoSound.wav");
            if (!audible() || logo == null) return;
            stop(logo);
            long offset = cueLatenessMillis + TimeUnit.NANOSECONDS.toMillis(Math.max(0, nanoTime.getAsLong() - requestedAt));
            // Missing or heavily delayed audio is skipped instead of playing an impact after the shine.
            if (offset >= LOGO_IMPACT_MILLIS) return;
            if (!logo.sound.seekMillis(offset)) return;
            volume(logo, LOGO_VOLUME);
            logo.sound.onFinished(() -> submit(() -> logo.playing = false));
            if (version != splashCueVersion.get() || closed.get()) return;
            logo.playing = true;
            logo.sound.start(false);
        });
    }

    /** Call once when creating a new round, not when rendering each question. */
    public void restartRoundMusic() {
        submit(() -> {
            Voice music = voices.get("inGameSound.wav");
            if (music != null) {
                stop(music);
                if (background == music) updateBackground();
            }
        });
    }

    public void play(Cue cue) {
        Objects.requireNonNull(cue);
        submit(() -> playCue(cue));
    }

    /** Compatibility for older screens. A click cue is intentionally not part of this sound set. */
    public void play(String name) {
        if (name == null) return;
        switch (name) {
            case "hover" -> play(Cue.HOVER);
            case "correct" -> play(Cue.CORRECT);
            case "wrong", "incorrect" -> play(Cue.WRONG);
            case "win" -> play(Cue.WIN);
            default -> { }
        }
    }

    public void setEnabled(boolean value) {
        if (!value) splashCueVersion.incrementAndGet();
        submit(() -> {
            if (enabled == value) return;
            enabled = value;
            if (!value) stopTransients();
            updateBackground();
        });
    }

    public void setSuspended(boolean value) {
        if (value) splashCueVersion.incrementAndGet();
        submit(() -> {
            if (suspended == value) return;
            suspended = value;
            if (value) stopTransients();
            updateBackground();
        });
    }

    @Override public void close() {
        if (!closed.compareAndSet(false, true)) return;
        // Interrupt a potentially slow device open. A resource returned after close is disposed below.
        worker.execute(() -> {
            for (Voice voice : voices.values()) closeQuietly(voice.sound);
            voices.clear();
            available = false;
            worker.close();
        });
        worker.interrupt();
    }

    private void submit(Runnable action) {
        if (!closed.get()) worker.execute(() -> {
            if (!closed.get()) action.run();
        });
    }

    private void loadNext(int index) {
        if (closed.get() || index >= FILES.size()) return;
        String file = FILES.get(index);
        try {
            Sound sound = backend.load("/audio/" + file);
            if (closed.get()) { closeQuietly(sound); return; }
            Voice voice = new Voice(sound);
            voices.put(file, voice);
            available = true;
            updateBackground();
            Cue loadedCue = cueForFile(file);
            if (loadedCue != null && pendingCue == loadedCue && audible()) {
                pendingCue = null;
                playCue(loadedCue);
            }
        } catch (Exception error) {
            if (!closed.get()) System.err.println("Audio unavailable (" + file + "): " + error.getMessage());
        }
        // Yield between resources so current scene/mute commands can overtake later loads.
        if (!closed.get()) worker.execute(() -> loadNext(index + 1));
    }

    private boolean audible() { return enabled && !suspended; }

    private void updateBackground() {
        String name = switch (scene) {
            case HOME -> "homeScreenSound.wav";
            case GAMEPLAY -> "inGameSound.wav";
            default -> null;
        };
        Voice next = name == null ? null : voices.get(name);
        if (background != next) {
            Voice old = background;
            background = next;
            if (old != null && old.playing) {
                if (audible()) fade(old, 0, 200, () -> pause(old));
                else pause(old);
            }
        }
        if (next == null) return;
        if (!audible()) { pause(next); return; }
        boolean duck = scene == Scene.GAMEPLAY && (outcome == Cue.CORRECT || outcome == Cue.WRONG);
        if (duck && !next.gain) { pause(next); return; }
        double target = duck ? DUCK_VOLUME : scene == Scene.GAMEPLAY ? GAMEPLAY_MUSIC_VOLUME : HOME_MUSIC_VOLUME;
        if (!next.playing) {
            cancelFade(next);
            volume(next, 0);
            next.playing = true;
            next.sound.start(true);
            fade(next, target, 160, null);
        } else if (Double.compare(next.goal, target) != 0) fade(next, target, 100, null);
    }

    private void playCue(Cue cue) {
        if (!audible()) return;
        Voice voice = voices.get(CUE_FILES.get(cue));
        if (voice == null) {
            if (cue != Cue.HOVER) pendingCue = cue;
            return;
        }
        if (cue != Cue.HOVER) pendingCue = null;
        if (cue == Cue.HOVER) {
            long now = nanoTime.getAsLong();
            if (hoverPlayed && now - lastHoverNanos < HOVER_INTERVAL_NANOS) return;
            hoverPlayed = true;
            lastHoverNanos = now;
        } else {
            // A subsequent answer replaces an earlier answer cue; hover may coexist with feedback.
            for (Cue other : List.of(Cue.CORRECT, Cue.WRONG, Cue.WIN)) {
                Voice previous = voices.get(CUE_FILES.get(other));
                if (previous != null) stop(previous);
            }
            outcome = cue;
            outcomeVersion++;
        }
        long version = cueVersions.merge(cue, 1L, Long::sum);
        long currentOutcome = outcomeVersion;
        stop(voice);
        volume(voice, cue == Cue.HOVER ? HOVER_VOLUME : EFFECT_VOLUME);
        voice.sound.onFinished(() -> submit(() -> {
            if (cueVersions.getOrDefault(cue, 0L) != version) return;
            voice.playing = false;
            if (cue != Cue.HOVER && outcomeVersion == currentOutcome && outcome == cue) {
                outcome = null;
                updateBackground();
            }
        }));
        voice.playing = true;
        voice.sound.start(false);
        if (cue != Cue.HOVER) updateBackground();
    }

    private void stopTransients() {
        pendingCue = null;
        outcome = null;
        outcomeVersion++;
        for (Cue cue : Cue.values()) {
            cueVersions.merge(cue, 1L, Long::sum);
            Voice voice = voices.get(CUE_FILES.get(cue));
            if (voice != null) stop(voice);
        }
        Voice logo = voices.get("logoSound.wav");
        if (logo != null) stop(logo);
        // A previous scene's track can still be fading after background has changed.
        for (String file : List.of("homeScreenSound.wav", "inGameSound.wav")) {
            Voice music = voices.get(file);
            if (music != null) pause(music);
        }
    }

    private static Cue cueForFile(String file) {
        for (var entry : CUE_FILES.entrySet()) {
            if (entry.getValue().equals(file)) return entry.getKey();
        }
        return null;
    }

    private void pause(Voice voice) {
        cancelFade(voice);
        voice.sound.pause();
        voice.playing = false;
    }

    private void stop(Voice voice) {
        pause(voice);
        voice.sound.rewind();
    }

    private void volume(Voice voice, double value) {
        voice.sound.setVolume(value);
        voice.volume = voice.goal = value;
    }

    private void cancelFade(Voice voice) { voice.fadeVersion++; }

    private void fade(Voice voice, double target, long millis, Runnable done) {
        long version = ++voice.fadeVersion;
        voice.goal = target;
        if (!voice.gain) {
            if (done != null) done.run();
            return;
        }
        double start = voice.volume;
        for (int step = 1; step <= 5; step++) {
            int position = step;
            worker.schedule(() -> {
                if (closed.get() || version != voice.fadeVersion) return;
                double value = start + (target - start) * position / 5;
                voice.sound.setVolume(value);
                voice.volume = value;
                if (position == 5 && done != null) done.run();
            }, millis * step / 5);
        }
    }

    private static void closeQuietly(Sound sound) {
        try { sound.close(); } catch (RuntimeException ignored) { }
    }

    private static Sound loadJavaSound(String resource) throws Exception {
        var input = GameAudio.class.getResourceAsStream(resource);
        if (input == null) throw new IOException("missing classpath resource " + resource);
        Clip clip = null;
        try (var buffered = new BufferedInputStream(input);
             var stream = AudioSystem.getAudioInputStream(buffered)) {
            clip = AudioSystem.getClip();
            clip.open(stream);
            return new ClipSound(clip);
        } catch (Exception error) {
            if (clip != null) clip.close();
            throw error;
        }
    }

    interface AudioBackend { Sound load(String resource) throws Exception; }
    interface Sound {
        void start(boolean loop);
        void pause();
        void rewind();
        default boolean seekMillis(long positionMillis) { return positionMillis == 0; }
        boolean setVolume(double linear);
        void onFinished(Runnable callback);
        void close();
    }
    interface Worker {
        void execute(Runnable action);
        void schedule(Runnable action, long delayMillis);
        default void interrupt() { }
        void close();
    }

    private static final class Voice {
        final Sound sound;
        final boolean gain;
        boolean playing;
        double volume, goal;
        long fadeVersion;
        Voice(Sound sound) {
            this.sound = Objects.requireNonNull(sound);
            gain = sound.setVolume(0);
        }
    }

    private static final class AudioWorker implements Worker {
        private volatile Thread thread;
        private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, runnable -> {
            thread = new Thread(runnable, "milly-audio");
            thread.setDaemon(true);
            return thread;
        });
        AudioWorker() { executor.setRemoveOnCancelPolicy(true); }
        public void execute(Runnable action) {
            try { executor.execute(action); } catch (RejectedExecutionException ignored) { }
        }
        public void schedule(Runnable action, long delayMillis) {
            try { executor.schedule(action, delayMillis, TimeUnit.MILLISECONDS); }
            catch (RejectedExecutionException ignored) { }
        }
        public void interrupt() { if (thread != null) thread.interrupt(); }
        public void close() { executor.shutdownNow(); }
    }

    private static final class ClipSound implements Sound {
        private final Clip clip;
        private final FloatControl gain;
        private volatile Runnable finished = () -> { };
        private volatile boolean running, looping;
        ClipSound(Clip clip) {
            this.clip = clip;
            gain = clip.isControlSupported(FloatControl.Type.MASTER_GAIN)
                    ? (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN) : null;
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && running && !looping
                        && clip.getFramePosition() >= clip.getFrameLength() - 1) {
                    running = false;
                    finished.run();
                }
            });
        }
        public void start(boolean loop) {
            looping = loop;
            running = true;
            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
            else clip.start();
        }
        public void pause() { running = false; clip.stop(); }
        public void rewind() { clip.setFramePosition(0); }
        public boolean seekMillis(long positionMillis) {
            clip.setMicrosecondPosition(positionMillis * 1000);
            return true;
        }
        public boolean setVolume(double linear) {
            if (gain == null) return false;
            double decibels = linear <= 0 ? gain.getMinimum() : 20 * Math.log10(linear);
            gain.setValue((float) Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels)));
            return true;
        }
        public void onFinished(Runnable callback) { finished = callback; }
        public void close() { running = false; clip.close(); }
    }
}
