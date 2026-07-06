package com.github.matinnameni.minihollowknight.model.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.event.EventBus;
import com.github.matinnameni.minihollowknight.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.asset.FalseKnightAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.enemy.FalseKnightAnimationType;

import java.util.ArrayList;
import java.util.List;

public class FalseKnight extends Boss {

    // --- Constants ---

    // Size
    public static final float WIDTH = 690f;
    public static final float HEIGHT = 400f;

    // Hitbox
    public static final float HITBOX_WIDTH = 160f;
    public static final float HITBOX_HEIGHT = 170f;
    public static final float HITBOX_X_OFFSET = 260f;
    public static final float HITBOX_Y_OFFSET = 35f;

    // Movement
    public static final float GRAVITY = Knight.GRAVITY;
    public static final float MAX_FALL_SPEED = 600f;
    public static final float WALK_SPEED = 90f;
    public static final float CHARGE_SPEED = 350f;
    public static final float JUMP_VELOCITY = 650f;
    public static final float KNOCKBACK_VELOCITY = 80f;

    // Initial state
    public static final float INITIAL_X_OFFSET = 0f;
    public static final float INITIAL_Y_OFFSET = 500f;
    public static final float ENTRANCE_NOCLIP_DURATION = 2f;
    public static final float ENTRANCE_NOT_RENDERING_DURATION = 1f;

    // Health
    public static final float MAX_HEALTH = 1500f;
    public static final float STUN_HP_THRESHOLD = MAX_HEALTH * 0.5f;

    // Stun
    public static final float STUN_DURATION = 5f;
    public static final int MAX_STUN_HITS = 5;

    // Phase 2 speed scaling
    public static final float PHASE2_SPEED_MULTIPLIER = 1.5f;
    public static final float PHASE2_AI_MULTIPLIER = 1.5f;

    // AI decision-making
    public static final float CLOSE_DISTANCE = 250f;
    public static final float FAR_DISTANCE = 600f;
    public static final float DECISION_COOLDOWN_BASE = 0.6f;
    public static final float RANDOM_OFFSET_RANGE = 0.3f;

    // Defensive leap
    public static final float DEFENSIVE_LEAP_HORIZONTAL_SPEED = 200f;
    public static final float DEFENSIVE_LEAP_HIT_TIMER = 2f;
    public static final int DEFENSIVE_LEAP_HIT_COUNTS_TRIGGER = 4;

    // Attack hitboxes
    public static final float SLAM_HITBOX_WIDTH = 280f;
    public static final float SLAM_HITBOX_HEIGHT = 80f;
    public static final float JUMP_ATTACK_HITBOX_WIDTH = 200f;
    public static final float JUMP_ATTACK_HITBOX_HEIGHT = 220f;

    // Camera shake
    public static final float SHAKE_SLAM = 6f;
    public static final float SHAKE_CHARGE = 3f;
    public static final float SHAKE_LAND = 4f;
    public static final float SHAKE_DEATH = 10f;

    // --- State ---

    private State state = State.IDLE;
    private int phase = 1;
    private boolean grounded = false;
    private boolean fightStarted = false;
    private boolean stunned = false;
    private Direction facingDirection = Direction.RIGHT;
    private State lastAttackMove = null;
    private int stunHits = 0;
    private int hitsInADuration;
    private Vector2 initialPosition = new Vector2();
    private boolean noclipEnabled = true;

    // Timing
    private float stateTime = 0f;
    private float decisionCooldown = 0f;
    private float stunTimer = 0f;
    private float hitTimer = 0f;
    private float entranceTimer = 0f;

    // Camera shake
    private float cameraShakeIntensity = 0f;
    private float cameraShakeDuration = 0f;
    private float cameraShakeTimer = 0f;

    // Shockwave flag
    private boolean jumpSpawnsShockwave = false;
    private boolean wantsShockwave = false;
    private Direction shockwaveDirection = Direction.LEFT;

    // Knight position
    private Vector2 lastKnightPos = new Vector2();

    // Assets
    private final FalseKnightAssetBundle assets;


    public FalseKnight(float x, float y, FalseKnightAssetBundle assets) {
        super(x, y);
        this.assets = assets;
        this.health = MAX_HEALTH;
        this.initialPosition.set(x + INITIAL_X_OFFSET, y + INITIAL_Y_OFFSET);
        entranceTimer = ENTRANCE_NOCLIP_DURATION;
    }

