package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.matinnameni.minihollowknight.model.enums.enemy.HuskHornheadAnimationType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class HuskHornheadAssetBundle extends AssetBundle {

    public static final String KEY = "huskHornhead";

    // --- Asset paths ---
    private final List<String> assetPaths = new ArrayList<>();

    // --- Animations ---
    private Map<HuskHornheadAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(HuskHornheadAnimationType.class);

    public HuskHornheadAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public void queue() {
        assetPaths.clear();
        for (HuskHornheadAnimationType type : HuskHornheadAnimationType.values()) {
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

    public void loadAnimations(HuskHornheadAnimationType animationType) {
        Animation<TextureRegion> animation = buildAnimation(
            animationType.path, animationType.columnCount, animationType.rowCount,
            animationType.frameDuration, animationType.playMode
        );
        animations.put(animationType, animation);
    }

    // --- Accessors ---

    public Animation<TextureRegion> getAnimation(HuskHornheadAnimationType animationType) {
        return animations.get(animationType);
    }
}
