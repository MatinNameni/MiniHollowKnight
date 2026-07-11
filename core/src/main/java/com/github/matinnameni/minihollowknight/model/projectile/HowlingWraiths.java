package com.github.matinnameni.minihollowknight.model.projectile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.asset.KnightAssetBundle;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.KnightAnimationType;

public class HowlingWraiths implements Projectile {
    // --- Constants ---

    /** Damage dealt to enemies on hit. */
    public static final int DAMAGE = 50;

    // Size
    private static final float HITBOX_WIDTH = 250f;
    private static final float HITBOX_HEIGHT = 250f;

    // --- State ---

    private final Vector2 position = new Vector2();
    private float stateTime = 0f;
    private boolean isDead;
    private final float damageMultiplier;

    // --- Dependencies ---

    private KnightAssetBundle assets;

    public HowlingWraiths(float x, float y, KnightAssetBundle assets) {
        this(x, y, assets, 1f);
    }

    public HowlingWraiths(float x, float y, KnightAssetBundle assets, float damageMultiplier) {
        this.position.set(x, y);
        this.assets = assets;
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public void update(float deltaTime) {
        if(isDead) { return; }

        stateTime += deltaTime;

        Animation<TextureRegion> animation = assets.getAnimation(KnightAnimationType.SOUL_SCREAM);
        if (animation != null && stateTime >= animation.getAnimationDuration()) {
            isDead = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> animation = getCurrentAnimation();
        if (animation == null) { return; }

        TextureRegion frame = animation.getKeyFrame(stateTime);

        float frameWidth = frame.getRegionWidth();
        float frameHeight = frame.getRegionHeight();

        batch.draw(frame,
            position.x - frameWidth / 2f, position.y - frameHeight / 2f,
            frameWidth / 2f, frameHeight / 2f,
            frameWidth, frameHeight,
            1, 1f,
            0f);
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            position.x - HITBOX_WIDTH / 2, position.y - HITBOX_HEIGHT / 2,
            HITBOX_WIDTH, HITBOX_HEIGHT
        );
    }

    @Override
    public void onHitEnemy(Enemy enemy) {
        enemy.takeDamage(getDamage(), getDirection(), 1f);
    }

    @Override
    public float getDamage() {
        return Knight.HOWLING_WRAITHS_DAMAGE_PER_FRAME * damageMultiplier;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public Direction getDirection() {
        return Direction.UP;
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

    // --- Helpers ---

    private Animation<TextureRegion> getCurrentAnimation() {
        if(isDead) {
            return null;
        }
        return assets.getAnimation(KnightAnimationType.SOUL_SCREAM);
    }

    // --- Inner classes ---

    public static final class SpawnInfo {
        public final float x;
        public final float y;
        public final KnightAssetBundle assets;
        public final float damageMultiplier;

        public SpawnInfo(float x, float y, KnightAssetBundle assets) {
            this(x, y, assets, 1f);
        }

        public SpawnInfo(float x, float y, KnightAssetBundle assets, float damageMultiplier) {
            this.x = x;
            this.y = y;
            this.assets = assets;
            this.damageMultiplier = damageMultiplier;
        }
    }
}
