package com.github.matinnameni.minihollowknight.model.enums;

/**
 * Every achievement the player can unlock.
 */
public enum AchievementType {
    COMPLETION (1, "Completion", "Complete the game"),
    SPEEDRUN (2, "Speedrun", "Complete the game in under 2 hours"),
    TRUE_HUNTER (3, "True Hunter", "Defeat every type of enemy"),
    DEFEAT_FALSE_KNIGHT (4, "Defeat False Knight", "Defeat the False Knight boss"),
    CHARMED (5, "Charmed", "Acquire your first Charm");

    public final int id;
    public final String displayName;
    public final String description;

    AchievementType(int id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public static AchievementType fromId(int id) {
        for (AchievementType achievement : values()) {
            if (achievement.id == id) return achievement;
        }
        return null;
    }
}
