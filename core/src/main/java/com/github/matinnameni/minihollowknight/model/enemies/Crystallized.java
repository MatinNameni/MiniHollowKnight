package com.github.matinnameni.minihollowknight.model.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.asset.CrystallizedAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.enemy.CrystallizedAnimationType;

public class Crystallized extends Enemy {

    // --- Constants ---

    // Size
    public static final float WIDTH = 145f;
    public static final float HEIGHT = 95f;

    // Hitbox
    public static final float HITBOX_WIDTH = 55f;
    public static final float HITBOX_HEIGHT = 60f;
    public static final float HITBOX_X_OFFSET = (WIDTH - HITBOX_WIDTH) / 2f;
    public static final float HITBOX_Y_OFFSET = (HEIGHT - HITBOX_HEIGHT) / 2f;

    // Movement
    public static final float GRAVITY = Knight.GRAVITY;
    public static final float MAX_FALL_SPEED = 400f;
    public static final float RUN_SPEED = 90f;
    public static final float KNOCKBACK_VELOCITY = 120f;
    public static final float DEATH_KNOCKBACK_VELOCITY = 300f;
    public static final float JUMP_INITIAL_VELOCITY = 300f;
    public static final float KNOCKBACK_COOLDOWN = 0.5f;

    // Timings
    public static final float IDLE_DURATION = 2.0f;
    public static final float RUN_DURATION = 2.5f;
    public static final float TURN_DURATION = CrystallizedAnimationType.TURN.getDuration();
    public static final float SHOOT_DURATION = CrystallizedAnimationType.SHOOT.getDuration();

    /** Delay between the shoot animation starting and when the laser actually fires. */
    public static final float LASER_FIRE_DELAY = 0.35f;
    /** Minimum cooldown between laser shots. */
    public static final float SHOOT_COOLDOWN = 2.5f;

    // Health
    public static final float MAX_HEALTH = 200f;

    // Vision
    /** Horizontal distance the enemy can see the Knight. */
    public static final float VISION_RANGE = 350f;
    public static final float VISION_HEIGHT = 80f;

    // Cliff probe
    public static final float CLIFF_PROBE_DISTANCE = 4f;
    public static final float CLIFF_PROBE_DEPTH = 4f;

    // --- State ---

    private Direction facingDirection;
    private State state = State.IDLE;
    private boolean grounded = false;
    private boolean airborneDeath = false;

    private float stateTime = 0f;
    private float knockbackCooldown = 0f;
    private float shootCooldown = 0f;

    /** Whether a laser was already spawned during the current SHOOTING state. */
    private boolean laserFired = false;

    /** Set to true by the controller when this enemy should fire a laser this frame. */
    private boolean shouldFireLaser = false;

    private final CrystallizedAssetBundle assets;

    public Crystallized(float x, float y, CrystallizedAssetBundle assets) {
        super(x, y);
        this.assets = assets;
        this.facingDirection = Direction.LEFT;
        this.health = MAX_HEALTH;
    }

    // --- Entity ---

    @Override
    public void update(float deltaTime) {
        updateCooldowns(deltaTime);
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
        float visionX = (facingDirection == Direction.RIGHT)
            ? bounds.x + bounds.width
            : bounds.x - VISION_RANGE;
        float visionY = bounds.y + (bounds.height - VISION_HEIGHT) / 2f;
        return new Rectangle(visionX, visionY, VISION_RANGE, VISION_HEIGHT);
    }

    /** Checks whether the Knight is inside this enemy's vision rectangle. */
    public boolean isKnightVisible(Knight knight) {
        if (isDead() || knight.isDead()) return false;
        if (state == State.SHOOTING || state == State.TURNING) return false;
        if (shootCooldown > 0f) return false;

        Rectangle knightBounds = knight.getBounds();
        return getVisionBounds().overlaps(knightBounds);
    }

    // --- Actions ---

    /** Begins the shooting sequence. */
    public void startShooting() {
        if (state == State.IDLE || state == State.RUNNING) {
            enterState(State.SHOOTING);
            laserFired = false;
        }
    }

    /** @return true if the enemy wants to fire a laser this frame. */
    public boolean wantsToFireLaser() {
        return shouldFireLaser;
    }

