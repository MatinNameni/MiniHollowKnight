package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.charm.CharmCatalog;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.achievement.AchievementManager;
import com.github.matinnameni.minihollowknight.model.enums.AchievementType;
import com.github.matinnameni.minihollowknight.model.enums.CharmType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Controller for the in-game inventory overlay.
 */
public class InventoryController {

    private final GameData gameData;
    private final Knight knight;

    /**
     * Called when the player asks to close the inventory (via the Close
     * button).
     */
    private final Runnable onCloseRequested;

    /** Currently selected charm (drives the description panel). */
    private CharmType selectedCharm = null;

    public InventoryController(GameData gameData, Knight knight, Runnable onCloseRequested) {
        this.gameData = gameData;
        this.knight = knight;
        this.onCloseRequested = onCloseRequested;
    }

    // --- Query helpers used by the overlay ---

    /** All charm types, in the canonical display order from the spec figure. */
    public CharmType[] allCharms() {
        return CharmType.values();
    }

    /** True if the player has collected {@code charm}. */
    public boolean isCollected(CharmType charm) {
        return gameData.hasCollected(charm);
    }

    /** True if {@code charm} is currently equipped. */
    public boolean isEquipped(CharmType charm) {
        return gameData.equippedCharms.contains(charm);
    }

    /** Notches currently consumed by equipped charms. */
    public int usedNotches() {
        return gameData.usedNotches();
    }

    /** Total notch budget. */
    public int totalNotches() {
        return gameData.totalNotches;
    }

    /** Remaining notches the player can still spend. */
    public int freeNotches() {
        return Math.max(0, totalNotches() - usedNotches());
    }

    /**
     * Returns true if {@code charm} could be equipped right now (i.e. it is
     * collected, not already equipped, and there is enough free notch budget).
     */
    public boolean canEquip(CharmType charm) {
        if (!isCollected(charm)) return false;
        if (isEquipped(charm)) return false;
        return usedNotches() + charm.notches <= totalNotches();
    }

    public CharmType getSelectedCharm() {
        return selectedCharm;
    }

    /** Display name for {@code charm} (delegates to {@link CharmCatalog}). */
    public String nameOf(CharmType charm) {
        return CharmCatalog.nameOf(charm);
    }

    /** Description for {@code charm} (delegates to {@link CharmCatalog}). */
    public String descriptionOf(CharmType charm) {
        return CharmCatalog.descriptionOf(charm);
    }

    /** Notch cost for {@code charm}. */
    public int notchCost(CharmType charm) {
        return CharmCatalog.notchCost(charm);
    }

    // --- Mutations ---

    /** Selects {@code charm} so the overlay can show its description. */
    public void onSelect(CharmType charm) {
        this.selectedCharm = charm;
    }

    /**
     * Toggles the equipped state of {@code charm}.
     *
     * @return {@code true} if the equipped set actually changed.
     */
    public boolean onToggleCharm(CharmType charm) {
        if (!isCollected(charm)) {
            return false;
        }

        if (isEquipped(charm)) {
            gameData.unequipCharm(charm);
            applyCharms();
            return true;
        }

        // Try to equip.
        boolean equipped = gameData.equipCharm(charm);
        if (!equipped) {
            return false;
        }
        applyCharms();

        // "Charmed" achievement
        AchievementManager manager = AchievementManager.getInstance();
        if (manager != null) {
            manager.unlock(AchievementType.CHARMED);
        }
        return true;
    }

    /** Re-applies the currently equipped charm set to the Knight. */
    private void applyCharms() {
        Set<CharmType> snapshot = gameData.equippedCharms.isEmpty()
            ? EnumSet.noneOf(CharmType.class)
            : EnumSet.copyOf(gameData.equippedCharms);
        knight.applyEquippedCharms(snapshot);
    }

    // --- Lifecycle events ---

    /** Called by the overlay when it is opened. */
    public void onOpen() {
        EventBus.getInstance().publish(GameEvent.UI_INVENTORY_OPENED);
        // Default the selection to the first collected charm so the description
        // panel isn't empty on first open.
        if (selectedCharm == null || !isCollected(selectedCharm)) {
            for (CharmType charm : CharmType.values()) {
                if (isCollected(charm)) {
                    selectedCharm = charm;
                    break;
                }
            }
        }
    }

    /**
     * Called by the overlay when it is closed (via the close button, the
     * inventory key, or Escape).
     */
    public void onClose() {
        EventBus.getInstance().publish(GameEvent.UI_INVENTORY_CLOSED);
        if (onCloseRequested != null) {
            onCloseRequested.run();
        }
    }
}
