package com.github.matinnameni.minihollowknight.model.enums;

/**
 * All Charms in the game.
 */
public enum CharmType {
    SOUL_CATCHER (1, 2, "Soul Catcher"),
    DASHMASTER (2, 2, "Dashmaster"),
    UNBREAKABLE_STRENGTH(3, 2, "Unbreakable Strength"),
    QUICK_SLASH (4, 3, "Quick Slash"),
    QUICK_FOCUS (5, 3, "Quick Focus"),
    HEAVY_BLOW (6, 2, "Heavy Blow"),
    SHARP_SHADOW (7, 2, "Sharp Shadow"),
    VOID_HEART (8, 1, "Void Heart");

    public final int id;
    public final int notches;
    public final String displayName;

    CharmType(int id, int notches, String displayName) {
        this.id = id;
        this.notches = notches;
        this.displayName = displayName;
    }

    public static CharmType fromId(int id) {
        for (CharmType charm : values()) {
            if (charm.id == id) return charm;
        }
        return null;
    }
}
