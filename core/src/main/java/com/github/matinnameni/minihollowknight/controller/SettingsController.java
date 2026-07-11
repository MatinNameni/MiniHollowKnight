package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.enums.MenuStyle;
import com.github.matinnameni.minihollowknight.model.enums.SupportedLanguage;
import com.github.matinnameni.minihollowknight.view.navigator.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.navigator.UiManager;

/**
 * Controller for the settings screen.
 */
public class SettingsController {
    private final ScreenNavigator navigator;
    private final Settings settings;

    /** Whether this settings screen was opened from the in-game pause menu, rather than the main menu. */
    private final boolean cameFromPause;

    public SettingsController(ScreenNavigator navigator, Settings settings) {
        this(navigator, settings, false);
    }

    public SettingsController(ScreenNavigator navigator, Settings settings, boolean cameFromPause) {
        this.navigator = navigator;
        this.settings = settings;
        this.cameFromPause = cameFromPause;
    }

    /** Called when the player changes the menu style. */
    public void onMenuStyleChange(MenuStyle style, MenuAssetBundle menuAssets) {
        menuAssets.setBackground(style);
        if (cameFromPause) {
            navigator.goToSettingsFromPause();
        } else {
            navigator.goToSettings();
        }
    }

    /** Called when the player taps the Audio Settings button. */
    public void onAudioSettings() {
        if (cameFromPause) {
            navigator.goToAudioSettingsFromPause();
        } else {
            navigator.goToAudioSettings();
        }
    }

    /** Called when the player taps the Key Bindings button. */
    public void onKeyBindings() {
        if (cameFromPause) {
            navigator.goToKeyBindingsFromPause();
        } else {
            navigator.goToKeyBindings();
        }
    }

    /** Called when the player picks a different language in the dropdown. */
    public void onLanguageChanged(SupportedLanguage language) {
        if (language.shortName.equals(settings.getLanguage())) return;

        settings.setLanguage(language.shortName);
        UiManager.getInstance().applySettings(settings);
        if (cameFromPause) {
            navigator.goToSettingsFromPause();
        } else {
            navigator.goToSettings();
        }
    }

    /** Called when the player adjusts the brightness slider. */
    public void onBrightnessChanged(float brightness) {
        settings.setBrightness(brightness);
        UiManager.getInstance().applySettings(settings);
    }

    public void onBack() {
        if (cameFromPause) {
            navigator.returnToPausedGame();
        } else {
            navigator.goToMainMenu();
        }
    }

    /** Whether the screen should have the menu background or not */
    public boolean shouldDrawBackground() {
        return !cameFromPause;
    }
}
