package com.github.matinnameni.minihollowknight.model.enums;

/**
 * All Charms in the game.
 */
public enum CharmType {
    SOUL_CATCHER (1, 1),
    DASHMASTER (2, 1),
    UNBREAKABLE_STRENGTH(3, 1),
    QUICK_SLASH (4, 1),
    QUICK_FOCUS (5, 1),
    HEAVY_BLOW (6, 1),
    SHARP_SHADOW (7, 1),
    VOID_HEART (8, 1);

    public final int id;
    public final int notches;

    CharmType(int id, int notches) {
        this.id = id;
        this.notches = notches;
    }

    public static CharmType fromId(int id) {
        for (CharmType charm : values()) {
            if (charm.id == id) return charm;
        }
        return null;
    }
}
