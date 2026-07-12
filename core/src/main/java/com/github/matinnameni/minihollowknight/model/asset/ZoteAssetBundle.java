package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.matinnameni.minihollowknight.model.enums.enemy.ZoteAnimationType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Asset bundle for the Zote NPC.
 */
public class ZoteAssetBundle extends AssetBundle {

    public static final String KEY = "zote";

    // --- Asset paths ---
    private final List<String> assetPaths = new ArrayList<>();

    // --- Animations ---
    private final Map<ZoteAnimationType, Animation<TextureRegion>> animations =
        new EnumMap<>(ZoteAnimationType.class);

    public ZoteAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public void queue() {
        assetPaths.clear();
        for (ZoteAnimationType type : ZoteAnimationType.values()) {
            loadAnimations(type);
        }
    }

    @Override
    public void onLoaded() {
        // Animations are built eagerly inside queue() via buildAnimation().
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public List<String> getAssetPaths() {
        return assetPaths;
    }

    // --- Animation loader ---

    public void loadAnimations(ZoteAnimationType animationType) {
        assetPaths.add(animationType.path);
        Animation<TextureRegion> animation = buildAnimation(
            animationType.path,
            animationType.columnCount,
            animationType.rowCount,
            animationType.frameDuration,
            animationType.playMode
        );
        animations.put(animationType, animation);
    }

    // --- Accessors ---

    public Animation<TextureRegion> getAnimation(ZoteAnimationType animationType) {
        return animations.get(animationType);
    }
}
