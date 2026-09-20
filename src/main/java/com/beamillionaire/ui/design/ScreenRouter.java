package com.beamillionaire.ui.design;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Lazily loads and retains screen instances so display-mode changes never reset state. */
public final class ScreenRouter {
    public enum Screen { HOME, CATEGORIES, GAMEPLAY, MENU, RESULTS, SCORES }
    public interface View {
        Parent root();
        default void onShown() {}
        default void refresh() {}
        default void onHidden() {}
    }
    private final StackPane host;
    private final Map<Screen, Supplier<View>> factories = new EnumMap<>(Screen.class);
    private final Map<Screen, View> views = new EnumMap<>(Screen.class);
    private Screen current;

    public ScreenRouter(StackPane host) { this.host = Objects.requireNonNull(host); }
    public void register(Screen screen, Supplier<View> factory) {
        if (factories.putIfAbsent(screen, Objects.requireNonNull(factory)) != null)
            throw new IllegalArgumentException("Screen already registered: " + screen);
    }
    public void show(Screen screen) {
        if (screen == current) return;
        var factory = factories.get(screen);
        if (factory == null) throw new IllegalArgumentException("Screen not registered: " + screen);
        View next = views.computeIfAbsent(screen, ignored -> Objects.requireNonNull(factory.get()));
        if (current != null) views.get(current).onHidden();
        host.getChildren().setAll(next.root());
        current = screen;
        next.refresh();
        next.onShown();
    }
    public Screen current() { return current; }
    public void refresh() { views.values().forEach(View::refresh); }
    public void refreshCurrent() {
        if (current == null) return;
        var focus = host.getScene() == null ? null : host.getScene().getFocusOwner();
        String id = focus == null ? null : focus.getId();
        views.get(current).refresh();
        if (id != null) {
            var replacement = host.lookup("#" + id);
            if (replacement != null && !replacement.isDisabled()) replacement.requestFocus();
        }
    }
}
