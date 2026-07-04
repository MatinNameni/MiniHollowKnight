package com.github.matinnameni.minihollowknight.model.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.GridObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a Tiled map for use in the game.
 */
public class TiledGameMap {

    // --- Constats ---

    public static final float UNIT_SCALE = 0.5f;

    private static final String SPAWN_POINT_NAME = "playerSpawnPoint";
    private static final String CRAWLID_SPAWN_NAME = "crawlidSpawn";
    private static final String MOSSFLY_SPAWN_NAME = "mossflySpawn";

    // --- Tiled ---

    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer renderer;

    // --- Cached layers ---

    private TiledMapTileLayer spikeLayer0;
    private TiledMapTileLayer spikeLayer1;
    private TiledMapTileLayer spikeLayer2;
    private TiledMapTileLayer spikeLayer3;

    private TiledMapTileLayer backgroundLayer;

    private TiledMapTileLayer mainBackgroundLayer;
    private TiledMapTileLayer mainLayer;
    private TiledMapTileLayer mainForegroundLayer;

    private TiledMapTileLayer foregroundLayer;

    // --- Extracted data ---

    private final List<GridObject> colliders = new ArrayList<>();
    private final Vector2 playerSpawn = new Vector2(100f, 100f);
    private final List<Vector2> crawlidSpawns = new ArrayList<>();
    private final List<Vector2> mossflySpawns = new ArrayList<>();

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
                case "spikeLayer0":
                    spikeLayer0 = tileLayer;
                    break;
                case "spikeLayer1":
                    spikeLayer1 = tileLayer;
                    break;
                case "spikeLayer2":
                    spikeLayer2 = tileLayer;
                    break;
                case "spikeLayer3":
                    spikeLayer3 = tileLayer;
                    break;
                case "background":
                    backgroundLayer = tileLayer;
                    break;
                case "mainBackground":
                    mainBackgroundLayer = tileLayer;
                    break;
                case "main":
                    mainLayer = tileLayer;
                    break;
                case "mainForeground":
                    mainForegroundLayer = tileLayer;
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
     * Reads every {@link RectangleMapObject} and point {@link MapObject} in the map.
     */
    private void extractObjects(float unitScale) {
        for (MapLayer layer : tiledMap.getLayers()) {
            for (MapObject mapObject : layer.getObjects()) {
                if (mapObject instanceof RectangleMapObject) {
                    extractRectangleObject((RectangleMapObject) mapObject, unitScale);
                } else if (mapObject instanceof PointMapObject) {
                    extractPointObject(mapObject, unitScale);
                }
            }
        }
    }

    /** Handles a rectangle-shaped object. */
    private void extractRectangleObject(RectangleMapObject rectangleObject, float unitScale) {
        Rectangle currentRectangle = rectangleObject.getRectangle();

        // Is deadly
        boolean isDeadly = false;
        if (rectangleObject.getProperties().containsKey("isDeadly")) {
            isDeadly = rectangleObject.getProperties().get("isDeadly", Boolean.class);
        }

        // Can pogo over it
        boolean canPogo = false;
        if (rectangleObject.getProperties().containsKey("canPogo")) {
            canPogo = rectangleObject.getProperties().get("canPogo", Boolean.class);
        }

        if (SPAWN_POINT_NAME.equals(rectangleObject.getName())) {
            playerSpawn.set(currentRectangle.x * unitScale, currentRectangle.y * unitScale);
        } else {
            colliders.add(new GridObject(
                currentRectangle.x * unitScale,
                currentRectangle.y * unitScale,
                currentRectangle.width * unitScale,
                currentRectangle.height * unitScale,
                isDeadly,
                canPogo
            ));
        }
    }

    /** Handles a Tiled Point object. */
    private void extractPointObject(MapObject mapObject, float unitScale) {
        MapProperties properties = mapObject.getProperties();
        float x = properties.get("x", 0f, Float.class);
        float y = properties.get("y", 0f, Float.class);

        switch (mapObject.getName()) {
            case CRAWLID_SPAWN_NAME:
                crawlidSpawns.add(new Vector2(x * unitScale, y * unitScale));
                break;
            case MOSSFLY_SPAWN_NAME:
                mossflySpawns.add(new Vector2(x * unitScale, y * unitScale));
                break;
        }
    }

    // --- Rendering ---

    /**
     * Renders the background tile layer.
     * Should be called before any other layers.
     */
    public void renderBackground(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.getBatch().setProjectionMatrix(camera.combined);
        renderer.getBatch().begin();
        if (backgroundLayer != null) renderer.renderTileLayer(backgroundLayer);
        renderer.getBatch().end();
    }

    /**
     * Renders the spike layers.
     * Should be called before drawing main layers.
     */
    public void renderSpikeLayer(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.getBatch().setProjectionMatrix(camera.combined);
        renderer.getBatch().begin();
        if (spikeLayer0 != null) renderer.renderTileLayer(spikeLayer0);
        if (spikeLayer1 != null) renderer.renderTileLayer(spikeLayer1);
        if (spikeLayer2 != null) renderer.renderTileLayer(spikeLayer2);
        if (spikeLayer3 != null) renderer.renderTileLayer(spikeLayer3);
        renderer.getBatch().end();
    }

    /**
     * Renders the main tile layers.
     * Should be called before drawing entities.
     */
    public void renderMain(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.getBatch().setProjectionMatrix(camera.combined);
        renderer.getBatch().begin();
        if (mainBackgroundLayer != null) renderer.renderTileLayer(mainBackgroundLayer);
        if (mainLayer != null) renderer.renderTileLayer(mainLayer);
        if (mainForegroundLayer != null) renderer.renderTileLayer(mainForegroundLayer);
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

    public List<GridObject> getColliders() {
        return colliders;
    }

    public Vector2 getPlayerSpawn() {
        return playerSpawn;
    }

    public List<Vector2> getCrawlidSpawns() {
        return crawlidSpawns;
    }

    public List<Vector2> getMossflySpawns() {
        return mossflySpawns;
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
