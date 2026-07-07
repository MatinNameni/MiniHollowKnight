package com.github.matinnameni.minihollowknight.controller.system;

import com.github.matinnameni.minihollowknight.model.Door;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

/**
 * Manages world-level objects: doors, breakable walls, and display text.
 */
public class WorldSystem {

    /** Display text to be shown by the view layer (null = nothing to show). */
    private String displayText;

    public WorldSystem() {
    }

    /**
     * Updates all doors and resolves knight-door collisions.
     */
    public void updateDoors(float delta, Knight knight, TiledGameMap gameMap, KnightPhysicsSystem knightPhysics) {
        for (Door door : gameMap.getDoors()) {
            door.update(delta);

            if (door.isSolid() && knight.getBounds().overlaps(door)) {
                knightPhysics.resolveCollisionWithObstacle(knight, door);
            }
        }
    }

    /** Closes all doors in the current map. */
    public void closeAllDoors(TiledGameMap gameMap) {
        for (Door door : gameMap.getDoors()) {
            door.closeDoor();
        }
    }

    /** Opens all doors in the current map. */
    public void openAllDoors(TiledGameMap gameMap) {
        for (Door door : gameMap.getDoors()) {
            door.openDoor();
        }
    }

    // --- Display text ---

    public void setDisplayText(String text) {
        this.displayText = text;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void resetDisplayText() {
        displayText = null;
    }

    /** Resets world system state (called on map change). */
    public void reset() {
        displayText = null;
    }
}
