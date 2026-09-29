package com.beamillionaire.ui;

import javafx.animation.*;
import javafx.geometry.VPos;
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
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.io.InputStream;

public final class SplashScreen {

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

    private static final Color PROGRESS_LABEL_COLOR = Color.web("#9fb7c7");
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

    private final Pane root = new Pane();

    private SVGPath letterA, letterI, letterASolid, letterISolid;
    private Group logoGroup;
    private Scale logoScaleT;
    private ImageView beView, aView, millionView, reView, hudView;
    private Group textGroup;
    private Text loadingLabel;
    private Rectangle progressTrack, progressFill;
    private Group progressGroup;

    private boolean introDone, loadingDone;
    private Transition shimmer, revealTrigger, reveal, introHold, spinHud, loadingIndicator;
    private Runnable onReady;

    public SplashScreen() {
        buildLogo();
        buildText();
        buildHud();
        buildProgressBar();

        root.getChildren().addAll(hudView, textGroup, logoGroup, progressGroup);
        root.setStyle("-fx-background-color: #070c12;");

        shimmer = buildShimmer();
        revealTrigger = new PauseTransition(Duration.seconds(3.1));
        revealTrigger.setOnFinished(event -> revealRestOfScene());
        shimmer.play();
        revealTrigger.play();
    }

    public Parent root() { return root; }

    public void resize(double sceneW, double sceneH) { layout(sceneW, sceneH); }

    public void setOnReady(Runnable onReady) {
        this.onReady = onReady;
        checkReady();
    }

    public void notifyLoadingComplete() {
        loadingDone = true;
        checkReady();
    }

    private void checkReady() {
        if (onReady != null && introDone && loadingDone) {
            Runnable callback = onReady;
            onReady = null;
            callback.run();
        }
    }

    public void stopAnimations() {
        for (Animation animation : new Animation[]{shimmer, revealTrigger, reveal, introHold, spinHud, loadingIndicator}) {
            if (animation != null) animation.stop();
        }
    }

    private void buildLogo() {
        letterA = new SVGPath();
        letterA.setFillRule(FillRule.NON_ZERO);
        letterA.setContent(
            "M 36.34 95.65 C 16.54 148.35 0.14 191.75 0.04 192.25 " +
                "C -0.16 192.65 12.14 192.95 27.34 192.85 L 55.04 192.55 L 63.44 169.65 " +
                "C 68.04 157.05 72.04 146.55 72.34 146.25 C 72.64 145.95 72.84 147.25 72.84 149.25 " +
                "C 72.84 154.05 75.44 162.45 79.14 169.35 C 84.04 178.65 92.54 185.05 106.84 190.35 " +
                "C 111.74 192.15 115.34 192.45 139.64 192.85 C 154.54 193.05 166.84 192.85 166.74 192.35 " +
                "C 166.74 191.95 162.34 181.15 156.94 168.55 L 147.24 145.55 L 110.04 145.25 " +
                "C 89.54 145.15 72.84 144.65 72.84 144.25 C 72.84 143.85 74.74 138.35 77.14 132.05 " +
                "C 80.94 121.75 81.64 120.55 83.94 120.25 C 86.44 119.95 86.74 120.55 90.44 131.45 " +
                "L 94.34 143.05 L 120.04 143.05 C 134.24 143.05 145.84 142.65 145.84 142.25 " +
                "C 145.84 141.75 135.14 109.75 121.94 70.95 L 98.04 0.55 L 85.24 0.25 L 72.34 -0.05 " +
                "L 36.34 95.65 Z " +
                "M 111.54 71.55 C 122.64 104.55 131.84 131.85 131.84 132.35 " +
                "C 131.84 132.75 125.04 132.95 116.74 132.85 L 101.64 132.55 L 97.54 121.25 L 93.44 110.05 " +
                "L 83.84 110.05 L 74.34 110.05 L 61.24 145.85 L 48.24 181.55 L 31.44 181.85 " +
                "C 15.74 182.05 14.64 181.95 15.04 180.35 C 15.34 179.35 29.64 141.45 46.74 96.05 " +
                "C 63.84 50.65 78.14 12.95 78.34 12.25 C 78.64 11.35 80.64 11.05 84.94 11.25 " +
                "L 91.14 11.55 L 111.54 71.55 Z " +
                "M 142.84 161.25 C 144.14 164.15 146.64 170.05 148.44 174.35 L 151.84 182.25 " +
                "L 131.64 181.85 C 111.94 181.55 111.14 181.45 105.84 178.85 " +
                "C 98.14 175.05 93.14 170.65 88.74 163.75 C 86.54 160.45 84.84 157.35 84.84 156.85 " +
                "C 84.84 156.45 97.34 156.05 112.74 156.05 L 140.64 156.05 L 142.84 161.25 Z"
        );

        letterI = new SVGPath();
        letterI.setFillRule(FillRule.NON_ZERO);
        letterI.setContent(
            "M 196.54 14.85 L 193.84 17.65 L 193.84 109.25 L 193.84 200.95 L 196.44 202.95 " +
                "C 198.94 204.95 200.34 205.05 221.54 205.05 C 242.64 205.05 244.24 204.95 246.94 202.95 " +
                "L 249.84 200.95 L 249.84 109.25 L 249.84 17.65 L 247.14 14.85 L 244.44 12.05 " +
                "L 221.84 12.05 L 199.24 12.05 L 196.54 14.85 Z " +
                "M 239.64 108.25 L 239.84 194.05 L 221.84 194.05 L 203.84 194.05 L 203.84 108.05 " +
                "L 203.84 22.05 L 221.64 22.25 L 239.34 22.55 L 239.64 108.25 Z"
        );

        letterA.setStroke(null);
        letterI.setStroke(null);

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
        logoScaleT = new Scale(1, 1, 0, 0);
        logoGroup.getTransforms().add(logoScaleT);
    }

