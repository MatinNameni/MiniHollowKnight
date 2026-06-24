package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.github.matinnameni.minihollowknight.model.enums.KnightAnimationType;

import java.util.*;

/**
 * Owns all assets for the Knight (player character):
 */
public class KnightAssetBundle extends AssetBundle {

    public static final String KEY = "knight";

    // --- Asset paths ---
    private KnightAnimationType currentAnimation;

    private final List<String> assetPaths = new ArrayList<>();

    // --- Resolved after loading ---
    private Map<KnightAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(KnightAnimationType.class);

    // --- Animation timings (seconds per frame) ---
    private static final float IDLE_FRAME_DURATION   = 0.10f;
    private static final float RUN_FRAME_DURATION    = 0.08f;
    private static final float ATTACK_FRAME_DURATION = 0.05f;
    private static final float JUMP_FRAME_DURATION   = 0.06f;
    private static final float DASH_FRAME_DURATION   = 0.04f;
    private static final float FALL_FRAME_DURATION   = 0.08f;

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
        if(animationType.isSpritesheet()) {
            loadSpritesheetAnimation(animationType);
        } else {
            loadAnimationWithPrefix(animationType);
        }
    }

    private void loadSpritesheetAnimation(KnightAnimationType animationType) {
        Texture texture = new Texture(animationType.path);

        TextureRegion[][] split = TextureRegion.split(
            texture,
            texture.getWidth() / animationType.columnCount,
            texture.getHeight() / animationType.rowCount
        );

        int frameCount = animationType.frameCount;
        TextureRegion[] frames = new TextureRegion[frameCount];

        int columns = split[0].length;

        for(int i = 0; i < frameCount; i++) {
            int row = i / columns;
            int column = i % columns;
            frames[i] = split[row][column];
        }

        Animation<TextureRegion> animation = new Animation<>(animationType.frameDuration, frames);
        animation.setPlayMode(animationType.playMode);

        animations.put(animationType, animation);
    }

    private void loadAnimationWithPrefix(KnightAnimationType animationType) {
        int frameCount = animationType.frameCount;
        Array<TextureRegion> keyFrames = new Array<>(frameCount);

        for (int i = animationType.startFrameNum; i <= animationType.endFrameNum; i++) {
            String frameNum = String.format("%03d", i);
            keyFrames.add(
                new TextureRegion(
                    new Texture(animationType.path + frameNum + animationType.suffix)
                )
            );
        }

        Animation<TextureRegion> animation = new Animation<>(animationType.frameDuration, keyFrames, animationType.playMode);

        animations.put(animationType, animation);
    }

    // --- Accessors ---

    public Animation<TextureRegion> getAnimation(KnightAnimationType animationType) {
        return animations.get(animationType);
    }
}
