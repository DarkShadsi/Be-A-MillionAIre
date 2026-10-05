package com.beamillionaire.ui;
/** Audio interface for retained screens; actual WAV playback is integrated in step 18.5. */
public final class GameAudio implements AutoCloseable {
    public GameAudio(boolean enabled) {}
    public boolean available(){return false;}
    public void setEnabled(boolean enabled) {}
    public void play(String name) {}
    public void close() {}
}
