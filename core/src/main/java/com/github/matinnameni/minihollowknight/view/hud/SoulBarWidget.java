package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.asset.HudAssetBundle;

/**
 * The soul orb on the HUD.
 */
public class SoulBarWidget extends Table {
    // Constants
    public static final float SIZE_SCALE = 0.9f;
    public static final float ANIMATION_DELAY = 0.7f;
    public static final float MAX_SOUL = GameData.MAX_SOUL;
    public static final float SOUL_BAR_TOP_MARGIN = 43f;
    public static final float SOUL_BAR_BOTTOM_MARGIN = 5f;
    public static final float SOUL_BAR_FIRST_FILL_DURATION = 2f;
    public static final float SOUL_EYE_LEFT_MARGIN = 25f;
    public static final float SOUL_EYE_BOTTOM_MARGIN = 25f;

    private final HudAssetBundle assets;
    private float soul;

    private OrbState state = OrbState.POPPING_UP;
    private float popUpTimer;
    private float firstFillTimer;

    private float delayTimer = 0.0f;
    private boolean hasPoppedUp = false;

    // --- Size ---
    private float displayWidth;
    private float displayHeight;

    public SoulBarWidget(HudAssetBundle assets) {
        this.assets = assets;
        this.soul = 0f;

        TextureRegion orb = assets.getHealthBarAnimation().getKeyFrames()[0];
        displayWidth  = orb.getRegionWidth() * SIZE_SCALE;
        displayHeight = orb.getRegionHeight() * SIZE_SCALE;
        setSize(displayWidth, displayHeight);
    }

    /** Call every frame with the knight's current soul value. */
    public void updateSoul(float currentSoul) {
        this.soul = currentSoul;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (state == OrbState.POPPING_UP) {
            if (!hasPoppedUp) {
                delayTimer += delta;
                if (delayTimer >= ANIMATION_DELAY) {
                    hasPoppedUp = true;
                }
            } else {
                popUpTimer += delta;
            }
        } else if(state == OrbState.START_FILLING) {
            firstFillTimer += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        if(assets.getHealthBarAnimation().isAnimationFinished(popUpTimer)) {
            TextureRegion frame = assets.getHealthBarAnimation().getKeyFrame(popUpTimer);
            batch.draw(frame, x, y, width, height);
        }

        switch (state) {
            case POPPING_UP:
                if(assets.getHealthBarAnimation().isAnimationFinished(popUpTimer)) {
                    state = OrbState.START_FILLING;
                } else {
                    TextureRegion frame = assets.getHealthBarAnimation().getKeyFrame(popUpTimer);
                    batch.draw(frame, x, y, width, height);
                }
                break;

            case START_FILLING:
                if(firstFillTimer <= SOUL_BAR_FIRST_FILL_DURATION) {
                    float fillRatio = firstFillTimer / SOUL_BAR_FIRST_FILL_DURATION;
                    drawSoulOrb(batch, x, y, width, height, soul * fillRatio);
                } else {
                    state = OrbState.REFILLING;
                    drawSoulOrb(batch, x, y, width, height, soul);
                }
                break;

            case REFILLING:
                drawSoulOrb(batch, x, y, width, height, soul);
                drawOrbEyes(batch, x, y, width, height);
                break;
        }
    }

    public OrbState getState() {
        return state;
    }

    // --- Helpers ---

    private void drawSoulOrb(Batch batch, float x, float y, float width, float height, float soul) {
        Texture fullSoulOrb = assets.getSoulOrbFull();
        float fillRatio = soul / MAX_SOUL;

        if (fillRatio <= 0) {
            return;
        }

        float totalOrbHeight = fullSoulOrb.getHeight();
        float totalOrbWidth = fullSoulOrb.getWidth();

        float activeOrbHeight = totalOrbHeight - SOUL_BAR_TOP_MARGIN - SOUL_BAR_BOTTOM_MARGIN;
        float filledOrbHeight = activeOrbHeight * fillRatio;

        if (filledOrbHeight <= 0) {
            return;
        }

        float scaleX = width / totalOrbWidth;
        float scaleY = height / totalOrbHeight;

        float srcX = 0;
        float srcY = totalOrbHeight - SOUL_BAR_BOTTOM_MARGIN - filledOrbHeight;
        float srcWidth = totalOrbWidth;
        float srcHeight = filledOrbHeight;

        float drawX = x;
        float drawY = y + (SOUL_BAR_BOTTOM_MARGIN * scaleY);
        float drawWidth = width;
        float drawHeight = filledOrbHeight * scaleY;

        batch.draw(
            fullSoulOrb,
            drawX, drawY,
            drawWidth, drawHeight,
            (int) srcX, (int) srcY,
            (int) srcWidth, (int) srcHeight,
            false, false
        );
    }

    private void drawOrbEyes(Batch batch, float x, float y, float width, float height) {
        if(soul < Knight.FOCUS_SOUL_COST) {
            return;
        }

        Texture orbEye = assets.getSoulOrbEye();
        float eyeX = x + SOUL_EYE_LEFT_MARGIN;
        float eyeY = y + SOUL_EYE_BOTTOM_MARGIN;
        batch.draw(orbEye, eyeX, eyeY, orbEye.getWidth(), orbEye.getHeight());
    }

    // --- Inner class ---

    public enum OrbState {
        POPPING_UP, START_FILLING, REFILLING
    }
}
