package com.github.matinnameni.minihollowknight.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Single owner of all loaded assets.
 */
public class GameAssets {

    // --- Asset paths ---
    private static final String BACKGROUND_IMG = "sprites/Menu/controller_prompt_bg.png";
    private static final String LOGO_IMG = "sprites/Menu/vheart_title.png";
    private static final String SEPARATOR = "sprites/Menu/separator.png";
    private static final String UI_SKIN = "ui/uiskin.json";

    private final AssetManager manager = new AssetManager();

    /**
     * Queue everything for loading.
     * Call once at startup.
     */
    public void loadAll() {
        manager.load(BACKGROUND_IMG, Texture.class);
        manager.load(LOGO_IMG, Texture.class);
        manager.load(SEPARATOR, Texture.class);
        manager.finishLoading();
    }

    // --- Accessors ---

    public Texture getBackground() {
        return manager.get(BACKGROUND_IMG, Texture.class);
    }

    public Texture getLogo() {
        return manager.get(LOGO_IMG, Texture.class);
    }

    public Texture getSeparator() {
        return manager.get(SEPARATOR, Texture.class);
    }

    public Skin createSkin() {
        return new Skin(Gdx.files.internal(UI_SKIN));
    }

    public void dispose() {
        manager.dispose();
    }
}
