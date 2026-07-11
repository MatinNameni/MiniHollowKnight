package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.view.navigator.ScreenNavigator;

/**
 * Controller for the main menu screen.
 */
public class MainMenuController {
    private final ScreenNavigator navigator;

    public MainMenuController(ScreenNavigator navigator) {
        this.navigator = navigator;
    }

    public void onStartGame() {
        navigator.goToStartGame();
    }

    public void onSettings() {
        navigator.goToSettings();
    }

    public void onGuide() {
        navigator.goToGuide();
    }

    public void onAchievements() {
        navigator.goToAchievements();
    }

    public void onQuit() {
        navigator.quitGame();
    }
}
