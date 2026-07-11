package com.github.matinnameni.minihollowknight.model.data;

import com.github.matinnameni.minihollowknight.model.enums.AchievementType;
import com.github.matinnameni.minihollowknight.model.enums.CharmType;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;

import java.util.*;

/**
 * The complete state of one save slot.
 */
public class GameData {

    // --- Slot metadata ---
    public int slotId = -1; // One-based
    public String slotName = "";
    public float playTimeSeconds = 0f;
    public long lastSavedAt = 0L; // Timestamp of the last save (milliseconds since epoch).

    // --- Player position ---
    public int currentEnvironment = GameEnvironment.FORGOTTEN_CROSSROADS.id; // Environment id
    public float playerX = -1f; // Knight's X position within the current environment
    public float playerY = -1f; // Knight's Y position within the current environment

    // --- Masks ---
    public int masks = 5; // 0..maxMasks
    public int maxMasks = 5;

    // --- Soul ---
    public float soul = 0f; // 0..MAX_SOUL
    public static final float MAX_SOUL = 99f;

    // --- Charms ---
    public Set<CharmType> collectedCharms = EnumSet.noneOf(CharmType.class);
    public Set<CharmType> equippedCharms = EnumSet.noneOf(CharmType.class);
    public int totalNotches = 3;

    // --- Achievements ---
    public Set<AchievementType> unlockedAchievements = EnumSet.noneOf(AchievementType.class);

    // --- Run statistics ---
    public int totalDeaths = 0;
    public int enemiesKilled = 0;
    public int bossesDefeated = 0;

    // --- Helpers ---

    /** @return true if this slot has ever been saved to the database. */
    public boolean isPersisted() {
        return slotId >= 1;
    }

    /** Adds soul. */
    public void gainSoul(int amount) {
        soul = Math.min(soul + amount, MAX_SOUL);
    }

    /**
     * Spends soul.
     * @return true if there was enough soul to spend.
     */
    public boolean spendSoul(int amount) {
        if (soul < amount) return false;
        soul -= amount;
        return true;
    }

    /**
     * Reduces Hp by one mask.
     * @return true if the player is now dead.
     */
    public boolean takeDamage() {
        if (masks > 0) masks--;
        return masks == 0;
    }

    /** Restores one mask. */
    public void healOneMask() {
        if (masks < maxMasks) masks++;
    }

    /**
     * Equips a charm.
     * @return true if the charm was equipped
     */
    public boolean equipCharm(CharmType charm) {
        if (!collectedCharms.contains(charm)) return false;
        if (equippedCharms.contains(charm))   return false;
        if (usedNotches() + charm.notches > totalNotches) return false;
        equippedCharms.add(charm);
        return true;
    }

    /** Removes a charm from the equipped set. */
    public void unequipCharm(CharmType charm) {
        equippedCharms.remove(charm);
    }

    /** Total notches that has been used. */
    public int usedNotches() {
        int used = 0;
        for (CharmType equippedCharm : equippedCharms) used += equippedCharm.notches;
        return used;
    }

    /**
     * Marks {@code charm} as collected by the player so it shows up in the
     * inventory and becomes equippable.
     *
     * @return true if this was a new collection
     */
    public boolean collectCharm(CharmType charm) {
        return collectedCharms.add(charm);
    }

    /** @return true if the player owns {@code charm}. */
    public boolean hasCollected(CharmType charm) {
        return collectedCharms.contains(charm);
    }

    /** Grants default charms available throughout the gameplay. */
    public void grantDefaultCharms() {
        for (CharmType charm : CharmType.values()) {
            if (charm == CharmType.VOID_HEART) continue;
            collectCharm(charm);
        }
    }

    /**
     * Unlocks an achievement.
     * @return true if this was a new unlock
     */
    public boolean unlockAchievement(AchievementType achievement) {
        return unlockedAchievements.add(achievement);
    }

    /**
     * Resets the data so this slot becomes a new game and
     * keeps the slot id
     */
    public void resetToNewGame() {
        currentEnvironment = 0;
        playerX = 0f;
        playerY = 0f;
        masks = 5;
        maxMasks = 5;
        soul = 0;
        totalDeaths = 0;
        enemiesKilled = 0;
        bossesDefeated = 0;
        playTimeSeconds = 0f;
        collectedCharms.clear();
        equippedCharms.clear();
        unlockedAchievements.clear();
    }
}
