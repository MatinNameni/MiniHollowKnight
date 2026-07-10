package com.github.matinnameni.minihollowknight.model.enums;

/**
 * Every achievement the player can unlock.
 */
public enum AchievementType {
    COMPLETION (1, "achievements.completion", "achievements.completionDescription"),
    SPEEDRUN (2, "achievements.speedrun", "achievements.speedrunDescription"),
    TRUE_HUNTER (3, "achievements.trueHunter", "achievements.trueHunterDescription"),
    DEFEAT_FALSE_KNIGHT (4, "achievements.defeatFalseKnight", "achievements.defeatFalseKnightDescription"),
    CHARMED (5, "achievements.charmed", "achievements.charmedDescription");

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
