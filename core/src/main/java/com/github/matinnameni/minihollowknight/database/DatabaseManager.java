package com.github.matinnameni.minihollowknight.database;

import com.github.matinnameni.minihollowknight.model.GameData;
import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.model.enums.AchievementType;
import com.github.matinnameni.minihollowknight.model.enums.CharmType;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Translates between {@link GameData} and {@link Settings} objects and
 * the SQLite database.
 */
public class DatabaseManager {
    private Connection connection;

    /** Initializes the database and creates the directory and file if not existing yet */
    public void init() throws SQLException {
        String dbPath = resolveDBPath();
        ensureDirectoryExists(dbPath);
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        enableForeignKeys();
        createTables();
    }

    /** Closes the JDBC connection. */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("[DatabaseManager] Warning: failed to close connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    // --- Settings ---

    /** Persists the given {@code settings} to the database */
    public void saveSettings(Settings settings) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DatabaseConfig.UPSERT_SETTINGS)) {
            statement.setFloat(1, settings.getMusicVolume());
            statement.setFloat(2, settings.getSfxVolume());
            statement.setInt(3, settings.isMusicEnabled() ? 1 : 0);
            statement.setInt(4, settings.isSfxEnabled()   ? 1 : 0);
            statement.setFloat(5, settings.getBrightness());
            statement.setString(6, settings.getLanguage());
            statement.setInt(7, settings.getKeyLeft());
            statement.setInt(8, settings.getKeyRight());
            statement.setInt(9, settings.getKeyUp());
            statement.setInt(10, settings.getKeyDown());
            statement.setInt(11, settings.getKeyJump());
            statement.setInt(12, settings.getKeyAttack());
            statement.setInt(13, settings.getKeyDash());
            statement.setInt(14, settings.getKeyFocus());
            statement.setInt(15, settings.getKeyCast());
            statement.setInt(16, settings.getKeyInteract());
            statement.setInt(17, settings.getKeyInventory());
            statement.setInt(18, settings.getKeyPause());
            statement.executeUpdate();
        }
    }

    /** Loads settings from the database. */
    public Settings loadSettings() throws SQLException {
        try (
            PreparedStatement statement = connection.prepareStatement(DatabaseConfig.LOAD_SETTINGS);
            ResultSet resultSet = statement.executeQuery()
        ) {

            if (!resultSet.next()) return null;

            Settings settings = new Settings();
            settings.setMusicVolume(resultSet.getFloat("music_volume"));
            settings.setSfxVolume(resultSet.getFloat("sfx_volume"));
            settings.setMusicEnabled(resultSet.getInt("music_enabled") == 1);
            settings.setSfxEnabled(resultSet.getInt("sfx_enabled") == 1);
            settings.setBrightness(resultSet.getFloat("brightness"));
            settings.setLanguage(resultSet.getString("language"));
            settings.setKeyLeft(resultSet.getInt("key_left"));
            settings.setKeyRight(resultSet.getInt("key_right"));
            settings.setKeyUp(resultSet.getInt("key_up"));
            settings.setKeyDown(resultSet.getInt("key_down"));
            settings.setKeyJump(resultSet.getInt("key_jump"));
            settings.setKeyAttack(resultSet.getInt("key_attack"));
            settings.setKeyDash(resultSet.getInt("key_dash"));
            settings.setKeyFocus(resultSet.getInt("key_focus"));
            settings.setKeyCast(resultSet.getInt("key_cast"));
            settings.setKeyInteract(resultSet.getInt("key_interact"));
            settings.setKeyInventory(resultSet.getInt("key_inventory"));
            settings.setKeyPause(resultSet.getInt("key_pause"));
            return settings;
        }
    }

    // --- Save slots ---

    /** Persists the given {@code data} to the database */
    public void saveGameData(GameData data) throws SQLException {
        if (data.slotId < 1) {
            throw new IllegalArgumentException("slotId must be >= 1, got: " + data.slotId);
        }

        connection.setAutoCommit(false);
        try {
            upsertSlotRow(data);
            replaceCharms(data);
            replaceAchievements(data);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /** Loads a save slot by its id. */
    public GameData loadGameData(int slotId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DatabaseConfig.LOAD_SAVE_SLOT)) {
            statement.setInt(1, slotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return null;
                GameData data = rowToGameData(resultSet);
                loadCharmsInto(data);
                loadAchievementsInto(data);
                return data;
            }
        }
    }

    /** Loads every save slot present in the database, ordered by {@code slot_id}. */
    public List<GameData> loadAllGameData() throws SQLException {
        List<GameData> list = new ArrayList<>();
        try (
            PreparedStatement statement = connection.prepareStatement(DatabaseConfig.LOAD_ALL_SAVE_SLOTS);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                GameData data = rowToGameData(resultSet);
                loadCharmsInto(data);
                loadAchievementsInto(data);
                list.add(data);
            }
        }
        return list;
    }

    /** Deletes a save slot. */
    public void deleteGameData(int slotId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DatabaseConfig.DELETE_SAVE_SLOT)) {
            statement.setInt(1, slotId);
            statement.executeUpdate();
        }
    }

    // --- Achievements (global / cross-slot) ---

    /**
     * Loads every distinct achievement id unlocked across all save slots.
     */
    public Set<Integer> loadAllUnlockedAchievementIds() throws SQLException {
        Set<Integer> ids = new HashSet<>();
        try (
            PreparedStatement statement = connection.prepareStatement(DatabaseConfig.LOAD_ALL_ACHIEVEMENTS);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                ids.add(resultSet.getInt("achievement_id"));
            }
        }
        return ids;
    }

    /**
     * Persists a single achievement unlock to the given slot's row immediately.
     * Uses {@code INSERT OR IGNORE} so calling it for an already-unlocked
     * achievement is a safe no-op.
     */
    public void saveAchievement(int slotId, int achievementId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DatabaseConfig.INSERT_ACHIEVEMENT)) {
            statement.setInt(1, slotId);
            statement.setInt(2, achievementId);
            statement.executeUpdate();
        }
    }

    // --- Killed enemy types (global, for True Hunter) ---

    /**
     * Loads the set of every enemy type name the player has ever killed (across all runs).
     */
    public Set<String> loadAllKilledEnemyTypes() throws SQLException {
        Set<String> types = new HashSet<>();
        try (
            PreparedStatement statement = connection.prepareStatement(DatabaseConfig.LOAD_ALL_KILLED_ENEMY_TYPES);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                types.add(resultSet.getString("enemy_type"));
            }
        }
        return types;
    }

    /**
     * Records that an enemy of {@code enemyType} was killed. Uses
     * {@code INSERT OR IGNORE} so duplicate kills are a safe no-op.
     */
    public void saveKilledEnemyType(String enemyType) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DatabaseConfig.INSERT_KILLED_ENEMY_TYPE)) {
            statement.setString(1, enemyType);
            statement.executeUpdate();
        }
    }

    // --- Helpers ---

    /** Returns the absolute path to the Database file inside the user's home directory. */
    private static String resolveDBPath() {
        return System.getProperty("user.home")
            + File.separator
            + DatabaseConfig.DB_DIR_PREFIX
            + File.separator
            + DatabaseConfig.DB_NAME;
    }

    /** Creates every directory component of {@code dbPath} if needed. */
    private static void ensureDirectoryExists(String dbPath) throws SQLException {
        File dir = new File(dbPath).getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs()) {
            throw new SQLException("Cannot create database directory: " + dir.getAbsolutePath());
        }
    }

    /** Switches on SQLite foreign-key enforcement. */
    private void enableForeignKeys() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }

    /** Creates every table the game needs. */
    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String query : DatabaseConfig.CREATE_TABLES_SQL) {
                statement.execute(query);
            }
        }
    }

    /** Upserts the main {@code save_slots} row. */
    private void upsertSlotRow(GameData data) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DatabaseConfig.UPSERT_SAVE_SLOT)) {
            statement.setInt(1, data.slotId);
            statement.setString(2, data.slotName);
            statement.setFloat(3, data.playTimeSeconds);
            statement.setLong(4, data.lastSavedAt);
            statement.setInt(5, data.currentEnvironment);
            statement.setFloat(6, data.playerX);
            statement.setFloat(7, data.playerY);
            statement.setInt(8, data.masks);
            statement.setInt(9, data.maxMasks);
            statement.setFloat(10, data.soul);
            statement.setInt(11, data.totalNotches);
            statement.setInt(12, data.totalDeaths);
            statement.setInt(13, data.enemiesKilled);
            statement.setInt(14, data.bossesDefeated);
            statement.executeUpdate();
        }
    }

    /** Deletes existing charm rows, then inserts the current {@code collectedCharms} set.*/
    private void replaceCharms(GameData data) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement(DatabaseConfig.DELETE_CHARMS_FOR_SLOT)) {
            deleteStatement.setInt(1, data.slotId);
            deleteStatement.executeUpdate();
        }
        try (PreparedStatement insertStatement = connection.prepareStatement(DatabaseConfig.INSERT_CHARM)) {
            for (CharmType charm : data.collectedCharms) {
                insertStatement.setInt(1, data.slotId);
                insertStatement.setInt(2, charm.id);
                insertStatement.setInt(3, data.equippedCharms.contains(charm) ? 1 : 0);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    /** Delete existing achievement rows, then inserts the current {@code unlockedAchievements} set. */
    private void replaceAchievements(GameData data) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement(DatabaseConfig.DELETE_ACHIEVEMENTS_FOR_SLOT)) {
            deleteStatement.setInt(1, data.slotId);
            deleteStatement.executeUpdate();
        }
        try (PreparedStatement insertStatement = connection.prepareStatement(DatabaseConfig.INSERT_ACHIEVEMENT)) {
            for (AchievementType achievement : data.unlockedAchievements) {
                insertStatement.setInt(1, data.slotId);
                insertStatement.setInt(2, achievement.id);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    /** Maps the current {@link ResultSet} row to a new {@link GameData}. */
    private static GameData rowToGameData(ResultSet resultSet) throws SQLException {
        GameData data = new GameData();
        data.slotId = resultSet.getInt("slot_id");
        data.slotName = resultSet.getString("slot_name");
        data.playTimeSeconds = resultSet.getFloat("play_time_seconds");
        data.lastSavedAt = resultSet.getLong("last_saved_at");
        data.currentEnvironment = resultSet.getInt("current_environment");
        data.playerX = resultSet.getFloat("player_x");
        data.playerY = resultSet.getFloat("player_y");
        data.masks = resultSet.getInt("masks");
        data.maxMasks = resultSet.getInt("max_masks");
        data.soul = resultSet.getInt("soul");
        data.totalNotches = resultSet.getInt("total_notches");
        data.totalDeaths = resultSet.getInt("total_deaths");
        data.enemiesKilled = resultSet.getInt("enemies_killed");
        data.bossesDefeated = resultSet.getInt("bosses_defeated");
        return data;
    }

    /** Populates {@code data.collectedCharms} and {@code data.equippedCharms} from the DB. */
    private void loadCharmsInto(GameData data) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DatabaseConfig.LOAD_CHARMS_FOR_SLOT)) {
            statement.setInt(1, data.slotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    CharmType charm = CharmType.fromId(resultSet.getInt("charm_id"));
                    if (charm == null) continue;
                    data.collectedCharms.add(charm);
                    if (resultSet.getInt("is_equipped") == 1) {
                        data.equippedCharms.add(charm);
                    }
                }
            }
        }
    }

    /** Populates {@code data.unlockedAchievements} from the DB. */
    private void loadAchievementsInto(GameData data) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DatabaseConfig.LOAD_ACHIEVEMENTS_FOR_SLOT)) {
            statement.setInt(1, data.slotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    AchievementType ach = AchievementType.fromId(resultSet.getInt("achievement_id"));
                    if (ach == null) continue;
                    data.unlockedAchievements.add(ach);
                }
            }
        }
    }
}
