package com.github.matinnameni.minihollowknight.model.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.github.matinnameni.minihollowknight.model.Arena;
import com.github.matinnameni.minihollowknight.model.BreakableWall;
import com.github.matinnameni.minihollowknight.model.Door;
import com.github.matinnameni.minihollowknight.model.GridObject;
import com.github.matinnameni.minihollowknight.model.asset.TiledMapAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;

import java.util.*;

/**
 * Wraps a Tiled map for use in the game.
 */
public class TiledGameMap {

    // --- Constants ---

    public static final float UNIT_SCALE = 0.5f;

    private static final String MAIN_LAYER_NAME = "MainLayer";
    private static final String DOORS_LAYER_NAME = "Doors";

    private static final String SPAWN_POINT_NAME = "playerSpawnPoint";
    private static final String CRAWLID_SPAWN_NAME = "crawlidSpawn";
    private static final String MOSSFLY_SPAWN_NAME = "mossflySpawn";
    private static final String HUSK_HORNHEAD_SPAWN_NAME = "huskHornheadSpawn";
    private static final String CRYSTALLIZED_SPAWN_NAME = "crystallizedSpawn";
    private static final String FALSE_KNIGHT_SPAWN_NAME = "falseKnightSpawn";

    private static final String FORGOTTEN_CROSSROADS_TO_GREENPATH = "fcToG";
    private static final String GREENPATH_TO_FORGOTTEN_CROSSROADS = "gToFc";

    private static final String PROP_CAN_POGO = "canPogo";
    private static final String PROP_IS_DEADLY = "isDeadly";
    private static final String PROP_IS_BREAKABLE = "isBreakable";
    private static final String PROP_TRANSFER_TO = "transferTo";
    private static final String PROP_HAVE_BOSS = "haveBoss";
    private static final String PROP_BOSS_NAME = "bossName";
    private static final String PROP_TEX_DEFAULT = "textureDefault";
    private static final String PROP_TEX_CRACKED = "textureCracked";
    private static final String PROP_TEX_BROKEN = "textureBroken";
    private static final String PROP_HITS_TO_BREAK = "hitsToBreak";

    // --- Arena ---
    private static final String ARENA_NAME = "arena";

    // --- Tiled ---

    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer renderer;

    // --- Cached layers ---

    private TiledMapTileLayer backgroundLayer;

    private TiledMapTileLayer blackMaskBGLayer;

    private TiledMapTileLayer spikeLayer0;
    private TiledMapTileLayer spikeLayer1;
    private TiledMapTileLayer spikeLayer2;
    private TiledMapTileLayer spikeLayer3;

    private TiledMapTileLayer mainBackgroundLayer;
    private TiledMapTileLayer mainLayer;
    private TiledMapTileLayer mainForegroundLayer;

    private TiledMapTileLayer blackMaskFGLayer;

    private TiledMapTileLayer foregroundLayer;

    // --- Extracted data ---

    private final List<GridObject> colliders = new ArrayList<>();
    private final Vector2 playerSpawn = new Vector2(100f, 100f);
    private final List<Vector2> crawlidSpawns = new ArrayList<>();
    private final List<Vector2> mossflySpawns = new ArrayList<>();
    private final List<Vector2> huskHornheadSpawns = new ArrayList<>();
    private final List<Vector2> crystallizedSpawns = new ArrayList<>();
    private final List<Vector2> falseKnightSpawns = new ArrayList<>();
    private final List<BreakableWall> breakableWalls = new ArrayList<>();
    private final List<Door> doors = new ArrayList<>();
    private final Map<ArrayList<GameEnvironment>, Vector2> transferPoints = new HashMap<>();

    // --- Arenas bounds ---
    private List<Arena> arenas = new ArrayList<>();

    // --- Map dimensions ---

    private final float mapWidth;
    private final float mapHeight;

    // --- Hidden room ---

    private boolean blackMaskRemoved = false;

    // --- Current environment ---
    private GameEnvironment currentEnvironment;

    // --- Assets ---
    private TiledMapAssetBundle mapAssets;

    public TiledGameMap(GameEnvironment environment, TiledMapAssetBundle mapAssets) {
        this(environment, UNIT_SCALE, mapAssets);
    }

