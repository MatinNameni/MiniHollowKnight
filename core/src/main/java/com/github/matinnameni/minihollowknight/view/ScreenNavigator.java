package com.github.matinnameni.minihollowknight.view;

import com.github.matinnameni.minihollowknight.model.GameData;
import com.github.matinnameni.minihollowknight.view.screens.GameScreen;

public interface ScreenNavigator {
    void goToMainMenu();
    void goToStartGame();
    void goToGame(GameData data);
    void goToSettings();
    void goToAudioSettings();
    void goToKeyBindings();
    void goToGuide();
    void goToAchievements();
    void quitGame();

    void goToSettingsFromPause(GameScreen pausedGame);
    void goToSettingsFromPause();
    void goToAudioSettingsFromPause();
    void goToKeyBindingsFromPause();
    void returnToPausedGame();
}
