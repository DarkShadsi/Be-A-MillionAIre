package com.beamillionaire.ui;

import com.beamillionaire.ui.design.DesignViewport;
import com.beamillionaire.ui.design.AiLogoGeometry;
import com.beamillionaire.ui.design.MotionScope;
import com.beamillionaire.ui.design.UiEffects;
import com.beamillionaire.ui.design.ThinkingIndicator;
import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.io.IOException;

/** Milly's branded intro, fitted to the design canvas and gated by actual startup readiness. */
public final class SplashScreen implements AutoCloseable {

    private static final double LOGO_BASE_W = 500;
    private static final double LOGO_BASE_H = 320;
    private static final double LOGO_PATH_W = 250;
    private static final double LOGO_PATH_H = 205;
    private static final double LOGO_SCALE_FACTOR = 0.385;

    private static final double WORD_HEIGHT_TO_LOGO_RATIO = 0.6;
    private static final double LINE_GAP_RATIO = 0;
    private static final double LINE_HEIGHT_RATIO = 0.9;
    private static final double WORD_GAP_RATIO = 0.35;
    private static final double HUD_SIZE_RATIO = 2.3;

    private static final double PROGRESS_LABEL_HEIGHT_TO_LOGO_H_RATIO = 0.14;
    private static final double PROGRESS_TRACK_HEIGHT_TO_LOGO_H_RATIO = 0.09;
    private static final double PROGRESS_BAR_GAP_RATIO = 0.8;
    private static final double PROGRESS_TRACK_WIDTH_TO_SCREEN_W_RATIO = 0.70;
    private static final double PROGRESS_BOTTOM_OFFSET_RATIO = 0.10;

    private static final Color PROGRESS_TRACK_COLOR = Color.web("#13202b");
    private static final Color PROGRESS_FILL_COLOR = Color.web("#00a3e0");

    private static final double SHIMMER_CYCLE_SECONDS = 5.0;
    private static final Color LOGO_SETTLE_COLOR = Color.web("#00a3e0");
    private static final double REVEAL_FADE_SECONDS = 0.75;
    private static final double REVEALED_HOLD_SECONDS = 3.0;

    // Classpath-relative asset paths (mirrors AssetCatalog's use of getResourceAsStream)
    private static final String ASSETS_DIR = "/ui/assets/dark/splash/";
    private static final String BE_IMAGE_PATH = ASSETS_DIR + "be.png";
    private static final String A_IMAGE_PATH = ASSETS_DIR + "a.png";
    private static final String MILLION_IMAGE_PATH = ASSETS_DIR + "million.png";
    private static final String RE_IMAGE_PATH = ASSETS_DIR + "re.png";
    private static final String HUD_IMAGE_PATH = ASSETS_DIR + "hud.png";

    private final DesignViewport viewport = new DesignViewport(1920, 1080);
    private final Pane root = viewport.canvas();
    private final Stage stage;
    private final boolean motionEnabled;
    private final MotionScope introMotion = new MotionScope();
    private final MotionScope hudMotion = new MotionScope();
    private final MotionScope loadingMotion = new MotionScope();
    private final InvalidationListener listener = ignored -> sync();

    private SVGPath letterA, letterI, letterASolid, letterISolid;
    private Group logoGroup;
    private Scale logoScaleT;
    private ImageView beView, aView, millionView, reView, hudView;
    private Group textGroup;
    private ThinkingIndicator loadingCaption;
    private Rectangle progressTrack, progressFill;
    private Group progressGroup;

    private boolean introDone, loadingDone, readyDelivered, closed;
    private Runnable onReady;

    public SplashScreen(Stage stage, boolean motionEnabled) throws IOException {
        this.stage = stage;
        this.motionEnabled = motionEnabled;
        buildLogo();
        buildText();
        buildHud();
        buildProgressBar();

        root.getChildren().addAll(hudView, textGroup, logoGroup, progressGroup);
        root.setStyle("-fx-background-color: #070c12;");
        viewport.setStyle("-fx-background-color: #070c12;");
        root.setClip(new Rectangle(1920, 1080));
        root.setMouseTransparent(true);
        layout(1920, 1080);
        if (motionEnabled) {
            introMotion.register(buildShimmer(), () -> {});
            introMotion.register(buildIntro(), () -> {});
            UiEffects.spin(hudMotion, hudView, 20).setDelay(Duration.seconds(3.1));
            loadingMotion.register(buildLoadingIndicator(), () -> {});
        } else {
            letterA.setOpacity(0); letterI.setOpacity(0);
            letterASolid.setOpacity(1); letterISolid.setOpacity(1);
            textGroup.setOpacity(1); hudView.setOpacity(1); progressGroup.setOpacity(1);
            progressFill.setWidth(progressTrack.getWidth() * .25);
            introDone = true;
        }
        viewport.sceneProperty().addListener(listener);
        viewport.visibleProperty().addListener(listener);
        stage.showingProperty().addListener(listener);
        stage.iconifiedProperty().addListener(listener);
        stage.sceneProperty().addListener(listener);
        sync();
    }

