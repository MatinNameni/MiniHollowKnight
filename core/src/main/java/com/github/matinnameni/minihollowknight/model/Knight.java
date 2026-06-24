package com.github.matinnameni.minihollowknight.model;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.event.EventBus;
import com.github.matinnameni.minihollowknight.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.asset.KnightAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.AttackDirection;
import com.github.matinnameni.minihollowknight.model.enums.KnightAnimationType;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;

/** The player-controlled Knight entity. */
public class Knight implements Entity {
    // --- Constants ---

    // Knight size
    public static final float WIDTH = 200f;
    public static final float HEIGHT = 100f;

     // Knight hitbox
    public static final float HITBOX_WIDTH = 30f;
    public static final float HITBOX_HEIGHT = 50f;

    // Hitbox offset
    public static final float HITBOX_X_OFFSET = (WIDTH - HITBOX_WIDTH) / 2f;
    public static final float HITBOX_Y_OFFSET = 5f;

    // Movement
    public static final float MOVE_SPEED = 200f;
    public static final float JUMP_INITIAL_VELOCITY = 420f;
    public static final float DOUBLE_JUMP_INITIAL_VELOCITY = 400f;
    public static final float WALL_JUMP_INITIAL_VELOCITY = 80f;
    public static final float GRAVITY = 980f;
    public static final float MAX_FALL_SPEED = 600f;

    // Dash
    public static final float DASH_SPEED = 500f;
    public static final float DASH_DURATION = 0.25f;
    public static final float DASH_COOLDOWN = 0.5f;

    // Attack
    public static final float ATTACK_DURATION = 0.30f;
    public static final float ATTACK_HITBOX_WIDTH = 60f;
    public static final float ATTACK_HITBOX_HEIGHT = 60f;
    public static final float ATTACK_HITBOX_RANGE = 70f;

    // Focus / Heal
    public static final float FOCUS_CHANNEL_TIME = 1f;
    public static final int FOCUS_SOUL_COST = 33;

    // Invincibility
    public static final float INVINCIBILITY_DURATION = 1.3f;
    public static final float KNOCKBACK_VELOCITY = 300f;

    // Spells
    private static final float VENGEFUL_SPIRIT_DURATION = KnightAnimationType.FIREBALL_CAST.getDuration();
    private static final float VENGEFUL_SPIRIT_COOLDOWN = 0.7f;

    private static final float HOWLING_WRAITHS_DURATION = KnightAnimationType.SCREAM.getDuration();
    private static final float HOWLING_WRAITHS_COOLDOWN = 0.7f;

    // --- State ---
    private KnightState state = KnightState.IDLE;
    private float stateTime = 0f;

    // --- Position ---
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private boolean grounded  = false;
    private boolean hittingWall = false;
    private boolean facingRight = true;
    private boolean canDoubleJump = true;
    private boolean canDash = true;

    // --- Combat ---
    private AttackDirection attackDirection = AttackDirection.RIGHT;
    private float attackCooldownTimer = 0f;
    private float attackTimer = 0f;

    // --- Dash ---
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;

    // --- Focus / Heal ---
    private float focusTimer = 0f;

    // --- Health ---
    private int masks;
    private int maxMasks;
    private int soul;

    // --- Invincibility ---
    private float invincibilityTimer = 0f;

    // --- Spells ---
    private float vengefulSpiritTimer = 0f;
    private float vengefulSpiritCooldownTimer = 0f;

    private float howlingWraithsTimer = 0f;
    private float howlingWraithsCooldownTimer = 0f;

    // --- Dependencies ---
    private final KnightAssetBundle assets;
    private final Settings settings;

    public Knight(KnightAssetBundle assets, Settings settings) {
        this.assets = assets;
        this.settings = settings;
    }

    // --- Entity ---

