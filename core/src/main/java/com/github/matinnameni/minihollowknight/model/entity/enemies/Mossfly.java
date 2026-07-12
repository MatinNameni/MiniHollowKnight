package com.github.matinnameni.minihollowknight.model.entity.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.asset.MossflyAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.enemy.MossflyAnimationType;

public class Mossfly extends Enemy {
    // --- Constants ---

    // Size
    public static final float WIDTH = 100f;
    public static final float HEIGHT = 80f;

    // Hitbox
    public static final float HITBOX_WIDTH = 50f;
    public static final float HITBOX_HEIGHT = 50f;
    public static final float HITBOX_X_OFFSET = (WIDTH - HITBOX_WIDTH) / 2f;
    public static final float HITBOX_Y_OFFSET = (HEIGHT - HITBOX_HEIGHT) / 2f;

    // Movement
    public static final float GRAVITY = 980f;
    public static final float SPEED = 120f;
    public static final float KNOCKBACK_VELOCITY = 150f;
    public static final float DEATH_KNOCKBACK_VELOCITY = 300f;
    public static final float KNOCKBACK_COOLDOWN = 0.4f;
    public static final float WAKING_UP_VELOCITY = 200f;
    public static final float MAX_FALL_SPEED = 480f;
    public static final float IDLE_BACK_AND_FORTH_DISTANCE = 150f;
    public static final float IDLE_UPWARD_VELOCITY_DURATION = 0.7f;

    // Health
    public static final float MAX_HEALTH = 150f;

    // Detection
    public static final float DETECTION_RANGE = 175f;
    public static final float ABORT_CHASE_DISTANCE = 500f;

    // --- State ---
    private boolean facingRight;
    private State state = State.HIDING;
    private boolean airborneDeath = false;
    private boolean detectedKnight = false;

    private float stateTime = 0f;
    private float knockbackCooldown = 0f;

    /** The distance this mossfly has gone back and forth in IDLE state. */
    private float idleDistanceGone = 0f;

    private final MossflyAssetBundle assets;

    public Mossfly(float x, float y, MossflyAssetBundle assets) {
        super(x, y);
        this.assets = assets;
        facingRight = true;
        this.health = MAX_HEALTH;
    }