    //  --- Boss phase ---

    @Override
    public int getPhase() {
        return phase;
    }

    //  --- Main loop ---

    @Override
    public void update(float deltaTime) {
        if (!fightStarted) {
            position.set(initialPosition);
            return;
        }

        updateTimers(deltaTime);

        if (state == State.STUNNED && stunTimer <= 0f) {
            enterState(State.STUN_RECOVERING);
            stunned = false;
        }

        updateState(deltaTime);
        applyPhysics(deltaTime);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!fightStarted ||
            entranceTimer >= ENTRANCE_NOCLIP_DURATION - ENTRANCE_NOT_RENDERING_DURATION) {
            return;
        }

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

    // --- Starting the fight ---

    public void startFight() {
        fightStarted = true;
        EventBus.getInstance().publish(GameEvent.FALSE_KNIGHT_FIGHT_STARTED, this);
    }

    //  --- AI decision-making ---

    public void decideNextAction(Knight knight) {
        if (!fightStarted || state != State.IDLE) return;
        if (decisionCooldown > 0f) return;

        if(noclipEnabled) {
            enterState(State.JUMP_ANTIC);
            return;
        }

        // Face the knight before attacking
        if (faceKnight(knight)) {
            return;
        }

        float distance = getDistanceTo(knight);
        lastKnightPos = new Vector2(
          knight.getBounds().x + knight.getBounds().width / 2,
          knight.getBounds().y + knight.getBounds().height / 2
        );

        // Build weighted move pool
        List<State> candidates = new ArrayList<>();

        // Close range
        if (distance < CLOSE_DISTANCE) {
            candidates.add(State.ATTACK_ANTIC);         // Mace Slam
            candidates.add(State.ATTACK_ANTIC);
            candidates.add(State.DEFENSIVE_LEAP_ANTIC); // Defensive leap
            if (phase >= 2) {
                candidates.add(State.JUMP_ANTIC);       // Shockwave Jump
            }
        }

        // Medium range
        else if (distance < FAR_DISTANCE) {
            candidates.add(State.OFFENSIVE_LEAP_ANTIC); // Offensive Leap
            candidates.add(State.OFFENSIVE_LEAP_ANTIC);
            candidates.add(State.CHARGE_ANTIC);         // Charge Run
            if (phase >= 2) {
                candidates.add(State.JUMP_ANTIC);       // Shockwave Jump
                candidates.add(State.JUMP_ANTIC);
            }
        }

        // Far range
        else {
            candidates.add(State.CHARGE_ANTIC);         // Charge Run
            candidates.add(State.CHARGE_ANTIC);
            candidates.add(State.OFFENSIVE_LEAP_ANTIC); // Offensive Leap
            if (phase >= 2) {
                candidates.add(State.JUMP_ANTIC);       // Shockwave Jump
            }
        }

        // Anti-spam
        if (lastAttackMove != null) {
            candidates.removeIf(state -> state == lastAttackMove);
            // Add it back only once so it has lower probability
            if (MathUtils.random() < 0.1f) {
                candidates.add(lastAttackMove);
            }
        }

        State chosen = (hitsInADuration >= DEFENSIVE_LEAP_HIT_COUNTS_TRIGGER) ?
            State.DEFENSIVE_LEAP_ANTIC :
            candidates.get(MathUtils.random(0, candidates.size() - 1));

        lastAttackMove = chosen;
        enterState(chosen);

        // Decision cooldown
        float baseCooldown = DECISION_COOLDOWN_BASE / (phase >= 2 ? PHASE2_AI_MULTIPLIER : 1f);
        decisionCooldown = baseCooldown + MathUtils.random(-RANDOM_OFFSET_RANGE, RANDOM_OFFSET_RANGE);
    }

    //  --- Damage ---

