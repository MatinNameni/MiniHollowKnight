package com.github.matinnameni.minihollowknight.model;

import com.badlogic.gdx.Gdx;
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
    public static final float JUMP_INITIAL_VELOCITY = 600f;
    public static final float DOUBLE_JUMP_INITIAL_VELOCITY = 400f;
    public static final float WALL_JUMP_INITIAL_VELOCITY = 80f;
    public static final float GRAVITY = 980f;
    public static final float MAX_FALL_SPEED = 600f;
    public static final float JUMP_CUT_MULTIPLIER = 0.5f;

    // Dash
    public static final float DASH_SPEED = 600f;
    public static final float DASH_DURATION = 0.25f;
    public static final float DASH_COOLDOWN = 0.5f;
    public static final float DASH_EFFECT_Y_OFFSET = 25f;
    public static final float DASH_EFFECT_X_OFFSET = 10f;

    // Attack
    public static final float SLASH_DAMAGE = 40f;
    public static final float ATTACK_DURATION = 0.30f;
    public static final float ATTACK_HITBOX_WIDTH = 80f;
    public static final float ATTACK_HITBOX_HEIGHT = 50f;
    public static final float ATTACK_HITBOX_RANGE = 100f;

    // Focus / Heal
    public static final float FOCUS_CHANNEL_TIME = 1.5f;
    public static final float FOCUS_SOUL_COST = 33f;
    public static final float FOCUS_DISABLED_COOLDOWN = 0.7f;

    // Invincibility
    public static final float INVINCIBILITY_DURATION = 1f;
    public static final float KNOCKBACK_VELOCITY = 100f;
    private static final float HIT_FREEZE_COOLDOWN = 0.4f;

    // Spells
    public static final float VENGEFUL_SPIRIT_DAMAGE_PER_FRAME = 1f;
    public static final float VENGEFUL_SPIRIT_DURATION = KnightAnimationType.FIREBALL_CAST.getDuration();
    public static final float VENGEFUL_SPIRIT_COOLDOWN = 1f;
    public static final float VENGEFUL_SPIRIT_SOUL_COST = 33f;
    public static final float VENGEFUL_SPIRIT_OFFSET = 40f;

    public static final float HOWLING_WRAITHS_DAMAGE_PER_FRAME = 0.7f;
    public static final float HOWLING_WRAITHS_DURATION = KnightAnimationType.SOUL_SCREAM.getDuration();
    public static final float HOWLING_WRAITHS_COOLDOWN = 0.7f;
    public static final float HOWLING_WRAITHS_SOUL_COST = 33f;
    public static final float HOWLING_WRAITHS_OFFSET = 120f;

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
    private boolean jumpKeyHeld = false;

    // --- Combat ---
    private Direction attackDirection = Direction.RIGHT;
    private float attackCooldownTimer = 0f;
    private float attackTimer = 0f;

    // --- Dash ---
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;

    // --- Focus / Heal ---
    private float focusTimer = 0f;
    private float focusCooldownTimer = 0f;
    private float consumedSoul = 0f;

    // --- Health ---
    private int masks;
    private int maxMasks;
    private float soul;

    // --- Invincibility ---
    private float invincibilityTimer = 0f;
    private float hitFreezeCooldownTimer = 0f;

    // --- Spells ---
    private float vengefulSpiritTimer = 0f;
    private float vengefulSpiritCooldownTimer = 0f;

    private float howlingWraithsTimer = 0f;
    private float howlingWraithsCooldownTimer = 0f;

    // --- Dependencies ---
    private final KnightAssetBundle assets;
    private final Settings settings;

    // --- Safe Spots ---
    private Vector2 lastSafePosition = new Vector2();

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

        boolean inputLocked = (state == KnightState.DASHING)
            || (state == KnightState.HIT)
            || (state == KnightState.VENGEFUL_SPIRIT)
            || (state == KnightState.HOWLING_WRAITHS);

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

        batch.draw(frame,
            position.x, position.y,
            WIDTH / 2f, 0,
            WIDTH, HEIGHT,
            facingRight ? -1 : 1, 1, 0);
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
        hitFreezeCooldownTimer = 0f;
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
        if(hitFreezeCooldownTimer > 0f) {
            hitFreezeCooldownTimer = Math.max(0f, hitFreezeCooldownTimer - delta);
        }
        if(focusCooldownTimer > 0f) {
            focusCooldownTimer = Math.max(0f, focusCooldownTimer - delta);
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
                velocity.y = 0f;
                EventBus.getInstance().publish(GameEvent.PLAYER_FOCUS_START);
            }
            return; // blocking other inputs
        }

        // Released focus key while focusing
        if(state == KnightState.FOCUSING) {
            cancelFocus();
        }

        // Quick cast
        if(Gdx.input.isKeyJustPressed(settings.getKeyCast())) {
            if (Gdx.input.isKeyPressed(settings.getKeyUp())) {
                if(canCastHowlingWraiths()) {
                    castHowlingWraiths();
                }

            } else if (canCastVengefulSpirit()) {
                castVengefulSpirit();
            }
            return; // blocking other inputs
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
                jumpKeyHeld = true;
                enterState(KnightState.JUMPING);
            } else if(hittingWall) {
                velocity.y = JUMP_INITIAL_VELOCITY;
                velocity.x = facingRight ? -WALL_JUMP_INITIAL_VELOCITY : WALL_JUMP_INITIAL_VELOCITY;
                hittingWall = false;
                facingRight = !facingRight;
                canDoubleJump = true;
                jumpKeyHeld = true;
                enterState(KnightState.WALL_JUMP);
            } else if (canDoubleJump) {
                velocity.y = DOUBLE_JUMP_INITIAL_VELOCITY;
                canDoubleJump = false;
                EventBus.getInstance().publish(GameEvent.PLAYER_DOUBLE_JUMP);
                enterState(KnightState.DOUBLE_JUMPING);
            }
        }

        if (jumpKeyHeld && !Gdx.input.isKeyPressed(settings.getKeyJump())) {
            jumpKeyHeld = false;
            if (velocity.y > 0f &&
                (state == KnightState.JUMPING || state == KnightState.WALL_JUMP)) {
                velocity.y *= JUMP_CUT_MULTIPLIER;
            }
        }

        // movement
        if (state != KnightState.WALL_JUMP) {
            if (state == KnightState.ATTACKING) {
                if (grounded) {
                    // root the knight on the ground during the slash
                    velocity.x = 0f;
                } else {
                    float moveInput = 0f;
                    if (Gdx.input.isKeyPressed(settings.getKeyLeft()))  moveInput -= 1f;
                    if (Gdx.input.isKeyPressed(settings.getKeyRight())) moveInput += 1f;
                    velocity.x = moveInput * MOVE_SPEED;
                }
            } else {
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
        }
    }

    // --- Focus ---

    private boolean canFocus() {
        return grounded
            && state != KnightState.ATTACKING
            && state != KnightState.DASHING
            && state != KnightState.HIT
            && soul > 0f
            && focusCooldownTimer <= 0f;
    }

    private void completeFocus() {
        if(masks < maxMasks) { masks++; }
        EventBus.getInstance().publish(GameEvent.PLAYER_SOUL_SPENT, FOCUS_SOUL_COST);
        EventBus.getInstance().publish(GameEvent.PLAYER_HEALED);
        EventBus.getInstance().publish(GameEvent.PLAYER_FOCUS_COMPLETE);
        focusTimer = 0f;
        focusCooldownTimer = FOCUS_DISABLED_COOLDOWN;
        consumedSoul = 0f;
        enterState(KnightState.IDLE);
    }

    private void cancelFocus() {
        focusTimer = 0f;
        consumedSoul = 0f;
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
        attackTimer = 0f;
        attackDirection = resolveAttackDirection();
        enterState(KnightState.ATTACKING);
    }

    private Direction resolveAttackDirection() {
        if (Gdx.input.isKeyPressed(settings.getKeyDown()) && !grounded) { return Direction.DOWN; }
        if (Gdx.input.isKeyPressed(settings.getKeyUp())) { return Direction.UP; }
        return facingRight ? Direction.RIGHT : Direction.LEFT;
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
        return howlingWraithsCooldownTimer <= 0
            && soul >= HOWLING_WRAITHS_SOUL_COST;
    }

    private void castHowlingWraiths() {
        howlingWraithsCooldownTimer = HOWLING_WRAITHS_COOLDOWN;
        enterState(KnightState.HOWLING_WRAITHS);
        velocity.x = 0;
        velocity.y = 0;
        spendSoul(HOWLING_WRAITHS_SOUL_COST);

        float spawnX = position.x + WIDTH / 2f;
        float spawnY = position.y + HEIGHT / 2f + HOWLING_WRAITHS_OFFSET;

        EventBus.getInstance().publish(GameEvent.PLAYER_HOWLING_WRAITHS_CAST,
            new HowlingWraiths.SpawnInfo(spawnX, spawnY, assets));
    }

    private boolean canCastVengefulSpirit() {
        return vengefulSpiritCooldownTimer <= 0
            && soul >= VENGEFUL_SPIRIT_SOUL_COST;
    }

    private void castVengefulSpirit() {
        vengefulSpiritCooldownTimer = VENGEFUL_SPIRIT_COOLDOWN;
        enterState(KnightState.VENGEFUL_SPIRIT);
        velocity.x = 0;
        velocity.y = 0;
        spendSoul(VENGEFUL_SPIRIT_SOUL_COST);

        float spawnX = position.x + WIDTH / 2f +
            (facingRight ? VENGEFUL_SPIRIT_OFFSET : -VENGEFUL_SPIRIT_OFFSET);
        float spawnY = position.y + HEIGHT / 2f;
        Direction fireDir = facingRight ? Direction.RIGHT : Direction.LEFT;

        EventBus.getInstance().publish(GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST,
            new VengefulSpirit.SpawnInfo(spawnX, spawnY, fireDir, assets));
    }

    // --- State ---

    private void enterState(KnightState newState) {
        this.state = newState;
        this.stateTime = 0f;
    }

    private void updateState(float deltaTime) {
        stateTime += deltaTime;

        switch (state) {

            case IDLE:
            case RUNNING:
                if(!grounded) {
                    enterState(KnightState.FALLING);
                }

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
                if (hitFreezeCooldownTimer <= 0f) {
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
                } else if (!hittingWall) {
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

            case FOCUSING:
                float ratio = deltaTime / FOCUS_CHANNEL_TIME;
                float consumedThisFrame = FOCUS_SOUL_COST * ratio;
                consumedSoul += consumedThisFrame;
                float remainingSoul = FOCUS_SOUL_COST - consumedSoul;
                consumedThisFrame = Math.min(consumedThisFrame, remainingSoul);
                spendSoul(consumedThisFrame);
                if (!canFocus()) {
                    cancelFocus();
                    break;
                }
                focusTimer += deltaTime;
                if (focusTimer >= FOCUS_CHANNEL_TIME) {
                    completeFocus();
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
        // Gravity - disabled during dash, focus and spells
        if (state != KnightState.DASHING && state != KnightState.FOCUSING &&
            state != KnightState.VENGEFUL_SPIRIT && state != KnightState.HOWLING_WRAITHS) {
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

    public void onNailHit(Enemy enemy) {
        EventBus.getInstance().publish(GameEvent.PLAYER_NAIL_HIT, enemy);
    }

    public void onDownAttackBounce() {
        if(attackDirection != Direction.DOWN) {
            return;
        }

        velocity.y = DOUBLE_JUMP_INITIAL_VELOCITY * 0.7f;
        grounded = false;
        canDoubleJump = true;
        canDash = true;
    }

    // --- Take damage ---

    public void takeDamage(Direction knockbackDirection) {
        if (invincibilityTimer > 0f || state == KnightState.DEAD) return;

        masks--;
        invincibilityTimer = INVINCIBILITY_DURATION;
        hitFreezeCooldownTimer = HIT_FREEZE_COOLDOWN;

        // Cancel any ongoing action
        if (state == KnightState.FOCUSING) {
            EventBus.getInstance().publish(GameEvent.PLAYER_FOCUS_CANCEL);
            focusTimer = 0f;
        }

        // Knockback
        float knockback = (knockbackDirection == Direction.RIGHT) ? 1f : -1f;
        velocity.x = knockback * KNOCKBACK_VELOCITY;
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

    public void gainSoul(float amount) {
        soul = Math.min(soul + amount, GameData.MAX_SOUL);
        EventBus.getInstance().publish(GameEvent.PLAYER_SOUL_GAINED, amount);
    }

    public void spendSoul(float amount) {
        soul = Math.max(soul - amount, 0);
        EventBus.getInstance().publish(GameEvent.PLAYER_SOUL_SPENT, amount);
    }

    // --- Render ---

    public void renderEffects(SpriteBatch batch) {
        if(state == KnightState.ATTACKING) {
            renderSlashEffect(batch);
        } else if(state == KnightState.DASHING) {
            renderDashEffect(batch);
        } else if(state == KnightState.VENGEFUL_SPIRIT) {
            renderBlastEffect(batch);
        }
    }

    /** Helper method for the blinking effect that plays when the knight gets hit */
    private boolean shouldSkipRenderFrame() {
        return ((int) (invincibilityTimer * 8f) % 2) == 0;
    }

    private void renderSlashEffect(SpriteBatch batch) {
        KnightAnimationType effectType;
        switch (attackDirection) {
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

        TextureRegion frame = animation.getKeyFrame(stateTime);

        float frameWidth = frame.getRegionWidth();
        float frameHeight = frame.getRegionHeight();

        float centerX = position.x + WIDTH / 2f;
        float centerY;
        switch (attackDirection) {
            case UP:
                centerY = position.y + HEIGHT;
                break;
            case DOWN:
                centerY = position.y;
                break;
            default:
                centerY = position.y + HEIGHT / 2f;
                break;
        }

        batch.draw(frame,
            centerX - frameWidth / 2f, centerY - frameHeight / 2f,
            frameWidth / 2f, 0,
            frameWidth, frameHeight,
            facingRight ? -1 : 1, 1,
            0f);
    }

    private void renderDashEffect(SpriteBatch batch) {
        Animation<TextureRegion> animation = assets.getAnimation(KnightAnimationType.DASH_EFFECT);
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(stateTime);

        float frameWidth = frame.getRegionWidth() / 2f;
        float frameHeight = frame.getRegionHeight() / 2f;

        float centerY = position.y + HEIGHT / 2f - DASH_EFFECT_Y_OFFSET;
        float centerX;
        if(facingRight) {
            centerX = position.x + DASH_EFFECT_X_OFFSET;
        } else {
            centerX = position.x + WIDTH - DASH_EFFECT_X_OFFSET;
        }

        batch.draw(frame,
            centerX - frameWidth / 2f, centerY - frameHeight / 2f,
            frameWidth / 2f, 0,
            frameWidth, frameHeight,
            facingRight ? 1 : -1, 1,
            0f);
    }

    private void renderBlastEffect(SpriteBatch batch) {
        Animation<TextureRegion> animation = assets.getAnimation(KnightAnimationType.BLAST);
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(stateTime);

        float frameWidth = frame.getRegionWidth();
        float frameHeight = frame.getRegionHeight();

        float centerY = position.y + HEIGHT / 2f;
        float centerX;
        if(facingRight) {
            centerX = position.x + WIDTH;
        } else {
            centerX = position.x;
        }

        batch.draw(frame,
            centerX - frameWidth / 2f, centerY - frameHeight / 2f,
            frameWidth / 2f, 0,
            frameWidth, frameHeight,
            facingRight ? 1 : -1, 1,
            0f);
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
            case LANDING: return assets.getAnimation(KnightAnimationType.LANDING);
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

    public void setSafePosition(float x, float y) {
        this.lastSafePosition.set(x, y);
    }

    public void goToLastSafePosition() {
        this.position.set(lastSafePosition);
    }

    public void onWallCollision(float pushX) {
        position.x = pushX;
        velocity.x = 0f;
        hittingWall = true;
    }

    public void onCeilingCollision(float pushY) {
        position.y = pushY;
        if (velocity.y > 0f) {
            velocity.y = 0f;
        }
    }

    public void onFloorCollision(float pushY) {
        position.y = pushY;
        if (velocity.y < 0f) {
            velocity.y = 0f;
        }
        grounded = true;
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

    public float isFreeze() {
        return hitFreezeCooldownTimer;
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

    public float getSoul() {
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

    public Direction getAttackDirection() {
        return attackDirection;
    }

    public Vector2 getLastSafePosition() {
        return lastSafePosition;
    }
}
