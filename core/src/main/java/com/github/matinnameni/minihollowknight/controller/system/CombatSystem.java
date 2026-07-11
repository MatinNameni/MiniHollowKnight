package com.github.matinnameni.minihollowknight.controller.system;

import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.object.BreakableWall;
import com.github.matinnameni.minihollowknight.model.charm.CharmEffects;
import com.github.matinnameni.minihollowknight.model.object.GridObject;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.laser.Laser;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.entity.enemies.FalseKnight;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles all combat interactions: nail attacks, enemy contact damage,
 * laser damage, and boss attack hitboxes.
 */
public class CombatSystem {

    private final CollisionSystem collisionSystem;

    // Track which enemies/walls were already hit in the current swing
    private final Set<Enemy> attackedEnemiesThisSwing = new HashSet<>();
    private final Set<BreakableWall> attackedWallsThisSwing = new HashSet<>();

    public CombatSystem(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }

    // --- Nail attack ---

    /**
     * If the Knight is in the ATTACKING state, resolves the nail hitbox
     * against platforms and breakable walls.
     */
    public void resolveNailAttack(Knight knight) {
        if (knight.getState() != KnightState.ATTACKING) {
            attackedEnemiesThisSwing.clear();
            attackedWallsThisSwing.clear();
            return;
        }

        resolvePlatformAttack(knight);
        resolveBreakableWallAttack(knight);
    }

    /** Pogo bounce when attacking a pogo-able platform from above or sideways. */
    private void resolvePlatformAttack(Knight knight) {
        Rectangle attackHitbox = knight.getAttackHitbox();
        Map<GridObject, Direction> collisions = collisionSystem.getOverlappingObjects(attackHitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (direction == Direction.UP) {
                if (platform.canPogo) {
                    knight.onDownAttackBounce();
                }
            } else if (direction == Direction.LEFT) {
                if (platform.canPogo) {
                    knight.onDownAttackBounce();
                }
            } else if (direction == Direction.RIGHT) {
                if (platform.canPogo) {
                    knight.onDownAttackBounce();
                }
            }
        }
    }

    /** Damages breakable walls within the nail hitbox. */
    private void resolveBreakableWallAttack(Knight knight) {
        TiledGameMap gameMap = collisionSystem.getGameMap();
        if (gameMap == null) return;

        Rectangle attackHitbox = knight.getAttackHitbox();
        if (attackHitbox == null) return;

        for (BreakableWall wall : gameMap.getBreakableWalls()) {
            if (wall.isPassable()) continue;
            if (attackedWallsThisSwing.contains(wall)) continue;
            if (!attackHitbox.overlaps(wall.getBounds())) continue;

            attackedWallsThisSwing.add(wall);
            boolean stateChanged = wall.hit();

            if (stateChanged && wall.isPassable()) {
                gameMap.removeBreakableWallCollider(wall);
                gameMap.removeBlackMask();
            }
        }
    }

    /**
     * Damages enemies within the nail hitbox.
     * @param enemies the current enemy list from EnemySystem.
     */
    public void resolveEnemyAttack(Knight knight, List<Enemy> enemies) {
        Rectangle attackHitbox = knight.getAttackHitbox();
        if (attackHitbox == null) return;

        for (Enemy enemy : enemies) {
            if (enemy.isDead() || attackedEnemiesThisSwing.contains(enemy)) continue;
            if (!attackHitbox.overlaps(enemy.getBounds())) continue;

            attackedEnemiesThisSwing.add(enemy);
            knight.onNailHit(enemy);
        }
    }

    /** Called via EventBus when the Knight's nail hits an enemy. */
    public void resolveNailHitOnEnemy(Knight knight, Enemy enemy) {
        CharmEffects charms = knight.getCharmEffects();

        float damage = Knight.SLASH_DAMAGE * charms.getNailDamageMultiplier();
        float soulGain = Knight.SLASH_SOUL_GAIN * charms.getSoulGainMultiplier();

        enemy.takeDamage(damage, knight.getAttackDirection(), charms.getEnemyKnockbackMultiplier());
        knight.gainSoul(soulGain);

        // pogo
        if (knight.getAttackDirection() == Direction.DOWN) {
            knight.onDownAttackBounce();
        }
    }

