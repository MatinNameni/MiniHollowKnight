package com.github.matinnameni.minihollowknight.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;

/**
 * Single owner of all loaded assets.
 */
public class GameAssets {

    // --- Asset paths ---
    private static final String BACKGROUND_IMG = "sprites/Menu/controller_prompt_bg.png";
    private static final String LOGO_IMG = "sprites/Menu/logo.png";
    private static final String SEPARATOR = "sprites/Inventory & UI/Fleurs/Warning_Fleur0008.png";
    private static final String UI_SKIN = "skins/gameskins.json";
    private static final String MENU_MUSIC = "Audio_Files/Title.wav";

    private static final String SAVE_SLOT_BG_FORGOTTEN_CROSSROADS = "sprites/Area save art/Area_Forgotten Crossroads.png";
    private static final String SAVE_SLOT_BG_GREENPATH = "sprites/Area save art/Area_Green_Path.png";

    private static final String PROFILE_SOUL = "sprites/Inventory & UI/select_game_HUD_0002_health_frame.png";
    private static final String PROFILE_MASK = "sprites/Inventory & UI/select_game_HUD_0001_health.png";
    private static final String PROFILE_FLEUR = "sprites/Inventory & UI/Fleurs/profile_fleur0011.png";

    private final AssetManager manager = new AssetManager();

    /**
     * Queue everything for loading.
     * Call once at startup.
     */
    public void loadAll() {
        manager.load(BACKGROUND_IMG, Texture.class);
        manager.load(LOGO_IMG, Texture.class);
        manager.load(SEPARATOR, Texture.class);
        manager.load(MENU_MUSIC, Music.class);
        manager.load(SAVE_SLOT_BG_FORGOTTEN_CROSSROADS, Texture.class);
        manager.load(SAVE_SLOT_BG_GREENPATH, Texture.class);
        manager.load(PROFILE_FLEUR, Texture.class);
        manager.load(PROFILE_SOUL, Texture.class);
        manager.load(PROFILE_MASK, Texture.class);
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

    public Music getMenuMusic() {
        return manager.get(MENU_MUSIC, Music.class);
    }

    public Texture getSaveSlotBGForgottenCrossroads() {
        return manager.get(SAVE_SLOT_BG_FORGOTTEN_CROSSROADS, Texture.class);
    }

    public Texture getSaveSlotBGGreenpath() {
        return manager.get(SAVE_SLOT_BG_GREENPATH, Texture.class);
    }

    public Texture getSaveSlotBG(GameData data) {
        GameEnvironment environment = GameEnvironment.fromId(data.currentEnvironment);

        if(environment == null) { return null; }
        switch (environment) {
            case FORGOTTEN_CROSSROADS:
                return getSaveSlotBGForgottenCrossroads();
            case GREENPATH:
                return getSaveSlotBGGreenpath();
        }
        return null;
    }

    public Texture getProfileFleur() {
        return manager.get(PROFILE_FLEUR, Texture.class);
    }

    public Texture getProfileMask() {
        return manager.get(PROFILE_MASK, Texture.class);
    }

    public Texture getProfileSoul() {
        return manager.get(PROFILE_SOUL, Texture.class);
    }

    public void dispose() {
        manager.dispose();
    }
}
