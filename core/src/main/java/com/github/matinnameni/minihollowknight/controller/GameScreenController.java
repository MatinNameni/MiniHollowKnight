package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.GameData;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.screens.GameScreen;

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

        float knightX = knightHitbox.x;
        float kngihtY = knightHitbox.y;
        float knightWidth = knightHitbox.width;
        float knightHeight = knightHitbox.height;

        for (Rectangle platform : gameMap.getColliders()) {
            if (knightX + knightWidth <= platform.x || knightX >= platform.x + platform.width
                || kngihtY + knightHeight <= platform.y || kngihtY >= platform.y + platform.height) {
                continue;
            }

            float pushLeft = (knightX + knightWidth) - platform.x;
            float pushRight = (platform.x + platform.width) - knightX;
            float pushUp = (platform.y + platform.height) - kngihtY;
            float pushDown = (kngihtY + knightHeight) - platform.y;

            float minPush = Math.min(
                Math.min(pushLeft, pushRight),
                Math.min(pushUp, pushDown)
            );

            if (minPush == pushUp) {
                float resolvedHitboxY = platform.y + platform.height;
                knight.onFloorCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
            } else if (minPush == pushDown) {
                float resolvedHitboxY = platform.y - knightHeight;
                knight.onCeilingCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
            } else if (minPush == pushLeft) {
                float resolvedHitboxX = platform.x - knightWidth;
                knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
                knight.setHittingWall(true);
            } else {
                float resolvedHitboxX = platform.x + platform.width;
                knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
            }

            // Refresh knight position
            knightX = knightHitbox.x;
            kngihtY = knightHitbox.y;
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
