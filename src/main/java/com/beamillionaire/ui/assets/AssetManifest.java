package com.beamillionaire.ui.assets;

import java.util.List;

/** Export provenance is data; pending entries must never be accepted as production assets. */
public record AssetManifest(int version, String sourceDesign, String workingCopy, List<Artwork> artwork,
                            List<Typeface> fonts) {
    public AssetManifest {
        artwork = List.copyOf(artwork);
        fonts = List.copyOf(fonts);
    }
    public record Artwork(String id, String theme, String screen, String sourceElement, String path,
                          int pixelWidth, int pixelHeight, double left, double top,
                          double width, double height, int layer, String role,
                          String sha256, String status, boolean rectangularHitArea) {}
    public record Typeface(String id, String path, String family, String fullName, String sha256, String status) {}
}
