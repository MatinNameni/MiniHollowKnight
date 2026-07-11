package com.github.matinnameni.minihollowknight.controller.system;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.object.Arena;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

/**
 * Handles camera follow, arena lock-on, and camera shake.
 */
public class CameraSystem {

    private static final float CAMERA_LERP = 4f;
    private static final float CAMERA_X_OFFSET = 0f;
    private static final float CAMERA_Y_OFFSET = 200f;

    private final Vector2 cameraTarget = new Vector2();
    private float cameraShakeIntensity = 0f;

    public CameraSystem() {
    }

    /** Sets the camera target to the Knight's center (call once at init). */
    public void initializeCameraTarget(Knight knight) {
        cameraTarget.set(
            knight.getPosition().x + Knight.WIDTH / 2f + CAMERA_X_OFFSET,
            knight.getPosition().y + Knight.HEIGHT / 2f + CAMERA_Y_OFFSET
        );
    }

    /** Advances the camera position by one frame. */
    public void updateCamera(float delta, OrthographicCamera camera, Knight knight, TiledGameMap gameMap) {
        Vector2 knightPos = knight.getPosition();

        boolean lockedOnArea = false;

        for (Arena arena : gameMap.getArenas()) {
            if (knight.getBounds().overlaps(arena)) {
                float arenaHalfWidth = arena.width / 2f;
                float arenaHalfHeight = arena.height / 2f;
                float arenaCenterX = arena.x + arenaHalfWidth;
                float arenaCenterY = arena.y + arenaHalfHeight;

                cameraTarget.x = arenaCenterX;
                cameraTarget.y = arenaCenterY;

                camera.position.x += (cameraTarget.x - camera.position.x) * CAMERA_LERP * delta;
                camera.position.y += (cameraTarget.y - camera.position.y) * CAMERA_LERP * delta;

                lockedOnArea = true;

                break;
            }
        }

        if (!lockedOnArea) {
            cameraTarget.x = knightPos.x + Knight.WIDTH / 2f;
            cameraTarget.y = knightPos.y + Knight.HEIGHT / 2f;

            camera.position.x += (cameraTarget.x - camera.position.x) * CAMERA_LERP * delta;
            camera.position.y += (cameraTarget.y - camera.position.y) * CAMERA_LERP * delta;

            // Clamp camera
            float halfWidth = camera.viewportWidth / 2f;
            float halfHeight = camera.viewportHeight / 2f;

            camera.position.x = Math.max(halfWidth, Math.min(gameMap.getMapWidth() - halfWidth, camera.position.x));
            camera.position.y = Math.max(halfHeight, Math.min(gameMap.getMapHeight() - halfHeight, camera.position.y));
        }

        // Apply camera shake
        if (cameraShakeIntensity > 0.1f) {
            camera.position.x += (float) ((Math.random() * 2 - 1) * cameraShakeIntensity);
            camera.position.y += (float) ((Math.random() * 2 - 1) * cameraShakeIntensity);
            cameraShakeIntensity *= 0.9f; // decay
        } else {
            cameraShakeIntensity = 0f;
        }

        camera.update();
    }

    /** Adds camera shake intensity (keeps the larger value). */
    public void addShake(float intensity) {
        if (intensity > cameraShakeIntensity) {
            cameraShakeIntensity = intensity;
        }
    }

    // --- Getters ---

    public Vector2 getCameraTarget() {
        return cameraTarget;
    }

    /** Resets camera shake (called on map change). */
    public void reset() {
        cameraShakeIntensity = 0f;
    }
}