    @Override
    public void update(float deltaTime) {
        updateTimers(deltaTime);

        if (state == KnightState.DEAD) {
            stateTime += deltaTime;
            return;
        }

        // Input is only processed when not locked by dash / hit
        boolean inputLocked = (state == KnightState.DASHING)
            || (state == KnightState.HIT);

        if (!inputLocked) {
            handleInput(deltaTime);
        }

        updateState(deltaTime);
        applyPhysics(deltaTime);
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> animation = getCurrentAnimation();
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(stateTime);

        // Blink effect during invincibility frames
        if (invincibilityTimer > 0f && shouldSkipRenderFrame()) {
            return;
        }

        int facingRightScaleX = -1;
        if(state == KnightState.WALL_JUMP) {
            facingRightScaleX = 1;
        }

        batch.draw(frame,
            position.x, position.y,
            WIDTH / 2f, 0,
            WIDTH, HEIGHT,
            facingRight ? facingRightScaleX : -facingRightScaleX, 1, 0);
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(position.x + HITBOX_X_OFFSET, position.y + HITBOX_Y_OFFSET,
                                HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    // --- Game data load/save ---

    /**
     * Loads the Knight's data from a {@link GameData}.
     */
    public void initializeFromSave(GameData data) {
        position.set(data.playerX, data.playerY);
        this.masks = data.masks;
        this.maxMasks = data.maxMasks;
        this.soul = data.soul;

        // Reset runtime state
        state = KnightState.IDLE;
        stateTime = 0f;
        velocity.setZero();
        invincibilityTimer = 0f;
        dashCooldownTimer = 0f;
        canDoubleJump = true;
    }

    /**
     * Writes the Knight's current data back into a {@link GameData}.
     */
    public void writeToSave(GameData data) {
        data.playerX = position.x;
        data.playerY = position.y;
        data.masks = masks;
        data.maxMasks = maxMasks;
        data.soul = soul;
    }

    // --- Timer ---

    private void updateTimers(float delta) {
        if (invincibilityTimer > 0f) {
            invincibilityTimer = Math.max(0f, invincibilityTimer - delta);
        }
        if (dashCooldownTimer > 0f) {
            dashCooldownTimer = Math.max(0f, dashCooldownTimer - delta);
        }
        if(attackCooldownTimer > 0f) {
            attackCooldownTimer = Math.max(0f, attackCooldownTimer - delta);
        }
        if(vengefulSpiritCooldownTimer > 0f) {
            vengefulSpiritCooldownTimer = Math.max(0f, vengefulSpiritCooldownTimer - delta);
        }
        if(howlingWraithsCooldownTimer > 0f) {
            howlingWraithsCooldownTimer = Math.max(0f, howlingWraithsCooldownTimer - delta);
        }
    }

    // --- Input Handler ---

    private void handleInput(float deltaTime) {
        // Focus
        if(Gdx.input.isKeyPressed(settings.getKeyFocus()) && canFocus()) {
            if (state != KnightState.FOCUSING) {
                enterState(KnightState.FOCUSING);
                focusTimer = 0f;
                velocity.x = 0f;
                EventBus.getInstance().publish(GameEvent.PLAYER_FOCUS_START);
            }
            focusTimer += deltaTime;
            if (focusTimer >= FOCUS_CHANNEL_TIME) {
                completeFocus();
            }
            return; // blocking other inputs
        }

        // Released focus key while focusing
        if(state == KnightState.FOCUSING) {
            cancelFocus();
        }

        // Quick cast
        // TODO: quick cast and up buttons are harcoded here, add it to settings key bindings
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                if(canCastHowlingWraiths()) {
                    castHowlingWraiths();
                }

            } else if (canCastVengefulSpirit()) {
                castVengefulSpirit();
            }
        }

        // Attack
        if(Gdx.input.isKeyJustPressed(settings.getKeyAttack()) && canAttack()) {
            startAttack();
            return;
        }

        // Dash
        if (Gdx.input.isKeyJustPressed(settings.getKeyDash()) && canDash()) {
            if(!grounded && !hittingWall) { canDash = false; }
            startDash();
            return;
        }

        // Jump
        if (Gdx.input.isKeyJustPressed(settings.getKeyJump())) {
            if (grounded) {
                velocity.y = JUMP_INITIAL_VELOCITY;
                grounded = false;
                canDoubleJump = true;
                enterState(KnightState.JUMPING);
            } else if(hittingWall) {
                velocity.y = JUMP_INITIAL_VELOCITY;
                velocity.x = facingRight ? -WALL_JUMP_INITIAL_VELOCITY : WALL_JUMP_INITIAL_VELOCITY;
                hittingWall = false;
                canDoubleJump = true;
                enterState(KnightState.WALL_JUMP);
            } else if (canDoubleJump) {
                velocity.y = DOUBLE_JUMP_INITIAL_VELOCITY;
                canDoubleJump = false;
                EventBus.getInstance().publish(GameEvent.PLAYER_DOUBLE_JUMP);
                enterState(KnightState.DOUBLE_JUMPING);
            }
        }

        // movement
        if (state != KnightState.WALL_JUMP) {
            float moveInput = 0f;
            if (Gdx.input.isKeyPressed(settings.getKeyLeft()))  moveInput -= 1f;
            if (Gdx.input.isKeyPressed(settings.getKeyRight())) moveInput += 1f;

            velocity.x = moveInput * MOVE_SPEED;

            if (moveInput != 0f) {
                facingRight = moveInput > 0f;
                if (grounded && state != KnightState.RUNNING) {
                    enterState(KnightState.RUNNING);
                }
            } else if (grounded && state == KnightState.RUNNING) {
                enterState(KnightState.IDLE);
            }
        }

        // Fell off a ledge
        if (!grounded && state == KnightState.RUNNING) {
            enterState(KnightState.FALLING);
        }
    }

