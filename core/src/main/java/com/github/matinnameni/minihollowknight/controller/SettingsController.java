package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.model.enums.SupportedLanguage;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.UiManager;

/**
 * Controller for the settings screen.
 */
public class SettingsController {
    private final ScreenNavigator navigator;
    private final Settings settings;

    public SettingsController(ScreenNavigator navigator, Settings settings) {
        this.navigator = navigator;
        this.settings = settings;
    }

    /** Called when the player picks a different language in the dropdown. */
    public void onLanguageChanged(SupportedLanguage language) {
        if (language.shortName.equals(settings.getLanguage())) return;

        settings.setLanguage(language.shortName);
        UiManager.getInstance().applySettings(settings);
        navigator.goToSettings();
    }

    /** Called when the player adjusts the brightness slider. */
    public void onBrightnessChanged(float brightness) {
        settings.setBrightness(brightness);
        UiManager.getInstance().applySettings(settings);
    }

    public void onBack() {
        navigator.goToMainMenu();
    }
}
