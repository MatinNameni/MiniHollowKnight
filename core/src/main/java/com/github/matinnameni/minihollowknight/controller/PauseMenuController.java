package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.view.navigator.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.navigator.UiManager;
import com.github.matinnameni.minihollowknight.view.screens.GameScreen;

/**
 * Controller for the in-game pause menu overlay.
 */
public class PauseMenuController {
    private final ScreenNavigator navigator;
    private final GameScreen gameScreen;

    private final Runnable onContinue;
    private final Runnable onShowCheatCodes;

    public PauseMenuController(ScreenNavigator navigator, GameScreen gameScreen,
                               Runnable onContinue, Runnable onShowCheatCodes) {
        this.navigator = navigator;
        this.gameScreen = gameScreen;
        this.onContinue = onContinue;
        this.onShowCheatCodes = onShowCheatCodes;
    }

    /** Called when the player taps the Continue button. */
    public void onContinue() {
        onContinue.run();
    }

    /** Called when the player taps the Options button. */
    public void onOptions() {
        navigator.goToSettingsFromPause(gameScreen);
    }

    /** Called when the player taps the Quit to menu button. */
    public void onQuitToMenu() {
        gameScreen.saveGame();
        navigator.goToMainMenu();
        UiManager.getInstance().playMenuMusic();
    }

    /** Called when the player taps the Show cheat codes button */
    public void onShowCheatCodes() {
        if (onShowCheatCodes != null) {
            onShowCheatCodes.run();
        }
    }
}
