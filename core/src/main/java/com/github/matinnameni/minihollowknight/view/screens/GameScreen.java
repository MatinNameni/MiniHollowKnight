package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.controller.GameScreenController;
import com.github.matinnameni.minihollowknight.controller.PauseMenuController;
import com.github.matinnameni.minihollowknight.model.*;
import com.github.matinnameni.minihollowknight.model.asset.*;
import com.github.matinnameni.minihollowknight.model.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.enemies.Mossfly;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;
import com.github.matinnameni.minihollowknight.model.map.MapLoader;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.hud.GameHud;
import com.github.matinnameni.minihollowknight.view.hud.PauseOverlay;

/**
 * The main gameplay screen.
 */
public class GameScreen implements Screen {

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
    private TiledGameMap gameMap;

    // --- HUD ---
    private GameHud gameHud;

    // --- Pause menu ---
    private final MenuAssetBundle menuAssets;
    private PauseOverlay pauseOverlay;
    private boolean paused = false;

    // --- Brightness overlay ---
    private final ShapeRenderer brightnessOverlayRenderer = new ShapeRenderer();

    /** True once {@link #show()} has fully initialized the world. */
    private boolean initialized = false;

    // --- Debug ---
    private boolean showDebugInfo = true;

    public GameScreen(ScreenNavigator navigator, GameData gameData, Settings settings,
                      KnightAssetBundle knightAssets, HudAssetBundle hudAssets, MenuAssetBundle menuAssets,
                      CrawlidAssetBundle crawlidAssets, MossflyAssetBundle mossflyAssets) {
        this.navigator = navigator;
        this.gameData = gameData;
        this.settings = settings;
        this.knight = new Knight(knightAssets, settings);
        this.controller = new GameScreenController(navigator, settings, gameData, knight, crawlidAssets, mossflyAssets);
        this.gameHud = new GameHud(knight, hudAssets);
        this.menuAssets = menuAssets;
    }

    // --- Screen ---

    @Override
    public void show() {
        if (initialized) {
            if (paused) {
                Gdx.input.setInputProcessor(pauseOverlay.getStage());
            } else {
                Gdx.input.setInputProcessor(null);
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
            MapLoader.loadMap(GameEnvironment.FORGOTTEN_CROSSROADS) :
            MapLoader.loadMap(currentEnvironment);
        controller.setGameMap(gameMap);

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

        // Initialize the pause menu overlay
        PauseMenuController pauseController = new PauseMenuController(navigator, this, this::resumeGame);
        pauseOverlay = new PauseOverlay(pauseController, menuAssets);
        pauseOverlay.init();
        pauseOverlay.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.input.setInputProcessor(null);
        // TODO: implement input processors for the game

        initialized = true;
    }

    @Override
    public void render(float delta) {
        // Temporary key bindings

        if (Gdx.input.isKeyJustPressed(settings.getKeyPause())) {
            togglePause();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showDebugInfo = !showDebugInfo;
        }

        if (!paused) {
            controller.update(delta, camera);

            // Update HUD
            gameHud.update(delta);
        }

        Gdx.gl.glClearColor(16/255f, 13/255f, 143/255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background layer
        gameMap.renderBackground(camera);

        // Spike layers
        gameMap.renderSpikeLayer(camera);

        // Main layers
        gameMap.renderMain(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Enemies
        for (Enemy enemy : controller.getEnemies()) {
            enemy.render(batch);
        }

        // Knight
        knight.render(batch);

        // Projectiles
        for (Projectile projectile : controller.getProjectiles()) {
            projectile.render(batch);
        }
        batch.end();

        // Foreground layer
        gameMap.renderForeground(camera);

        // Knight effects
        batch.begin();
        knight.renderEffects(batch);
        batch.end();

        // Debug overlay
        if (showDebugInfo) {
            renderDebugOverlay();
        }

        // HUD
        gameHud.draw();

        // Pause menu overlay
        if (paused) {
            pauseOverlay.update(delta);
            pauseOverlay.draw();
        }

        // Brightness overlay
        drawBrightnessOverlay();
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
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
        float previousX = camera.position.x;
        float previousY = camera.position.y;

        camera.setToOrtho(false, width, height);
        camera.position.set(previousX, previousY, 0f);
        camera.update();
        gameHud.resize(width, height);
        pauseOverlay.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        gameMap.dispose();
        controller.dispose();
        gameHud.dispose();
        pauseOverlay.dispose();
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
}
