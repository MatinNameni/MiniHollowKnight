package com.github.matinnameni.minihollowknight.view.renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.asset.KnightAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.KnightAnimationType;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;

/**
 * Handles all rendering and animation logic for the Knight entity.
 */
public class KnightRenderer {

    private final Knight knight;
    private final KnightAssetBundle assets;

    public KnightRenderer(Knight knight, KnightAssetBundle assets) {
        this.knight = knight;
        this.assets = assets;
    }

    // ---- Main sprite ----

    /**
     * Draws the Knight's current animation frame.
     */
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> animation = getCurrentAnimation();
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(knight.getStateTime());

        // Blink effect during invincibility frames
        if (knight.isInvincible() && shouldSkipRenderFrame()) {
            return;
        }

        Vector2 pos = knight.getPosition();

        batch.draw(frame,
            pos.x, pos.y,
            Knight.WIDTH / 2f, 0,
            Knight.WIDTH, Knight.HEIGHT,
            knight.isFacingRight() ? -1 : 1, 1, 0);
    }

    // ---- Effects ----

    /**
     * Draws visual effects that layer on top of the knight.
     */
    public void renderEffects(SpriteBatch batch) {
        KnightState state = knight.getState();
        if (state == KnightState.ATTACKING) {
            renderSlashEffect(batch);
        } else if (state == KnightState.DASHING) {
            renderDashEffect(batch);
        } else if (state == KnightState.VENGEFUL_SPIRIT) {
            renderBlastEffect(batch);
        }
    }

    // ---- Animation selection ----

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (knight.getState()) {
            case IDLE: return assets.getAnimation(KnightAnimationType.IDLE);
            case RUNNING: return assets.getAnimation(KnightAnimationType.RUN);
            case JUMPING: return assets.getAnimation(KnightAnimationType.AIR_BORNE);
            case DOUBLE_JUMPING: return assets.getAnimation(KnightAnimationType.DOUBLE_JUMP);
            case FALLING: return assets.getAnimation(KnightAnimationType.FALL);
            case WALL_SLIDE: return assets.getAnimation(KnightAnimationType.WALL_SLIDE);
            case WALL_JUMP: return assets.getAnimation(KnightAnimationType.WALL_JUMP);
            case DASHING: return getDashAnimation();
            case FOCUSING: return assets.getAnimation(KnightAnimationType.FOCUS);
            case HIT: return assets.getAnimation(KnightAnimationType.IDLE);
            case LANDING: return assets.getAnimation(KnightAnimationType.LANDING);
            case VENGEFUL_SPIRIT: return assets.getAnimation(KnightAnimationType.FIREBALL_CAST);
            case HOWLING_WRAITHS: return assets.getAnimation(KnightAnimationType.SCREAM);
            case DEAD: return assets.getAnimation(KnightAnimationType.DEATH);
            case ATTACKING:
                switch (knight.getAttackDirection()) {
                    case UP: return assets.getAnimation(KnightAnimationType.UP_SLASH);
                    case DOWN: return assets.getAnimation(KnightAnimationType.DOWN_SLASH);
                    default: return assets.getAnimation(KnightAnimationType.SLASH);
                }
            default: return assets.getAnimation(KnightAnimationType.IDLE);
        }
    }

    // ---- Effect renderers ----

    private void renderSlashEffect(SpriteBatch batch) {
        KnightAnimationType effectType;
        switch (knight.getAttackDirection()) {
            case UP:
                effectType = KnightAnimationType.UP_SLASH_EFFECT;
                break;
            case DOWN:
                effectType = KnightAnimationType.DOWN_SLASH_EFFECT;
                break;
            default:
                effectType = KnightAnimationType.SLASH_EFFECT;
                break;
        }

        Animation<TextureRegion> animation = assets.getAnimation(effectType);
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(knight.getStateTime());

        float frameWidth = frame.getRegionWidth();
        float frameHeight = frame.getRegionHeight();

        Vector2 pos = knight.getPosition();
        float centerX = pos.x + Knight.WIDTH / 2f;
        float centerY;
        switch (knight.getAttackDirection()) {
            case UP:
                centerY = pos.y + Knight.HEIGHT;
                break;
            case DOWN:
                centerY = pos.y;
                break;
            default:
                centerY = pos.y + Knight.HEIGHT / 2f;
                break;
        }

        batch.draw(frame,
            centerX - frameWidth / 2f, centerY - frameHeight / 2f,
            frameWidth / 2f, 0,
            frameWidth, frameHeight,
            knight.isFacingRight() ? -1 : 1, 1,
            0f);
    }

    private void renderDashEffect(SpriteBatch batch) {
        Animation<TextureRegion> animation = assets.getAnimation(KnightAnimationType.DASH_EFFECT);
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(knight.getStateTime());

        float frameWidth = frame.getRegionWidth() / 2f;
        float frameHeight = frame.getRegionHeight() / 2f;

        Vector2 pos = knight.getPosition();
        float centerY = pos.y + Knight.HEIGHT / 2f - Knight.DASH_EFFECT_Y_OFFSET;
        float centerX;
        if (knight.isFacingRight()) {
            centerX = pos.x + Knight.DASH_EFFECT_X_OFFSET;
        } else {
            centerX = pos.x + Knight.WIDTH - Knight.DASH_EFFECT_X_OFFSET;
        }

        batch.draw(frame,
            centerX - frameWidth / 2f, centerY - frameHeight / 2f,
            frameWidth / 2f, 0,
            frameWidth, frameHeight,
            knight.isFacingRight() ? 1 : -1, 1,
            0f);
    }

    private void renderBlastEffect(SpriteBatch batch) {
        Animation<TextureRegion> animation = assets.getAnimation(KnightAnimationType.BLAST);
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(knight.getStateTime());

        float frameWidth = frame.getRegionWidth();
        float frameHeight = frame.getRegionHeight();

        Vector2 pos = knight.getPosition();
        float centerY = pos.y + Knight.HEIGHT / 2f;
        float centerX;
        if (knight.isFacingRight()) {
            centerX = pos.x + Knight.WIDTH;
        } else {
            centerX = pos.x;
        }

        batch.draw(frame,
            centerX - frameWidth / 2f, centerY - frameHeight / 2f,
            frameWidth / 2f, 0,
            frameWidth, frameHeight,
            knight.isFacingRight() ? 1 : -1, 1,
            0f);
    }

    // ---- Helpers ----

    /** Blink effect: skip every other render frame during invincibility. */
    private boolean shouldSkipRenderFrame() {
        return ((int) (knight.getInvincibilityTimer() * 8f) % 2) == 0;
    }

    /**
     * @return the shadow animation if the knight has sharp shadow,
     * normal animation otherwise
     */
    private Animation<TextureRegion> getDashAnimation() {
        if (knight.isDashingThroughEnemies()) {
            return assets.getAnimation(KnightAnimationType.SHADOW_DASH);
        }
        return assets.getAnimation(KnightAnimationType.DASH);
    }
}
