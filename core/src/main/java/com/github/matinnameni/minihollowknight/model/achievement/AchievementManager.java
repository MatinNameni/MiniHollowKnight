package com.github.matinnameni.minihollowknight.model.achievement;

import com.github.matinnameni.minihollowknight.model.database.DatabaseManager;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Crawlid;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Crystallized;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.entity.enemies.FalseKnight;
import com.github.matinnameni.minihollowknight.model.entity.enemies.HuskHornhead;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Mossfly;
import com.github.matinnameni.minihollowknight.model.enums.AchievementType;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Global, app-lifetime store of every achievement the player has unlocked
 * across all save slots.
 */
public class AchievementManager {

    /** Speedrun threshold. for {@code AchievementType.SPEEDRUN}. */
    public static final float SPEEDRUN_THRESHOLD_SECONDS = 2f * 60f * 60f;

    /**
     * Every enemy type that must be killed at least once to unlock
     * {@link AchievementType#TRUE_HUNTER}
     */
    private static final Set<String> REQUIRED_ENEMY_TYPES = new LinkedHashSet<>();
    static {
        REQUIRED_ENEMY_TYPES.add(Crawlid.class.getSimpleName());
        REQUIRED_ENEMY_TYPES.add(Mossfly.class.getSimpleName());
        REQUIRED_ENEMY_TYPES.add(HuskHornhead.class.getSimpleName());
        REQUIRED_ENEMY_TYPES.add(Crystallized.class.getSimpleName());
        REQUIRED_ENEMY_TYPES.add(FalseKnight.class.getSimpleName());
    }

    private static AchievementManager instance;

    private final DatabaseManager database;

    /** Globally unlocked achievements (union of all slots). */
    private final Set<AchievementType> unlocked = EnumSet.noneOf(AchievementType.class);

    /** Cumulative set of enemy types the player has ever killed. */
    private final Set<String> killedEnemyTypes = new LinkedHashSet<>();

    /** The currently active save slot, or {@code null} when sitting in the menus. */
    private GameData activeGameData;

    private AchievementManager(DatabaseManager database) {
        this.database = database;
    }

    /** Creates and initializes the singleton. Call once after the database is ready. */
    public static AchievementManager init(DatabaseManager database) {
        if (instance == null) {
            instance = new AchievementManager(database);
            instance.load();
        }
        return instance;
    }

    /** Returns the singleton, or {@code null} if not yet initialized. */
    public static AchievementManager getInstance() {
        return instance;
    }

    // --- Lifecycle / persistence ---

    /**
     * Loads the global achievement state from the database:
     * the union of all slots' unlocked achievements, plus every killed
     * enemy type.
     */
    private void load() {
        if (database == null) return;
        try {
            // Unlocked achievements: union across all save slots.
            for (Integer id : database.loadAllUnlockedAchievementIds()) {
                AchievementType type = AchievementType.fromId(id);
                if (type != null) unlocked.add(type);
            }
            // Killed enemy types: global table.
            killedEnemyTypes.addAll(database.loadAllKilledEnemyTypes());
        } catch (SQLException e) {
            System.err.println("[AchievementManager] Failed to load achievements from DB: " + e.getMessage());
        }
        // Re-evaluate True Hunter in case the loaded kill set now satisfies it.
        checkTrueHunter();
    }

    /** Binds the active save slot so unlocks are mirrored into per-slot data. */
    public void setActiveGameData(GameData gameData) {
        this.activeGameData = gameData;
        if (gameData != null) {
            // Merge any achievements this slot already has into the global set,
            // and vice versa, so loading an old save keeps everything in sync.
            unlocked.addAll(gameData.unlockedAchievements);
            gameData.unlockedAchievements.addAll(unlocked);
        }
    }

    /** Clears the active save slot reference (e.g. when returning to the main menu). */
    public void clearActiveGameData() {
        this.activeGameData = null;
    }

