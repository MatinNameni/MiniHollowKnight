package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.UiManager;

import java.util.Map;

/**
 * Controller for the key bindings screen.
 */
public class KeyBindingsController {
    private final ScreenNavigator navigator;
    private final Settings settings;

    public KeyBindingsController(ScreenNavigator navigator, Settings settings) {
        this.navigator = navigator;
        this.settings = settings;
    }

    // --- Key changed actions ---

    /** Called when the player rebinds the Left action to {@code keycode}. */
    public void onKeyLeftChanged(int keycode) {
        settings.setKeyLeft(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Called when the player rebinds the Right action to {@code keycode}. */
    public void onKeyRightChanged(int keycode) {
        settings.setKeyRight(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Called when the player rebinds the Jump action to {@code keycode}. */
    public void onKeyJumpChanged(int keycode) {
        settings.setKeyJump(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Called when the player rebinds the Attack action to {@code keycode}. */
    public void onKeyAttackChanged(int keycode) {
        settings.setKeyAttack(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Called when the player rebinds the Dash action to {@code keycode}. */
    public void onKeyDashChanged(int keycode) {
        settings.setKeyDash(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Called when the player rebinds the Focus action to {@code keycode}. */
    public void onKeyFocusChanged(int keycode) {
        settings.setKeyFocus(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Called when the player rebinds the Interact action to {@code keycode}. */
    public void onKeyInteractChanged(int keycode) {
        settings.setKeyInteract(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Called when the player rebinds the Inventory action to {@code keycode}. */
    public void onKeyInventoryChanged(int keycode) {
        settings.setKeyInventory(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Called when the player rebinds the Pause action to {@code keycode}. */
    public void onKeyPauseChanged(int keycode) {
        settings.setKeyPause(keycode);
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    /** Resets key bindings to default values */
    public void resetKeyBindings() {
        settings.resetKeys();
        UiManager.getInstance().applySettings(settings);
        UiManager.getInstance().goToKeyBindings();
    }

    // --- Navigation ---

    /** Returns to the main settings screen. */
    public void onBack() {
        navigator.goToSettings();
    }

    // --- Key conflict ---

    /**
     * @return the {@link TextButton} that {@code keycode} has already been set for it.
     */
    public TextButton getConflictKey(Map<String, TextButton> buttons, TextButton listeningButton, int keycode) {
        for(String keyTitles : buttons.keySet()) {
            if(getKeyCode(keyTitles) == keycode) {
                TextButton button = buttons.get(keyTitles);
                if(button == listeningButton) {
                    return null;
                }
                return button;
            }
        }
        return null;
    }

    // --- Helpers ---

    /**
     * Finds the button title that matches {@code keyText} and returns its keycode,
     * -1 of none found.
     */
    private int getKeyCode(String keyText) {
        for(Map.Entry<String, Integer> keyEntry : settings.getKeys().entrySet()) {
            if(keyEntry.getKey().equals(keyText)) {
                return keyEntry.getValue();
            }
        }
        return -1;
    }
}
