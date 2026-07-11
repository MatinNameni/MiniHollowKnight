package com.github.matinnameni.minihollowknight.controller.system;

import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.object.Door;
import com.github.matinnameni.minihollowknight.model.object.GridObject;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides shared collision-detection primitives used by all other systems.
 */
public class CollisionSystem {

    private TiledGameMap gameMap;

    public CollisionSystem() {
    }

    /** Must be called whenever the active map changes. */
    public void setGameMap(TiledGameMap gameMap) {
        this.gameMap = gameMap;
    }

    public TiledGameMap getGameMap() {
        return gameMap;
    }

    // --- Overlap detection ---

    /**
     * Returns all map colliders that overlap {@code rect}, paired with the
     * direction from which the overlap should be resolved.
     */
    public Map<GridObject, Direction> getOverlappingObjects(Rectangle rect) {
        Map<GridObject, Direction> result = new LinkedHashMap<>();

        float x = rect.x;
        float y = rect.y;
        float width = rect.width;
        float height = rect.height;

        for (GridObject platform : getColliders()) {
            if (!rect.overlaps(platform)) {
                continue;
            }

            float pushLeft  = (x + width) - platform.x;
            float pushRight = (platform.x + platform.width) - x;
            float pushUp    = (platform.y + platform.height) - y;
            float pushDown  = (y + height) - platform.y;

            float minPush = Math.min(
                Math.min(pushLeft, pushRight),
                Math.min(pushUp, pushDown)
            );

            if (minPush == pushUp) {
                result.put(platform, Direction.UP);
            } else if (minPush == pushDown) {
                result.put(platform, Direction.DOWN);
            } else if (minPush == pushLeft) {
                result.put(platform, Direction.LEFT);
            } else {
                result.put(platform, Direction.RIGHT);
            }
        }

        return result;
    }

    // --- Proximity helpers ---

    /** @return true if a wall is within {@code epsilon} pixels of the given rectangle. */
    public boolean isAdjacentToWall(Rectangle rect) {
        return isAdjacentToWall(rect, 1f);
    }

    public boolean isAdjacentToWall(Rectangle rect, float epsilon) {
        for (GridObject platform : getColliders()) {
            if (rect.y + rect.height <= platform.y) continue;
            if (rect.y >= platform.y + platform.height) continue;

            float rightGap = Math.abs(platform.x - (rect.x + rect.width));
            if (rightGap >= 0f && rightGap <= epsilon) return true;

            float leftGap = Math.abs(rect.x - (platform.x + platform.width));
            if (leftGap >= 0f && leftGap <= epsilon) return true;
        }
        return false;
    }

    /** @return true if a floor is within {@code epsilon} pixels below the given rectangle. */
    public boolean isOnFloor(Rectangle rect) {
        return isOnFloor(rect, 1f);
    }

    public boolean isOnFloor(Rectangle rect, float epsilon) {
        for (GridObject platform : getColliders()) {
            if (rect.x + rect.width <= platform.x) continue;
            if (rect.x >= platform.x + platform.width) continue;

            float gap = Math.abs(rect.y - (platform.y + platform.height));
            if (gap >= 0f && gap <= epsilon) return true;
        }
        for (Door door : getDoors()) {
            if (!door.isSolid()) continue;
            if (rect.x + rect.width <= door.x) continue;
            if (rect.x >= door.x + door.width) continue;

            float gap = Math.abs(rect.y - (door.y + door.height));
            if (gap >= 0f && gap <= epsilon) return true;
        }
        return false;
    }

    /** @return true if there is a floor collider directly beneath {@code probe}. */
    public boolean hasFloorBelow(Rectangle probe) {
        for (GridObject platform : getColliders()) {
            if (platform.isDeadly) continue;
            if (probe.overlaps(platform)) return true;
        }
        return false;
    }

    // --- Convenience accessors ---

    private List<GridObject> getColliders() {
        return gameMap != null ? gameMap.getColliders() : List.of();
    }

    private List<Door> getDoors() {
        return gameMap != null ? gameMap.getDoors() : List.of();
    }
}