    /** @return the currently active save slot, or {@code null} when sitting in the menus. */
    public GameData getActiveGameData() {
        return activeGameData;
    }

    // --- Queries ---

    /** @return {@code true} if {@code achievement} has been unlocked globally. */
    public boolean isUnlocked(AchievementType achievement) {
        return unlocked.contains(achievement);
    }

    /** @return how many achievements are currently unlocked. */
    public int unlockedCount() {
        return unlocked.size();
    }

    /** @return total number of achievements defined in the game. */
    public int totalCount() {
        return AchievementType.values().length;
    }

    /** @return the immutable set of enemy type names that still need to be killed for True Hunter. */
    public Set<String> missingEnemyTypes() {
        Set<String> missing = new LinkedHashSet<>(REQUIRED_ENEMY_TYPES);
        missing.removeAll(killedEnemyTypes);
        return missing;
    }

    // --- Mutations ---

    /**
     * Unlocks {@code achievement} globally if it isn't already.
     *
     * @return {@code true} if this call actually unlocked the achievement.
     */
    public boolean unlock(AchievementType achievement) {
        if (achievement == null) return false;
        if (unlocked.contains(achievement)) return false;

        unlocked.add(achievement);

        if (activeGameData != null) {
            activeGameData.unlockAchievement(achievement);
            persistAchievement(activeGameData.slotId, achievement);
        }

        EventBus.getInstance().publish(GameEvent.ACHIEVEMENT_UNLOCKED, achievement);
        EventBus.getInstance().publish(GameEvent.UI_ACHIEVEMENT_POPUP, achievement);
        return true;
    }

    /**
     * Records that an enemy of the given type was killed and, once every
     * required type has been killed at least once, unlocks True Hunter.
     *
     * @return {@code true} if this kill was the first one for its type.
     */
    public boolean recordEnemyKill(Enemy enemy) {
        if (enemy == null) return false;
        String typeName = enemy.getClass().getSimpleName();
        boolean firstKill = killedEnemyTypes.add(typeName);
        if (firstKill) {
            persistKilledEnemyType(typeName);
            checkTrueHunter();
        }
        return firstKill;
    }

    /** Unlocks the Completion achievement, and Speedrun if the active playtime is under the threshold. */
    public void unlockCompletion() {
        unlock(AchievementType.COMPLETION);

        float playtime = (activeGameData != null) ? activeGameData.playTimeSeconds : 0f;
        if (playtime <= SPEEDRUN_THRESHOLD_SECONDS) {
            unlock(AchievementType.SPEEDRUN);
        }
    }

    // --- Helpers ---

    /** Unlocks True Hunter if every required enemy type has been killed. */
    private void checkTrueHunter() {
        if (killedEnemyTypes.containsAll(REQUIRED_ENEMY_TYPES)) {
            unlock(AchievementType.TRUE_HUNTER);
        }
    }

    /** Writes a single achievement row into the active slot's DB table. */
    private void persistAchievement(int slotId, AchievementType achievement) {
        if (database == null || slotId < 1) return;
        try {
            database.saveAchievement(slotId, achievement.id);
        } catch (SQLException e) {
            System.err.println("[AchievementManager] Failed to persist achievement to DB: " + e.getMessage());
        }
    }

    /** Writes a single killed-enemy-type row into the global DB table. */
    private void persistKilledEnemyType(String typeName) {
        if (database == null) return;
        try {
            database.saveKilledEnemyType(typeName);
        } catch (SQLException e) {
            System.err.println("[AchievementManager] Failed to persist killed enemy type to DB: " + e.getMessage());
        }
    }

    // --- Getters ---

    Set<AchievementType> getUnlockedSnapshot() {
        return EnumSet.copyOf(unlocked.isEmpty() ? EnumSet.noneOf(AchievementType.class) : unlocked);
    }

    Set<String> getKilledEnemyTypesSnapshot() {
        return new HashSet<>(killedEnemyTypes);
    }
}