    @Override
    public void takeDamage(float damage, Direction knockbackDirection) {
        if(state == State.DEAD) {
            return;
        }

        if(state == State.HIDING) {
            wakeUp();
            return;
        }

        health -= damage;

        // Knockback
        float knockbackDir = 1f;
        switch (knockbackDirection) {
            case LEFT:
                knockbackDir = -1f;
                velocity.x = -KNOCKBACK_VELOCITY;
                break;
            case RIGHT:
                knockbackDir = 1f;
                velocity.x = KNOCKBACK_VELOCITY;
                break;
            case UP:
                velocity.y = KNOCKBACK_VELOCITY * 0.5f;
                break;
            case DOWN:
                velocity.y = -KNOCKBACK_VELOCITY * 0.5f;
                break;
        }

        knockbackCooldown = KNOCKBACK_COOLDOWN;

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

    @Override
    public boolean canDamagePlayer() {
        return !isDead() &&
            (state == State.IDLE || state == State.CHASING);
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
            facingRight ? -1 : 1, 1, 0);
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

    /** Makes the mossfly appear if it's in hiding state. */
    public void wakeUp() {
        if (state == State.HIDING) {
            enterState(State.APPEARING);
        }
    }

    /**
     * @return a circle representing the range that this mossfly
     * can detect the Knight within it.
     */
    public Circle getDetectionBounds() {
        return new Circle(
            position.x + WIDTH / 2,
            position.y + HEIGHT / 2,
            DETECTION_RANGE
        );
    }

    /** Checks if the Knight is in detection range. */
    public boolean isKnightDetected(Knight knight) {
        if (isDead() || knight.isDead()) {
            detectedKnight = false;
            return false;
        }

        // Don't chase the knight when knockback is being applied
        if (knockbackCooldown > 0.1) {
            detectedKnight = false;
            return false;
        } else if (knockbackCooldown > 0f && knockbackCooldown <= 0.1f) {
            detectedKnight = true;
            return true;
        }

        Vector2 center = new Vector2(
            knight.getBounds().x + knight.getBounds().width / 2,
            knight.getBounds().y + knight.getBounds().height / 2
        );

        if (getDetectionBounds().contains(center)) {
            detectedKnight = true;
            return true;
        }
        return false;
    }

    /** Chases the Knight. */
    public void chaseKnight(Knight knight) {
        if(knight.isDead()) {
            return;
        }

        float xDistance = knight.getBounds().x - getBounds().x;
        float yDistance = knight.getBounds().y - getBounds().y;
        float distance = (float) Math.hypot(xDistance, yDistance);

        if (distance < 0.1f) {
            velocity.x = 0f;
            velocity.y = 0f;
            return;
        }

        if(distance > ABORT_CHASE_DISTANCE) {
            detectedKnight = false;
            return;
        }

        velocity.x = SPEED * xDistance / distance;
        velocity.y = SPEED * yDistance / distance;
    }

    /** @return true if the Knight has already been detected by this mossfly. */
    public boolean hasDetectedKnightAlready() {
        return detectedKnight;
    }

    // --- External collision hooks ---

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void onFloorCollision(float pushY) {
        position.y = pushY;
        if (velocity.y < 0f) {
            velocity.y = 0f;
        }
    }

    public void onCeilingCollision(float pushY) {
        position.y = pushY;
        if (velocity.y > 0f) {
            velocity.y = 0f;
        }
    }

    public void onWallCollision(float pushX) {
        position.x = pushX;
        velocity.x *= -1f;
        idleDistanceGone = 0f;
    }

    // --- Helpers ---

    private void enterState(State newState) {
        this.state = newState;
        stateTime = 0f;
    }

    private void updateState(float deltaTime) {
        stateTime += deltaTime;

        switch (state) {
            case HIDING:
                if(detectedKnight) {
                    enterState(State.APPEARING);
                } else {
                    velocity.x = 0f;
                    velocity.y = 0f;
                }
                break;

            case APPEARING:
                velocity.y = WAKING_UP_VELOCITY;
                velocity.x = 0;
                Animation<TextureRegion> appearAnimation = assets.getAnimation(MossflyAnimationType.APPEAR);
                if(appearAnimation.isAnimationFinished(stateTime)) {
                    enterState(State.TURN_TO_FLY);
                }
                break;

            case TURN_TO_FLY:
                velocity.y = 0f;
                velocity.x = 0f;
                Animation<TextureRegion> turnAnimation = assets.getAnimation(MossflyAnimationType.TURN_TO_FLY);
                if(turnAnimation.isAnimationFinished(stateTime)) {
                    enterState(State.IDLE);
                }
                break;

            case IDLE:
                if (knockbackCooldown >= 0) {
                    knockbackCooldown -= deltaTime;
                    break;
                }

                facingRight = velocity.x > 0;

                if(detectedKnight) {
                    enterState(State.CHASING);
                    break;
                }

                idleDistanceGone += Math.abs(velocity.x * deltaTime);
                if(idleDistanceGone >= IDLE_BACK_AND_FORTH_DISTANCE) {
                    idleDistanceGone = 0f;
                    velocity.x *= -1;
                }

                velocity.y = (stateTime <= IDLE_UPWARD_VELOCITY_DURATION) ? SPEED : 0f;
                break;

            case CHASING:
                if (knockbackCooldown >= 0) {
                    knockbackCooldown -= deltaTime;
                    break;
                }

                facingRight = velocity.x > 0;

                if(!detectedKnight) {
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

        }
    }

    private void applyPhysics(float delta) {
        // Apply gravity if it's dead or hiding
        if(state == State.DYING || state == State.DEAD || state == State.HIDING) {
            velocity.y -= GRAVITY * delta;
            if (velocity.y < -MAX_FALL_SPEED) {
                velocity.y = -MAX_FALL_SPEED;
            }
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case HIDING: return assets.getAnimation(MossflyAnimationType.SHAKE);
            case APPEARING: return assets.getAnimation(MossflyAnimationType.APPEAR);
            case TURN_TO_FLY: return assets.getAnimation(MossflyAnimationType.TURN_TO_FLY);
            case IDLE, CHASING: return assets.getAnimation(MossflyAnimationType.FLY);
            case DYING, DEAD: return getDeathAnimation();
            default: return null;
        }
    }

    private Animation<TextureRegion> getDeathAnimation() {
        return assets.getAnimation(airborneDeath ? MossflyAnimationType.DEATH_AIR : MossflyAnimationType.DEATH_LAND);
    }

    // --- Inner type ---

    public enum State {
        HIDING,
        APPEARING,
        TURN_TO_FLY,
        IDLE,
        CHASING,
        DYING,
        DEAD
    }
}