    private void buildText() {
        beView = loadImageView(BE_IMAGE_PATH);
        aView = loadImageView(A_IMAGE_PATH);
        millionView = loadImageView(MILLION_IMAGE_PATH);
        reView = loadImageView(RE_IMAGE_PATH);
        textGroup = new Group(beView, aView, millionView, reView);
        textGroup.setOpacity(0);
    }

    private void buildHud() {
        hudView = loadImageView(HUD_IMAGE_PATH);
        hudView.setOpacity(0);
    }

    private void buildProgressBar() {
        loadingLabel = new Text("Loading...");
        loadingLabel.setFill(PROGRESS_LABEL_COLOR);
        loadingLabel.setTextOrigin(VPos.TOP);
        try (InputStream stream = SplashScreen.class.getResourceAsStream("/ui/assets/fonts/body.ttf")) {
            Font font = stream == null ? null : Font.loadFont(stream, 24);
            if (font != null) loadingLabel.setFont(font);
        } catch (java.io.IOException error) {
            loadingLabel.setFont(Font.getDefault());
        }

        progressTrack = new Rectangle();
        progressTrack.setFill(PROGRESS_TRACK_COLOR);

        progressFill = new Rectangle();
        progressFill.setFill(PROGRESS_FILL_COLOR);
        progressFill.setWidth(0);

        progressGroup = new Group(progressTrack, progressFill, loadingLabel);
        progressGroup.setOpacity(0);
    }

    private ImageView loadImageView(String classpathPath) {
        ImageView view;
        try (InputStream stream = SplashScreen.class.getResourceAsStream(classpathPath)) {
            view = stream != null ? new ImageView(new Image(stream)) : new ImageView();
        } catch (Exception e) {
            view = new ImageView();
        }
        view.setPreserveRatio(true);
        return view;
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

    private void revealRestOfScene() {
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

        reveal = new ParallelTransition(
            fadeShimmerAOut, fadeShimmerIOut, fadeSolidAIn, fadeSolidIIn,
            fadeText, fadeHud, fadeProgress
        );
        reveal.setInterpolator(Interpolator.EASE_BOTH);
        introHold = new PauseTransition(Duration.seconds(REVEALED_HOLD_SECONDS));
        introHold.setOnFinished(event -> {
            introDone = true;
            checkReady();
        });
        reveal.setOnFinished(event -> introHold.play());
        reveal.play();

        var hudRotation = new RotateTransition(Duration.seconds(20), hudView);
        hudRotation.setByAngle(360);
        spinHud = hudRotation;
        spinHud.setCycleCount(Animation.INDEFINITE);
        spinHud.setInterpolator(Interpolator.LINEAR);
        spinHud.play();

        loadingIndicator = new Transition() {
            {
                setCycleDuration(Duration.seconds(1.2));
                setCycleCount(Animation.INDEFINITE);
                setAutoReverse(true);
                setInterpolator(Interpolator.EASE_BOTH);
            }

            @Override protected void interpolate(double fraction) {
                double width = progressTrack.getWidth();
                double segmentWidth = width * 0.25;
                progressFill.setWidth(segmentWidth);
                progressFill.setX(progressTrack.getX() + fraction * (width - segmentWidth));
            }
        };
        loadingIndicator.play();
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

        loadingLabel.setFont(new Font(loadingLabel.getFont().getName(), labelHeight));

        double blockLeftX = million_startX;
        double trackBottomY = sceneH * (1.0 - PROGRESS_BOTTOM_OFFSET_RATIO);
        double trackY = trackBottomY - trackHeight;
        double labelY = trackY - barGap - labelHeight;

        loadingLabel.setLayoutX(blockLeftX);
        loadingLabel.setLayoutY(labelY);

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
