package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.database.DatabaseManager;
import com.github.matinnameni.minihollowknight.model.GameData;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.UiManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the start game screen.
 */
public class StartGameController {

    /** Total number of save slots the game supports. */
    public static final int TOTAL_SLOTS = 4;

    private final ScreenNavigator navigator;
    private final DatabaseManager database;

    /** Cached save-slot data. slot index 0 = save slot 1 */
    private final GameData[] slots = new GameData[TOTAL_SLOTS];

    public StartGameController(ScreenNavigator navigator) {
        this.navigator = navigator;
        this.database = UiManager.getInstance().getDatabase();
    }

    /**
     * Loads all save slots from the database into the internal cache.
     * Called once when the screen is shown.
     */
    public void loadAllSlots() {
        // Reset cache
        Arrays.fill(slots, null);

        try {
            List<GameData> saved = database.loadAllGameData();
            for (GameData data : saved) {
                int index = data.slotId - 1;
                if (index >= 0 && index < TOTAL_SLOTS) {
                    slots[index] = data;
                }
            }
        } catch (SQLException e) {
            System.err.println("[StartGameController] Failed to load save slots: " + e.getMessage());
        }
    }

    /**
     * Returns the {@link GameData} for the given slot,
     * or null if the slot is empty.
     */
    public GameData getSlotData(int slotId) {
        if (slotId < 1 || slotId > TOTAL_SLOTS) return null;
        return slots[slotId - 1];
    }

    /**
     * Returns true if the given slot has saved data.
     */
    public boolean isSlotOccupied(int slotId) {
        return getSlotData(slotId) != null;
    }

    /**
     * Called when the player clicks a save slot.
     */
    public void onSlotSelected(int slotId) {
        GameData data = getSlotData(slotId);

        // New game
        if (data == null) {
            data = new GameData();
            data.slotId = slotId;
            data.slotName = "Slot " + slotId;
            data.lastSavedAt = System.currentTimeMillis();

            // Persist the new slot immediately so it shows up next time
            try {
                database.saveGameData(data);
            } catch (SQLException e) {
                System.err.println("[StartGameController] Failed to save new slot: " + e.getMessage());
            }
        }

        // Navigate to the gameplay screen
        navigator.goToGame(data);
    }

    /**
     * Called when the player clicks the reset button for a given slot.
     */
    public void onResetSlot(int slotId) {
        try {
            database.deleteGameData(slotId);
        } catch (SQLException e) {
            System.err.println("[StartGameController] Failed to delete slot " + slotId + ": " + e.getMessage());
        }

        // Clear cache
        slots[slotId - 1] = null;

        navigator.goToStartGame();
    }

    /** Returns to the main settings screen. */
    public void onBack() {
        navigator.goToMainMenu();
    }
}
