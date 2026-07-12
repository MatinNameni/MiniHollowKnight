package com.github.matinnameni.minihollowknight.model.entity.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.asset.CrawlidAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.enemy.CrawlidAnimationType;

public class Crawlid extends Enemy {
    // --- Constants ---

    // Size
    public static final float WIDTH = 120f;
    public static final float HEIGHT = 60f;

    // Hitbox
    public static final float HITBOX_WIDTH = 36f;
    public static final float HITBOX_HEIGHT = 30f;
    public static final float HITBOX_X_OFFSET = (WIDTH - HITBOX_WIDTH) / 2f;
    public static final float HITBOX_Y_OFFSET = 0f;

    // Movement
    public static final float GRAVITY = Knight.GRAVITY;
    public static final float MAX_FALL_SPEED = 400f;
    public static final float SPEED = 100f;
    public static final float KNOCKBACK_VELOCITY = 100f;
    public static final float DEATH_KNOCKBACK_VELOCITY = 300f;
    public static final float JUMP_INITIAL_VELOCITY = 300f;
    public static final float TURN_DURATION = CrawlidAnimationType.TURN.getDuration();
    public static final float KNOCKBACK_COOLDOWN = 0.5f;

    // Cliff probe
    public static final float CLIFF_PROBE_DISTANCE = 4f;
    public static final float CLIFF_PROBE_DEPTH = 4f;

    // Health
    public static final float MAX_HEALTH = 100f;

    // --- State ---
    private Direction movingDirection;
    private State state = State.WALKING;
    private boolean grounded = false;
    private boolean airborneDeath = false;

    private float stateTime = 0f;
    private float knockbackCooldown = 0f;

    private final CrawlidAssetBundle assets;

    public Crawlid(float x, float y, CrawlidAssetBundle assets) {
        super(x, y);
        this.assets = assets;
        this.movingDirection = Direction.RIGHT;
        this.health = MAX_HEALTH;
    }

    @Override
    public void update(float deltaTime) {
        updateState(deltaTime);
        applyPhysics(deltaTime);
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> animation = getCurrentAnimation();
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(stateTime);

        batch.draw(frame,
            position.x, position.y,
            WIDTH / 2f, 0,
            WIDTH, HEIGHT,
            movingDirection == Direction.RIGHT ? -1 : 1, 1, 0);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            position.x + HITBOX_X_OFFSET,
            position.y + HITBOX_Y_OFFSET,
            HITBOX_WIDTH,
            HITBOX_HEIGHT
        );
    }

    // --- External collision hooks ---

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public void onFloorCollision(float pushY) {
        position.y = pushY;
        if (velocity.y < 0f) {
            velocity.y = 0f;
        }
        grounded = true;
    }

    public void onCeilingCollision(float pushY) {
        position.y = pushY;
        if (velocity.y > 0f) {
            velocity.y = 0f;
        }
    }

    public void onWallCollision(float pushX) {
        position.x = pushX;
        onHitWall();
    }

    public void onHitWall() {
        if (state !=  State.WALKING) return;
        enterState(State.TURNING);
    }

    // --- Getters ---

    public Direction getMovingDirection() {
        return movingDirection;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public Rectangle getCliffProbe() {
        Rectangle bounds = getBounds();
        float probeX = (movingDirection == Direction.RIGHT) ?
            bounds.x + bounds.width :
            bounds.x - CLIFF_PROBE_DISTANCE;
        return new Rectangle(probeX, bounds.y - CLIFF_PROBE_DEPTH, CLIFF_PROBE_DISTANCE, CLIFF_PROBE_DEPTH);
    }

    // --- Helpers ---

    private void enterState(State newState) {
        this.state = newState;
        this.stateTime = 0f;
    }

    private void updateState(float deltaTime) {
        stateTime += deltaTime;

        switch (state) {
            case WALKING:
                if(knockbackCooldown > 0f) {
                    knockbackCooldown -= deltaTime;
                    break;
                }

                switch (movingDirection) {
                    case LEFT:
                        velocity.x = -SPEED;
                        break;
                    case RIGHT:
                        velocity.x = SPEED;
                        break;
                }
                break;

            case TURNING:
                velocity.x = 0f;
                if (stateTime >= TURN_DURATION) {
                    switch (movingDirection) {
                        case LEFT:
                            movingDirection = Direction.RIGHT;
                            break;
                        case RIGHT:
                            movingDirection = Direction.LEFT;
                            break;
                    }
                    enterState(State.WALKING);
                }
                break;

            case DYING:
                Animation<TextureRegion> deathAnimation = getDeathAnimation();
                if (deathAnimation != null && deathAnimation.isAnimationFinished(stateTime)) {
                    state = State.DEAD;
                }
                break;

            case DEAD:
                velocity.x = 0f;
        }
    }

    private void applyPhysics(float delta) {
        // Gravity
        velocity.y -= GRAVITY * delta;
        if (velocity.y < -MAX_FALL_SPEED) {
            velocity.y = -MAX_FALL_SPEED;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    private Animation<TextureRegion> getDeathAnimation() {
        return assets.getAnimation(airborneDeath ? CrawlidAnimationType.DEATH_AIR : CrawlidAnimationType.DEATH_LAND);
    }

    @Override
    public boolean canDamagePlayer() {
        return !isDead();
    }

    @Override
    public void takeDamage(float damage, Direction knockbackDirection) {
        if(state == State.DEAD) {
            return;
        }

        health -= damage;

        // Knockback
        float knockbackDir = 0f;
        switch (knockbackDirection) {
            case LEFT:
                knockbackDir = -1;
                break;
            case RIGHT:
                knockbackDir = 1;
                break;
        }

        velocity.x = knockbackDir * KNOCKBACK_VELOCITY;
        velocity.y = JUMP_INITIAL_VELOCITY * 0.5f;

        if(knockbackDir != 0f) {
            knockbackCooldown = KNOCKBACK_COOLDOWN;
        }

        if(isDead()) {
            velocity.x = knockbackDir * DEATH_KNOCKBACK_VELOCITY;
            enterState(State.DYING);
            airborneDeath = knockbackDir != 0;
        }

    }

    @Override
    public void kill() {
        super.kill();
        state = State.DYING;
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case WALKING: return assets.getAnimation(CrawlidAnimationType.WALK);
            case TURNING: return assets.getAnimation(CrawlidAnimationType.TURN);
            case DYING: return getDeathAnimation();
            case DEAD: return getDeathAnimation();
            default: return null;
        }
    }


    // --- Inner type ---

    public enum State {
        WALKING,
        TURNING,
        DYING,
        DEAD
    }
}
