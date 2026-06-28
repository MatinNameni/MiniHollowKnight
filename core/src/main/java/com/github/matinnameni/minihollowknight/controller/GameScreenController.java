package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.GameData;
import com.github.matinnameni.minihollowknight.model.GridObject;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.screens.GameScreen;

import java.util.LinkedHashMap;
import java.util.Map;

public class GameScreenController {
    private ScreenNavigator navigator;
    private Settings settings;
    private GameData gameData;
    private Knight knight;
    private TiledGameMap gameMap;

    // --- Camera control ---
    private static final float CAMERA_LERP = 4f;
    private final Vector2 cameraTarget = new Vector2();

    public GameScreenController(ScreenNavigator navigator, Settings settings,
                                GameData gameData, Knight knight) {
        this.navigator = navigator;
        this.settings = settings;
        this.gameData = gameData;
        this.knight = knight;
    }

    public void initializeCameraTarget() {
        cameraTarget.set(knight.getPosition().x + Knight.WIDTH / 2f,
            knight.getPosition().y + Knight.HEIGHT / 2f);
    }

    public void update(float delta, OrthographicCamera camera) {
        delta = Math.min(delta, 0.05f);

        resolveNailAttack();

        knight.setGrounded(false);
        knight.setHittingWall(false);

        resolveCollisions();

        knight.update(delta);

        updateCamera(delta, camera);
    }

    // --- Update Helpers ---

    /** Resolves collisions between the Knight's bounding box and all map colliders. */
    private void resolveCollisions() {
        Rectangle knightHitbox = knight.getBounds();

        Map<GridObject, Direction> collisions = getOverlappingObjects(knightHitbox);

        for(Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (direction == Direction.UP) {
                if(platform.isDeadly) {
                    knight.takeDamage(-1);
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxY = platform.y + platform.height;
                knight.onFloorCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
                knight.setSafePosition(knight.getPosition().x, knight.getPosition().y);
            } else if (direction == Direction.DOWN) {
                if(platform.isDeadly) {
                    knight.takeDamage(-1);
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxY = platform.y - knightHitbox.height;
                knight.onCeilingCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                if(platform.isDeadly) {
                    knight.takeDamage(-1);
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxX = platform.x - knightHitbox.width;
                knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
                knight.setHittingWall(true);
            } else {
                if(platform.isDeadly) {
                    knight.takeDamage(1);
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxX = platform.x + platform.width;
                knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
            }
        }
    }

    private void updateCamera(float delta, OrthographicCamera camera) {
        Vector2 knightPos = knight.getPosition();

        cameraTarget.x = knightPos.x + Knight.WIDTH / 2f;
        cameraTarget.y = knightPos.y + Knight.HEIGHT / 2f;

        // for smoother display, the camera stays a little behind the knight
        camera.position.x += (cameraTarget.x - camera.position.x) * CAMERA_LERP * delta;
        camera.position.y += (cameraTarget.y - camera.position.y) * CAMERA_LERP * delta;

        // Clamp camera so it doesn't show outside the map
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        camera.position.x = Math.max(halfWidth, Math.min(gameMap.getMapWidth() - halfWidth, camera.position.x));
        camera.position.y = Math.max(halfHeight, Math.min(gameMap.getMapHeight() - halfHeight, camera.position.y));

        camera.update();
    }

    /** Resolves the Knight's nail attack if he's in attacking state. */
    public void resolveNailAttack() {
        if(knight.getState() != KnightState.ATTACKING) {
            return;
        }

        resolvePlatformAttack();
    }

    /** Resolves collisions between the Knight's attack hitbox and all map colliders. */
    public void resolvePlatformAttack() {
        Rectangle attackHitbox = knight.getAttackHitbox();

        Map<GridObject, Direction> collisions = getOverlappingObjects(attackHitbox);

        for(Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (direction == Direction.UP) {
                if (platform.canPogo) {
                    knight.onDownAttackBounce();
                }
            }
        }
    }

    // --- Helpers ---

    private Map<GridObject, Direction> getOverlappingObjects(Rectangle rect) {
        Map<GridObject, Direction> overlappingObjects = new LinkedHashMap<>();

        float x = rect.x;
        float y = rect.y;
        float width = rect.width;
        float height = rect.height;

        for (GridObject platform : gameMap.getColliders()) {
            if (!rect.overlaps(platform)) {
                continue;
            }

            float pushLeft = (x + width) - platform.x;
            float pushRight = (platform.x + platform.width) - x;
            float pushUp = (platform.y + platform.height) - y;
            float pushDown = (y + height) - platform.y;

            float minPush = Math.min(
                Math.min(pushLeft, pushRight),
                Math.min(pushUp, pushDown)
            );

            if (minPush == pushUp) {
                overlappingObjects.put(platform, Direction.UP);
            } else if (minPush == pushDown) {
                overlappingObjects.put(platform, Direction.DOWN);
            } else if (minPush == pushLeft) {
                overlappingObjects.put(platform, Direction.LEFT);
            } else {
                overlappingObjects.put(platform, Direction.RIGHT);
            }
        }

        return overlappingObjects;
    }

    // --- Getters ---

    public Vector2 getCameraTarget() {
        return cameraTarget;
    }

    // --- Setters ---

    /** Should be called after each time a new map was loaded in {@link GameScreen}. */
    public void setGameMap(TiledGameMap gameMap) {
        this.gameMap = gameMap;
    }
}
