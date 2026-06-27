package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.controller.GameScreenController;
import com.github.matinnameni.minihollowknight.model.GameData;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;
import com.github.matinnameni.minihollowknight.model.map.MapLoader;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;
import com.github.matinnameni.minihollowknight.model.asset.KnightAssetBundle;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;

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

    // --- Debug ---
    private boolean showDebugInfo = true;

    public GameScreen(ScreenNavigator navigator, GameData gameData, Settings settings, KnightAssetBundle knightAssets) {
        this.navigator = navigator;
        this.gameData = gameData;
        this.settings = settings;
        this.knight = new Knight(knightAssets, settings);
        this.controller = new GameScreenController(navigator, settings, gameData, knight);
    }

    // --- Screen ---

    @Override
    public void show() {
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

        Gdx.input.setInputProcessor(null);
        // TODO: implement input processors for the game
    }

    @Override
    public void render(float delta) {
        // Temporary key bindings

        if (Gdx.input.isKeyJustPressed(settings.getKeyPause())) {
            navigator.goToMainMenu();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showDebugInfo = !showDebugInfo;
        }

        controller.update(delta, camera);

        Gdx.gl.glClearColor(16/255f, 13/255f, 143/255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background layer
        gameMap.renderBackground(camera);

        // Knight
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        knight.render(batch);
        batch.end();

        // Foreground layer
        gameMap.renderForeground(camera);

        // Debug overlay
        if (showDebugInfo) {
            renderDebugOverlay();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.update();
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

        shapeRenderer.end();
    }
}