    public Parent root() { return viewport; }
    public boolean motionRunning() {
        return introMotion.motionRunning() || hudMotion.motionRunning() || loadingMotion.motionRunning()
                || loadingCaption.motionRunning();
    }

    public void setOnReady(Runnable onReady) {
        if (closed || readyDelivered) return;
        this.onReady = onReady;
        checkReady();
    }

    public void notifyLoadingComplete() {
        if (closed || loadingDone) return;
        loadingDone = true;
        // Milly's indeterminate segment keeps bouncing through the remaining intro and dismissal.
        if (!motionEnabled) {
            progressFill.setX(progressTrack.getX());
            progressFill.setWidth(progressTrack.getWidth());
        }
        checkReady();
    }

    private void checkReady() {
        if (!closed && !readyDelivered && visible() && onReady != null && introDone && loadingDone) {
            readyDelivered = true;
            loadingCaption.finish();
            Runnable callback = onReady;
            onReady = null;
            callback.run();
        }
    }

    private boolean visible() {
        return viewport.getScene() != null && viewport.getScene() == stage.getScene()
                && viewport.isVisible() && stage.isShowing() && !stage.isIconified();
    }
    private void sync() {
        if (closed) return;
        loadingCaption.setActive(visible());
        if (visible()) {
            if (motionEnabled) {
                introMotion.resume(); hudMotion.resume(); loadingMotion.resume();
            }
            checkReady();
        } else {
            introMotion.pause(); hudMotion.pause(); loadingMotion.pause();
        }
    }
    @Override public void close() {
        if (closed) return;
        closed = true; onReady = null;
        introMotion.dispose(); hudMotion.dispose(); loadingMotion.dispose();
        loadingCaption.close();
        viewport.sceneProperty().removeListener(listener);
        viewport.visibleProperty().removeListener(listener);
        stage.showingProperty().removeListener(listener);
        stage.iconifiedProperty().removeListener(listener);
        stage.sceneProperty().removeListener(listener);
    }

    private void buildLogo() {
        letterA = AiLogoGeometry.letterA();

        letterI = AiLogoGeometry.letterI();

        letterA.setStroke(null);
        letterI.setStroke(null);
        letterA.setFill(Color.TRANSPARENT);
        letterI.setFill(Color.TRANSPARENT);

        letterASolid = new SVGPath();
        letterASolid.setFillRule(FillRule.NON_ZERO);
        letterASolid.setContent(letterA.getContent());
        letterASolid.setStroke(null);
        letterASolid.setFill(LOGO_SETTLE_COLOR);
        letterASolid.setOpacity(0);

        letterISolid = new SVGPath();
        letterISolid.setFillRule(FillRule.NON_ZERO);
        letterISolid.setContent(letterI.getContent());
        letterISolid.setStroke(null);
        letterISolid.setFill(LOGO_SETTLE_COLOR);
        letterISolid.setOpacity(0);

        logoGroup = new Group(letterA, letterI, letterASolid, letterISolid);
        logoGroup.setId("splash-logo");
        logoScaleT = new Scale(1, 1, 0, 0);
        logoGroup.getTransforms().add(logoScaleT);
    }

    private void buildText() throws IOException {
        beView = loadImageView(BE_IMAGE_PATH);
        aView = loadImageView(A_IMAGE_PATH);
        millionView = loadImageView(MILLION_IMAGE_PATH);
        reView = loadImageView(RE_IMAGE_PATH);
        textGroup = new Group(beView, aView, millionView, reView);
        textGroup.setId("splash-title");
        textGroup.setOpacity(0);
    }

    private void buildHud() throws IOException {
        hudView = loadImageView(HUD_IMAGE_PATH);
        hudView.setId("splash-hud");
        hudView.setOpacity(0);
    }

