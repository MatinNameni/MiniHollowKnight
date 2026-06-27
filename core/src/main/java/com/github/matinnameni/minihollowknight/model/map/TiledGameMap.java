package com.github.matinnameni.minihollowknight.model.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a Tiled map for use in the game.
 */
public class TiledGameMap {

    // --- Constats ---

    public static final float UNIT_SCALE = 0.5f;

    private static final String SPAWN_POINT_NAME = "playerSpawnPoint";

    // --- Tiled ---

    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer renderer;

    // --- Cached layers ---

    private TiledMapTileLayer backgroundLayer;
    private TiledMapTileLayer mainLayer;
    private TiledMapTileLayer foregroundLayer;

    // --- Extracted data ---

    private final List<Rectangle> colliders = new ArrayList<>();
    private final Vector2 playerSpawn = new Vector2(100f, 100f);

    // --- Map dimensions ---

    private final float mapWidth;
    private final float mapHeight;

    public TiledGameMap(String path) {
        this(path, UNIT_SCALE);
    }

    public TiledGameMap(String path, float unitScale) {
        tiledMap = new TmxMapLoader().load(path);
        renderer = new OrthogonalTiledMapRenderer(tiledMap, unitScale);

        MapProperties properties = tiledMap.getProperties();
        int tileWidth = properties.get("tilewidth", int.class);
        int tileHeight = properties.get("tileheight", int.class);
        int tilesHorizontalCount = properties.get("width",  int.class);
        int tilesVerticalCount = properties.get("height", int.class);

        this.mapWidth = tilesHorizontalCount * tileWidth * unitScale;
        this.mapHeight = tilesVerticalCount * tileHeight * unitScale;

        cacheLayers();
        extractObjects(unitScale);
    }

    // --- Layer cache ---

    private void cacheLayers() {
        for (MapLayer layer : tiledMap.getLayers()) {
            if (!(layer instanceof TiledMapTileLayer)) continue;
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
            switch (tileLayer.getName()) {
                case "background":
                    backgroundLayer = tileLayer;
                    break;
                case "main":
                    mainLayer = tileLayer;
                    break;
                case "foreground":
                    foregroundLayer = tileLayer;
                    break;
                default: break;
            }
        }
    }

    // --- Object extraction ---

    /**
     * Reads every {@link RectangleMapObject} in the map.
     */
    private void extractObjects(float unitScale) {
        for (MapLayer layer : tiledMap.getLayers()) {
            for (MapObject mapObject : layer.getObjects()) {
                if (!(mapObject instanceof RectangleMapObject)) continue;

                RectangleMapObject rectangleObject = (RectangleMapObject) mapObject;
                Rectangle currentRectangle = rectangleObject.getRectangle();

                if (SPAWN_POINT_NAME.equals(mapObject.getName())) {
                    playerSpawn.set(currentRectangle.x * unitScale, currentRectangle.y * unitScale);
                } else {
                    colliders.add(new Rectangle(
                        currentRectangle.x * unitScale,
                        currentRectangle.y * unitScale,
                        currentRectangle.width * unitScale,
                        currentRectangle.height * unitScale
                    ));
                }
            }
        }
    }

    // --- Rendering ---

    /**
     * Renders the background and main tile layers.
     * Should be called before drawing entities.
     */
    public void renderBackground(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.getBatch().setProjectionMatrix(camera.combined);
        renderer.getBatch().begin();
        if (backgroundLayer != null) renderer.renderTileLayer(backgroundLayer);
        if (mainLayer != null) renderer.renderTileLayer(mainLayer);
        renderer.getBatch().end();
    }

    /**
     * Renders the foreground tile layer.
     * Should be called after drawing entities.
     */
    public void renderForeground(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.getBatch().setProjectionMatrix(camera.combined);
        renderer.getBatch().begin();
        if (foregroundLayer != null) renderer.renderTileLayer(foregroundLayer);
        renderer.getBatch().end();
    }

    // --- Getters ---

    public List<Rectangle> getColliders() {
        return colliders;
    }

    public Vector2 getPlayerSpawn() {
        return playerSpawn;
    }

    public float getMapWidth() {
        return mapWidth;
    }

    public float getMapHeight() {
        return mapHeight;
    }

    public OrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    // --- Lifecycle ---

    public void dispose() {
        tiledMap.dispose();
        renderer.dispose();
    }
}
