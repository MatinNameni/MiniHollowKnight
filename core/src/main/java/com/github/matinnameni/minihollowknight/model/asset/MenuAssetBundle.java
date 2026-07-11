package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns all assets used by the main-menu system.
 */
public class MenuAssetBundle extends AssetBundle {

    public static final String KEY = "menu";

    // --- Asset paths ---
    private static final String UI_SKIN = "skins/gameskins.json";
    private static final String BACKGROUND_IMG = "sprites/Menu/controller_prompt_bg.png";
    private static final String LOGO_IMG = "sprites/Menu/logo.png";
    private static final String SEPARATOR = "sprites/Inventory & UI/Fleurs/Warning_Fleur0008.png";
    private static final String MENU_MUSIC = "Audio_Files/Title.wav";

    private static final String SAVE_SLOT_BG_FORGOTTEN_CROSSROADS = "sprites/Area save art/Area_Forgotten Crossroads.png";
    private static final String SAVE_SLOT_BG_GREENPATH = "sprites/Area save art/Area_Green_Path.png";

    private static final String PROFILE_SOUL = "sprites/Inventory & UI/select_game_HUD_0002_health_frame.png";
    private static final String PROFILE_MASK = "sprites/Inventory & UI/select_game_HUD_0001_health.png";
    private static final String PROFILE_FLEUR = "sprites/Inventory & UI/Fleurs/profile_fleur0011.png";

    private static final String PAUSE_OVERLAY_TOP_FLEUR = "sprites/Inventory & UI/Fleurs/pause_top_fleur0008.png";
    private static final String PAUSE_OVERLAY_BOTTOM_FLEUR = "sprites/Inventory & UI/Fleurs/bottom_fleur0008.png";

    private final List<String> assetPaths = new ArrayList<>();

    public MenuAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void queue() {
        assetPaths.clear();
        queue(BACKGROUND_IMG, Texture.class);
        queue(LOGO_IMG, Texture.class);
        queue(SEPARATOR, Texture.class);
        queue(MENU_MUSIC, Music.class);
        queue(SAVE_SLOT_BG_FORGOTTEN_CROSSROADS, Texture.class);
        queue(SAVE_SLOT_BG_GREENPATH, Texture.class);
        queue(PROFILE_FLEUR, Texture.class);
        queue(PROFILE_SOUL, Texture.class);
        queue(PROFILE_MASK, Texture.class);
        queue(PAUSE_OVERLAY_TOP_FLEUR, Texture.class);
        queue(PAUSE_OVERLAY_BOTTOM_FLEUR, Texture.class);
    }

    @Override
    public void onLoaded() {
        // nothing extra to build after loading.
    }

    @Override
    public List<String> getAssetPaths() {
        return assetPaths;
    }

    // --- Queue helper ---

    private <T> void queue(String path, Class<T> type) {
        manager.load(path, type);
        assetPaths.add(path);
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

    public Texture getPauseOverlayTopFleur() {
        return manager.get(PAUSE_OVERLAY_TOP_FLEUR, Texture.class);
    }

    public Texture getPauseOverlayBottomFleur() {
        return manager.get(PAUSE_OVERLAY_BOTTOM_FLEUR, Texture.class);
    }

    /** Creates a {@link Skin} from the UI skin file. */
    public Skin createSkin() {
        return new Skin(Gdx.files.internal(UI_SKIN));
    }
}
