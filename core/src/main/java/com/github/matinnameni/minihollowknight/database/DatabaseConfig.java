package com.github.matinnameni.minihollowknight.database;

import com.github.matinnameni.minihollowknight.model.achievement.AchievementManager;

/**
 * Central place that database configuration constants is being held.
 */
public final class DatabaseConfig {

    private DatabaseConfig() { }

    // ------------------------------------------------------------------ paths
    /** File name of the SQLite database. */
    public static final String DB_NAME = "minihollowknight.db";
    public static final String DB_DIR_PREFIX = ".minihollowknight";
    public static final int MAX_POOL_SIZE = 1;

    public static final String[] CREATE_TABLES_SQL = {
        // --- Settings ---
        """
            CREATE TABLE IF NOT EXISTS settings (
                id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),

                -- Audio
                music_volume REAL NOT NULL DEFAULT 1.0,
                sfx_volume REAL NOT NULL DEFAULT 1.0,
                music_enabled INTEGER NOT NULL DEFAULT 1,
                sfx_enabled INTEGER NOT NULL DEFAULT 1,

                -- Display
                brightness REAL NOT NULL DEFAULT 1.0,
                language TEXT NOT NULL DEFAULT 'en',

                -- Key bindings
                key_left INTEGER NOT NULL DEFAULT 21,
                key_right INTEGER NOT NULL DEFAULT 22,
                key_up INTEGER NOT NULL DEFAULT 19,
                key_down INTEGER NOT NULL DEFAULT 20,
                key_jump INTEGER NOT NULL DEFAULT 54,
                key_attack INTEGER NOT NULL DEFAULT 52,
                key_dash INTEGER NOT NULL DEFAULT 31,
                key_focus INTEGER NOT NULL DEFAULT 29,
                key_cast INTEGER NOT NULL DEFAULT 47,
                key_interact INTEGER NOT NULL DEFAULT 33,
                key_inventory INTEGER NOT NULL DEFAULT 37,
                key_pause INTEGER NOT NULL DEFAULT 111
            )
            """,

        // --- Save slots ---
        """
            CREATE TABLE IF NOT EXISTS save_slots (
                slot_id INTEGER PRIMARY KEY,
                slot_name TEXT NOT NULL DEFAULT '',
                play_time_seconds REAL NOT NULL DEFAULT 0.0,
                last_saved_at INTEGER NOT NULL DEFAULT 0,

                -- Player position
                current_environment INTEGER NOT NULL DEFAULT 0,
                player_x REAL NOT NULL DEFAULT 0.0,
                player_y REAL NOT NULL DEFAULT 0.0,

                -- Masks
                masks INTEGER NOT NULL DEFAULT 5,
                max_masks INTEGER NOT NULL DEFAULT 5,

                -- Soul
                soul INTEGER NOT NULL DEFAULT 0,
                total_notches INTEGER NOT NULL DEFAULT 3,

                -- Run statistics
                total_deaths INTEGER NOT NULL DEFAULT 0,
                enemies_killed INTEGER NOT NULL DEFAULT 0,
                bosses_defeated INTEGER NOT NULL DEFAULT 0
            )
            """,

        // --- Slot charms ---
        """
            CREATE TABLE IF NOT EXISTS slot_charms (
                slot_id INTEGER NOT NULL,
                charm_id INTEGER NOT NULL,
                is_equipped INTEGER NOT NULL DEFAULT 0,

                PRIMARY KEY (slot_id, charm_id),

                FOREIGN KEY (slot_id)
                    REFERENCES save_slots(slot_id)
                    ON DELETE CASCADE
            )
            """,

        // --- Slot achievements ---
        """
            CREATE TABLE IF NOT EXISTS slot_achievements (
                slot_id INTEGER NOT NULL,
                achievement_id INTEGER NOT NULL,

                PRIMARY KEY (slot_id, achievement_id),

                FOREIGN KEY (slot_id)
                    REFERENCES save_slots(slot_id)
                    ON DELETE CASCADE
            )
            """,

        // --- Killed enemy types (global, used for the True Hunter achievement) ---
        """
            CREATE TABLE IF NOT EXISTS killed_enemy_types (
                enemy_type TEXT PRIMARY KEY
            )
            """
    };

    // --- Settings queries ---

