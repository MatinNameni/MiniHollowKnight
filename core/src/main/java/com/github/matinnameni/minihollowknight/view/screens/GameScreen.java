package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.controller.CheatCodeRegistry;
import com.github.matinnameni.minihollowknight.controller.GameScreenController;
import com.github.matinnameni.minihollowknight.controller.input.GameScreenInputProcessor;
import com.github.matinnameni.minihollowknight.controller.InventoryController;
import com.github.matinnameni.minihollowknight.controller.PauseMenuController;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.event.EventListener;
import com.github.matinnameni.minihollowknight.model.asset.*;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Crystallized;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.entity.enemies.FalseKnight;
import com.github.matinnameni.minihollowknight.model.entity.enemies.HuskHornhead;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Mossfly;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.enums.BossType;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;
import com.github.matinnameni.minihollowknight.model.laser.Laser;
import com.github.matinnameni.minihollowknight.model.map.MapLoader;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;
import com.github.matinnameni.minihollowknight.model.object.Arena;
import com.github.matinnameni.minihollowknight.model.object.BreakableWall;
import com.github.matinnameni.minihollowknight.model.object.Door;
import com.github.matinnameni.minihollowknight.model.projectile.Projectile;
import com.github.matinnameni.minihollowknight.view.hud.*;
import com.github.matinnameni.minihollowknight.view.navigator.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.navigator.UiManager;
import com.github.matinnameni.minihollowknight.view.renderer.KnightRenderer;

/**
 * The main gameplay screen.
 */
public class GameScreen implements Screen, EventListener {

    // --- Constants ---

    // Respawn
    private static final float DEATH_ANIM_WAIT = 0.3f;
    private static final float FADE_DURATION = 0.8f;
    private static final float DEATH_ANIM_DURATION = 1.2f;

    private final ScreenNavigator navigator;
    private final GameData gameData;
    private final Settings settings;
    private final GameScreenController controller;

    // --- Core ---
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // --- World ---
    private Knight knight;
    private KnightRenderer knightRenderer;
    private TiledGameMap gameMap;
    private TiledMapAssetBundle mapAssets;

    // --- HUD ---
    private GameHud gameHud;

    // --- Display overlay ---
    private DisplayTextOverlay displayOverlay;

    // --- Pause menu ---
    private final MenuAssetBundle menuAssets;
    private PauseOverlay pauseOverlay;
    private boolean paused = false;

    // --- Inventory overlay ---
    private final CharmAssetBundle charmAssets;
    private InventoryOverlay inventoryOverlay;
    private InventoryController inventoryController;
    private boolean inventoryOpen = false;

    // --- Cheat codes overlay ---
    private CheatCodesOverlay cheatCodesOverlay;
    private boolean cheatCodesOpen = false;

    // --- Achievement popup ---
    private final AchievementAssetBundle achievementAssets;
    private AchievementPopupOverlay achievementPopup;

    // --- Input + cheats ---
    private CheatCodeRegistry cheatRegistry;
    private GameScreenInputProcessor gameInputProcessor;

    // --- Brightness overlay ---
    private final ShapeRenderer brightnessOverlayRenderer = new ShapeRenderer();

    /** True once {@link #show()} has fully initialized the world. */
    private boolean initialized = false;

    // --- Debug ---
    private boolean showDebugInfo = false;

    // --- Respawn state ---
    private RespawnPhase respawnPhase = RespawnPhase.NONE;
    private float respawnTimer = 0f;
    private float fadeAlpha = 0f;

    // --- Area-transfer state ---
    /** The environment we are currently fading into, or {@code null} when no transfer is in progress. */
    private GameEnvironment transferTarget = null;
    private TransferPhase transferPhase = TransferPhase.NONE;
    private float transferTimer = 0f;
    /** Separate alpha for the transfer fade so it doesn't collide with the respawn fade. */
    private float transferFadeAlpha = 0f;
    private static final float TRANSFER_FADE_DURATION = 0.5f;

    // --- End-game state ---
    private static final float END_GAME_WAIT_DURATION = 2.8f;
    private static final float END_GAME_FADE_DURATION = 1.0f;
    private float endGameFadeAlpha = 0f;
    private EndGamePhase endGamePhase = EndGamePhase.NONE;
    private float endGameTimer = 0f;
    private boolean endGameTriggered = false;
    private boolean gameEnded = false;

