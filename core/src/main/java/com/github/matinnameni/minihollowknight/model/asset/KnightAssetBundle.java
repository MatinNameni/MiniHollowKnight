package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.github.matinnameni.minihollowknight.model.enums.KnightAnimationType;

import java.util.*;

/**
 * Owns all assets for the Knight.
 */
public class KnightAssetBundle extends AssetBundle {

    public static final String KEY = "knight";

    // --- Asset paths ---
    private KnightAnimationType currentAnimation;

    private final List<String> assetPaths = new ArrayList<>();

    // --- Resolved after loading ---
    private Map<KnightAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(KnightAnimationType.class);

    public KnightAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void queue() {
        assetPaths.clear();
        for(KnightAnimationType type : KnightAnimationType.values()) {
            loadAnimations(type);
        }
    }

    @Override
    public void onLoaded() {

    }

    @Override
    public List<String> getAssetPaths() {
        return assetPaths;
    }

    // --- Animation loader ---

    public void loadAnimations(KnightAnimationType animationType) {
        Animation<TextureRegion> animation;
        if(animationType.isSpritesheet()) {
            animation = buildAnimation(animationType.path, animationType.columnCount,
                animationType.rowCount, animationType.frameDuration, animationType.playMode);
        } else {
            animation = buildAnimation(animationType.path, animationType.frameCount, animationType.suffix,
                animationType.startFrameNum, animationType.endFrameNum, animationType.frameDuration, animationType.playMode);
        }
        animations.put(animationType, animation);
    }

    // --- Accessors ---

    public Animation<TextureRegion> getAnimation(KnightAnimationType animationType) {
        return animations.get(animationType);
    }
}
