package com.beamillionaire.ui.assets;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import java.io.*;
import java.util.*;

/** Loads artwork and fonts without changing their appearance. */
public final class AssetCatalog {
    private final Map<String, AssetManifest.Artwork> descriptors = new HashMap<>();
    private final Map<String, Image> images = new HashMap<>();
    private final Map<String, Font> sizedFonts = new HashMap<>();
    private final Map<String, Font> verifiedFonts = new HashMap<>();

    public interface Resources { InputStream open(String path) throws IOException; }

    public static Resources bundledResources() {
        return path -> AssetCatalog.class.getResourceAsStream(path);
    }

    public AssetCatalog(AssetManifest manifest, Resources resources) throws IOException {
        for (var asset : manifest.artwork()) {
            try (var stream = resources.open(asset.path())) {
                if (stream == null) throw new IOException("Missing artwork: " + asset.path());
                Image image = new Image(stream);
                if (image.isError()) throw new IOException("Image could not load: " + asset.path(), image.getException());
                descriptors.put(asset.theme() + "/" + asset.id(), asset);
                images.put(asset.theme() + "/" + asset.id(), image);
            }
        }
        for (var typeface : manifest.fonts()) {
            try (var stream = resources.open(typeface.path())) {
                if (stream == null) throw new IOException("Missing font: " + typeface.path());
                byte[] bytes = stream.readAllBytes();
                Font font = Font.loadFont(new ByteArrayInputStream(bytes), 24);
                if (font == null || !font.getFamily().equals(typeface.family())
                        || !font.getName().equals(typeface.fullName()))
                    throw new IOException("Source font identity/load failure: " + typeface.id());
                verifiedFonts.put(typeface.id(), font);
                sizedFonts.put(typeface.id()+"/24.0",font);
            }
        }
    }

    public static AssetCatalog load() throws IOException {
        var resources = bundledResources();
        try (var stream = resources.open("/ui/assets/manifest.json")) {
            if (stream == null) throw new IOException("Missing artwork index.");
            var manifest = new com.google.gson.Gson().fromJson(
                    new InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8), AssetManifest.class);
            return new AssetCatalog(manifest, resources);
        } catch (RuntimeException error) {
            throw new IOException("Could not load artwork: " + error.getMessage(), error);
        }
    }

    public AssetManifest.Artwork artwork(String theme, String id) {
        return Objects.requireNonNull(descriptors.get(theme + "/" + id), "Unregistered artwork: " + id);
    }
    public Image image(String theme, String id) {
        return Objects.requireNonNull(images.get(theme + "/" + id), "Unregistered asset: " + theme + "/" + id);
    }

    public ImageView imageView(String theme, String id) {
        var view = new ImageView(image(theme, id));
        view.setMouseTransparent(true);
        view.setPreserveRatio(true);
        return view;
    }

    public Font font(String id, double size) {
        if (!Double.isFinite(size) || size <= 0) throw new IllegalArgumentException("Invalid font size.");
        Font verified=Objects.requireNonNull(verifiedFonts.get(id),"Unregistered font: "+id);
        Font font=sizedFonts.computeIfAbsent(id+"/"+size,key->new Font(verified.getName(),size));
        if(!font.getName().equals(verified.getName()))throw new IllegalStateException("Verified font identity changed: "+id);
        return font;
    }
}