    // --- Focus ---

    private boolean canFocus() {
        return grounded
            && state != KnightState.ATTACKING
            && state != KnightState.DASHING
            && state != KnightState.HIT
            && soul >= FOCUS_SOUL_COST;
    }

    private void completeFocus() {
        if (soul >= FOCUS_SOUL_COST) {
            soul -= FOCUS_SOUL_COST;
            if(masks < maxMasks) { masks++; }
            EventBus.getInstance().publish(GameEvent.PLAYER_SOUL_SPENT, FOCUS_SOUL_COST);
            EventBus.getInstance().publish(GameEvent.PLAYER_HEALED);
            EventBus.getInstance().publish(GameEvent.PLAYER_FOCUS_COMPLETE);
        }
        focusTimer = 0f;
        enterState(KnightState.IDLE);
    }

    private void cancelFocus() {
        focusTimer = 0f;
        EventBus.getInstance().publish(GameEvent.PLAYER_FOCUS_CANCEL);
        enterState(KnightState.IDLE);
    }

    // --- Attack ---

    private boolean canAttack() {
        return attackCooldownTimer <= 0 &&
            state != KnightState.FOCUSING &&
            state != KnightState.DASHING;
    }

    private void startAttack() {
        attackCooldownTimer = ATTACK_DURATION;
        attackDirection = resolveAttackDirection();
        enterState(KnightState.ATTACKING);
    }

