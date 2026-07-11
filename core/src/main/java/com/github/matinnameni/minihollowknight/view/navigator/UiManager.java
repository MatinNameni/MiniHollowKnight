package com.github.matinnameni.minihollowknight.view.navigator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.github.matinnameni.minihollowknight.controller.AudioSettingsController;
import com.github.matinnameni.minihollowknight.controller.GameMusicManager;
import com.github.matinnameni.minihollowknight.controller.KeyBindingsController;
import com.github.matinnameni.minihollowknight.controller.SettingsController;
import com.github.matinnameni.minihollowknight.model.database.DatabaseManager;
import com.github.matinnameni.minihollowknight.model.localization.Lang;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.achievement.AchievementManager;
import com.github.matinnameni.minihollowknight.model.asset.*;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;
import com.github.matinnameni.minihollowknight.model.enums.SupportedLanguage;
import com.github.matinnameni.minihollowknight.controller.MainMenuController;
import com.github.matinnameni.minihollowknight.controller.StartGameController;
import com.github.matinnameni.minihollowknight.model.data.GameData;
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
    private boolean tiledMapAssetsLoaded = false;
    private boolean charmAssetsLoaded = false;
    private boolean achievementAssetsLoaded = false;
    private boolean gameMusicAssetsLoaded = false;

    private GameMusicManager gameMusicManager;

    private GameScreen pausedGameScreen;

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
            instance.initAchievements();
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
        registry.register(new TiledMapAssetBundle(registry.getManager()));
        registry.register(new CharmAssetBundle(registry.getManager()));
        registry.register(new AchievementAssetBundle(registry.getManager()));
        registry.register(new GameMusicAssetBundle(registry.getManager()));
        EnemiesAssetsManager.getInstance(registry).initAssets();
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

    /** Initializes the global {@link AchievementManager}. */
    private void initAchievements() {
        AchievementManager.init(database);
    }

    public void setScreen(Screen screen) {
        Screen old = game.getScreen();
        if (old != null) old.dispose();
        game.setScreen(screen);
    }

    /** Swaps to {@code screen} without disposing the currently active screen. */
    private void setScreenKeepingCurrent(Screen screen) {
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

    /** Applies the current volume settings to every active music source. */
    public void applyVolumeSettings() {
        if (menuMusic != null) {
            menuMusic.setVolume(settings.isMusicEnabled() ? settings.getMusicVolume() : 0f);
        }
        if (gameMusicManager != null) {
            gameMusicManager.applyVolumeSettings();
        }
    }

    // --- ScreenNavigator ---

    @Override
    public void goToMainMenu() {
        AchievementManager manager = AchievementManager.getInstance();
        if (manager != null) {
            manager.clearActiveGameData();
        }
        if (gameMusicManager != null) {
            gameMusicManager.stop();
        }
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
    public void goToSettingsFromPause(GameScreen pausedGame) {
        this.pausedGameScreen = pausedGame;
        goToSettingsFromPause();
    }

    @Override
    public void goToSettingsFromPause() {
        SettingsController controller = new SettingsController(this, settings, true);
        setScreenKeepingCurrent(new SettingsScreen(registry, settings, controller));
    }

    @Override
    public void goToAudioSettingsFromPause() {
        AudioSettingsController controller = new AudioSettingsController(this, settings, true);
        setScreenKeepingCurrent(new AudioSettingsScreen(registry, settings, controller));
    }

    @Override
    public void goToKeyBindingsFromPause() {
        KeyBindingsController controller = new KeyBindingsController(this, settings, true);
        setScreenKeepingCurrent(new KeyBindingsScreen(registry, settings, controller));
    }

    @Override
    public void returnToPausedGame() {
        if (pausedGameScreen == null) {
            goToMainMenu();
            return;
        }
        GameScreen resumed = pausedGameScreen;
        pausedGameScreen = null;
        Screen settingsScreen = game.getScreen();
        setScreenKeepingCurrent(resumed);
        if (settingsScreen != null) {
            settingsScreen.dispose();
        }
    }

    @Override
    public void goToGame(GameData data) {
        ensureKnightAssetsLoaded();
        ensureHudAssetsLoaded();
        ensureEnemiesAssetsLoaded();
        ensureTiledMapAssetsLoaded();
        ensureCharmAssetsLoaded();
        ensureAchievementAssetsLoaded();
        ensureGameMusicAssetsLoaded();
        stopMenuMusic();
        startGameMusic();

        AchievementManager manager = AchievementManager.getInstance();
        if (manager != null) {
            manager.setActiveGameData(data);
        }

        KnightAssetBundle knightAssets = (KnightAssetBundle) registry.get(KnightAssetBundle.KEY);
        HudAssetBundle hudAssets = (HudAssetBundle) registry.get(HudAssetBundle.KEY);
        TiledMapAssetBundle mapAssets = (TiledMapAssetBundle) registry.get(TiledMapAssetBundle.KEY);
        CharmAssetBundle charmAssets = (CharmAssetBundle) registry.get(CharmAssetBundle.KEY);
        AchievementAssetBundle achievementAssets = (AchievementAssetBundle) registry.get(AchievementAssetBundle.KEY);
        setScreen(new GameScreen(this, data, settings, knightAssets, hudAssets,
            getMenuAssets(), mapAssets, EnemiesAssetsManager.getInstance(registry), charmAssets, achievementAssets));
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

    /**
     * Loads all enemies asset bundles if they haven't been loaded yet.
     */
    private void ensureEnemiesAssetsLoaded() {
        EnemiesAssetsManager.getInstance(registry).loadBundles();
    }

    /**
     * Loads the TiledMapAssetBundle if it hasn't been loaded yet.
     */
    private void ensureTiledMapAssetsLoaded() {
        if (!tiledMapAssetsLoaded) {
            registry.loadBundle(TiledMapAssetBundle.KEY);
            tiledMapAssetsLoaded = true;
        }
    }

    /**
     * Loads the CharmAssetBundle if it hasn't been loaded yet.
     */
    private void ensureCharmAssetsLoaded() {
        if (!charmAssetsLoaded) {
            registry.loadBundle(CharmAssetBundle.KEY);
            charmAssetsLoaded = true;
        }
    }

    /**
     * Loads the AchievementAssetBundle if it hasn't been loaded yet.
     */
    private void ensureAchievementAssetsLoaded() {
        if (!achievementAssetsLoaded) {
            registry.loadBundle(AchievementAssetBundle.KEY);
            achievementAssetsLoaded = true;
        }
    }

    /**
     * Loads the {@link GameMusicAssetBundle} if it hasn't been loaded yet.
     */
    private void ensureGameMusicAssetsLoaded() {
        if (!gameMusicAssetsLoaded) {
            registry.loadBundle(GameMusicAssetBundle.KEY);
            gameMusicAssetsLoaded = true;
        }
    }

    // --- Gameplay music ---

    private void startGameMusic() {
        if (gameMusicManager != null) {
            gameMusicManager.dispose();
            gameMusicManager = null;
        }
        GameMusicAssetBundle gameMusicAssets = (GameMusicAssetBundle) registry.get(GameMusicAssetBundle.KEY);
        gameMusicManager = new GameMusicManager(gameMusicAssets, settings);
    }

    /** Stops and disposes the gameplay music manager (if any). */
    private void stopGameMusic() {
        if (gameMusicManager != null) {
            gameMusicManager.dispose();
            gameMusicManager = null;
        }
    }

    /**
     * Notifies the gameplay music manager that the knight has entered (or
     * been respawned into) a new environment.
     */
    public void onGameEnvironmentChanged(GameEnvironment environment) {
        if (gameMusicManager != null && environment != null) {
            gameMusicManager.onEnvironmentChanged(environment);
        }
    }

    @Override
    public void goToGuide() {
        // TODO: write this method after implementing GuideScreen
    }

    @Override
    public void goToAchievements() {
        ensureAchievementAssetsLoaded();
        AchievementAssetBundle achievementAssets =
            (AchievementAssetBundle) registry.get(AchievementAssetBundle.KEY);
        setScreen(new AchievementsScreen(
            registry,
            this,
            AchievementManager.getInstance(),
            achievementAssets
        ));
    }

    @Override
    public void quitGame() {
        Gdx.app.exit();
    }

    // --- Lifecycle ---

    public void dispose() {
        stopGameMusic();
        stopMenuMusic();
        if (registry != null) {
            registry.dispose();
        }
    }
}
