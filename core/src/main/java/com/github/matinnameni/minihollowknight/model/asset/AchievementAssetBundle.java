package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.github.matinnameni.minihollowknight.model.enums.AchievementType;

import java.util.EnumMap;
import java.util.Map;

public class AchievementAssetBundle extends AssetBundle {

    public static final String KEY = "achievements";

    // --- Asset paths (relative to the assets folder) ---
    private static final String DIR = "sprites/Achievements/";
    private static final String ICON_COMPLETION = DIR + "achievement__0000_100_complete.png";
    private static final String ICON_SPEEDRUN = DIR + "achievement_fast_completionist.png";
    private static final String ICON_TRUE_HUNTER = DIR + "achievement_Hunter_Journal.png";
    private static final String ICON_FALSE_KNIGHT = DIR + "achievement_false_knight.png";
    private static final String ICON_CHARMED = DIR + "achievement__0033_charm_01 #00002667.png";

    private final Map<AchievementType, String> pathByType = new EnumMap<>(AchievementType.class);
    private final java.util.List<String> assetPaths = new java.util.ArrayList<>();

    public AchievementAssetBundle(AssetManager manager) {
        super(manager);
        pathByType.put(AchievementType.COMPLETION,           ICON_COMPLETION);
        pathByType.put(AchievementType.SPEEDRUN,             ICON_SPEEDRUN);
        pathByType.put(AchievementType.TRUE_HUNTER,          ICON_TRUE_HUNTER);
        pathByType.put(AchievementType.DEFEAT_FALSE_KNIGHT,  ICON_FALSE_KNIGHT);
        pathByType.put(AchievementType.CHARMED,              ICON_CHARMED);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void queue() {
        assetPaths.clear();
        for (String path : pathByType.values()) {
            manager.load(path, Texture.class);
            assetPaths.add(path);
        }
    }

    @Override
    public void onLoaded() {
        // Textures are fetched lazily through the AssetManager in getIcon().
    }

    @Override
    public java.util.List<String> getAssetPaths() {
        return assetPaths;
    }

    /** @return the icon texture for {@code type}. */
    public Texture getIcon(AchievementType type) {
        String path = pathByType.get(type);
        if (path == null) return null;
        return manager.get(path, Texture.class);
    }
}