    @Override
    public void takeDamage(float damage, Direction knockbackDirection) {
        if (state == State.DEAD) return;

        // changing state while false knight is stunned and
        // it's taking damage (for changing its animation).
        if (stunned && state == State.STUNNED) {
            enterState(State.STUNNED_HIT);
            stunHits++;
        }

        health -= damage;

        // hit in a duration
        if(damage >= Knight.SLASH_DAMAGE) {
            if (hitsInADuration++ == 0) {
                hitTimer = DEFENSIVE_LEAP_HIT_TIMER;
            }

        }

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
        if (!stunned) { velocity.x = knockbackDir * KNOCKBACK_VELOCITY; }

        // Stun check
        if (!stunned && health <= STUN_HP_THRESHOLD && phase == 1
            && state != State.STUNNED && state != State.STUN_RECOVERING
            && state != State.DYING && state != State.DEAD) {
            enterStun();
            phase = 2;
            EventBus.getInstance().publish(GameEvent.FALSE_KNIGHT_PHASE2_STARTED, this);
            return;
        }

        // Death
        if (isDead()) {
            enterState(State.DYING);
            triggerCameraShake(SHAKE_DEATH, 0.8f);
            EventBus.getInstance().publish(GameEvent.FALSE_KNIGHT_DEFEATED, this);
        }
    }

    private void enterStun() {
        stunned = true;
        stunTimer = STUN_DURATION;
        enterState(State.STUNNED);
        triggerCameraShake(5f, 0.3f);
        EventBus.getInstance().publish(GameEvent.FALSE_KNIGHT_STUNNED, this);
    }

    @Override
    public boolean canDamagePlayer() {
        if (isDead()) return false;
        return !stunned && isFightStarted() && state != State.IDLE;
    }

    //  --- Camera shake ---

    public float getCameraShakeIntensity() {
        if (cameraShakeTimer <= 0f) return 0f;
        // Shake decays over the duration
        float progress = 1f - (cameraShakeTimer / cameraShakeDuration);
        return cameraShakeIntensity * (1f - progress);
    }

    private void triggerCameraShake(float intensity, float duration) {
        this.cameraShakeIntensity = intensity;
        this.cameraShakeDuration = duration;
        this.cameraShakeTimer = duration;
    }

    //  --- Shockwave ---

    /** @return true if the boss just landed a power slam and wants to spawn shockwaves. */
    public boolean wantsShockwave() {
        return wantsShockwave;
    }

    /** @return the direction the shockwave should travel. */
    public Direction getShockwaveDirection() {
        return shockwaveDirection;
    }

    /** Consumes the shockwave spawn request. */
    public void consumeShockwave() {
        wantsShockwave = false;
    }

    // --- Timers ---

    public void updateTimers(float deltaTime) {
        // Decay camera shake
        if (cameraShakeTimer > 0f) {
            cameraShakeTimer -= deltaTime;
            if (cameraShakeTimer <= 0f) {
                cameraShakeIntensity = 0f;
            }
        }

        // Stun timer
        if (state == State.STUNNED && stunTimer > 0) {
            stunTimer -= deltaTime;
        }

        // Decision cooldown
        if (decisionCooldown > 0) {
            decisionCooldown -= deltaTime;
        }

        // Hit timer
        if(hitTimer > 0) {
            hitTimer -= deltaTime;
        } else {
            hitsInADuration = 0;
        }

        // Noclip timer
        if(entranceTimer > 0) {
            entranceTimer -= deltaTime;
            noclipEnabled = entranceTimer > 0f;
        }
    }

    //  --- State ---

    private void updateState(float deltaTime) {
        stateTime += deltaTime;
        float speedMult = (phase >= 2) ? PHASE2_SPEED_MULTIPLIER : 1f;

        switch (state) {
            case IDLE:
                velocity.x = 0f;
                break;

            case TURNING:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.TURN.getDuration()) {
                    facingDirection = (facingDirection == Direction.RIGHT) ? Direction.LEFT : Direction.RIGHT;
                    enterState(State.IDLE);
                }
                break;

            case WALKING:
                velocity.x = (facingDirection == Direction.RIGHT ? 1 : -1) * WALK_SPEED * speedMult;
                break;

            case CHARGE_ANTIC:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.RUN_ANTIC.getDuration()) {
                    enterState(State.CHARGING);
                }
                break;

            case CHARGING:
                velocity.x = (facingDirection == Direction.RIGHT ? 1 : -1) * CHARGE_SPEED * speedMult;
                break;

