package com.beamillionaire.ui.design;

/** One transform shared by artwork, controls, and modal content. */
public record DesignMetrics(double scale, double offsetX, double offsetY) {
    public static DesignMetrics fit(double width, double height, double sourceWidth, double sourceHeight) {
        if (!Double.isFinite(width) || !Double.isFinite(height) || width < 0 || height < 0
                || !Double.isFinite(sourceWidth) || !Double.isFinite(sourceHeight)
                || sourceWidth <= 0 || sourceHeight <= 0) {
            throw new IllegalArgumentException("Viewport dimensions must be finite and source dimensions positive.");
        }
        double scale = Math.min(width / sourceWidth, height / sourceHeight);
        return new DesignMetrics(scale, (width - sourceWidth * scale) / 2,
                (height - sourceHeight * scale) / 2);
    }
}
