package com.github.matinnameni.minihollowknight.model.entity.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.asset.ZoteAssetBundle;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.enemy.ZoteAnimationType;

/**
 * Zote the Mighty.
 */
public class Zote extends Enemy {

    // --- Constants ---

    // Size
    public static final float WIDTH = 200f;
    public static final float HEIGHT = 110f;

    // Movement
    public static final float GRAVITY = Knight.GRAVITY;
    public static final float MAX_FALL_SPEED = 400f;
    public static final float SPEED = 200f;

    // Hitbox
    public static final float HITBOX_WIDTH = 50f;
    public static final float HITBOX_HEIGHT = 60f;
    public static final float HITBOX_X_OFFSET = (WIDTH - HITBOX_WIDTH) / 2f;
    public static final float HITBOX_Y_OFFSET = 5f;

    // Health (Zote never dies)
    public static final float MAX_HEALTH = Float.POSITIVE_INFINITY;

    // Anger timing
    public static final float ANGER_DURATION = 2.0f;
    public static final float ATTACK_DURATION = ZoteAnimationType.ATTACK.getDuration();
    public static final float FALL_DURATION = ZoteAnimationType.FALL.getDuration();
    public static final float GET_UP_DURATION = ZoteAnimationType.GET_UP.getDuration();

    // --- State ---

    private State state = State.IDLE;
    private Direction facingDirection = Direction.RIGHT;
    private float stateTime = 0f;
    private float angerTimer = 0f;
    private boolean grounded = false;

    /** True while the dialogue overlay is open. */
    private boolean talking = false;

    private final ZoteAssetBundle assets;

    public Zote(float x, float y, ZoteAssetBundle assets) {
        super(x, y);
        this.assets = assets;
        this.health = MAX_HEALTH;
    }

    // --- Main loop ---

    @Override
    public void update(float deltaTime) {
        stateTime += deltaTime;

        switch (state) {
            case IDLE:
                velocity.x = 0;
                break;

            case ATTACK:
                velocity.x = (facingDirection == Direction.RIGHT) ?
                    SPEED :
                    -SPEED;
                if (stateTime >= ATTACK_DURATION) {
                    enterState(State.FALL);
                }
                break;

            case FALL:
                velocity.x = 0;
                if (stateTime >= FALL_DURATION && Math.abs(velocity.y) <= 0.1f) {
                    enterState(State.GET_UP);
                }
                break;

            case GET_UP:
                velocity.x = 0;
                if (stateTime >= GET_UP_DURATION) {
                    enterState(State.IDLE);
                }
                break;

            case TURN:
                if (stateTime >= ZoteAnimationType.TURN.getDuration()) {
                    enterState(State.IDLE);
                }
                break;
        }

        if (angerTimer > 0f) {
            angerTimer -= deltaTime;
        }

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

    // --- Physics ---

    private void applyPhysics(float delta) {
        // Gravity
        velocity.y -= GRAVITY * delta;
        if (velocity.y < -MAX_FALL_SPEED) {
            velocity.y = -MAX_FALL_SPEED;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
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
    }

    // --- Combat ---

    @Override
    public void takeDamage(float damage, Direction knockbackDirection) {
        if (state == State.ATTACK || state == State.FALL || state == State.GET_UP) {
            return;
        }

        if (knockbackDirection == Direction.LEFT) {
            facingDirection = Direction.RIGHT;
        } else if (knockbackDirection == Direction.RIGHT) {
            facingDirection = Direction.LEFT;
        }

        angerTimer = ANGER_DURATION;
        enterState(State.ATTACK);
    }

    @Override
    public boolean canDamagePlayer() {
        return false;
    }

    /** Zote is invincible. */
    @Override
    public boolean isDead() {
        return false;
    }

    @Override
    public void kill() {
        // no-op - Zote is eternal.
    }

    // --- Dialogue hooks ---

    /** Called by the controller when the dialogue box opens. */
    public void startTalking() {
        talking = true;
    }

    /** Called by the controller when the dialogue box closes. */
    public void stopTalking() {
        talking = false;
    }

    /** Faces Zote toward the player's position. */
    public void faceTowards(float playerX) {
        Direction wanted = (playerX >= getBounds().x) ? Direction.RIGHT : Direction.LEFT;
        if (wanted != facingDirection) {
            if (state == State.IDLE && !talking) {
                enterState(State.TURN);
            }
            facingDirection = wanted;
        }
    }

    // --- Getters ---

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public State getState() {
        return state;
    }

    public boolean isTalking() {
        return talking;
    }

    public boolean isAngry() {
        return angerTimer > 0f;
    }

    public float getStateTime() {
        return stateTime;
    }

    // --- Helpers ---

    private void enterState(State newState) {
        this.state = newState;
        this.stateTime = 0f;
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        if (talking && state == State.IDLE) {
            return assets.getAnimation(ZoteAnimationType.TALK);
        }
        switch (state) {
            case IDLE: return assets.getAnimation(ZoteAnimationType.IDLE);
            case ATTACK: return assets.getAnimation(ZoteAnimationType.ATTACK);
            case FALL: return assets.getAnimation(ZoteAnimationType.FALL);
            case GET_UP: return assets.getAnimation(ZoteAnimationType.GET_UP);
            case TURN: return assets.getAnimation(ZoteAnimationType.TURN);
            default: return assets.getAnimation(ZoteAnimationType.IDLE);
        }
    }

    // --- Inner type ---

    public enum State {
        IDLE,
        ATTACK,
        FALL,
        GET_UP,
        TURN
    }
}
