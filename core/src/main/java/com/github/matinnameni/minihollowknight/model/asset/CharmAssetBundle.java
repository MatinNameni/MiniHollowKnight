package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.github.matinnameni.minihollowknight.model.enums.CharmType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Owns all charm icon textures used by the inventory overlay.
 * <p>
 * Each {@link CharmType} is mapped to a single PNG located under
 * {@code sprites/Inventory & UI/Charms/}. The mapping from {@link CharmType}
 * to filename lives in {@link #pathFor(CharmType)} so the rest of the game
 * only ever deals with the enum.
 */
public class CharmAssetBundle extends AssetBundle {

    public static final String KEY = "charms";

    /** Base folder for every charm icon. */
    private static final String CHARM_FOLDER = "sprites/Inventory & UI/Charms/";

    // --- Charm icon filenames ---
    private static final String SOUL_CATCHER_ICON = CHARM_FOLDER + "Soul Catcher - _0001_charm_more_soul.png";
    private static final String UNBREAKABLE_STRENGTH_ICON = CHARM_FOLDER + "Unbreakable Strength_0002_charm_glass_attack_up_full.png";
    private static final String QUICK_SLASH_ICON = CHARM_FOLDER + "Quick Slash - _0003_charm_nail_slash_speed_up.png";
    private static final String QUICK_FOCUS_ICON = CHARM_FOLDER + "Quick Focus - _0005_charm_fast_focus.png";
    private static final String HEAVY_BLOW_ICON = CHARM_FOLDER + "Heavy Blow - _0008_charm_nail_damage_up.png";
    private static final String DASHMASTER_ICON = CHARM_FOLDER + "Dashmaster - _0011_charm_generic_03.png";
    private static final String SHARP_SHADOW_ICON = CHARM_FOLDER + "Sharp Shadow - charm_shade_impact.png";
    private static final String VOID_HEART_ICON = CHARM_FOLDER + "Void Heart - charm_black.png";

    private final Map<CharmType, Texture> textures = new EnumMap<>(CharmType.class);

    public CharmAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void queue() {
        manager.load(SOUL_CATCHER_ICON, Texture.class);
        manager.load(UNBREAKABLE_STRENGTH_ICON, Texture.class);
        manager.load(QUICK_SLASH_ICON, Texture.class);
        manager.load(QUICK_FOCUS_ICON, Texture.class);
        manager.load(HEAVY_BLOW_ICON, Texture.class);
        manager.load(DASHMASTER_ICON, Texture.class);
        manager.load(SHARP_SHADOW_ICON, Texture.class);
        manager.load(VOID_HEART_ICON, Texture.class);
    }

    @Override
    public void onLoaded() {
        textures.put(CharmType.SOUL_CATCHER, manager.get(SOUL_CATCHER_ICON, Texture.class));
        textures.put(CharmType.UNBREAKABLE_STRENGTH, manager.get(UNBREAKABLE_STRENGTH_ICON, Texture.class));
        textures.put(CharmType.QUICK_SLASH, manager.get(QUICK_SLASH_ICON, Texture.class));
        textures.put(CharmType.QUICK_FOCUS, manager.get(QUICK_FOCUS_ICON, Texture.class));
        textures.put(CharmType.HEAVY_BLOW, manager.get(HEAVY_BLOW_ICON, Texture.class));
        textures.put(CharmType.DASHMASTER, manager.get(DASHMASTER_ICON, Texture.class));
        textures.put(CharmType.SHARP_SHADOW, manager.get(SHARP_SHADOW_ICON, Texture.class));
        textures.put(CharmType.VOID_HEART, manager.get(VOID_HEART_ICON, Texture.class));
    }

    @Override
    public java.util.List<String> getAssetPaths() {
        return Arrays.asList(
            SOUL_CATCHER_ICON,
            UNBREAKABLE_STRENGTH_ICON,
            QUICK_SLASH_ICON,
            QUICK_FOCUS_ICON,
            HEAVY_BLOW_ICON,
            DASHMASTER_ICON,
            SHARP_SHADOW_ICON,
            VOID_HEART_ICON
        );
    }

    /** Returns the icon texture for {@code charm}. */
    public Texture getIcon(CharmType charm) {
        return textures.get(charm);
    }
}