    public TiledGameMap(GameEnvironment environment, float unitScale, TiledMapAssetBundle mapAssets) {
        tiledMap = new TmxMapLoader().load(environment.path);
        renderer = new OrthogonalTiledMapRenderer(tiledMap, unitScale);

        MapProperties properties = tiledMap.getProperties();
        int tileWidth = properties.get("tilewidth", int.class);
        int tileHeight = properties.get("tileheight", int.class);
        int tilesHorizontalCount = properties.get("width",  int.class);
        int tilesVerticalCount = properties.get("height", int.class);

        this.mapWidth = tilesHorizontalCount * tileWidth * unitScale;
        this.mapHeight = tilesVerticalCount * tileHeight * unitScale;

        this.currentEnvironment = environment;
        this.mapAssets = mapAssets;

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
                case "blackMaskBG":
                    blackMaskBGLayer = tileLayer;
                    break;
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
                case "mainBackground":
                    mainBackgroundLayer = tileLayer;
                    break;
                case "main":
                    mainLayer = tileLayer;
                    break;
                case "mainForeground":
                    mainForegroundLayer = tileLayer;
                    break;
                case "blackMaskFG":
                    blackMaskFGLayer = tileLayer;
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
        // Main layer
        MapLayer doorsLayer = tiledMap.getLayers().get(MAIN_LAYER_NAME);
        if (doorsLayer != null) {
            for (MapObject mapObject : doorsLayer.getObjects()) {
                if (mapObject instanceof RectangleMapObject) {
                    extractRectangleObject((RectangleMapObject) mapObject, unitScale);
                } else if (mapObject instanceof PointMapObject) {
                    extractPointObject(mapObject, unitScale);
                }
            }
        }

        // Doors
        MapLayer mainLayer = tiledMap.getLayers().get(DOORS_LAYER_NAME);
        if (mainLayer != null) {
            for (MapObject mapObject : mainLayer.getObjects()) {
                if (mapObject instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) mapObject).getRectangle();

                    Door door = new Door(
                        rect.x * unitScale,
                        rect.y * unitScale,
                        rect.width * unitScale,
                        rect.height * unitScale,
                        mapAssets);
                    doors.add(door);
                }
            }
        }
    }

    /** Handles a rectangle-shaped object. */
    private void extractRectangleObject(RectangleMapObject rectangleObject, float unitScale) {
        Rectangle currentRectangle = rectangleObject.getRectangle();

        // Is breakable
        boolean isBreakable = false;
        if (rectangleObject.getProperties().containsKey(PROP_IS_BREAKABLE)) {
            isBreakable = rectangleObject.getProperties().get(PROP_IS_BREAKABLE, Boolean.class);
        }

        if (isBreakable) {
            BreakableWall wall = createBreakableWall(rectangleObject, currentRectangle, unitScale);
            breakableWalls.add(wall);

            if (wall.getCollider() != null) {
                colliders.add(wall.getCollider());
            }
            return;
        }

        // Is deadly
        boolean isDeadly = false;
        if (rectangleObject.getProperties().containsKey(PROP_IS_DEADLY)) {
            isDeadly = rectangleObject.getProperties().get(PROP_IS_DEADLY, Boolean.class);
        }

        // Can pogo over it
        boolean canPogo = false;
        if (rectangleObject.getProperties().containsKey(PROP_CAN_POGO)) {
            canPogo = rectangleObject.getProperties().get(PROP_CAN_POGO, Boolean.class);
        }

        // Transfers the knight to another area
        String transferTo = null;
        if (rectangleObject.getProperties().containsKey(PROP_TRANSFER_TO)) {
            transferTo = rectangleObject.getProperties().get(PROP_TRANSFER_TO, String.class);
        }

        if (SPAWN_POINT_NAME.equals(rectangleObject.getName())) {
            playerSpawn.set(currentRectangle.x * unitScale, currentRectangle.y * unitScale);
        } else if (ARENA_NAME.equals(rectangleObject.getName())) {
            boolean haveBoss = false;
            if(rectangleObject.getProperties().containsKey(PROP_HAVE_BOSS)) {
                haveBoss = rectangleObject.getProperties().get(PROP_HAVE_BOSS, Boolean.class);
            }

            Arena newArena = new Arena(
                currentRectangle.x * unitScale,
                currentRectangle.y * unitScale,
                currentRectangle.width * unitScale,
                currentRectangle.height * unitScale,
                haveBoss
            );

            if (newArena.haveBoss) {
                newArena.resolveBossName(rectangleObject.getProperties().get(PROP_BOSS_NAME, String.class));
            }

            arenas.add(newArena);

        } else {
            colliders.add(new GridObject(
                currentRectangle.x * unitScale,
                currentRectangle.y * unitScale,
                currentRectangle.width * unitScale,
                currentRectangle.height * unitScale,
                isDeadly,
                canPogo,
                transferTo
            ));
        }
    }

    /** Creates a {@link BreakableWall} from a Tiled rectangle object. */
    private BreakableWall createBreakableWall(RectangleMapObject obj, Rectangle rect, float unitScale) {
        MapProperties props = obj.getProperties();

        String texDefault = props.get(PROP_TEX_DEFAULT, String.class);
        String texCracked = props.get(PROP_TEX_CRACKED, String.class);
        String texBroken = props.get(PROP_TEX_BROKEN,  String.class);

        int hitsToBreak = 3;
        if (props.containsKey(PROP_HITS_TO_BREAK)) {
            hitsToBreak = props.get(PROP_HITS_TO_BREAK, Integer.class);
        }

        return new BreakableWall(
            rect.x * unitScale,
            rect.y * unitScale,
            rect.width * unitScale,
            rect.height * unitScale,
            -1, 1,
            currentEnvironment,
            texDefault,
            texCracked,
            texBroken,
            hitsToBreak
        );
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
            case HUSK_HORNHEAD_SPAWN_NAME:
                huskHornheadSpawns.add(new Vector2(x * unitScale, y * unitScale));
                break;
            case CRYSTALLIZED_SPAWN_NAME:
                crystallizedSpawns.add(new Vector2(x * unitScale, y * unitScale));
                break;
            case FALSE_KNIGHT_SPAWN_NAME:
                falseKnightSpawns.add(new Vector2(x * unitScale, y * unitScale));
                break;
            case FORGOTTEN_CROSSROADS_TO_GREENPATH:
                transferPoints.put(new ArrayList<>(
                    List.of(GameEnvironment.FORGOTTEN_CROSSROADS, GameEnvironment.GREENPATH)),
                    new Vector2(x * unitScale, y * unitScale)
                );
                break;
            case GREENPATH_TO_FORGOTTEN_CROSSROADS:
                transferPoints.put(new ArrayList<>(
                        List.of(GameEnvironment.GREENPATH, GameEnvironment.FORGOTTEN_CROSSROADS)),
                    new Vector2(x * unitScale, y * unitScale)
                );
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
        if (blackMaskBGLayer != null) renderer.renderTileLayer(blackMaskBGLayer);
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
        if (blackMaskFGLayer != null) renderer.renderTileLayer(blackMaskFGLayer);
        if (foregroundLayer != null) renderer.renderTileLayer(foregroundLayer);
        renderer.getBatch().end();
    }

    // --- Getters ---

    public GameEnvironment getCurrentEnvironment() {
        return currentEnvironment;
    }

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

    public List<Vector2> getHuskHornheadSpawns() {
        return huskHornheadSpawns;
    }

    public List<Vector2> getCrystallizedSpawns() {
        return crystallizedSpawns;
    }

    public List<Vector2> getFalseKnightSpawns() {
        return falseKnightSpawns;
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

    public List<BreakableWall> getBreakableWalls() {
        return breakableWalls;
    }

    public List<Arena> getArenas() {
        return arenas;
    }

    public List<Door> getDoors() {
        return doors;
    }

    public Vector2 getTransferPoint(GameEnvironment previous, GameEnvironment current) {
        for (Map.Entry<ArrayList<GameEnvironment>, Vector2> entry : transferPoints.entrySet()) {
            GameEnvironment env1 = entry.getKey().getFirst();
            GameEnvironment env2 = entry.getKey().getLast();

            if (env1.id != previous.id || env2.id != current.id) continue;

            return entry.getValue();
        }
        return null;
    }

    /** Called by the controller when a breakable wall is fully destroyed. */
    public void removeBreakableWallCollider(BreakableWall wall) {
        colliders.remove(wall.getCollider());
    }

    /** Removes the black mask covering the hidden room. */
    public void removeBlackMask() {
        if (blackMaskRemoved) return;
        blackMaskRemoved = true;
        if (blackMaskBGLayer != null) {
            blackMaskBGLayer.setVisible(false);
            blackMaskBGLayer.setOpacity(0f);
        }
        if (blackMaskFGLayer != null) {
            blackMaskFGLayer.setVisible(false);
            blackMaskFGLayer.setOpacity(0f);
        }
    }

    // --- Lifecycle ---

    public void dispose() {
        for (BreakableWall wall : breakableWalls) {
            wall.dispose();
        }
        tiledMap.dispose();
        renderer.dispose();
    }
}