    public static final String UPSERT_SETTINGS = """
            INSERT INTO settings (id, music_volume, sfx_volume, music_enabled,
                                  sfx_enabled, brightness, language,
                                  key_left, key_right, key_up, key_down,
                                  key_jump, key_attack, key_dash,
                                  key_focus, key_cast, key_interact,
                                  key_inventory, key_pause)
            VALUES (1,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT(id) DO UPDATE SET
                music_volume = excluded.music_volume,
                sfx_volume = excluded.sfx_volume,
                music_enabled = excluded.music_enabled,
                sfx_enabled = excluded.sfx_enabled,
                brightness = excluded.brightness,
                language = excluded.language,
                key_left = excluded.key_left,
                key_right = excluded.key_right,
                key_up = excluded.key_up,
                key_down = excluded.key_down,
                key_jump = excluded.key_jump,
                key_attack = excluded.key_attack,
                key_dash = excluded.key_dash,
                key_focus = excluded.key_focus,
                key_cast = excluded.key_cast,
                key_interact = excluded.key_interact,
                key_inventory = excluded.key_inventory,
                key_pause = excluded.key_pause
            """;

    public static final String LOAD_SETTINGS =
        "SELECT music_volume, sfx_volume, music_enabled, sfx_enabled, " +
            "brightness, language, " +
            "key_left, key_right, key_up, key_down, key_jump, " +
            "key_attack, key_dash, key_focus, key_cast, " +
            "key_interact, key_inventory, key_pause " +
            "FROM settings WHERE id = 1";

    // --- Save slot queries ---

    public static final String UPSERT_SAVE_SLOT = """
            INSERT INTO save_slots (slot_id, slot_name, play_time_seconds, last_saved_at,
                                    current_environment, player_x, player_y,
                                    masks, max_masks, soul, total_notches,
                                    total_deaths, enemies_killed, bosses_defeated)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT(slot_id) DO UPDATE SET
                slot_name = excluded.slot_name,
                play_time_seconds = excluded.play_time_seconds,
                last_saved_at = excluded.last_saved_at,
                current_environment = excluded.current_environment,
                player_x = excluded.player_x,
                player_y = excluded.player_y,
                masks = excluded.masks,
                max_masks = excluded.max_masks,
                soul = excluded.soul,
                total_notches = excluded.total_notches,
                total_deaths = excluded.total_deaths,
                enemies_killed = excluded.enemies_killed,
                bosses_defeated = excluded.bosses_defeated
            """;

    public static final String LOAD_SAVE_SLOT =
        "SELECT slot_id, slot_name, play_time_seconds, last_saved_at, " +
            "current_environment, player_x, player_y, " +
            "masks, max_masks, soul, total_notches, " +
            "total_deaths, enemies_killed, bosses_defeated " +
            "FROM save_slots WHERE slot_id = ?";

    public static final String LOAD_ALL_SAVE_SLOTS =
        "SELECT slot_id, slot_name, play_time_seconds, last_saved_at, " +
            "current_environment, player_x, player_y, " +
            "masks, max_masks, soul, total_notches, " +
            "total_deaths, enemies_killed, bosses_defeated " +
            "FROM save_slots ORDER BY slot_id";

    public static final String DELETE_SAVE_SLOT =
        "DELETE FROM save_slots WHERE slot_id = ?";

    // --- Charm / Achievement queries ---

    public static final String DELETE_CHARMS_FOR_SLOT =
        "DELETE FROM slot_charms WHERE slot_id = ?";

    public static final String DELETE_ACHIEVEMENTS_FOR_SLOT =
        "DELETE FROM slot_achievements WHERE slot_id = ?";

    public static final String INSERT_CHARM =
        "INSERT OR IGNORE INTO slot_charms (slot_id, charm_id, is_equipped) " +
            "VALUES (?, ?, ?)";

    public static final String LOAD_CHARMS_FOR_SLOT =
        "SELECT charm_id, is_equipped FROM slot_charms WHERE slot_id = ?";

    public static final String INSERT_ACHIEVEMENT =
        "INSERT OR IGNORE INTO slot_achievements (slot_id, achievement_id) " +
            "VALUES (?, ?)";

    public static final String LOAD_ACHIEVEMENTS_FOR_SLOT =
        "SELECT achievement_id FROM slot_achievements WHERE slot_id = ?";

    public static final String LOAD_ALL_ACHIEVEMENTS =
        "SELECT DISTINCT achievement_id FROM slot_achievements";

    // --- Killed enemy type queries (global, for True Hunter) ---

    public static final String LOAD_ALL_KILLED_ENEMY_TYPES =
        "SELECT enemy_type FROM killed_enemy_types";

    public static final String INSERT_KILLED_ENEMY_TYPE =
        "INSERT OR IGNORE INTO killed_enemy_types (enemy_type) VALUES (?)";
}