    // --- Contact damage ---

    /** Resolves contact damage between the Knight and any living enemy. */
    public void resolveKnightEnemyContact(Knight knight, List<Enemy> enemies) {
        if (knight.isInvincible() || knight.isDead()) return;
        if (knight.isDashingThroughEnemies()) return;

        Rectangle knightHitbox = knight.getBounds();

        for (Enemy enemy : enemies) {
            if (!enemy.canDamagePlayer()) continue;
            if (!knightHitbox.overlaps(enemy.getBounds())) continue;

            Direction knockback = (knight.getBounds().x < enemy.getBounds().x) ? Direction.LEFT : Direction.RIGHT;
            knight.takeDamage(knockback);
            EventBus.getInstance().publish(GameEvent.CAMERA_SHAKE);
            break;
        }
    }

    /**
     * Sharp Shadow: while the Knight dashes through enemies, damages every living enemy
     * overlapping the Knight's hitbox.
     */
    public void resolveSharpShadowDashDamage(Knight knight, List<Enemy> enemies) {
        if (!knight.isDashingThroughEnemies()) return;

        Rectangle knightHitbox = knight.getBounds();
        CharmEffects charms = knight.getCharmEffects();

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            if (!knightHitbox.overlaps(enemy.getBounds())) continue;
            if (!knight.tryMarkDashHit(enemy)) continue;

            float damage = Knight.SLASH_DAMAGE * charms.getNailDamageMultiplier();
            Direction knockback = (knight.getBounds().x < enemy.getBounds().x) ? Direction.LEFT : Direction.RIGHT;
            enemy.takeDamage(damage, knockback, charms.getEnemyKnockbackMultiplier());
            knight.gainSoul(Knight.SLASH_SOUL_GAIN * charms.getSoulGainMultiplier());
        }
    }

    /** Resolves contact damage between the Knight and all active lasers. */
    public void resolveLaserKnightContact(Knight knight, List<Laser> lasers) {
        if (knight.isInvincible() || knight.isDead()) return;

        Rectangle knightHitbox = knight.getBounds();

        for (Laser laser : lasers) {
            if (!laser.isActive()) continue;
            if (!knightHitbox.overlaps(laser.getBounds())) continue;

            Direction knockback = (knight.getBounds().x < laser.getBounds().x) ? Direction.LEFT : Direction.RIGHT;
            knight.takeDamage(knockback);
            break; // only take damage from one laser per frame
        }
    }

    // --- Boss attack hitboxes ---

    /** Resolves damage from the False Knight's special attack hitboxes against the Knight. */
    public void resolveFalseKnightAttackHitboxes(Knight knight, FalseKnight activeFalseKnight) {
        if (activeFalseKnight == null) return;
        if (knight.isInvincible() || knight.isDead()) return;

        Rectangle knightHitbox = knight.getBounds();

        // Mace slam hitbox
        Rectangle slamHitbox = activeFalseKnight.getSlamHitbox();
        if (slamHitbox != null && knightHitbox.overlaps(slamHitbox)) {
            Direction knockback = (knightHitbox.x < slamHitbox.x) ? Direction.LEFT : Direction.RIGHT;
            knight.takeDamage(knockback);
            return;
        }

        // Jump attack hitbox
        Rectangle jumpHitbox = activeFalseKnight.getJumpAttackHitbox();
        if (jumpHitbox != null && knightHitbox.overlaps(jumpHitbox)) {
            Direction knockback = (knightHitbox.x < jumpHitbox.x) ? Direction.LEFT : Direction.RIGHT;
            knight.takeDamage(knockback);
        }
    }

    // --- Reset ---

    /** Clears swing-tracking state (called on map change). */
    public void reset() {
        attackedEnemiesThisSwing.clear();
        attackedWallsThisSwing.clear();
    }
}
