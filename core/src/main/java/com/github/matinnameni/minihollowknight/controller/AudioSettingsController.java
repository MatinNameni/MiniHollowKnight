package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.UiManager;

/**
 * Controller for the audio settings screen.
 */
public class AudioSettingsController {
    private final ScreenNavigator navigator;
    private final Settings settings;

    public AudioSettingsController(ScreenNavigator navigator, Settings settings) {
        this.navigator = navigator;
        this.settings = settings;
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
        UiManager.getInstance().goToAudioSettings();
    }

    /** Returns to the main settings screen. */
    public void onBack() {
        navigator.goToSettings();
    }
}