    /** Consumes the laser fire request. */
    public void consumeLaserFire() {
        shouldFireLaser = false;
    }

    public float getLaserOriginY() {
        return position.y + HITBOX_Y_OFFSET + HITBOX_HEIGHT / 2f;
    }

    public float getLaserOriginX() {
        if (facingDirection == Direction.RIGHT) {
            return position.x + HITBOX_X_OFFSET + HITBOX_WIDTH;
        }
        return position.x + HITBOX_X_OFFSET;
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
        if (state == State.RUNNING || state == State.IDLE) {
            enterState(State.TURNING);
        }
    }

    // --- Getters ---

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isShooting() {
        return state == State.SHOOTING;
    }

    public Rectangle getCliffProbe() {
        Rectangle bounds = getBounds();
        float probeX = (facingDirection == Direction.RIGHT)
            ? bounds.x + bounds.width
            : bounds.x - CLIFF_PROBE_DISTANCE;
        return new Rectangle(probeX, bounds.y - CLIFF_PROBE_DEPTH, CLIFF_PROBE_DISTANCE, CLIFF_PROBE_DEPTH);
    }

    // --- Damage ---

    @Override
    public boolean canDamagePlayer() {
        return !isDead();
    }

    @Override
    public void takeDamage(float damage, Direction knockbackDirection) {
        if (state == State.DEAD) return;

        if (state == State.SHOOTING) {
            enterState(State.IDLE);
            laserFired = false;
        }

        health -= damage;

        // Knockback
        float knockbackDir = 0f;
        switch (knockbackDirection) {
            case LEFT:
                knockbackDir = -1f;
                break;
            case RIGHT:
                knockbackDir = 1f;
                break;
            default:
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
            airborneDeath = knockbackDir != 0f;
        }
    }

    // --- State ---

    private void enterState(State newState) {
        this.state = newState;
        this.stateTime = 0f;
    }

    private void updateCooldowns(float deltaTime) {
        if (shootCooldown > 0f) {
            shootCooldown -= deltaTime;
        }
        if (knockbackCooldown > 0f) {
            knockbackCooldown -= deltaTime;
        }
    }

    private void updateState(float deltaTime) {
        stateTime += deltaTime;

        switch (state) {
            case IDLE:
                velocity.x = 0f;
                if (knockbackCooldown > 0f) break;
                if (stateTime >= IDLE_DURATION) {
                    enterState(State.RUNNING);
                }
                break;

            case RUNNING:
                if (knockbackCooldown > 0f) {
                    knockbackCooldown -= deltaTime;
                    break;
                }

                velocity.x = (facingDirection == Direction.RIGHT) ? RUN_SPEED : -RUN_SPEED;

                if (stateTime >= RUN_DURATION) {
                    enterState(State.IDLE);
                }
                break;

            case TURNING:
                velocity.x = 0f;
                if (stateTime >= TURN_DURATION) {
                    facingDirection = (facingDirection == Direction.RIGHT) ? Direction.LEFT : Direction.RIGHT;
                    enterState(State.IDLE);
                }
                break;

            case SHOOTING:
                velocity.x = 0f;

                if (!laserFired && stateTime >= LASER_FIRE_DELAY) {
                    laserFired = true;
                    shouldFireLaser = true;
                    shootCooldown = SHOOT_COOLDOWN;
                }

                if (stateTime >= SHOOT_DURATION) {
                    enterState(State.IDLE);
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
                break;
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

    // --- Animation ---

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case IDLE: return assets.getAnimation(CrystallizedAnimationType.IDLE);
            case RUNNING: return assets.getAnimation(CrystallizedAnimationType.RUN);
            case TURNING: return assets.getAnimation(CrystallizedAnimationType.TURN);
            case SHOOTING: return assets.getAnimation(CrystallizedAnimationType.SHOOT);
            case DYING, DEAD: return getDeathAnimation();
            default: return null;
        }
    }

    private Animation<TextureRegion> getDeathAnimation() {
        return assets.getAnimation(airborneDeath
            ? CrystallizedAnimationType.DEATH_AIR
            : CrystallizedAnimationType.DEATH_LAND);
    }

    // --- Inner type ---

    public enum State {
        IDLE,
        RUNNING,
        TURNING,
        SHOOTING,
        DYING,
        DEAD
    }
}
