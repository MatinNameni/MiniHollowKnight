package com.github.matinnameni.minihollowknight.controller.system;

import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.GridObject;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.enums.Direction;

import java.util.Map;

/**
 * Handles all collision resolution for the Knight entity against the world geometry.
 */
public class KnightPhysicsSystem {

    private final CollisionSystem collisionSystem;

    public KnightPhysicsSystem(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }

    /**
     * Resolves all collisions between the Knight and map colliders for this frame.
     * Also updates grounded / hitting-wall state and safe positions.
     */
    public void resolveCollisions(Knight knight) {
        Rectangle knightHitbox = knight.getBounds();
        Map<GridObject, Direction> collisions = collisionSystem.getOverlappingObjects(knightHitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (direction == Direction.UP) {
                if (platform.isDeadly) {
                    knight.takeDamage(resolvePlatformDeathKnockback(knight));
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxY = platform.y + platform.height;
                knight.onFloorCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
                knight.setSafePosition(knight.getPosition().x, knight.getPosition().y);
            } else if (direction == Direction.DOWN) {
                if (platform.isDeadly) {
                    knight.takeDamage(resolvePlatformDeathKnockback(knight));
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxY = platform.y - knightHitbox.height;
                knight.onCeilingCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                if (platform.isDeadly) {
                    knight.takeDamage(resolvePlatformDeathKnockback(knight));
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxX = platform.x - knightHitbox.width;
                knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
                knight.setHittingWall(true);
            } else {
                if (platform.isDeadly) {
                    knight.takeDamage(resolvePlatformDeathKnockback(knight));
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxX = platform.x + platform.width;
                knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
            }
        }
    }

    /**
     * Resolves a collision between the Knight and a solid obstacle (e.g. a closed door).
     */
    public void resolveCollisionWithObstacle(Knight knight, GridObject obstacle) {
        if (!obstacle.overlaps(knight.getBounds())) {
            return;
        }

        if (obstacle.isDeadly) {
            knight.takeDamage(resolvePlatformDeathKnockback(knight));
            knight.goToLastSafePosition();
            return;
        }

        knight.setGrounded(false);

        Rectangle entityHitbox = knight.getBounds();

        float x = entityHitbox.x;
        float y = entityHitbox.y;
        float width = entityHitbox.width;
        float height = entityHitbox.height;

        float pushLeft  = (x + width) - obstacle.x;
        float pushRight = (obstacle.x + obstacle.width) - x;
        float pushUp    = (obstacle.y + obstacle.height) - y;
        float pushDown  = (y + height) - obstacle.y;

        float minPush = Math.min(
            Math.min(pushLeft, pushRight),
            Math.min(pushUp, pushDown)
        );

        if (minPush == pushUp) {
            float resolvedHitboxY = obstacle.y + obstacle.height;
            knight.onFloorCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
            knight.setSafePosition(knight.getPosition().x, knight.getPosition().y);
        } else if (minPush == pushDown) {
            float resolvedHitboxY = obstacle.y - entityHitbox.height;
            knight.onCeilingCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
            knight.setSafePosition(knight.getPosition().x, knight.getPosition().y);
        } else if (minPush == pushLeft) {
            float resolvedHitboxX = obstacle.x - entityHitbox.width;
            knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
        } else {
            float resolvedHitboxX = obstacle.x + obstacle.width;
            knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
        }

        // wall proximity check
        if (collisionSystem.isAdjacentToWall(knight.getBounds())) {
            knight.setHittingWall(true);
        }

        // floor proximity check
        if (collisionSystem.isOnFloor(knight.getBounds())) {
            knight.setGrounded(true);
        }
    }

    /** Determines the knockback direction when the Knight touches a deadly surface. */
    private Direction resolvePlatformDeathKnockback(Knight knight) {
        if (knight.getPosition().x - knight.getLastSafePosition().x > 0) {
            return Direction.LEFT;
        }
        return Direction.RIGHT;
    }
}