            case ATTACK_ANTIC:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.ATTACK_ANTIC.getDuration()) {
                    enterState(State.ATTACKING);
                    triggerCameraShake(SHAKE_SLAM, 0.3f);
                }
                break;

            case ATTACKING:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.ATTACK.getDuration()) {
                    enterState(State.ATTACK_RECOVER);
                }
                break;

            case ATTACK_RECOVER:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.ATTACK_RECOVER.getDuration()) {
                    enterState(State.IDLE);
                }
                break;

            case JUMP_ANTIC:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.JUMP_ANTIC.getDuration()) {
                    enterState(State.JUMPING);
                    velocity.y = JUMP_VELOCITY;
                }
                jumpSpawnsShockwave = true;
                break;

            case OFFENSIVE_LEAP_ANTIC:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.JUMP_ANTIC.getDuration()) {
                    enterState(State.JUMPING);
                    velocity.x = lastKnightPos.x - getBounds().x;
                    velocity.y = JUMP_VELOCITY;
                }
                jumpSpawnsShockwave = false;
                break;

            case JUMPING:
                if (velocity.y <= 0f && stateTime > 0.15f) {
                    enterState(State.JUMP_ATTACKING);
                }
                break;

            case JUMP_ATTACKING:
                // Continue in jump attack until landing
                if (grounded) {
                    if (phase >= 2 && jumpSpawnsShockwave) {
                        triggerCameraShake(SHAKE_SLAM * 1.5f, 0.4f);
                        wantsShockwave = true;
                        shockwaveDirection = facingDirection;
                    } else {
                        triggerCameraShake(SHAKE_LAND, 0.25f);
                    }
                    enterState(State.LANDING);
                }
                break;

            case LANDING:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.LAND.getDuration()) {
                    enterState(State.IDLE);
                }
                break;

            case DEFENSIVE_LEAP_ANTIC:
                if(stateTime >= FalseKnightAnimationType.JUMP_ANTIC.getDuration()) {
                    velocity.y = JUMP_VELOCITY;
                    velocity.x = (facingDirection == Direction.RIGHT) ?
                        -DEFENSIVE_LEAP_HORIZONTAL_SPEED:
                        DEFENSIVE_LEAP_HORIZONTAL_SPEED;
                    enterState(State.DEFENSIVE_LEAP);
                }
                break;

            case DEFENSIVE_LEAP:
                if (grounded) {
                    enterState(State.LANDING);
                }
                break;

            case STUNNED:
                velocity.x = 0f;
                velocity.y = 0f;
                if(stunHits > MAX_STUN_HITS) {
                    enterState(State.ATTACK_RECOVER);
                    stunHits = 0;
                    stunTimer = 0f;
                    stunned = false;
                }
                break;

            case STUNNED_HIT:
                if (stateTime >= FalseKnightAnimationType.DEATH_HIT.getDuration()) {
                    enterState(State.STUNNED);
                }
                break;

            case STUN_RECOVERING:
                velocity.x = 0f;
                if (stateTime >= FalseKnightAnimationType.STUN_RECOVER.getDuration()) {
                    enterState(State.IDLE);
                }
                break;

            case DYING: {
                Animation<TextureRegion> deathAnim = assets.getAnimation(FalseKnightAnimationType.DEATH_HIT);
                if (deathAnim != null && deathAnim.isAnimationFinished(stateTime)) {
                    state = State.DEAD;
                }
                break;
            }

            case DEAD:
                velocity.x = 0f;
                velocity.y = 0f;
                break;
        }
    }

    //  --- Physics ---

    private void applyPhysics(float delta) {
        if (state == State.STUNNED || state == State.DEAD) return;

        velocity.y -= GRAVITY * delta;
        if (velocity.y < -MAX_FALL_SPEED) {
            velocity.y = -MAX_FALL_SPEED;
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    //  --- External collision hooks ---

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
        // Stop charging if we hit a wall
        if (state == State.CHARGING) {
            velocity.x = 0f;
            triggerCameraShake(SHAKE_CHARGE, 0.2f);
            enterState(State.ATTACK_RECOVER);
        }
    }

    /** Face toward the given position.
     * @return true if the boss started turning, false if already facing the correct way.
     */
    public boolean faceKnight(Knight knight) {
        float bossCenterX = position.x + WIDTH / 2f;
        float knightCenterX = knight.getPosition().x + Knight.WIDTH / 2f;
        Direction newFacingDirection = (knightCenterX > bossCenterX) ? Direction.RIGHT : Direction.LEFT;
        if(newFacingDirection != facingDirection) {
            enterState(State.TURNING);
            return true;
        }
        return false;
    }

    //  --- Attack hitboxes ---

    /** @return the hitbox for the mace slam attack, or null if not active. */
    public Rectangle getSlamHitbox() {
        if (state != State.ATTACKING) return null;
        float hitboxX = facingDirection == Direction.LEFT ?
            position.x + HITBOX_X_OFFSET - SLAM_HITBOX_WIDTH + HITBOX_WIDTH :
            position.x + HITBOX_X_OFFSET;
        return new Rectangle(hitboxX, position.y + HITBOX_Y_OFFSET, SLAM_HITBOX_WIDTH, SLAM_HITBOX_HEIGHT);
    }

    /** @return the hitbox for the jump attack, or null if not active. */
    public Rectangle getJumpAttackHitbox() {
        if (state != State.JUMP_ATTACKING) return null;
        float hitboxX = facingDirection == Direction.LEFT ?
            position.x + HITBOX_X_OFFSET - JUMP_ATTACK_HITBOX_WIDTH + HITBOX_WIDTH :
            position.x + HITBOX_X_OFFSET;
        return new Rectangle(
            hitboxX,
            position.y + HITBOX_Y_OFFSET,
            JUMP_ATTACK_HITBOX_WIDTH,
            JUMP_ATTACK_HITBOX_HEIGHT
        );
    }

    //  --- Getters ---

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isStunned() {
        return stunned;
    }

    public boolean isFightStarted() {
        return fightStarted;
    }

    public boolean isCharging() {
        return state == State.CHARGING;
    }

    public State getState() {
        return state;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean isNoclipEnabled() {
        return noclipEnabled;
    }

    //  --- Helpers ---

    private void enterState(State newState) {
        this.state = newState;
        this.stateTime = 0f;
    }

    private float getDistanceTo(Knight knight) {
        float bossCenterX = position.x + WIDTH / 2f;
        float knightCenterX = knight.getPosition().x + Knight.WIDTH / 2f;
        return Math.abs(bossCenterX - knightCenterX);
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (state) {
            case IDLE: return assets.getAnimation(FalseKnightAnimationType.IDLE);
            case TURNING: return assets.getAnimation(FalseKnightAnimationType.TURN);
            case WALKING: return assets.getAnimation(FalseKnightAnimationType.RUN);
            case CHARGE_ANTIC: return assets.getAnimation(FalseKnightAnimationType.RUN_ANTIC);
            case CHARGING: return assets.getAnimation(FalseKnightAnimationType.RUN);
            case ATTACK_ANTIC, OFFENSIVE_LEAP_ANTIC,
                 DEFENSIVE_LEAP_ANTIC: return assets.getAnimation(FalseKnightAnimationType.ATTACK_ANTIC);
            case ATTACKING: return assets.getAnimation(FalseKnightAnimationType.ATTACK);
            case ATTACK_RECOVER: return assets.getAnimation(FalseKnightAnimationType.ATTACK_RECOVER);
            case JUMP_ANTIC: return assets.getAnimation(FalseKnightAnimationType.JUMP_ANTIC);
            case JUMPING, DEFENSIVE_LEAP: return assets.getAnimation(FalseKnightAnimationType.JUMP);
            case JUMP_ATTACKING: return assets.getAnimation(FalseKnightAnimationType.JUMP_ATTACK);
            case LANDING: return assets.getAnimation(FalseKnightAnimationType.LAND);
            case STUNNED: return assets.getAnimation(FalseKnightAnimationType.BODY);
            case STUN_RECOVERING: return assets.getAnimation(FalseKnightAnimationType.STUN_RECOVER);
            case STUNNED_HIT: return assets.getAnimation(FalseKnightAnimationType.DEATH_HIT);
            case DYING, DEAD: return assets.getAnimation(FalseKnightAnimationType.DEATH_LAND);
            default: return assets.getAnimation(FalseKnightAnimationType.IDLE);
        }
    }

    // ---  State ---

    public enum State {
        IDLE,
        TURNING,
        WALKING,
        CHARGE_ANTIC,
        CHARGING,
        ATTACK_ANTIC,
        ATTACKING,
        ATTACK_RECOVER,
        JUMP_ANTIC,
        OFFENSIVE_LEAP_ANTIC,
        JUMPING,
        JUMP_ATTACKING,
        DEFENSIVE_LEAP_ANTIC,
        DEFENSIVE_LEAP,
        LANDING,
        STUNNED,
        STUNNED_HIT,
        STUN_RECOVERING,
        DYING,
        DEAD
    }
}