    private void buildProgressBar() throws IOException {
        try (InputStream stream = SplashScreen.class.getResourceAsStream("/ui/assets/fonts/body.ttf")) {
            if (stream == null) throw new IOException("Missing splash font.");
            Font font = Font.loadFont(stream, 24);
            if (font == null) throw new IOException("Splash font could not load.");
            loadingCaption=new ThinkingIndicator(font,motionEnabled,Duration.seconds(3.1));
        }

        progressTrack = new Rectangle();
        progressTrack.setId("splash-progress-track");
        progressTrack.setFill(PROGRESS_TRACK_COLOR);

        progressFill = new Rectangle();
        progressFill.setId("splash-progress-fill");
        progressFill.setFill(PROGRESS_FILL_COLOR);
        progressFill.setWidth(0);

        progressGroup = new Group(progressTrack, progressFill, loadingCaption);
        progressGroup.setOpacity(0);
    }

    private ImageView loadImageView(String classpathPath) throws IOException {
        try (InputStream stream = SplashScreen.class.getResourceAsStream(classpathPath)) {
            if (stream == null) throw new IOException("Missing splash artwork: " + classpathPath);
            var image = new Image(stream);
            if (image.isError()) throw new IOException("Splash artwork could not load: " + classpathPath, image.getException());
            var view = new ImageView(image);
            view.setPreserveRatio(true);
            return view;
        }
    }

    private Transition buildShimmer() {
        Stop[] shimmerStops = new Stop[]{
            new Stop(0.00, Color.TRANSPARENT),
            new Stop(0.42, Color.TRANSPARENT),
            new Stop(0.47, Color.web("#00a3e0", 0.9)),
            new Stop(0.50, Color.web("#ffffff")),
            new Stop(0.53, Color.web("#00a3e0", 0.9)),
            new Stop(0.58, Color.TRANSPARENT),
            new Stop(1.00, Color.TRANSPARENT)
        };
        final double x0 = 0, y0 = 193, x1 = 250, y1 = 12;

        return new Transition() {
            {
                setCycleDuration(Duration.seconds(SHIMMER_CYCLE_SECONDS));
                setCycleCount(1);
                setInterpolator(Interpolator.LINEAR);
            }

            @Override
            protected void interpolate(double frac) {
                double offset = -1.2 + (frac * 2.4);
                double startX = x0 + offset * (x1 - x0);
                double startY = y0 + offset * (y1 - y0);
                double endX = x0 + (1.0 + offset) * (x1 - x0);
                double endY = y0 + (1.0 + offset) * (y1 - y0);

                LinearGradient gradient = new LinearGradient(
                    startX, startY, endX, endY,
                    false, CycleMethod.NO_CYCLE, shimmerStops
                );
                letterA.setFill(gradient);
                letterI.setFill(gradient);
            }
        };
    }

    private SequentialTransition buildIntro() {
        Duration fadeDuration = Duration.seconds(REVEAL_FADE_SECONDS);

        FadeTransition fadeShimmerAOut = new FadeTransition(fadeDuration, letterA);
        fadeShimmerAOut.setToValue(0);
        FadeTransition fadeShimmerIOut = new FadeTransition(fadeDuration, letterI);
        fadeShimmerIOut.setToValue(0);
        FadeTransition fadeSolidAIn = new FadeTransition(fadeDuration, letterASolid);
        fadeSolidAIn.setToValue(1);
        FadeTransition fadeSolidIIn = new FadeTransition(fadeDuration, letterISolid);
        fadeSolidIIn.setToValue(1);

        FadeTransition fadeText = new FadeTransition(fadeDuration, textGroup);
        fadeText.setToValue(1);
        FadeTransition fadeHud = new FadeTransition(fadeDuration, hudView);
        fadeHud.setToValue(1);
        FadeTransition fadeProgress = new FadeTransition(fadeDuration, progressGroup);
        fadeProgress.setToValue(1);

        var reveal = new ParallelTransition(
            fadeShimmerAOut, fadeShimmerIOut, fadeSolidAIn, fadeSolidIIn,
            fadeText, fadeHud, fadeProgress
        );
        reveal.setInterpolator(Interpolator.EASE_BOTH);
        var intro = new SequentialTransition(new PauseTransition(Duration.seconds(3.1)),
                reveal, new PauseTransition(Duration.seconds(REVEALED_HOLD_SECONDS)));
        intro.setOnFinished(event -> {
            introDone = true;
            checkReady();
        });
        return intro;
    }