    private AttackDirection resolveAttackDirection() {
        // TODO: up and down key is hard coded here, add them to settings keys
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  return AttackDirection.DOWN;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) return AttackDirection.UP;
        return facingRight ? AttackDirection.RIGHT : AttackDirection.LEFT;
    }

    // --- Dash ---

    private boolean canDash() {
        return canDash
            && dashCooldownTimer <= 0f
            && state != KnightState.FOCUSING
            && state != KnightState.ATTACKING;
    }

    private void startDash() {
        dashCooldownTimer = DASH_COOLDOWN;
        velocity.y = 0f;
        velocity.x = (facingRight ? 1f : -1f) * DASH_SPEED;
        enterState(KnightState.DASHING);
        EventBus.getInstance().publish(GameEvent.PLAYER_DASH);
    }

    // --- Spells ---

    private boolean canCastHowlingWraiths() {
        return howlingWraithsCooldownTimer <= 0;
    }

    private void castHowlingWraiths() {
        howlingWraithsCooldownTimer = HOWLING_WRAITHS_COOLDOWN;
        enterState(KnightState.HOWLING_WRAITHS);
    }

    private boolean canCastVengefulSpirit() {
        return vengefulSpiritCooldownTimer <= 0;
    }

    private void castVengefulSpirit() {
        vengefulSpiritCooldownTimer = VENGEFUL_SPIRIT_COOLDOWN;
        enterState(KnightState.VENGEFUL_SPIRIT);
    }

    // --- State ---

    private void enterState(KnightState newState) {
        this.state = newState;
        this.stateTime = 0f;
    }

    private void updateState(float deltaTime) {
        stateTime += deltaTime;

        switch (state) {

            case ATTACKING:
                attackTimer += deltaTime;
                if (attackTimer >= ATTACK_DURATION) {
                    attackTimer = 0f;
                    enterState(resolvePostActionState());
                }
                break;

            case DASHING:
                dashTimer += deltaTime;
                if (dashTimer >= DASH_DURATION) {
                    dashTimer = 0f;
                    velocity.x = 0f;
                    enterState(grounded ? KnightState.IDLE : KnightState.FALLING);
                }
                break;

            case HIT:
                if (invincibilityTimer <= 0f) {
                    enterState(resolvePostActionState());
                }
                break;

            case JUMPING:
                if (velocity.y <= 0f) {
                    enterState(KnightState.FALLING);
                }
                break;

            case DOUBLE_JUMPING:
                if (velocity.y <= 0f) {
                    enterState(KnightState.FALLING);
                }
                break;

            case WALL_JUMP:
                if (velocity.y <= 0f) {
                    enterState(KnightState.FALLING);
                }
                break;

            case WALL_SLIDE:
                if (grounded) {
                    enterState(KnightState.IDLE);
                } else if (!hittingWall) {     // ← add this
                    enterState(KnightState.FALLING);
                }
                break;

            case VENGEFUL_SPIRIT:
                vengefulSpiritTimer += deltaTime;
                if(vengefulSpiritTimer >= VENGEFUL_SPIRIT_DURATION) {
                    enterState(KnightState.IDLE);
                    vengefulSpiritTimer = 0f;
                }
                break;

            case HOWLING_WRAITHS:
                howlingWraithsTimer += deltaTime;
                if(howlingWraithsTimer >= HOWLING_WRAITHS_DURATION) {
                    enterState(KnightState.IDLE);
                    howlingWraithsTimer = 0f;
                }
                break;

            default:
                break;
        }
    }

    private KnightState resolvePostActionState() {
        if (!grounded && !hittingWall) {
            return velocity.y > 0f ? KnightState.JUMPING : KnightState.FALLING;
        } else if(!grounded && hittingWall) {
            return KnightState.WALL_SLIDE;
        }
        return velocity.x != 0f ? KnightState.RUNNING : KnightState.IDLE;
    }

    // --- Physics ---

    private void applyPhysics(float delta) {
        // Gravity - disabled during dash and focus
        if (state != KnightState.DASHING && state != KnightState.FOCUSING) {
            velocity.y -= GRAVITY * delta;
            if (velocity.y < -MAX_FALL_SPEED) {
                velocity.y = -MAX_FALL_SPEED;
            }
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Reset ait abilities
        if (grounded) {
            canDoubleJump = true;
            canDash = true;
        }

        if (grounded && (state == KnightState.FALLING || state == KnightState.JUMPING)) {
            enterState(velocity.x != 0f ? KnightState.RUNNING : KnightState.IDLE);
        } else if ((hittingWall && (state == KnightState.FALLING || state == KnightState.JUMPING))) {
            canDoubleJump = true;
            canDash = true;
            enterState(KnightState.WALL_SLIDE);
        }
    }

    public Rectangle getAttackHitbox() {
        if (state != KnightState.ATTACKING) { return null; }

        float centerX = getBounds().x + getBounds().width / 2f;
        float centerY = getBounds().y + getBounds().height / 2f;

        switch (attackDirection) {
            case LEFT:
                return new Rectangle(
                    centerX - ATTACK_HITBOX_RANGE,
                    centerY - ATTACK_HITBOX_HEIGHT / 2f,
                    ATTACK_HITBOX_RANGE, ATTACK_HITBOX_HEIGHT);

            case RIGHT:
                return new Rectangle(
                    centerX,
                    centerY - ATTACK_HITBOX_HEIGHT / 2f,
                    ATTACK_HITBOX_RANGE, ATTACK_HITBOX_HEIGHT);

            case UP:
                return new Rectangle(
                    centerX - ATTACK_HITBOX_WIDTH / 2f,
                    centerY,
                    ATTACK_HITBOX_WIDTH, ATTACK_HITBOX_RANGE);

            case DOWN:
                return new Rectangle(
                    centerX - ATTACK_HITBOX_WIDTH / 2f,
                    centerY - ATTACK_HITBOX_RANGE,
                    ATTACK_HITBOX_WIDTH, ATTACK_HITBOX_RANGE);
        }
        return null;
    }

    // --- Nail ---

    public void onNailHit(Object enemy) {
        EventBus.getInstance().publish(GameEvent.PLAYER_NAIL_HIT, enemy);
    }

    public void onDownAttackBounce() {
        velocity.y = DOUBLE_JUMP_INITIAL_VELOCITY * 0.7f;
        grounded = false;
        enterState(KnightState.JUMPING);
        canDoubleJump = true;
    }

    // --- Take damage ---

    public void takeDamage(float knockbackDirection) {
        if (invincibilityTimer > 0f || state == KnightState.DEAD) return;

        masks--;
        invincibilityTimer = INVINCIBILITY_DURATION;

        // Cancel any ongoing action
        if (state == KnightState.FOCUSING) {
            EventBus.getInstance().publish(GameEvent.PLAYER_FOCUS_CANCEL);
            focusTimer = 0f;
        }

        // Knockback
        velocity.x = knockbackDirection * KNOCKBACK_VELOCITY;
        velocity.y = JUMP_INITIAL_VELOCITY * 0.5f;
        grounded = false;

        enterState(KnightState.HIT);
        EventBus.getInstance().publish(GameEvent.PLAYER_DAMAGED);

        if (masks <= 0) {
            masks = 0;
            enterState(KnightState.DEAD);
            EventBus.getInstance().publish(GameEvent.PLAYER_DIED);
        }
    }

    // --- Soul ---

    public void gainSoul(int amount) {
        soul = Math.min(soul + amount, GameData.MAX_SOUL);
        EventBus.getInstance().publish(GameEvent.PLAYER_SOUL_GAINED, amount);
    }

    // --- Render ---

    /** Helper method for the blinking effect that plays when the knight gets hit */
    private boolean shouldSkipRenderFrame() {
        return ((int) (invincibilityTimer * 8f) % 2) == 0;
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case IDLE: return assets.getAnimation(KnightAnimationType.IDLE);
            case RUNNING: return assets.getAnimation(KnightAnimationType.RUN);
            case JUMPING: return assets.getAnimation(KnightAnimationType.AIR_BORNE);
            case DOUBLE_JUMPING: return assets.getAnimation(KnightAnimationType.DOUBLE_JUMP);
            case FALLING: return assets.getAnimation(KnightAnimationType.FALL);
            case WALL_SLIDE: return assets.getAnimation(KnightAnimationType.WALL_SLIDE);
            case WALL_JUMP: return assets.getAnimation(KnightAnimationType.WALL_JUMP);
            case DASHING: return assets.getAnimation(KnightAnimationType.DASH);
            case FOCUSING: return assets.getAnimation(KnightAnimationType.FOCUS);
            case HIT: return assets.getAnimation(KnightAnimationType.IDLE);
            case VENGEFUL_SPIRIT: return assets.getAnimation(KnightAnimationType.FIREBALL_CAST);
            case HOWLING_WRAITHS: return assets.getAnimation(KnightAnimationType.SCREAM);
            case DEAD: return assets.getAnimation(KnightAnimationType.DEATH);
            case ATTACKING:
                switch (attackDirection) {
                    case UP: return assets.getAnimation(KnightAnimationType.UP_SLASH);
                    case DOWN: return assets.getAnimation(KnightAnimationType.DOWN_SLASH);
                    default: return assets.getAnimation(KnightAnimationType.SLASH);
                }
            default: return assets.getAnimation(KnightAnimationType.IDLE);
        }
    }

    // --- External collision hooks ---

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public void setHittingWall(boolean hittingWall) {
        this.hittingWall = hittingWall;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public void onWallCollision(float pushX) {
        position.x = pushX;
        velocity.x = 0f;
    }

    public void onCeilingCollision(float pushY) {
        position.y = pushY;
        if (velocity.y > 0f) {
            velocity.y = 0f;
        }
    }

    // --- Getters ---

    public KnightState getState() {
        return state;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0f;
    }

    public boolean isDead() {
        return state == KnightState.DEAD;
    }

    public boolean isAttacking() {
        return state == KnightState.ATTACKING;
    }

    public boolean isDashing() {
        return state == KnightState.DASHING;
    }

    public boolean isFocusing() {
        return state == KnightState.FOCUSING;
    }

    public int getMasks() {
        return masks;
    }

    public int getMaxMasks() {
        return maxMasks;
    }

    public int getSoul() {
        return soul;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }

    public AttackDirection getAttackDirection() {
        return attackDirection;
    }
}
