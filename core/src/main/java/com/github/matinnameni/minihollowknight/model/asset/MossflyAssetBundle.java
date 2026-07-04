package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.matinnameni.minihollowknight.model.enums.enemy.CrawlidAnimationType;
import com.github.matinnameni.minihollowknight.model.enums.enemy.MossflyAnimationType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MossflyAssetBundle extends AssetBundle {

    public static final String KEY = "mossfly";

    // --- Asset paths ---
    private final List<String> assetPaths = new ArrayList<>();

    // --- Animations ---
    private CrawlidAnimationType currentAnimation;
    private Map<MossflyAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(MossflyAnimationType.class);

    public MossflyAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public void queue() {
        assetPaths.clear();
        for (MossflyAnimationType type : MossflyAnimationType.values()) {
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

    public void loadAnimations(MossflyAnimationType animationType) {
        Animation<TextureRegion> animation = buildAnimation(
            animationType.path, animationType.columnCount, animationType.rowCount,
            animationType.frameDuration, animationType.playMode
        );
        animations.put(animationType, animation);
    }

    // --- Accessors ---

    public Animation<TextureRegion> getAnimation(MossflyAnimationType animationType) {
        return animations.get(animationType);
    }
}
