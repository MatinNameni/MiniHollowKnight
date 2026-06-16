package com.github.matinnameni.minihollowknight.event;

/**
 * All game events
 */
public enum GameEvent {

    // --- Player ---
    PLAYER_DAMAGED,
    PLAYER_HEALED,
    PLAYER_DIED,
    PLAYER_RESPAWNED,
    PLAYER_SOUL_GAINED,
    PLAYER_SOUL_SPENT,
    PLAYER_FOCUS_START,
    PLAYER_FOCUS_CANCEL,
    PLAYER_FOCUS_COMPLETE,
    PLAYER_DASH,
    PLAYER_DOUBLE_JUMP,
    PLAYER_NAIL_HIT,
    PLAYER_SPELL_CAST,
    PLAYER_COLLECTED_GEO,

    // --- Enemies ---
    ENEMY_DAMAGED,
    ENEMY_DIED,
    ENEMY_SPAWNED,

    // --- Boss ---
    FALSE_KNIGHT_FIGHT_STARTED,
    FALSE_KNIGHT_PHASE2_STARTED,
    FALSE_KNIGHT_STUNNED,
    FALSE_KNIGHT_DEFEATED,

    // --- Achievements ---
    ACHIEVEMENT_UNLOCKED,

    // --- Game lifecycle ---
    GAME_SAVED,
    GAME_LOADED,
    GAME_PAUSED,
    GAME_RESUMED,
    GAME_OVER,
    GAME_COMPLETED,

    // --- UI / HUD ---
    UI_SHOW_DIALOGUE,
    UI_HIDE_DIALOGUE,
    UI_INVENTORY_OPENED,
    UI_INVENTORY_CLOSED,
    UI_ACHIEVEMENT_POPUP,   // payload = achievement id (String)
}
