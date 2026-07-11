package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.view.navigator.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.navigator.UiManager;

/**
 * Controller for the audio settings screen.
 */
public class AudioSettingsController {
    private final ScreenNavigator navigator;
    private final Settings settings;

    /** Whether this settings screen was opened from the in-game pause menu, rather than the main menu. */
    private final boolean cameFromPause;

    public AudioSettingsController(ScreenNavigator navigator, Settings settings) {
        this(navigator, settings, false);
    }

    public AudioSettingsController(ScreenNavigator navigator, Settings settings, boolean cameFromPause) {
        this.navigator = navigator;
        this.settings = settings;
        this.cameFromPause = cameFromPause;
    }

    /** Called when the player toggles music on/off. */
    public void onMusicToggled(boolean enabled) {
        settings.setMusicEnabled(enabled);
        UiManager.getInstance().applySettings(settings);
    }

    /** Called when the player adjusts the music volume slider. */
    public void onMusicVolumeChanged(float volume) {
        settings.setMusicVolume(volume);
        UiManager.getInstance().applySettings(settings);
    }

    /** Called when the player toggles SFX on/off. */
    public void onSfxToggled(boolean enabled) {
        settings.setSfxEnabled(enabled);
        UiManager.getInstance().applySettings(settings);
    }

    /** Called when the player adjusts the SFX volume slider. */
    public void onSfxVolumeChanged(float volume) {
        settings.setSfxVolume(volume);
        UiManager.getInstance().applySettings(settings);
    }

    /** Resets audio settings to default values */
    public void resetAudioSettings() {
        settings.resetVolumes();
        UiManager.getInstance().applySettings(settings);
        if (cameFromPause) {
            navigator.goToAudioSettingsFromPause();
        } else {
            navigator.goToAudioSettings();
        }
    }

    /** Returns to the main settings screen. */
    public void onBack() {
        if (cameFromPause) {
            navigator.goToSettingsFromPause();
        } else {
            navigator.goToSettings();
        }
    }

    /** Whether the screen should have the menu background or not */
    public boolean shouldDrawBackground() {
        return !cameFromPause;
    }
}
