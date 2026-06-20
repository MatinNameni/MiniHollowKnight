package com.github.matinnameni.minihollowknight.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.github.matinnameni.minihollowknight.model.GameAssets;
import com.github.matinnameni.minihollowknight.controller.MainMenuController;
import com.github.matinnameni.minihollowknight.view.screens.MainMenuScreen;

public class UiManager implements ScreenNavigator {
    private GameAssets assets;
    private Game game;

    private static UiManager instance;

    private UiManager() {}

    /**
     * Initializes {@link #instance}.
     * Call once at startup.
     */
    public static void init(Game game) {
        if(instance == null) {
            instance = new UiManager();
            instance.setGame(game);
            instance.setAssets(new GameAssets());
            instance.getAssets().loadAll();
        }
    }

    public void setScreen(Screen screen) {
        game.setScreen(screen);
    }

    // --- Getters ---

    public GameAssets getAssets() {
        return assets;
    }

    public Game getGame() {
        return game;
    }

    public static UiManager getInstance() {
        return instance;
    }

    // --- Setters ---

    public void setAssets(GameAssets assets) {
        this.assets = assets;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    // --- ScreenNavigator ---

    @Override
    public void goToMainMenu() {
        MainMenuController controller = new MainMenuController(this);
        setScreen(new MainMenuScreen(assets, controller));
    }

    @Override
    public void goToStartGame() {
        // TODO: write this method after implementing StartGameScreen
    }

    @Override
    public void goToSettings() {
        // TODO: write this method after implementing SettingsScreen
    }

    @Override
    public void goToGuide() {
        // TODO: write this method after implementing GuideScreen
    }

    @Override
    public void goToAchievements() {
        // TODO: write this method after implementing AchievementsScreen
    }

    @Override
    public void quitGame() {
        Gdx.app.exit();
    }
}
