package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.matinnameni.minihollowknight.model.enums.enemy.CrawlidAnimationType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CrawlidAssetBundle extends AssetBundle {

    public static final String KEY = "crawlid";

    // --- Asset paths ---
    private final List<String> assetPaths = new ArrayList<>();

    // --- Animations ---
    private CrawlidAnimationType currentAnimation;
    private Map<CrawlidAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(CrawlidAnimationType.class);

    public CrawlidAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public void queue() {
        assetPaths.clear();
        for (CrawlidAnimationType type : CrawlidAnimationType.values()) {
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

    public void loadAnimations(CrawlidAnimationType animationType) {
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

    public Animation<TextureRegion> getAnimation(CrawlidAnimationType animationType) {
        return animations.get(animationType);
    }
}
