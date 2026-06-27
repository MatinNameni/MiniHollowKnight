package com.github.matinnameni.minihollowknight.view;

import com.github.matinnameni.minihollowknight.model.GameData;

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
}
