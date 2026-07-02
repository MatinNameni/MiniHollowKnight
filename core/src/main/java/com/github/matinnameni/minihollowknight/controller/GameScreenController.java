package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.event.EventBus;
import com.github.matinnameni.minihollowknight.event.GameEvent;
import com.github.matinnameni.minihollowknight.event.EventListener;
import com.github.matinnameni.minihollowknight.model.*;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.screens.GameScreen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameScreenController implements EventListener {
    private ScreenNavigator navigator;
    private Settings settings;
    private GameData gameData;
    private Knight knight;
    private TiledGameMap gameMap;

    // --- Projectiles ---
    private final List<Projectile> projectiles = new ArrayList<>();

    // --- Camera control ---
    private static final float CAMERA_LERP = 4f;
    private final Vector2 cameraTarget = new Vector2();

    public GameScreenController(ScreenNavigator navigator, Settings settings,
                                GameData gameData, Knight knight) {
        this.navigator = navigator;
        this.settings = settings;
        this.gameData = gameData;
        this.knight = knight;

        EventBus.getInstance().subscribe(GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST, this);
        EventBus.getInstance().subscribe(GameEvent.PLAYER_HOWLING_WRAITHS_CAST, this);
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

        // wall proximity check
        if (isAdjacentToWall(knight.getBounds())) {
            knight.setHittingWall(true);
        }

        // floor proximity check
        if(isOnFloor(knight.getBounds())) {
            knight.setGrounded(true);
        }

        knight.update(delta);

        updateProjectiles(delta);

        updateCamera(delta, camera);
    }

    // --- Projectiles ---

    /** Returns the list of active projectiles. */
    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    /** Updates all active projectiles. */
    private void updateProjectiles(float delta) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();

            if(projectile instanceof VengefulSpirit) {
                updateVengefulSpirit(delta, (VengefulSpirit) projectile);
            } else if(projectile instanceof HowlingWraiths) {
                updateHowlingWraiths(delta, (HowlingWraiths) projectile);
            }

            if (projectile.isDead()) {
                iterator.remove();
            }
        }
    }

    private void updateVengefulSpirit(float delta, VengefulSpirit projectile) {
        if (projectile.isFlying()) {
            Rectangle projectileBounds = projectile.getBounds();
            for (GridObject platform : gameMap.getColliders()) {
                if (projectileBounds.y >= platform.y &&
                projectileBounds.y + projectileBounds.height <= platform.y + platform.height &&
                projectileBounds.overlaps(platform)) {
                    projectile.onHitWall();
                    break;
                }
            }
        }

        projectile.update(delta);
    }

    private void updateHowlingWraiths(float delta, HowlingWraiths projectile) {
        projectile.update(delta);
    }

    // --- EventListener ---

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event == GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST && payload instanceof VengefulSpirit.SpawnInfo) {
            VengefulSpirit.SpawnInfo info = (VengefulSpirit.SpawnInfo) payload;
            projectiles.add(new VengefulSpirit(info.x, info.y, info.direction, info.assets));
        } else if(event == GameEvent.PLAYER_HOWLING_WRAITHS_CAST && payload instanceof HowlingWraiths.SpawnInfo) {
            HowlingWraiths.SpawnInfo info = (HowlingWraiths.SpawnInfo) payload;
            projectiles.add(new HowlingWraiths(info.x, info.y, info.assets));
        }
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST, this);
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_HOWLING_WRAITHS_CAST, this);
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
                    knight.takeDamage(resolvePlatformDeathKnockback());
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxY = platform.y + platform.height;
                knight.onFloorCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
                knight.setSafePosition(knight.getPosition().x, knight.getPosition().y);
            } else if (direction == Direction.DOWN) {
                if(platform.isDeadly) {
                    knight.takeDamage(resolvePlatformDeathKnockback());
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxY = platform.y - knightHitbox.height;
                knight.onCeilingCollision(resolvedHitboxY - Knight.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                if(platform.isDeadly) {
                    knight.takeDamage(resolvePlatformDeathKnockback());
                    knight.goToLastSafePosition();
                    return;
                }
                float resolvedHitboxX = platform.x - knightHitbox.width;
                knight.onWallCollision(resolvedHitboxX - Knight.HITBOX_X_OFFSET);
                knight.setHittingWall(true);
            } else {
                if(platform.isDeadly) {
                    knight.takeDamage(resolvePlatformDeathKnockback());
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
            } else if(direction == Direction.LEFT) {
                if (platform.canPogo) {
                    knight.onDownAttackBounce();
                }
            } else if(direction == Direction.RIGHT) {
                if(platform.canPogo) {
                    knight.onDownAttackBounce();
                }
            }
        }
    }

    /** @return true if a wall is within {@code EPSILON} pixels of the given rectangle */
    private boolean isAdjacentToWall(Rectangle rect) {
        final float EPSILON = 1f;
        for (GridObject platform : gameMap.getColliders()) {
            if (rect.y + rect.height <= platform.y) continue;
            if (rect.y >= platform.y + platform.height) continue;

            float rightGap = Math.abs(platform.x - (rect.x + rect.width));
            if (rightGap >= 0f && rightGap <= EPSILON) return true;

            float leftGap = Math.abs(rect.x - (platform.x + platform.width));
            if (leftGap >= 0f && leftGap <= EPSILON) return true;
        }
        return false;
    }

    /** @return true if a floor is within {@code EPSILON} pixels of the given rectangle */
    private boolean isOnFloor(Rectangle rect) {
        final float EPSILON = 1f;
        for (GridObject platform : gameMap.getColliders()) {
            if (rect.x + rect.width <= platform.x) continue;
            if (rect.x >= platform.x + platform.width) continue;

            float gap = Math.abs(rect.y - (platform.y + platform.height));
            if (gap >= 0f && gap <= EPSILON) return true;
        }
        return false;
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

    /**
     * Whether Knight's knockback direction should be to left
     * or right based on its current position and safe position.
     */
    private Direction resolvePlatformDeathKnockback() {
        if (knight.getPosition().x - knight.getLastSafePosition().x > 0) {
            return Direction.LEFT;
        }
        return Direction.RIGHT;
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
