package com.github.matinnameni.minihollowknight.model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.event.EventBus;
import com.github.matinnameni.minihollowknight.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.asset.KnightAssetBundle;
import com.github.matinnameni.minihollowknight.model.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.KnightAnimationType;

/**
 * The Vengeful Spirit projectile
 */
public class VengefulSpirit implements Projectile {

    // --- Constants ---

    /** Horizontal speed. */
    public static final float SPEED = 600f;

    /** Maximum lifetime before the projectile destroys. */
    public static final float MAX_LIFETIME = 5f;

    /** Damage dealt to enemies on hit. */
    public static final int DAMAGE = 50;

    // Size
    private static final float HITBOX_WIDTH = 180f;
    private static final float HITBOX_HEIGHT = 100f;

    // --- State ---

    private final Vector2 position = new Vector2();
    private final Direction direction;
    private State state = State.FLYING;
    private float stateTime = 0f;
    private float lifetime = 0f;

    // --- Dependencies ---

    private KnightAssetBundle assets;

    public VengefulSpirit(float x, float y, Direction direction, KnightAssetBundle assets) {
        this.position.set(x, y);
        this.direction = (direction == Direction.LEFT) ? Direction.LEFT : Direction.RIGHT;
        this.assets = assets;
    }

    // --- Projectile ---

    @Override
    public void update(float deltaTime) {
        stateTime += deltaTime;

        switch (state) {
            case FLYING:
                lifetime += deltaTime;

                float dir = (direction == Direction.RIGHT) ? 1f : -1f;
                position.x += dir * SPEED * deltaTime;

                if (lifetime >= MAX_LIFETIME) {
                    transitionToImpact();
                }
                break;

            case IMPACT:
                Animation<TextureRegion> impactAnimation = assets.getAnimation(KnightAnimationType.BALL_END);
                if (impactAnimation != null && stateTime >= impactAnimation.getAnimationDuration()) {
                    state = State.DEAD;
                }
                break;

            case DEAD:
            default:
                break;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> animation = getCurrentAnimation();
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(stateTime);

        float frameWidth = frame.getRegionWidth();
        float frameHeight = frame.getRegionHeight();

        float scaleX = (direction == Direction.LEFT) ? -1f : 1f;

        batch.draw(frame,
            position.x - frameWidth / 2f, position.y - frameHeight / 2f,
            frameWidth / 2f, frameHeight / 2f,
            frameWidth, frameHeight,
            scaleX, 1f,
            0f);
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            position.x - HITBOX_WIDTH / 2f,
            position.y - HITBOX_HEIGHT / 2f,
            HITBOX_WIDTH, HITBOX_HEIGHT
        );
    }

    @Override
    public void onHitEnemy(Enemy enemy) {
        enemy.takeDamage(getDamage(), getDirection());
    }

    @Override
    public float getDamage() {
        return Knight.VENGEFUL_SPIRIT_DAMAGE_PER_FRAME;
    }

    @Override
    public boolean isDead() {
        return state == State.DEAD;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean hasEffectOnEnemies() {
        return true;
    }

    @Override
    public void onHitKnight(Knight knight) {

    }

    @Override
    public boolean hasEffectOnKnight() {
        return false;
    }

    // --- Actions ---

    public void onHitWall() {
        if (state == State.FLYING) {
            transitionToImpact();
        }
    }

    // --- Getters ---

    public State getState() {
        return state;
    }

    public boolean isFlying() {
        return state == State.FLYING;
    }

    // --- Helpers ---

    private void transitionToImpact() {
        state = State.IMPACT;
        stateTime = 0f;
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case FLYING: return assets.getAnimation(KnightAnimationType.SOUL_BALL);
            case IMPACT: return assets.getAnimation(KnightAnimationType.BALL_END);
            default: return null;
        }
    }

    // --- Inner classes ---

    public enum State {
        FLYING,
        IMPACT,
        DEAD
    }

    public static final class SpawnInfo {
        public final float x;
        public final float y;
        public final Direction direction;
        public final KnightAssetBundle assets;

        public SpawnInfo(float x, float y, Direction direction, KnightAssetBundle assets) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.assets = assets;
        }
    }
}
