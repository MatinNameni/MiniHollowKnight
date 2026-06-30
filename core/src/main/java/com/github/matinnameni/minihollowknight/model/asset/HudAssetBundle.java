package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns all assets for the in-game HUD.
 */
public class HudAssetBundle extends AssetBundle {

    public static final String KEY = "hud";

    // --- Asset paths ---
    private static final String SOUL_ORB_EMPTY = "animation/HUD/SoulOrb_Empty.png";
    private static final String SOUL_ORB_FULL = "animation/HUD/SoulOrb_Half.png";
    private static final String SOUL_ORB_EYE = "animation/HUD/SoulOrb_Eye.png";
    private static final String HEALTH_BAR = "animation/HUD/HealthBar.png";
    private static final String FILLED_HEALTH = "animation/HUD/FilledHealth.png";
    private static final String EMPTY_HEALTH = "animation/HUD/EmptyHealth.png";
    private static final String BREAK_HEALTH = "animation/HUD/BreakHealth.png";
    private static final String REFILL_HEALTH = "animation/HUD/HealthRefill.png";

    // --- Textures ---
    private Texture soulOrbEmpty;
    private Texture soulOrbFull;
    private Texture soulOrbEye;
    private Texture filledHealth;
    private Texture emptyHealth;
    private Texture breakHealthSheet;
    private Texture refillHealthSheet;

    // --- Animations ---
    private Animation<TextureRegion> healthBarAnimation;
    public static final int HEALTH_BAR_COLUMNS = 6;
    public static final int HEALTH_BAR_ROWS = 1;
    public static final float HEALTH_BAR_FRAME_DURATION = 1/15f;
    public static final Animation.PlayMode HEALTH_BAR_PLAYMODE = Animation.PlayMode.NORMAL;

    private Animation<TextureRegion> maskBreakAnimation;
    public static final int MASK_BREAK_COLUMNS = 6;
    public static final int MASK_BREAK_ROWS = 1;
    public static final float MASK_BREAK_FRAME_DURATION = 1/15f;
    public static final Animation.PlayMode MASK_BREAK_PLAY_MODE = Animation.PlayMode.NORMAL;

    private Animation<TextureRegion> maskRefillAnimation;
    public static final int MASK_REFILL_COLUMNS = 5;
    public static final int MASK_REFILL_ROWS = 1;
    public static final float MASK_REFILL_FRAME_DURATION = 1/15f;
    public static final Animation.PlayMode MASK_REFILL_PLAY_MODE = Animation.PlayMode.NORMAL;

    private final List<String> assetPaths = new ArrayList<>();

    public HudAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void queue() {
        assetPaths.clear();
        manager.load(SOUL_ORB_EMPTY, Texture.class);
        assetPaths.add(SOUL_ORB_EMPTY);
        manager.load(SOUL_ORB_FULL, Texture.class);
        assetPaths.add(SOUL_ORB_FULL);
        manager.load(SOUL_ORB_EYE, Texture.class);
        assetPaths.add(SOUL_ORB_EYE);
        manager.load(HEALTH_BAR, Texture.class);
        assetPaths.add(HEALTH_BAR);
        manager.load(FILLED_HEALTH, Texture.class);
        assetPaths.add(FILLED_HEALTH);
        manager.load(EMPTY_HEALTH, Texture.class);
        assetPaths.add(EMPTY_HEALTH);
        manager.load(BREAK_HEALTH, Texture.class);
        assetPaths.add(BREAK_HEALTH);
        manager.load(REFILL_HEALTH, Texture.class);
        assetPaths.add(REFILL_HEALTH);
    }

    @Override
    public void onLoaded() {
        soulOrbEmpty = manager.get(SOUL_ORB_EMPTY, Texture.class);
        soulOrbFull = manager.get(SOUL_ORB_FULL, Texture.class);
        soulOrbEye = manager.get(SOUL_ORB_EYE, Texture.class);
        filledHealth = manager.get(FILLED_HEALTH, Texture.class);
        emptyHealth = manager.get(EMPTY_HEALTH, Texture.class);
        breakHealthSheet = manager.get(BREAK_HEALTH, Texture.class);
        refillHealthSheet = manager.get(REFILL_HEALTH, Texture.class);

        maskBreakAnimation = buildAnimation(BREAK_HEALTH, MASK_BREAK_COLUMNS,
            MASK_BREAK_ROWS, MASK_BREAK_FRAME_DURATION, MASK_BREAK_PLAY_MODE);

        maskRefillAnimation = buildAnimation(REFILL_HEALTH, MASK_REFILL_COLUMNS,
            MASK_REFILL_ROWS, MASK_REFILL_FRAME_DURATION, MASK_REFILL_PLAY_MODE);

        healthBarAnimation = buildAnimation(HEALTH_BAR, HEALTH_BAR_COLUMNS,
            HEALTH_BAR_ROWS, HEALTH_BAR_FRAME_DURATION, HEALTH_BAR_PLAYMODE);
    }

    @Override
    public List<String> getAssetPaths() {
        return assetPaths;
    }

    // --- Getters ---


    public Texture getSoulOrbEmpty() {
        return soulOrbEmpty;
    }

    public Texture getSoulOrbFull() {
        return soulOrbFull;
    }

    public Texture getSoulOrbEye() {
        return soulOrbEye;
    }

    public Texture getFilledHealth() {
        return filledHealth;
    }

    public Texture getEmptyHealth() {
        return emptyHealth;
    }

    public Texture getBreakHealthSheet() {
        return breakHealthSheet;
    }

    public Texture getRefillHealthSheet() {
        return refillHealthSheet;
    }

    public Animation<TextureRegion> getMaskBreakAnimation() {
        return maskBreakAnimation;
    }

    public Animation<TextureRegion> getMaskRefillAnimation() {
        return maskRefillAnimation;
    }

    public Animation<TextureRegion> getHealthBarAnimation() {
        return healthBarAnimation;
    }
}
