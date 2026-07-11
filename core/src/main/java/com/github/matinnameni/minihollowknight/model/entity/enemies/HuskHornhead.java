package com.github.matinnameni.minihollowknight.model.entity.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.asset.HuskHornheadAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.enemy.HuskHornheadAnimationType;

public class HuskHornhead extends Enemy {
    // --- Constants ---

    // Size
    public static final float WIDTH = 140f;
    public static final float HEIGHT = 128f;

    // Hitbox
    public static final float HITBOX_WIDTH = 65f;
    public static final float HITBOX_HEIGHT = 60f;
    public static final float HITBOX_X_OFFSET = (WIDTH - HITBOX_WIDTH) / 2f;
    public static final float HITBOX_Y_OFFSET = 10f;

    // Movement
    public static final float GRAVITY = Knight.GRAVITY;
    public static final float MAX_FALL_SPEED = 400f;
    public static final float WALK_SPEED = 70f;
    public static final float CHARGE_SPEED = 320f;
    public static final float KNOCKBACK_VELOCITY = 110f;
    public static final float DEATH_KNOCKBACK_VELOCITY = 300f;
    public static final float JUMP_INITIAL_VELOCITY = 300f;
    public static final float KNOCKBACK_COOLDOWN = 0.5f;

    // Timings
    public static final float WALK_DURATION = 2.5f;
    public static final float REST_DURATION = 1.5f;
    public static final float TURN_DURATION = HuskHornheadAnimationType.TURN.getDuration();
    public static final float ANTICIPATE_DURATION = HuskHornheadAnimationType.ATTACK_ANTICIPATE.getDuration();

    // Health
    public static final float MAX_HEALTH = 160f;

    // Vision
    public static final float VISION_RANGE = 260f;
    public static final float VISION_HEIGHT = 70f;

    // Cliff probe
    public static final float CLIFF_PROBE_DISTANCE = 4f;
    public static final float CLIFF_PROBE_DEPTH = 4f;

    // --- State ---
    private Direction facingDirection;
    private State state = State.WALKING;
    private boolean grounded = false;

    private float stateTime = 0f;
    private float knockbackCooldown = 0f;

    private final HuskHornheadAssetBundle assets;

    public HuskHornhead(float x, float y, HuskHornheadAssetBundle assets) {
        super(x, y);
        this.assets = assets;
        this.facingDirection = Direction.RIGHT;
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
            facingDirection == Direction.RIGHT ? -1 : 1, 1, 0);
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

    // --- Vision ---

    /** @return the forward-facing rectangle this enemy uses to spot the Knight. */
    public Rectangle getVisionBounds() {
        Rectangle bounds = getBounds();
        float visionX = (facingDirection == Direction.RIGHT) ?
            bounds.x + bounds.width :
            bounds.x - VISION_RANGE;
        float visionY = bounds.y + (bounds.height - VISION_HEIGHT) / 2f;
        return new Rectangle(visionX, visionY, VISION_RANGE, VISION_HEIGHT);
    }

    /** Checks whether the Knight is inside this enemy's vision rectangle. */
    public boolean isKnightVisible(Knight knight) {
        if (isDead() || knight.isDead()) return false;
        if (state == State.CHARGING || state == State.ANTICIPATING) return false;

        Rectangle knightBounds = knight.getBounds();
        return getVisionBounds().overlaps(knightBounds);
    }

    /** Starts the anticipation wind-up before charging at the Knight. */
    public void startCharge() {
        if (state == State.WALKING || state == State.RESTING) {
            enterState(State.ANTICIPATING);
        }
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
        onObstacleReached();
    }

    /** Called when a wall or cliff edge is reached. */
    public void onObstacleReached() {
        if (state == State.CHARGING) {
            velocity.x = 0f;
            enterState(State.RESTING);
            return;
        }
        if (state != State.WALKING) return;
        enterState(State.TURNING);
    }

    // --- Getters ---

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isCharging() {
        return state == State.CHARGING;
    }

    public Rectangle getCliffProbe() {
        Rectangle bounds = getBounds();
        float probeX = (facingDirection == Direction.RIGHT) ?
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
                if (knockbackCooldown > 0f) {
                    knockbackCooldown -= deltaTime;
                    break;
                }

                velocity.x = (facingDirection == Direction.RIGHT) ? WALK_SPEED : -WALK_SPEED;

                if (stateTime >= WALK_DURATION) {
                    enterState(State.RESTING);
                }
                break;

            case RESTING:
                velocity.x = 0f;
                if (stateTime >= REST_DURATION) {
                    enterState(State.WALKING);
                }
                break;

            case TURNING:
                velocity.x = 0f;
                if (stateTime >= TURN_DURATION) {
                    facingDirection = (facingDirection == Direction.RIGHT) ? Direction.LEFT : Direction.RIGHT;
                    enterState(State.WALKING);
                }
                break;

            case ANTICIPATING:
                velocity.x = 0f;
                if (stateTime >= ANTICIPATE_DURATION) {
                    enterState(State.CHARGING);
                }
                break;

            case CHARGING:
                velocity.x = (facingDirection == Direction.RIGHT) ? CHARGE_SPEED : -CHARGE_SPEED;
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
        return assets.getAnimation(HuskHornheadAnimationType.DEATH_LAND);
    }

    @Override
    public boolean canDamagePlayer() {
        return !isDead() && (state == State.CHARGING || state == State.WALKING);
    }

    @Override
    public void takeDamage(float damage, Direction knockbackDirection) {
        if (state == State.DEAD) {
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

        if (knockbackDir != 0f) {
            knockbackCooldown = KNOCKBACK_COOLDOWN;
        }

        if (isDead()) {
            velocity.x = knockbackDir * DEATH_KNOCKBACK_VELOCITY;
            enterState(State.DYING);
        }
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case WALKING: return assets.getAnimation(HuskHornheadAnimationType.WALK);
            case RESTING: return assets.getAnimation(HuskHornheadAnimationType.IDLE);
            case TURNING: return assets.getAnimation(HuskHornheadAnimationType.TURN);
            case ANTICIPATING: return assets.getAnimation(HuskHornheadAnimationType.ATTACK_ANTICIPATE);
            case CHARGING: return assets.getAnimation(HuskHornheadAnimationType.ATTACK_LUNGE);
            case DYING, DEAD: return getDeathAnimation();
            default: return null;
        }
    }

    // --- Inner type ---

    public enum State {
        WALKING,
        RESTING,
        TURNING,
        ANTICIPATING,
        CHARGING,
        DYING,
        DEAD
    }
}
