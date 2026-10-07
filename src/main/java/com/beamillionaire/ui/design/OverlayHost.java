package com.beamillionaire.ui.design;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import java.util.ArrayList;
import java.util.List;

/** Modal host shares the design canvas and temporarily disables the screen below it. */
public final class OverlayHost extends StackPane {
    private final Node content;
    private Node previousFocus;
    private boolean previouslyDisabled;

    public OverlayHost(Node content) {
        this.content = content;
        setVisible(false);
        setManaged(false);
        setPickOnBounds(true);
        setFocusTraversable(true);
        setStyle("-fx-background-color: rgba(0,0,0,0.65);");
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                List<Node> targets = new ArrayList<>();
                for (Node node : getChildren()) collectFocusable(node, targets);
                if (targets.isEmpty()) requestFocus();
                else {
                    int index = targets.indexOf(getScene().getFocusOwner());
                    int next = index < 0 ? (event.isShiftDown() ? targets.size() - 1 : 0)
                            : Math.floorMod(index + (event.isShiftDown() ? -1 : 1), targets.size());
                    targets.get(next).requestFocus();
                }
                event.consume();
            }
        });
    }

    public void show(Parent popup) {
        if (!isVisible()) {
            previousFocus = getScene() == null ? null : getScene().getFocusOwner();
            previouslyDisabled = content.isDisable();
            content.setDisable(true);
        }
        getChildren().setAll(popup);
        setManaged(true);
        setVisible(true);
        toFront();
        List<Node> focusable = new ArrayList<>();
        collectFocusable(popup, focusable);
        (focusable.isEmpty() ? this : focusable.getFirst()).requestFocus();
    }

    public boolean close() {
        if (!isVisible()) return false;
        getChildren().clear();
        setVisible(false);
        setManaged(false);
        content.setDisable(previouslyDisabled);
        if (previousFocus != null && previousFocus.getScene() == getScene()) previousFocus.requestFocus();
        previousFocus = null;
        return true;
    }

    private static void collectFocusable(Node node, List<Node> targets) {
        if (!node.isVisible() || node.isDisabled()) return;
        if (node.isFocusTraversable()) targets.add(node);
        if (node instanceof Parent parent)
            for (Node child : parent.getChildrenUnmodifiable()) collectFocusable(child, targets);
    }
}