    public GameScreen(ScreenNavigator navigator, GameData gameData, Settings settings,
                      KnightAssetBundle knightAssets, HudAssetBundle hudAssets, MenuAssetBundle menuAssets,
                      TiledMapAssetBundle mapAssets, EnemiesAssetsManager enemiesAssets,
                      CharmAssetBundle charmAssets, AchievementAssetBundle achievementAssets) {
        this.navigator = navigator;
        this.gameData = gameData;
        this.settings = settings;
        this.knight = new Knight(knightAssets, settings);
        this.knightRenderer = new KnightRenderer(knight, knightAssets);
        this.controller = new GameScreenController(knight, enemiesAssets, gameData);
        this.gameHud = new GameHud(knight, hudAssets);
        this.menuAssets = menuAssets;
        this.mapAssets = mapAssets;
        this.charmAssets = charmAssets;
        this.achievementAssets = achievementAssets;
    }

    // --- Screen ---

    @Override
    public void show() {
        if (cheatCodesOpen) {
            cheatCodesOverlay.init();
            Gdx.input.setInputProcessor(cheatCodesOverlay.getStage());
        } else if (initialized) {
            if (paused) {
                pauseOverlay.init();
                Gdx.input.setInputProcessor(pauseOverlay.getStage());
            } else if (inventoryOpen) {
                inventoryOverlay.init();
                Gdx.input.setInputProcessor(inventoryOverlay.getStage());
            } else {
                Gdx.input.setInputProcessor(gameInputProcessor);
            }
            return;
        }

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Load the Tiled map
        GameEnvironment currentEnvironment = GameEnvironment.fromId(gameData.currentEnvironment);
        gameMap = (currentEnvironment == null) ?
            MapLoader.loadMap(GameEnvironment.FORGOTTEN_CROSSROADS, mapAssets, gameData) :
            MapLoader.loadMap(currentEnvironment, mapAssets, gameData);
        controller.setGameMap(gameMap);

        // Start the area-appropriate background music.
        GameEnvironment musicEnv = (currentEnvironment == null)
            ? GameEnvironment.FORGOTTEN_CROSSROADS : currentEnvironment;
        UiManager.getInstance().onGameEnvironmentChanged(musicEnv);

        if (gameData.collectedCharms.isEmpty()) {
            gameData.grantDefaultCharms();
        }

        // Initialize knight from save data
        knight.initializeFromSave(gameData);

        // For a new save, use the spawn point from the map
        if (gameData.playerX < 0f && gameData.playerY < 0f) {
            Vector2 spawn = gameMap.getPlayerSpawn();
            knight.setPosition(spawn.x, spawn.y);
        }

        // Center camera on knight immediately
        controller.initializeCameraTarget();
        camera.position.set(controller.getCameraTarget().x, controller.getCameraTarget().y, 0f);
        camera.update();

        // Initialize and position HUD elements
        gameHud.init();
        gameHud.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize the display text overlay
        displayOverlay = new DisplayTextOverlay(menuAssets);
        displayOverlay.init();
        displayOverlay.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize the pause menu overlay
        PauseMenuController pauseController = new PauseMenuController(
            navigator, this, this::resumeGame, this::showCheatCodes);
        pauseOverlay = new PauseOverlay(pauseController, menuAssets);
        pauseOverlay.init();
        pauseOverlay.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize the cheat codes overlay.
        cheatCodesOverlay = new CheatCodesOverlay(menuAssets, this::closeCheatCodes);
        cheatCodesOverlay.init();
        cheatCodesOverlay.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize the inventory overlay.
        inventoryController = new InventoryController(gameData, knight, this::closeInventory);
        inventoryOverlay = new InventoryOverlay(inventoryController, charmAssets, menuAssets);
        inventoryOverlay.init();
        inventoryOverlay.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize the achievement popup overlay.
        achievementPopup = new AchievementPopupOverlay(achievementAssets, menuAssets);
        achievementPopup.init();
        achievementPopup.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Wire up the respawn callback
        controller.setOnPlayerDied(this::onPlayerDied);

        // Listen for the False Knight's defeat so we can transition to the
        // end-game screen once the death animation has played.
        EventBus.getInstance().subscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);

