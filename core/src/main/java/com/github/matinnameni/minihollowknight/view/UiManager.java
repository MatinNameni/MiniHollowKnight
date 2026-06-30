package com.github.matinnameni.minihollowknight.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.github.matinnameni.minihollowknight.controller.AudioSettingsController;
import com.github.matinnameni.minihollowknight.controller.KeyBindingsController;
import com.github.matinnameni.minihollowknight.controller.SettingsController;
import com.github.matinnameni.minihollowknight.database.DatabaseManager;
import com.github.matinnameni.minihollowknight.model.Lang;
import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.model.asset.AssetRegistry;
import com.github.matinnameni.minihollowknight.model.asset.HudAssetBundle;
import com.github.matinnameni.minihollowknight.model.asset.KnightAssetBundle;
import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.SupportedLanguage;
import com.github.matinnameni.minihollowknight.controller.MainMenuController;
import com.github.matinnameni.minihollowknight.controller.StartGameController;
import com.github.matinnameni.minihollowknight.model.GameData;
import com.github.matinnameni.minihollowknight.view.screens.*;

import java.sql.SQLException;

public class UiManager implements ScreenNavigator {
    private AssetRegistry registry;
    private Game game;
    private DatabaseManager database;
    private Settings settings;
    private Music menuMusic;
    private boolean knightAssetsLoaded = false;
    private boolean hudAssetsLoaded = false;

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
            instance.initAssetRegistry();
            instance.initDatabaseAndSettings();
            instance.playMenuMusic();
        }
    }

    /**
     * Registers all asset bundles and loads the menu bundle.
     */
    private void initAssetRegistry() {
        registry = new AssetRegistry();
        registry.register(new MenuAssetBundle(registry.getManager()));
        registry.loadBundle(MenuAssetBundle.KEY);
        registry.register(new KnightAssetBundle(registry.getManager()));
        registry.register(new HudAssetBundle(registry.getManager()));
    }

    /**
     * Opens the database and loads previously saved settings if any exist.
     */
    private void initDatabaseAndSettings() {
        database = new DatabaseManager();
        try {
            database.init();
            Settings loaded = database.loadSettings();
            if (loaded != null) {
                settings = loaded;
            } else {
                settings = new Settings();
                database.saveSettings(settings); // first run: persist the defaults
            }
        } catch (SQLException e) {
            System.err.println("[UiManager] Failed to load settings, using defaults: " + e.getMessage());
            settings = new Settings();
        }
        Lang.load(SupportedLanguage.fromShortName(settings.getLanguage()));
    }

    public void setScreen(Screen screen) {
        Screen old = game.getScreen();
        if (old != null) old.dispose();
        game.setScreen(screen);
    }

    // --- Getters ---

    public AssetRegistry getRegistry() {
        return registry;
    }

    public MenuAssetBundle getMenuAssets() {
        return (MenuAssetBundle) registry.get(MenuAssetBundle.KEY);
    }

    public Game getGame() {
        return game;
    }

    public Settings getSettings() {
        return settings;
    }

    public DatabaseManager getDatabase() {
        return database;
    }

    public static UiManager getInstance() {
        return instance;
    }

    // --- Setters ---

    public void setGame(Game game) {
        this.game = game;
    }

    /** Stores {@code settings}, applies its configurations, and persists it to the DB. */
    public void applySettings(Settings settings) {
        this.settings = settings;
        Lang.load(SupportedLanguage.fromShortName(settings.getLanguage()));
        applyVolumeSettings();
        try {
            database.saveSettings(settings);
        } catch (SQLException e) {
            System.err.println("[UiManager] Failed to save settings: " + e.getMessage());
        }
    }

    // --- Audio ---

    /** Starts playing the menu background music on loop using current volume settings. */
    public void playMenuMusic() {
        stopMenuMusic();
        menuMusic = getMenuAssets().getMenuMusic();
        menuMusic.setLooping(true);
        menuMusic.setVolume(settings.isMusicEnabled() ? settings.getMusicVolume() : 0f);
        menuMusic.play();
    }

    /** Stops and disposes the current menu music instance. */
    public void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic = null;
        }
    }

    /** Applies the current volume settings. */
    public void applyVolumeSettings() {
        if (menuMusic != null) {
            menuMusic.setVolume(settings.isMusicEnabled() ? settings.getMusicVolume() : 0f);
        }
    }

    // --- ScreenNavigator ---

    @Override
    public void goToMainMenu() {
        playMenuMusic();
        MainMenuController controller = new MainMenuController(this);
        setScreen(new MainMenuScreen(registry, settings, controller));
    }

    @Override
    public void goToStartGame() {
        StartGameController controller = new StartGameController(this);
        setScreen(new StartGameScreen(registry, controller));
    }

    @Override
    public void goToSettings() {
        SettingsController controller = new SettingsController(this, settings);
        setScreen(new SettingsScreen(registry, settings, controller));
    }

    @Override
    public void goToAudioSettings() {
        AudioSettingsController controller = new AudioSettingsController(this, settings);
        setScreen(new AudioSettingsScreen(registry, settings, controller));
    }

    @Override
    public void goToKeyBindings() {
        KeyBindingsController controller = new KeyBindingsController(this, settings);
        setScreen(new KeyBindingsScreen(registry, settings, controller));
    }

    @Override
    public void goToGame(GameData data) {
        ensureKnightAssetsLoaded();
        ensureHudAssetsLoaded();
        stopMenuMusic();
        KnightAssetBundle knightAssets = (KnightAssetBundle) registry.get(KnightAssetBundle.KEY);
        HudAssetBundle hudAssets = (HudAssetBundle) registry.get(HudAssetBundle.KEY);
        setScreen(new GameScreen(this, data, settings, knightAssets, hudAssets));
    }

    /**
     * Loads the KnightAssetBundle if it hasn't been loaded yet.
     */
    private void ensureKnightAssetsLoaded() {
        if (!knightAssetsLoaded) {
            registry.loadBundle(KnightAssetBundle.KEY);
            knightAssetsLoaded = true;
        }
    }

    /**
     * Loads the HudAssetBundle if it hasn't been loaded yet.
     */
    private void ensureHudAssetsLoaded() {
        if (!hudAssetsLoaded) {
            registry.loadBundle(HudAssetBundle.KEY);
            hudAssetsLoaded = true;
        }
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

    // --- Lifecycle ---

    public void dispose() {
        if (registry != null) {
            registry.dispose();
        }
    }
}
