package com.github.matinnameni.minihollowknight.model.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.projectile.HowlingWraiths;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.projectile.VengefulSpirit;
import com.github.matinnameni.minihollowknight.model.asset.KnightAssetBundle;
import com.github.matinnameni.minihollowknight.model.charm.CharmEffects;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.enums.CharmType;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.KnightAnimationType;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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

    // Death hitbox
    public static final float DEATH_HITBOX_WIDTH = HITBOX_WIDTH;
    public static final float DEATH_HITBOX_HEIGHT = 20f;
    public static final float DEATH_HITBOX_X_OFFSET = HITBOX_X_OFFSET;
    public static final float DEATH_HITBOX_Y_OFFSET = HITBOX_Y_OFFSET + 30f;

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
    public static final float SLASH_SOUL_GAIN = 11f;
    public static final float SOUL_GAIN_DURATION = 0.4f;
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
    public static final float VENGEFUL_SPIRIT_DAMAGE = 50f;
    public static final float VENGEFUL_SPIRIT_DURATION = KnightAnimationType.FIREBALL_CAST.getDuration();
    public static final float VENGEFUL_SPIRIT_COOLDOWN = 1f;
    public static final float VENGEFUL_SPIRIT_SOUL_COST = 33f;
    public static final float VENGEFUL_SPIRIT_OFFSET = 40f;

    public static final float HOWLING_WRAITHS_DAMAGE = 75f;
    public static final float HOWLING_WRAITHS_DURATION = KnightAnimationType.SOUL_SCREAM.getDuration();
    public static final float HOWLING_WRAITHS_COOLDOWN = 0.7f;
    public static final float HOWLING_WRAITHS_SOUL_COST = 33f;
    public static final float HOWLING_WRAITHS_OFFSET = 120f;

    // --- State ---
    private KnightState state = KnightState.IDLE;
    private float stateTime = 0f;
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private boolean facingRight = true;
    private boolean grounded = false;
    private boolean hittingWall = false;

    // --- Health & Soul ---
    private int masks = 5;
    private int maxMasks = 5;
    private float soul = 0f;
    private float pendingSoulGain = 0f; // Soul that has been earned but not yet added to soul.

    // --- Cheat flags ---
    private boolean godMode = false;
    private boolean noclip = false;
    private static final float NOCLIP_MOVE_SPEED_MULTIPLIER = 2.0f;

    // --- Timers ---
    private float invincibilityTimer = 0f;
    private float hitFreezeCooldownTimer = 0f;
    private float dashCooldownTimer = 0f;
    private float attackCooldownTimer = 0f;
    private float vengefulSpiritCooldownTimer = 0f;
    private float vengefulSpiritTimer = 0f;
    private float howlingWraithsCooldownTimer = 0f;
    private float howlingWraithsTimer = 0f;
    private float focusTimer = 0f;
    private float focusCooldownTimer = 0f;
    private float consumedSoul = 0f;
    private float attackTimer = 0f;
    private float dashTimer = 0f;

    // --- Air abilities ---
    private boolean canDoubleJump = true;
    private boolean canDash = true;

    // --- Attack ---
    private Direction attackDirection = Direction.LEFT;
    private final Set<Enemy> dashHitEnemiesThisDash = new HashSet<>();

    // --- Charms ---
    private CharmEffects charmEffects = CharmEffects.NONE;

    // --- Assets & settings ---
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
            applyPhysics(deltaTime);
            velocity.x = 0;
            stateTime += deltaTime;
            return;
        }

        applyPendingSoul(deltaTime);

        updateState(deltaTime);
        applyPhysics(deltaTime);
    }

    /**
     * Drains {@link #pendingSoulGain} into {@link #soul} over
     * {@link #SOUL_GAIN_DURATION} seconds, capped at {@link GameData#MAX_SOUL}.
     */
    private void applyPendingSoul(float deltaTime) {
        if (pendingSoulGain <= 0f) return;

        // Amount that should move from pending to soul this frame.
        float amount = pendingSoulGain * (deltaTime / SOUL_GAIN_DURATION);

        // Don't overshoot MAX_SOUL or the remaining pending.
        float headroom = GameData.MAX_SOUL - soul;
        amount = Math.min(amount, headroom);
        amount = Math.min(amount, pendingSoulGain);

        soul += amount;
        pendingSoulGain -= amount;

        if (pendingSoulGain <= 0f) {
            pendingSoulGain = 0f;
        }
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public Rectangle getBounds() {
        if (state == KnightState.DEAD) {
            return getDeathHitbox();
        }

        return new Rectangle(position.x + HITBOX_X_OFFSET, position.y + HITBOX_Y_OFFSET,
            HITBOX_WIDTH, HITBOX_HEIGHT);
    }


    @Override
    public void render(SpriteBatch batch) {
        // Rendering is handled by KnightRenderer
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
        this.pendingSoulGain = 0f;
        applyEquippedCharms(data.equippedCharms);

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
        data.soul = Math.min(soul + pendingSoulGain, GameData.MAX_SOUL);
    }

    // --- Charms ---

    /**
     * Recomputes the active {@link CharmEffects} from the given equipped-charm set.
     * Should be called whenever the equipped charms change (on load and whenever
     * the player equips/unequips a charm from the inventory menu).
     */
    public void applyEquippedCharms(Set<CharmType> equippedCharms) {
        this.charmEffects = CharmEffects.of(equippedCharms);
    }

    /** Returns the currently active charm effects. */
    public CharmEffects getCharmEffects() {
        return charmEffects;
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

    // --- Focus ---

    public boolean canFocus() {
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

    /** Cancels an in-progress focus. */
    public void cancelFocus() {
        focusTimer = 0f;
        consumedSoul = 0f;
        EventBus.getInstance().publish(GameEvent.PLAYER_FOCUS_CANCEL);
        enterState(KnightState.IDLE);
    }

    // --- Attack ---

    public boolean canAttack() {
        return attackCooldownTimer <= 0 &&
            state != KnightState.FOCUSING &&
            state != KnightState.DASHING;
    }

    /** Returns the current effective attack duration/cooldown. */
    public float getEffectiveAttackDuration() {
        return ATTACK_DURATION * charmEffects.getAttackDurationMultiplier();
    }

    /** Starts a nail attack. The attack direction should be set by the caller. */
    public void startAttack() {
        attackCooldownTimer = getEffectiveAttackDuration();
        attackTimer = 0f;
        enterState(KnightState.ATTACKING);
        EventBus.getInstance().publish(GameEvent.PLAYER_ATTACK_START, attackDirection);
    }

    // --- Dash ---

    public boolean canDash() {
        return canDash
            && dashCooldownTimer <= 0f
            && state != KnightState.FOCUSING
            && state != KnightState.ATTACKING;
    }

    /** Returns the current effective dash cooldown. */
    public float getEffectiveDashCooldown() {
        return DASH_COOLDOWN * charmEffects.getDashCooldownMultiplier();
    }

    /** Returns the current effective dash duration. */
    public float getEffectiveDashDuration() {
        return DASH_DURATION * charmEffects.getDashDurationMultiplier();
    }

    /** Starts a dash in the current facing direction. */
    public void startDash() {
        dashCooldownTimer = getEffectiveDashCooldown();
        velocity.y = 0f;
        velocity.x = (facingRight ? 1f : -1f) * DASH_SPEED;
        enterState(KnightState.DASHING);
        dashHitEnemiesThisDash.clear();
        EventBus.getInstance().publish(GameEvent.PLAYER_DASH);
    }

    /**
     * @return true while the Knight should be immune to enemy contact damage and pass through enemies.
     */
    public boolean isDashingThroughEnemies() {
        return state == KnightState.DASHING && charmEffects.hasSharpShadow();
    }

    /**
     * Records that {@code enemy} was already damaged by the current Sharp Shadow dash pass,
     * so it isn't hit multiple times by the same dash.
     * @return true if this enemy had not been hit yet this dash.
     */
    public boolean tryMarkDashHit(Enemy enemy) {
        return dashHitEnemiesThisDash.add(enemy);
    }

    // --- Spells ---

    public boolean canCastHowlingWraiths() {
        return howlingWraithsCooldownTimer <= 0
            && soul >= HOWLING_WRAITHS_SOUL_COST;
    }

    /** Casts Howling Wraiths and publishes the spawn event. */
    public void castHowlingWraiths() {
        howlingWraithsCooldownTimer = HOWLING_WRAITHS_COOLDOWN;
        enterState(KnightState.HOWLING_WRAITHS);
        velocity.x = 0;
        velocity.y = 0;
        spendSoul(HOWLING_WRAITHS_SOUL_COST);

        float spawnX = position.x + WIDTH / 2f;
        float spawnY = position.y + HEIGHT / 2f + HOWLING_WRAITHS_OFFSET;

        EventBus.getInstance().publish(GameEvent.PLAYER_HOWLING_WRAITHS_CAST,
            new HowlingWraiths.SpawnInfo(spawnX, spawnY, assets, hasVoidHear(), charmEffects.getSpellDamageMultiplier()));
    }

    public boolean canCastVengefulSpirit() {
        return vengefulSpiritCooldownTimer <= 0
            && soul >= VENGEFUL_SPIRIT_SOUL_COST;
    }

    /** Casts Vengeful Spirit and publishes the spawn event. */
    public void castVengefulSpirit() {
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
            new VengefulSpirit.SpawnInfo(spawnX, spawnY, fireDir, assets, hasVoidHear(), charmEffects.getSpellDamageMultiplier()));
    }

    // --- State ---

    /** Transitions to a new state and resets {@link #stateTime}. */
    public void enterState(KnightState newState) {
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
                break;

            case ATTACKING:
                attackTimer += deltaTime;
                if (attackTimer >= getEffectiveAttackDuration()) {
                    attackTimer = 0f;
                    enterState(resolvePostActionState());
                }
                break;

            case DASHING:
                dashTimer += deltaTime;
                if (dashTimer >= getEffectiveDashDuration()) {
                    dashTimer = 0f;
                    velocity.x = 0f;
                    dashHitEnemiesThisDash.clear();
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
                float effectiveFocusTime = FOCUS_CHANNEL_TIME * charmEffects.getFocusChannelTimeMultiplier();
                float ratio = deltaTime / effectiveFocusTime;
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
                if (focusTimer >= effectiveFocusTime) {
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
        if (noclip) {
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
            return;
        }

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

        // Reset air abilities
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

    public Rectangle getDeathHitbox() {
        return new Rectangle(
            position.x + DEATH_HITBOX_X_OFFSET,
            position.y + DEATH_HITBOX_Y_OFFSET,
            DEATH_HITBOX_WIDTH,
            DEATH_HITBOX_HEIGHT
        );
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
        if (godMode) return;

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

    public void takeDamage(Direction knockbackDirection, int numOfMasks) {
        if (numOfMasks <= 0) return;
        masks -= numOfMasks - 1;
        takeDamage(knockbackDirection);
    }

    // --- Soul ---

    public void gainSoul(float amount) {
        float totalIfAdded = soul + pendingSoulGain + amount;
        if (totalIfAdded <= GameData.MAX_SOUL) {
            pendingSoulGain += amount;
            EventBus.getInstance().publish(GameEvent.PLAYER_SOUL_GAINED, amount);
        } else {
            pendingSoulGain = GameData.MAX_SOUL - soul;
        }
    }

    public void spendSoul(float amount) {
        soul = Math.max(soul - amount, 0);
        EventBus.getInstance().publish(GameEvent.PLAYER_SOUL_SPENT, amount);
    }

    // --- Respawn ---

    /** Resets the knight's runtime state after a death/respawn cycle. */
    public void resetAfterDeath() {
        this.masks = maxMasks;
        this.soul = 0f;
        this.pendingSoulGain = 0f;
        this.state = KnightState.IDLE;
        this.stateTime = 0f;
        this.velocity.setZero();
        this.invincibilityTimer = 0f;
        this.hitFreezeCooldownTimer = 0f;
        this.dashCooldownTimer = 0f;
        this.attackCooldownTimer = 0f;
        this.vengefulSpiritCooldownTimer = 0f;
        this.howlingWraithsCooldownTimer = 0f;
        this.focusCooldownTimer = 0f;
        this.focusTimer = 0f;
        this.consumedSoul = 0f;
        this.attackTimer = 0f;
        this.dashTimer = 0f;
        this.canDoubleJump = true;
        this.canDash = true;
        this.grounded = false;
        this.hittingWall = false;
        this.facingRight = true;
        this.dashHitEnemiesThisDash.clear();
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

    public float getStateTime() {
        return stateTime;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isHittingWall() {
        return hittingWall;
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0f;
    }

    public float getInvincibilityTimer() {
        return invincibilityTimer;
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

    public boolean canDoubleJump() {
        return canDoubleJump;
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

    // --- Cheat accessors ---

    public boolean isGodMode() {
        return godMode;
    }

    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }

    public boolean isNoclip() {
        return noclip;
    }

    public void setNoclip(boolean noclip) {
        this.noclip = noclip;
        if (!noclip) {
            velocity.setZero();
        }
    }

    /** @return the effective horizontal move speed. */
    public float getEffectiveMoveSpeed() {
        return MOVE_SPEED * (noclip ? NOCLIP_MOVE_SPEED_MULTIPLIER : 1f);
    }

    /** Fully refills the soul vessel. */
    public void refillSoul() {
        this.soul = GameData.MAX_SOUL;
        this.pendingSoulGain = 0f;
    }

    /** Restores one mask for the knight. */
    public void heal() {
        this.masks = Math.min(masks + 1, maxMasks);
    }

    /** Restores the knight to full masks. */
    public void fullHeal() {
        this.masks = maxMasks;
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

    public boolean hasVoidHear() {
        return charmEffects.hasVoidHeart();
    }

    // --- Setters ---

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public void setCanDoubleJump(boolean canDoubleJump) {
        this.canDoubleJump = canDoubleJump;
    }

    public void setCanDash(boolean canDash) {
        this.canDash = canDash;
    }

    public void setAttackDirection(Direction attackDirection) {
        this.attackDirection = attackDirection;
    }

    public void setFocusTimer(float focusTimer) {
        this.focusTimer = focusTimer;
    }

    /** Returns the Settings reference (used by KnightInputProcessor for key bindings). */
    public Settings getSettings() {
        return settings;
    }

    /** Returns the asset bundle (used for spell spawn events). */
    public KnightAssetBundle getAssets() {
        return assets;
    }
}
