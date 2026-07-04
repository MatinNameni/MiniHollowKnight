package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.matinnameni.minihollowknight.model.enums.enemy.CrystallizedAnimationType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CrystallizedAssetBundle extends AssetBundle {

    public static final String KEY = "crystallized";

    // --- Asset paths ---
    private final List<String> assetPaths = new ArrayList<>();

    // --- Animations ---
    private Map<CrystallizedAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(CrystallizedAnimationType.class);

    public CrystallizedAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public void queue() {
        assetPaths.clear();
        for (CrystallizedAnimationType type : CrystallizedAnimationType.values()) {
            loadAnimations(type);
        }
    }

    @Override
    public void onLoaded() {
    }

    @Override
    public String getKey() {
        return KEY;
    }

    // --- Animation loader ---

    public void loadAnimations(CrystallizedAnimationType animationType) {
        Animation<TextureRegion> animation = buildAnimation(
            animationType.path, animationType.columnCount, animationType.rowCount,
            animationType.frameDuration, animationType.playMode
        );
        animations.put(animationType, animation);
    }

    // --- Accessors ---

    public Animation<TextureRegion> getAnimation(CrystallizedAnimationType animationType) {
        return animations.get(animationType);
    }
}