    private Transition buildLoadingIndicator() {
        return new Transition() {
            {
                setCycleDuration(Duration.seconds(1.2));
                setCycleCount(Animation.INDEFINITE);
                setAutoReverse(true);
                setInterpolator(Interpolator.EASE_BOTH);
                setDelay(Duration.seconds(3.1));
            }

            @Override protected void interpolate(double fraction) {
                double width = progressTrack.getWidth();
                double segmentWidth = width * 0.25;
                progressFill.setWidth(segmentWidth);
                progressFill.setX(progressTrack.getX() + fraction * (width - segmentWidth));
            }
        };
    }

    private void layout(double sceneW, double sceneH) {
        if (sceneW <= 0 || sceneH <= 0) return;

        double fitScale = Math.min(sceneW / LOGO_BASE_W, sceneH / LOGO_BASE_H);
        double logoScale = fitScale * LOGO_SCALE_FACTOR;
        double logoW = LOGO_PATH_W * logoScale;
        double logoH = LOGO_PATH_H * logoScale;
        double logoTX = (sceneW - logoW) / 2.0;
        double logoTY = (sceneH - logoH) / 2.0;

        logoScaleT.setX(logoScale);
        logoScaleT.setY(logoScale);
        logoGroup.setTranslateX(logoTX);
        logoGroup.setTranslateY(logoTY);

        double wordHeight = logoH * WORD_HEIGHT_TO_LOGO_RATIO;
        double lineGap = wordHeight * LINE_GAP_RATIO;
        double lineHeight = wordHeight * LINE_HEIGHT_RATIO;
        double wordGap = wordHeight * WORD_GAP_RATIO;

        beView.setFitHeight(wordHeight);
        aView.setFitHeight(wordHeight);
        millionView.setFitHeight(wordHeight);
        reView.setFitHeight(wordHeight);

        double logoBottomY = logoTY + logoH;
        double logoCenterY = logoTY + logoH / 2.0;
        double line2Y = logoBottomY - wordHeight;
        double line1Y = line2Y - lineHeight;

        double millionWidth = millionView.getBoundsInLocal().getWidth();
        double million_endX = logoTX - lineGap;
        double million_startX = million_endX - millionWidth;
        millionView.setLayoutX(million_startX);
        millionView.setLayoutY(line2Y);

        double beWidth = beView.getBoundsInLocal().getWidth();
        beView.setLayoutX(million_startX);
        beView.setLayoutY(line1Y);
        aView.setLayoutX(million_startX + beWidth + wordGap);
        aView.setLayoutY(line1Y);

        double re_startX = logoTX + logoW + lineGap;
        reView.setLayoutX(re_startX);
        reView.setLayoutY(line2Y);

        double hudSize = logoH * HUD_SIZE_RATIO;
        double re_endX = re_startX + reView.getBoundsInLocal().getWidth();
        double hudCenterX = re_endX + lineGap * 2 + (2 * hudSize) / 3;

        hudView.setFitWidth(hudSize);
        hudView.setFitHeight(hudSize);
        hudView.setLayoutX(hudCenterX - hudSize / 2.0);
        hudView.setLayoutY(logoCenterY - hudSize / 2.0);

        double labelHeight = logoH * PROGRESS_LABEL_HEIGHT_TO_LOGO_H_RATIO;
        double trackHeight = logoH * PROGRESS_TRACK_HEIGHT_TO_LOGO_H_RATIO;
        double trackWidth = sceneW * PROGRESS_TRACK_WIDTH_TO_SCREEN_W_RATIO;
        double barGap = labelHeight * PROGRESS_BAR_GAP_RATIO;

        loadingCaption.setFontSize(labelHeight);

        double blockLeftX = million_startX;
        double trackBottomY = sceneH * (1.0 - PROGRESS_BOTTOM_OFFSET_RATIO);
        double trackY = trackBottomY - trackHeight;
        double labelY = trackY - barGap - labelHeight;

        loadingCaption.resizeRelocate(blockLeftX,labelY,trackWidth,labelHeight*1.3);

        progressTrack.setX(blockLeftX);
        progressTrack.setY(trackY);
        progressTrack.setWidth(trackWidth);
        progressTrack.setHeight(trackHeight);
        progressTrack.setArcWidth(trackHeight);
        progressTrack.setArcHeight(trackHeight);

        progressFill.setX(blockLeftX);
        progressFill.setY(trackY);
        progressFill.setHeight(trackHeight);
        progressFill.setArcWidth(trackHeight);
        progressFill.setArcHeight(trackHeight);
    }
}
