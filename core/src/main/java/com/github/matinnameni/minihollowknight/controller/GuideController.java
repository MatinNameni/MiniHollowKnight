package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.view.navigator.ScreenNavigator;

/**
 * Controller for the guide screen.
 */
public class GuideController {
    private final ScreenNavigator navigator;

    public GuideController(ScreenNavigator navigator) {
        this.navigator = navigator;
    }

    /** Called when the player taps the Back button. */
    public void onBack() {
        navigator.goToMainMenu();
    }
}