        // Build the cheat registry + input processor.
        cheatRegistry = new CheatCodeRegistry();
        CheatCodeRegistry.Context cheatContext = new CheatCodeRegistry.Context(
            knight, gameData, gameMap, controller.getEnemies(), this::teleportToBossArena);
        gameInputProcessor = new GameScreenInputProcessor(
            settings,
            cheatRegistry,
            cheatContext,
            () -> respawnPhase == RespawnPhase.NONE && transferPhase == TransferPhase.NONE, // canTogglePause
            () -> !paused && respawnPhase == RespawnPhase.NONE && transferPhase == TransferPhase.NONE, // canToggleInventory
            this::togglePause,
            this::toggleInventory,
            () -> showDebugInfo = !showDebugInfo
        );
        Gdx.input.setInputProcessor(gameInputProcessor);

        initialized = true;
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);

        UiManager.getInstance().updateGameMusic(delta);
        UiManager.getInstance().updateSoundEffects(delta);

        // --- Update logic ---
        if (respawnPhase != RespawnPhase.NONE) {
            updateRespawn(delta);
        }

        // Area-transfer transition takes priority over normal gameplay
        if (transferPhase != TransferPhase.NONE) {
            updateTransfer(delta);
        }

        // End-game sequence
        if (endGamePhase != EndGamePhase.NONE) {
            updateEndGame(delta);
            if(gameEnded) return;
        }

        boolean gameplayBlocked = paused || inventoryOpen;

        if (!gameplayBlocked) {
            controller.update(delta, camera);

            // Update HUD
            gameHud.update(delta);

            // Check whether the knight stepped on a transfer trigger this frame.
            GameEnvironment pending = controller.consumePendingTransfer();
            if (pending != null) {
                startTransfer(pending);
            }

            gameData.playTimeSeconds += delta;
        }

        Color bgColor = controller.getCurrentBackgroundColor(gameMap);
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameMap.update(delta);

        // Background layer
        gameMap.renderBackground(camera);

        // Spike layers
        gameMap.renderSpikeLayer(camera);

        // Breakable walls
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (BreakableWall wall : gameMap.getBreakableWalls()) {
            wall.render(batch);
        }
        batch.end();

        // Main layers
        gameMap.renderMain(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Doors
        for (Door door : gameMap.getDoors()) {
            door.render(batch);
        }

        // Enemies
        for (Enemy enemy : controller.getEnemies()) {
            enemy.render(batch);
        }

        // Crystallized lasers
        for (Laser laser : controller.getLasers()) {
            laser.render(batch);
        }

        // Knight
        knightRenderer.render(batch);

        // Projectiles
        for (Projectile projectile : controller.getProjectiles()) {
            projectile.render(batch);
        }
        batch.end();

        // Foreground layer
        gameMap.renderForeground(camera);

        // Knight effects
        batch.begin();
        knightRenderer.renderEffects(batch);
        batch.end();

        // Debug overlay
        if (showDebugInfo) {
            renderDebugOverlay();
        }

        // HUD
        gameHud.draw();

        // Display Overlay
        if (controller.getDisplayText() != null) {
            displayOverlay.setBodyText(controller.getDisplayText());
            controller.resetDisplayText();

            displayOverlay.startDisplay();
        }

        if (displayOverlay.isDisplaying()) {
            displayOverlay.update(delta);
            displayOverlay.draw();
        }

        // Pause menu overlay
        if (paused) {
            pauseOverlay.update(delta);
            pauseOverlay.draw();
        }

        // Cheat codes overlay
        if (cheatCodesOpen) {
            cheatCodesOverlay.update(delta);
            cheatCodesOverlay.draw();
        }

        // Inventory overlay
        if (inventoryOpen) {
            inventoryOverlay.update(delta);
            inventoryOverlay.draw();
        }

        // Brightness overlay
        drawBrightnessOverlay();

        // Achievement popup
        achievementPopup.update(delta);
        achievementPopup.draw();

        // Death/respawn fade overlay
        if (fadeAlpha > 0f) {
            drawFadeOverlay();
        }

        // Area-transfer fade overlay
        if (transferFadeAlpha > 0f) {
            drawTransferFadeOverlay();
        }

        // End-game fade overlay
        if (endGameFadeAlpha > 0f) {
            drawEndGameFadeOverlay();
        }
    }

    // --- Respawn ---

    /** Called when the knight's masks reach zero (via PLAYER_DIED event). */
    private void onPlayerDied() {
        respawnPhase = RespawnPhase.DEATH_ANIM;
        respawnTimer = 0f;
        fadeAlpha = 0f;
    }

    // --- End-game sequence ---

    /**
     * Listener for {@link GameEvent#FALSE_KNIGHT_DEFEATED}. Kicks off the
     * end-game sequence.
     */
    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event != GameEvent.FALSE_KNIGHT_DEFEATED) return;
        if (endGameTriggered) return;
        endGameTriggered = true;

        Gdx.input.setInputProcessor(null);

        endGamePhase = EndGamePhase.WAIT;
        endGameTimer = 0f;
        endGameFadeAlpha = 0f;
    }

    /** Advances the end-game state machine each frame. */
    private void updateEndGame(float delta) {
        delta = Math.min(delta, 0.05f);

        switch (endGamePhase) {
            case WAIT:
                endGameTimer += delta;
                if (endGameTimer >= END_GAME_WAIT_DURATION) {
                    endGamePhase = EndGamePhase.FADE_TO_BLACK;
                    endGameTimer = 0f;
                }
                break;

            case FADE_TO_BLACK:
                endGameTimer += delta;
                endGameFadeAlpha = Math.min(1f, endGameTimer / END_GAME_FADE_DURATION);
                if (endGameFadeAlpha >= 1f) {
                    endGamePhase = EndGamePhase.DONE;
                    navigator.goToEndGame(gameData);
                    gameEnded = true;
                    return;
                }
                break;

            case DONE:
                break;

            default:
                break;
        }
    }

    /** Draws a black overlay with the current end-game fade alpha. */
    private void drawEndGameFadeOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, endGameFadeAlpha);

        float x = camera.position.x - camera.viewportWidth / 2f;
        float y = camera.position.y - camera.viewportHeight / 2f;
        shapeRenderer.rect(x, y, camera.viewportWidth, camera.viewportHeight);
        shapeRenderer.end();
    }

    /** Advances the respawn state machine each frame. */
    private void updateRespawn(float delta) {
        delta = Math.min(delta, 0.05f);

        switch (respawnPhase) {
            case DEATH_ANIM:
                respawnTimer += delta;

                if (respawnTimer >= DEATH_ANIM_DURATION + DEATH_ANIM_WAIT) {
                    respawnPhase = RespawnPhase.FADE_TO_BLACK;
                    respawnTimer = 0f;
                }
                break;

            case FADE_TO_BLACK:
                respawnTimer += delta;
                fadeAlpha = Math.min(1f, respawnTimer / FADE_DURATION);
                if (fadeAlpha >= 1f) {
                    performRespawnReset();
                    respawnPhase = RespawnPhase.FADE_IN;
                    respawnTimer = 0f;
                }
                break;

            case FADE_IN:
                respawnTimer += delta;
                fadeAlpha = Math.max(0f, 1f - respawnTimer / FADE_DURATION);
                if (fadeAlpha <= 0f) {
                    fadeAlpha = 0f;
                    respawnPhase = RespawnPhase.NONE;
                }
                break;

            default:
                break;
        }
    }

    /** Resets the world state for respawn. */
    private void performRespawnReset() {
        // Dispose old map and load Forgotten Crossroads
        if (gameMap != null) {
            gameMap.dispose();
        }
        gameMap = MapLoader.loadMap(GameEnvironment.FORGOTTEN_CROSSROADS, mapAssets, gameData);
        controller.setGameMap(gameMap);

        // Reset the background music
        UiManager.getInstance().onGameEnvironmentChanged(GameEnvironment.FORGOTTEN_CROSSROADS);

        // Reset the knight at the map's spawn point
        Vector2 spawn = gameMap.getPlayerSpawn();
        knight.setPosition(spawn.x, spawn.y);
        knight.resetAfterDeath();

        // Re-center camera immediately on the spawn point
        controller.initializeCameraTarget();
        camera.position.set(controller.getCameraTarget().x, controller.getCameraTarget().y, 0f);
        camera.update();

        // Reset HUD
        gameHud.update(0f);

        // Increment death counter in game data
        gameData.totalDeaths++;

        // Publish respawn event
        EventBus.getInstance().publish(GameEvent.PLAYER_RESPAWNED);
    }

    /**
     * Draws a black overlay with the current fade alpha.
     * Used for the death fade-to-black and respawn fade-in.
     */
    private void drawFadeOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, fadeAlpha);

        float x = camera.position.x - camera.viewportWidth / 2f;
        float y = camera.position.y - camera.viewportHeight / 2f;
        shapeRenderer.rect(x, y, camera.viewportWidth, camera.viewportHeight);
        shapeRenderer.end();
    }

    // --- Area transfer ---

    /**
     * Begins a fade-to-black to swap-map to fade-in transition into
     * {@code target}. No-op if a transfer or respawn is already in progress.
     */
    private void startTransfer(GameEnvironment target) {
        if (target == null) return;
        if (transferPhase != TransferPhase.NONE) return;
        if (respawnPhase != RespawnPhase.NONE) return;

        transferTarget = target;
        transferPhase = TransferPhase.FADE_TO_BLACK;
        transferTimer = 0f;
        transferFadeAlpha = 0f;
    }

    /** Advances the transfer state machine each frame. */
    private void updateTransfer(float delta) {
        delta = Math.min(delta, 0.05f);

        switch (transferPhase) {
            case FADE_TO_BLACK:
                transferTimer += delta;
                transferFadeAlpha = Math.min(1f, transferTimer / TRANSFER_FADE_DURATION);
                if (transferFadeAlpha >= 1f) {
                    performTransfer();
                    transferPhase = TransferPhase.FADE_IN;
                    transferTimer = 0f;
                }
                break;

            case FADE_IN:
                transferTimer += delta;
                transferFadeAlpha = Math.max(0f, 1f - transferTimer / TRANSFER_FADE_DURATION);
                if (transferFadeAlpha <= 0f) {
                    transferFadeAlpha = 0f;
                    transferPhase = TransferPhase.NONE;
                    transferTarget = null;
                }
                break;

            default:
                break;
        }
    }

    /**
     * Swaps the current map for {@link #transferTarget}, placing the knight at
     * the new map's spawn point. Called once the fade-to-black is complete so
     * the swap is hidden from the player.
     */
    private void performTransfer() {
        GameEnvironment target = transferTarget;
        if (target == null) return;

        GameEnvironment previousMap = gameMap.getCurrentEnvironment();

        // Dispose the old map and load the target one.
        if (gameMap != null) {
            gameMap.dispose();
        }
        gameMap = MapLoader.loadMap(target, mapAssets, gameData);
        controller.setGameMap(gameMap);

        // Place the knight at the new map's transfer point.
        Vector2 spawn = gameMap.getTransferPoint(previousMap, target);
        knight.setPosition(spawn.x, spawn.y);

        // Re-center camera immediately on the new spawn point.
        controller.initializeCameraTarget();
        camera.position.set(controller.getCameraTarget().x, controller.getCameraTarget().y, 0f);
        camera.update();

        // Record the new environment on the save data.
        gameData.currentEnvironment = target.id;

        // Refresh the HUD.
        gameHud.update(0f);

        // Switch the background music to the new area.
        UiManager.getInstance().onGameEnvironmentChanged(target);
    }

    // --- Boss Arena Teleport (cheat) ---

    /** Instantly teleports the Knight to the False Knight arena. */
    private void teleportToBossArena() {
        if (gameMap == null) return;

        // Transfer the knight to forgotten crossroads if it's not there already
        if (gameMap.getCurrentEnvironment() != GameEnvironment.FORGOTTEN_CROSSROADS) {
            transferTarget = GameEnvironment.FORGOTTEN_CROSSROADS;
            performTransfer();
        }

        Arena target = null;
        for (Arena arena : gameMap.getArenas()) {
            if (arena.haveBoss && arena.arenaBossType == BossType.FALSE_KNIGHT) {
                target = arena;
                break;
            }
        }

        if (target == null) {
            return;
        }

        // Place the knight at the center-top of the arena.
        float x = target.x + target.width / 2f - Knight.HITBOX_WIDTH / 2f;
        float y = target.y + target.height - Knight.HITBOX_HEIGHT;
        knight.setPosition(x, y);

        // Snap the camera to the new position.
        controller.initializeCameraTarget();
        camera.position.set(controller.getCameraTarget().x, controller.getCameraTarget().y, 0f);
        camera.update();

        // Start the boss fight
        controller.forceStartBossFight(gameMap);
    }

    /** Draws the black overlay used during area transfers. */
    private void drawTransferFadeOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, transferFadeAlpha);

        float x = camera.position.x - camera.viewportWidth / 2f;
        float y = camera.position.y - camera.viewportHeight / 2f;
        shapeRenderer.rect(x, y, camera.viewportWidth, camera.viewportHeight);
        shapeRenderer.end();
    }

    // --- Pause ---

    /** Toggles the paused state. */
    private void togglePause() {
        if (paused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    /** Pauses gameplay. */
    private void pauseGame() {
        paused = true;
        Gdx.input.setInputProcessor(pauseOverlay.getStage());
    }

    /** Resumes gameplay. */
    private void resumeGame() {
        paused = false;
        Gdx.input.setInputProcessor(gameInputProcessor);
    }

    // --- Inventory ---

    /** Toggles the inventory overlay open / closed. */
    private void toggleInventory() {
        if (inventoryOpen) {
            closeInventory();
        } else {
            openInventory();
        }
    }

    /** Opens the inventory overlay and pauses gameplay input. */
    private void openInventory() {
        inventoryOpen = true;
        inventoryController.onOpen();
        Gdx.input.setInputProcessor(inventoryOverlay.getStage());
    }

    /** Closes the inventory overlay and restores gameplay input. */
    private void closeInventory() {
        inventoryOpen = false;
        Gdx.input.setInputProcessor(gameInputProcessor);
    }

    // --- Cheat registry accessor ---

    /** @return the cheat-code registry, or {@code null} if not initialized yet. */
    public CheatCodeRegistry getCheatRegistry() {
        return cheatRegistry;
    }

    // --- Saving ---

    /** Persists the current game state to the database. */
    public void saveGame() {
        // Sync live knight state into gameData.
        if (knight != null) {
            knight.writeToSave(gameData);
        }

        // Sync the currently loaded environment.
        if (gameMap != null && gameMap.getCurrentEnvironment() != null) {
            gameData.currentEnvironment = gameMap.getCurrentEnvironment().id;
        }

        // Stamp the save timestamp.
        gameData.lastSavedAt = System.currentTimeMillis();

        // Persist to the database.
        UiManager ui = UiManager.getInstance();
        if (ui != null && ui.getDatabase() != null && gameData.isPersisted()) {
            try {
                ui.getDatabase().saveGameData(gameData);
            } catch (java.sql.SQLException e) {
                System.err.println("[GameScreen] Failed to save game: " + e.getMessage());
            }
        }
    }

    // --- Cheat codes overlay ---

    /** Opens the cheat codes overlay on top of the pause menu. */
    private void showCheatCodes() {
        cheatCodesOpen = true;
        Gdx.input.setInputProcessor(cheatCodesOverlay.getStage());
    }

    /** Closes the cheat codes overlay and returns focus to the pause menu. */
    private void closeCheatCodes() {
        cheatCodesOpen = false;
        Gdx.input.setInputProcessor(pauseOverlay.getStage());
    }

    @Override
    public void resize(int width, int height) {
        float previousX = camera.position.x;
        float previousY = camera.position.y;

        camera.setToOrtho(false, width, height);
        camera.position.set(previousX, previousY, 0f);
        camera.update();
        gameHud.resize(width, height);
        displayOverlay.resize(width, height);
        pauseOverlay.resize(width, height);
        inventoryOverlay.resize(width, height);
        achievementPopup.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        EventBus.getInstance().unsubscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
        batch.dispose();
        shapeRenderer.dispose();
        gameMap.dispose();
        controller.dispose();
        gameHud.dispose();
        displayOverlay.dispose();
        pauseOverlay.dispose();
        inventoryOverlay.dispose();
        achievementPopup.dispose();
    }

    // --- Helpers ---

    private void renderDebugOverlay() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Draw knight bounds
        shapeRenderer.setColor(Color.GREEN);
        Rectangle bounds = knight.getBounds();
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw colliders
        shapeRenderer.setColor(Color.WHITE);
        for (Rectangle collider : gameMap.getColliders()) {
            shapeRenderer.rect(collider.x, collider.y, collider.width, collider.height);
        }

        // Draw doors collisions
        shapeRenderer.setColor(Color.WHITE);
        for (Door door : gameMap.getDoors()) {
            if (door.isSolid()) {
                shapeRenderer.rect(door.x, door.y, door.width, door.height);
            }
        }

        // Draw arenas
        shapeRenderer.setColor(Color.WHITE);
        for (Arena arena : gameMap.getArenas()) {
            shapeRenderer.rect(arena.x, arena.y, arena.width, arena.height);
        }

        // Draw attack hitbox if attacking
        Rectangle attackHitbox = knight.getAttackHitbox();
        if (attackHitbox != null) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
        }

        // Draw projectile hitboxes
        shapeRenderer.setColor(Color.YELLOW);
        for (Projectile projectile : controller.getProjectiles()) {
            Rectangle pBounds = projectile.getBounds();
            shapeRenderer.rect(pBounds.x, pBounds.y, pBounds.width, pBounds.height);
        }

        // Draw enemy hitboxes
        shapeRenderer.setColor(Color.ORANGE);
        for (Enemy enemy : controller.getEnemies()) {
            Rectangle eBounds = enemy.getBounds();
            shapeRenderer.rect(eBounds.x, eBounds.y, eBounds.width, eBounds.height);

            // Draw mossfly detection range
            if(enemy instanceof Mossfly) {
                Circle mCircle = ((Mossfly) enemy).getDetectionBounds();
                shapeRenderer.circle(mCircle.x, mCircle.y, mCircle.radius);
            }

            // Draw husk hornhead vision rectangle
            if(enemy instanceof HuskHornhead) {
                Rectangle vision = ((HuskHornhead) enemy).getVisionBounds();
                shapeRenderer.rect(vision.x, vision.y, vision.width, vision.height);
            }

            // Draw crystallized vision rectangle
            if(enemy instanceof Crystallized) {
                shapeRenderer.setColor(Color.CYAN);
                Rectangle vision = ((Crystallized) enemy).getVisionBounds();
                shapeRenderer.rect(vision.x, vision.y, vision.width, vision.height);
            }

            // Draw False Knight debug info
            if (enemy instanceof FalseKnight) {
                FalseKnight fk = (FalseKnight) enemy;
                shapeRenderer.setColor(Color.PURPLE);

                // Slam hitbox
                Rectangle slamHB = fk.getSlamHitbox();
                if (slamHB != null) {
                    shapeRenderer.rect(slamHB.x, slamHB.y, slamHB.width, slamHB.height);
                }

                // Jump attack hitbox
                shapeRenderer.setColor(Color.MAGENTA);
                Rectangle jumpHB = fk.getJumpAttackHitbox();
                if (jumpHB != null) {
                    shapeRenderer.rect(jumpHB.x, jumpHB.y, jumpHB.width, jumpHB.height);
                }
            }
        }

        // Draw laser hitboxes
        shapeRenderer.setColor(Color.MAGENTA);
        for (Laser laser : controller.getLasers()) {
            if (!laser.isDead()) {
                Rectangle lBounds = laser.getBounds();
                shapeRenderer.rect(lBounds.x, lBounds.y, lBounds.width, lBounds.height);
            }
        }

        shapeRenderer.end();
    }

    /**
     * Draws a semi-transparent black overlay on top of everything to simulate lower brightness.
     */
    private void drawBrightnessOverlay() {
        float brightness = settings.getBrightness();
        if (brightness >= 0.99f) return;

        float alpha = 1f - brightness;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float x = camera.position.x - camera.viewportWidth / 2f;
        float y = camera.position.y - camera.viewportHeight / 2f;

        brightnessOverlayRenderer.setProjectionMatrix(camera.combined);
        brightnessOverlayRenderer.begin(ShapeRenderer.ShapeType.Filled);
        brightnessOverlayRenderer.setColor(0, 0, 0, alpha);
        brightnessOverlayRenderer.rect(x, y, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        brightnessOverlayRenderer.end();
    }

    // --- Respawn state machine ---

    private enum RespawnPhase {
        NONE, // Normal gameplay
        DEATH_ANIM, // Waiting for death animation to finish
        FADE_TO_BLACK, // Black overlay alpha increasing
        FADE_IN // Black overlay alpha decreasing (map already reset)
    }

    // --- Area-transfer state machine ---

    private enum TransferPhase {
        NONE, // Normal gameplay
        FADE_TO_BLACK, // Black overlay alpha increasing (map not swapped yet)
        FADE_IN // Black overlay alpha decreasing (map already swapped)
    }

    // --- End-game (victory) state machine ---

    private enum EndGamePhase {
        NONE, // Normal gameplay
        WAIT, // Boss defeated - letting the death animation play out
        FADE_TO_BLACK, // Fading the screen to black before handing off
        DONE // Screen is about to be swapped
    }
}
