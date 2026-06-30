package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.matinnameni.minihollowknight.model.asset.HudAssetBundle;

/**
 * A single health-mask on the HUD.
 */
public class MaskWidget extends Table {
    // Constants
    public static final float SIZE_SCALE = 0.6f;

    private final HudAssetBundle assets;

    private MaskState state = MaskState.FILLED;
    private float animationTimer = 0f;

    // --- Size ---
    private float displayWidth;
    private float displayHeight;

    public MaskWidget(HudAssetBundle assets) {
        this.assets = assets;
        displayWidth  = assets.getFilledHealth().getWidth() * SIZE_SCALE;
        displayHeight = assets.getFilledHealth().getHeight() * SIZE_SCALE;
        setSize(displayWidth, displayHeight);
    }

    /** Transitions this mask to FILLED. */
    public void fill(boolean animate) {
        if (animate && state != MaskState.FILLED) {
            state = MaskState.REFILLING;
        } else {
            state = MaskState.FILLED;
        }
        animationTimer = 0f;
    }

    /** Transitions this mask to EMPTY. */
    public void breakMask(boolean animate) {
        if (animate && state != MaskState.EMPTY) {
            state = MaskState.BREAKING;
        } else {
            state = MaskState.EMPTY;
        }
        animationTimer = 0f;
    }

    /** Forces an immediate state. */
    public void forceState(MaskState state) {
        this.state = state;
        animationTimer = 0f;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (state == MaskState.BREAKING || state == MaskState.REFILLING) {
            animationTimer += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        switch (state) {
            case FILLED:
                batch.draw(assets.getFilledHealth(), x, y, width, height);
                break;

            case EMPTY:
                batch.draw(assets.getEmptyHealth(), x, y, width, height);
                break;

            case BREAKING: {
                TextureRegion frame = assets.getMaskBreakAnimation().getKeyFrame(animationTimer);
                if (assets.getMaskBreakAnimation().isAnimationFinished(animationTimer)) {
                    state = MaskState.EMPTY;
                    batch.draw(assets.getEmptyHealth(), x, y, width, height);
                } else {
                    batch.draw(frame, x, y, width, height);
                }
                break;
            }

            case REFILLING: {
                TextureRegion frame = assets.getMaskRefillAnimation().getKeyFrame(animationTimer);
                if (assets.getMaskRefillAnimation().isAnimationFinished(animationTimer)) {
                    state = MaskState.FILLED;
                    batch.draw(assets.getFilledHealth(), x, y, width, height);
                } else {
                    batch.draw(frame, x, y, width, height);
                }
                break;
            }
        }
    }

    public MaskState getState() {
        return state;
    }

    // --- Inner classes ---

    public enum MaskState {
        FILLED, EMPTY, BREAKING, REFILLING
    }
}
