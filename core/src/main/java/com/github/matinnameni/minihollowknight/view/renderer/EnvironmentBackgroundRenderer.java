package com.github.matinnameni.minihollowknight.view.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;

import java.util.EnumMap;
import java.util.Map;

/**
 * Renders a background picture behind the Tiled map.
 */
public class EnvironmentBackgroundRenderer {

    // --- Background paths ---

    public static String pathFor(GameEnvironment env) {
        if (env == null) return null;
        switch (env) {
            case FORGOTTEN_CROSSROADS:
                return "sprites/Architecture & Environment/Area specfic architecture/Forgotten Crossroads/background/forgottenCrossroadsBackground.png";
            case GREENPATH:
                return "sprites/Architecture & Environment/Area specfic architecture/Greenpath/background/greenpathBackground.png";
            default:
                return null;
        }
    }

    // --- State ---

    private final AssetManager assetManager;
    private final Map<GameEnvironment, Texture> cache = new EnumMap<>(GameEnvironment.class);

    private GameEnvironment currentEnvironment;
    private Texture currentTexture;

    public EnvironmentBackgroundRenderer(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void setCurrentEnvironment(GameEnvironment env) {
        if (env == currentEnvironment) return;
        currentEnvironment = env;
        currentTexture = (env == null) ? null : resolve(env);
    }

    public void render(SpriteBatch batch, Camera camera) {
        if (currentTexture == null) return;

        float viewW = camera.viewportWidth;
        float viewH = camera.viewportHeight;
        float x = camera.position.x - viewW / 2f;
        float y = camera.position.y - viewH / 2f;

        Matrix4 oldProj = batch.getProjectionMatrix();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(currentTexture, x, y, viewW, viewH);
        batch.end();
        batch.setProjectionMatrix(oldProj);
    }

    private Texture resolve(GameEnvironment env) {
        Texture cached = cache.get(env);
        if (cached != null) return cached;

        String path = pathFor(env);
        if (path == null) return null;

        Texture tex;
        if (assetManager != null && assetManager.isLoaded(path, Texture.class)) {
            tex = assetManager.get(path, Texture.class);
        } else if (Gdx.files.internal(path).exists()) {
            tex = new Texture(Gdx.files.internal(path));
        } else {
            return null;
        }

        cache.put(env, tex);
        return tex;
    }

    public void dispose() {
        for (Texture tex : cache.values()) {
            if (tex == null) continue;
            if (assetManager == null) {
                tex.dispose();
            }
        }
        cache.clear();
        currentTexture = null;
    }
}
