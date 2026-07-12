package com.github.matinnameni.minihollowknight.controller.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;

/**
 * Handles all keyboard input for the Knight.
 */
public class KnightInputProcessor extends InputAdapter {

    private final Knight knight;
    private final Settings settings;

    /** Tracks whether the jump key is currently held (for jump-cut). */
    private boolean jumpKeyHeld = false;

    public KnightInputProcessor(Knight knight, Settings settings) {
        this.knight = knight;
        this.settings = settings;
    }

    /**
     * Reads the current keyboard state and triggers knight actions.
     */
    public void processInput(float deltaTime) {
        KnightState state = knight.getState();

        if (state == KnightState.DEAD) {
            return;
        }

        // --- Noclip ---
        if (knight.isNoclip()) {
            processNoclipInput();
            return;
        }

        boolean inputLocked = state == KnightState.DASHING
            || state == KnightState.HIT
            || state == KnightState.VENGEFUL_SPIRIT
            || state == KnightState.HOWLING_WRAITHS;

        if (inputLocked) return;

        // ---- Focus ----
        if (Gdx.input.isKeyPressed(settings.getKeyFocus()) && knight.canFocus()) {
            if (knight.getState() != KnightState.FOCUSING) {
                knight.enterState(KnightState.FOCUSING);
                knight.setFocusTimer(0f);
                knight.getVelocity().x = 0f;
                knight.getVelocity().y = 0f;
                EventBus.getInstance()
                    .publish(GameEvent.PLAYER_FOCUS_START);
            }
            return; // focus blocks other inputs
        }

        // Released focus key while focusing
        if (knight.getState() == KnightState.FOCUSING) {
            knight.cancelFocus();
        }

        // ---- Quick cast ----
        if (Gdx.input.isKeyJustPressed(settings.getKeyCast())) {
            if (Gdx.input.isKeyPressed(settings.getKeyUp())) {
                if (knight.canCastHowlingWraiths()) {
                    knight.castHowlingWraiths();
                }
            } else if (knight.canCastVengefulSpirit()) {
                knight.castVengefulSpirit();
            }
            return; // cast blocks other inputs
        }

        // ---- Attack ----
        if (Gdx.input.isKeyJustPressed(settings.getKeyAttack()) && knight.canAttack()) {
            knight.setAttackDirection(resolveAttackDirection());
            knight.startAttack();
            return;
        }

        // ---- Dash ----
        if (Gdx.input.isKeyJustPressed(settings.getKeyDash()) && knight.canDash()) {
            if (!knight.isGrounded() && !knight.isHittingWall()) {
                knight.setCanDash(false);
            }
            knight.startDash();
            return;
        }

        // ---- Jump ----
        if (Gdx.input.isKeyJustPressed(settings.getKeyJump())) {
            if (knight.isGrounded()) {
                knight.getVelocity().y = Knight.JUMP_INITIAL_VELOCITY;
                knight.setGrounded(false);
                knight.setCanDoubleJump(true);
                jumpKeyHeld = true;
                knight.enterState(KnightState.JUMPING);
            } else if (knight.isHittingWall()) {
                knight.getVelocity().y = Knight.JUMP_INITIAL_VELOCITY;
                knight.getVelocity().x = knight.isFacingRight()
                    ? -Knight.WALL_JUMP_INITIAL_VELOCITY
                    : Knight.WALL_JUMP_INITIAL_VELOCITY;
                knight.setHittingWall(false);
                knight.setFacingRight(!knight.isFacingRight());
                knight.setCanDoubleJump(true);
                jumpKeyHeld = true;
                knight.enterState(KnightState.WALL_JUMP);
            } else if (knight.canDoubleJump()) {
                knight.getVelocity().y = Knight.DOUBLE_JUMP_INITIAL_VELOCITY;
                knight.setCanDoubleJump(false);
                EventBus.getInstance()
                    .publish(GameEvent.PLAYER_DOUBLE_JUMP);
                knight.enterState(KnightState.DOUBLE_JUMPING);
            }
        }

        // Jump-cut: releasing the jump key while still rising cuts upward velocity
        if (jumpKeyHeld && !Gdx.input.isKeyPressed(settings.getKeyJump())) {
            jumpKeyHeld = false;
            if (knight.getVelocity().y > 0f &&
                (knight.getState() == KnightState.JUMPING || knight.getState() == KnightState.WALL_JUMP)) {
                knight.getVelocity().y *= Knight.JUMP_CUT_MULTIPLIER;
            }
        }

        // ---- Horizontal movement ----
        if (knight.getState() != KnightState.WALL_JUMP) {
            if (knight.getState() == KnightState.ATTACKING) {
                if (knight.isGrounded()) {
                    // Root the knight on the ground during the slash
                    knight.getVelocity().x = 0f;
                } else {
                    float moveInput = 0f;
                    if (Gdx.input.isKeyPressed(settings.getKeyLeft()))  moveInput -= 1f;
                    if (Gdx.input.isKeyPressed(settings.getKeyRight())) moveInput += 1f;
                    knight.getVelocity().x = moveInput * knight.getEffectiveMoveSpeed();
                }
            } else {
                float moveInput = 0f;
                if (Gdx.input.isKeyPressed(settings.getKeyLeft()))  moveInput -= 1f;
                if (Gdx.input.isKeyPressed(settings.getKeyRight())) moveInput += 1f;

                knight.getVelocity().x = moveInput * knight.getEffectiveMoveSpeed();

                if (moveInput != 0f) {
                    knight.setFacingRight(moveInput > 0f);
                    if (knight.isGrounded() && knight.getState() != KnightState.RUNNING) {
                        knight.enterState(KnightState.RUNNING);
                    }
                } else if (knight.isGrounded() && knight.getState() == KnightState.RUNNING) {
                    knight.enterState(KnightState.IDLE);
                }
            }
        }
    }

    private void processNoclipInput() {
        float speed = knight.getEffectiveMoveSpeed();
        float vx = 0f;
        float vy = 0f;

        if (Gdx.input.isKeyPressed(settings.getKeyLeft())) vx -= 1f;
        if (Gdx.input.isKeyPressed(settings.getKeyRight())) vx += 1f;
        if (Gdx.input.isKeyPressed(settings.getKeyUp())) vy += 1f;
        if (Gdx.input.isKeyPressed(settings.getKeyDown())) vy -= 1f;

        knight.getVelocity().x = vx * speed;
        knight.getVelocity().y = vy * speed;

        if (vx != 0f) {
            knight.setFacingRight(vx > 0f);
        }
    }

    /**
     * Determines the attack direction based on the currently held directional keys.
     *
     * @return the resolved attack direction
     */
    public Direction resolveAttackDirection() {
        if (Gdx.input.isKeyPressed(settings.getKeyDown()) && !knight.isGrounded()) {
            return Direction.DOWN;
        }
        if (Gdx.input.isKeyPressed(settings.getKeyUp())) {
            return Direction.UP;
        }
        return knight.isFacingRight()
            ? Direction.RIGHT
            : Direction.LEFT;
    }

    /** @return true if interaction key is being held. */
    public boolean interactionKeyHeld() {
        return Gdx.input.isKeyPressed(settings.getKeyInteract());
    }
}
