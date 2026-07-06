package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.matinnameni.minihollowknight.model.enums.enemy.FalseKnightAnimationType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class FalseKnightAssetBundle extends AssetBundle {
    public static final String KEY = "falseKnight";

    // --- Asset paths ---
    private final List<String> assetPaths = new ArrayList<>();

    // --- Animations ---
    private Map<FalseKnightAnimationType, Animation<TextureRegion>> animations = new EnumMap<>(FalseKnightAnimationType.class);

    // --- Shockwave ---
    private static final String SHOCKWAVE_PATH = "animation/Projectile/Shockwave.png";
    private static final int SHOCKWAVE_FRAMES = 8;
    private static final float SHOCKWAVE_FRAME_DURATION = 1 / 12f;
    private Animation<TextureRegion> shockwaveAnimation;

    public FalseKnightAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public void queue() {
        assetPaths.clear();
        for (FalseKnightAnimationType type : FalseKnightAnimationType.values()) {
            loadAnimations(type);
        }
        loadShockwaveAnimation();
    }

    @Override
    public void onLoaded() {
        // Animations are already built in queue() via buildAnimation() which loads textures eagerly
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

    public void loadAnimations(FalseKnightAnimationType animationType) {
        assetPaths.add(animationType.path);
        Animation<TextureRegion> animation = buildAnimation(
            animationType.path, animationType.columnCount, animationType.rowCount,
            animationType.frameDuration, animationType.playMode
        );
        animations.put(animationType, animation);
    }

    private void loadShockwaveAnimation() {
        assetPaths.add(SHOCKWAVE_PATH);
        Texture texture = new Texture(SHOCKWAVE_PATH);
        TextureRegion[][] split = TextureRegion.split(
            texture,
            texture.getWidth() / SHOCKWAVE_FRAMES,
            texture.getHeight()
        );
        TextureRegion[] frames = new TextureRegion[SHOCKWAVE_FRAMES];
        for (int i = 0; i < SHOCKWAVE_FRAMES; i++) {
            frames[i] = split[0][i];
        }
        shockwaveAnimation = new Animation<>(SHOCKWAVE_FRAME_DURATION, frames);
        shockwaveAnimation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
    }

    // --- Accessors ---

    public Animation<TextureRegion> getAnimation(FalseKnightAnimationType animationType) {
        return animations.get(animationType);
    }

    public Animation<TextureRegion> getShockwaveAnimation() {
        return shockwaveAnimation;
    }
}
