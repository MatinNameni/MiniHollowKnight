package com.github.matinnameni.minihollowknight.model.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.Projectile;
import com.github.matinnameni.minihollowknight.model.asset.FalseKnightAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.Direction;

public class Shockwave implements Projectile {

    // --- Constants ---

    // Size
    public static final float WIDTH = 200f;
    public static final float HEIGHT = 150f;

    // Hitbox
    public static final float HITBOX_WIDTH = 110f;
    public static final float HITBOX_HEIGHT = 75f;
    public static final float HITBOX_X_OFFSET_R = 60f;
    public static final float HITBOX_X_OFFSET_L = 30f;
    public static final float HITBOX_Y_OFFSET = 0f;

    // Movement
    public static final float ACCELERATION = 50f;
    public static final float INITIAL_SPEED = 200f;

    // Life cycle
    public static final float MAX_LIFETIME = 4f;

    // --- State ---
    private final float x;
    private final float y;
    private final Direction direction;
    private float lifetime = 0f;
    private float stateTime = 0f;
    private final Animation<TextureRegion> animation;
    private boolean isDead = false;

    public Shockwave(float x, float y, Direction direction, FalseKnightAssetBundle assets) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.animation = assets.getShockwaveAnimation();
    }

    public void update(float deltaTime) {
        lifetime += deltaTime;
        stateTime += deltaTime;

        if (lifetime >= MAX_LIFETIME) {
            isDead = true;
        }
    }

    public void render(SpriteBatch batch) {
        TextureRegion frame = animation.getKeyFrame(stateTime);

        float deltaX = ((lifetime * ACCELERATION) / 2 + INITIAL_SPEED) * lifetime;

        float drawX = (direction == Direction.RIGHT) ?
            x + deltaX :
            x - deltaX - WIDTH;
        float scaleX = direction == Direction.RIGHT ? 1 : -1;

        batch.draw(frame,
            drawX, y,
            WIDTH / 2f, 0,
            WIDTH, HEIGHT,
            scaleX, 1, 0);
    }

    public Rectangle getBounds() {
        float deltaX = ((lifetime * ACCELERATION) / 2 + INITIAL_SPEED) * lifetime;
        float currentX = (direction == Direction.RIGHT) ?
            x + deltaX :
            x - deltaX - WIDTH;

        float startX = (direction == Direction.RIGHT) ?
            currentX + HITBOX_X_OFFSET_R :
            currentX + HITBOX_X_OFFSET_L;

        return new Rectangle(
            startX,
            y + HITBOX_Y_OFFSET,
            HITBOX_WIDTH,
            HITBOX_HEIGHT
        );
    }

    public void onHitWall() {
        isDead = true;
    }

    // --- Projectile ---


    @Override
    public void onHitEnemy(Enemy enemy) {

    }

    @Override
    public boolean hasEffectOnEnemies() {
        return false;
    }

    @Override
    public void onHitKnight(Knight knight) {
        if (knight.isInvincible() || knight.isDead()) return;

        knight.takeDamage(direction, 2);
    }

    @Override
    public boolean hasEffectOnKnight() {
        return true;
    }

    @Override
    public Vector2 getPosition() {
        return null;
    }

    @Override
    public float getDamage() {
        return 0;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    public Direction getDirection() {
        return direction;
    }
}
