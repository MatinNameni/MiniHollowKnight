package com.github.matinnameni.minihollowknight.controller.system;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.object.Arena;
import com.github.matinnameni.minihollowknight.model.object.GridObject;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.laser.Laser;
import com.github.matinnameni.minihollowknight.model.asset.EnemiesAssetsManager;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Crawlid;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Crystallized;
import com.github.matinnameni.minihollowknight.model.laser.CrystallizedLaser;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.entity.enemies.FalseKnight;
import com.github.matinnameni.minihollowknight.model.entity.enemies.HuskHornhead;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Mossfly;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Zote;
import com.github.matinnameni.minihollowknight.model.enums.BossType;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages enemy lifecycle: spawning from map spawn points, per-frame physics
 * updates (collision with world geometry), AI triggers, and laser management.
 */
public class EnemySystem {

    private final CollisionSystem collisionSystem;

    // --- Enemies ---
    private final List<Enemy> enemies = new ArrayList<>();
    private boolean enemiesSpawned = false;

    /**
     * Tracks enemies whose death has already been announced via
     * {@link GameEvent#ENEMY_DIED}.
     */
    private final Set<Enemy> announcedDeaths = new HashSet<>();

    // --- False Knight boss reference ---
    private FalseKnight activeFalseKnight;

    // --- Zote NPC reference (only one per map, if any) ---
    private Zote activeZote;

    // --- Lasers ---
    private final List<Laser> lasers = new ArrayList<>();

    // --- Assets ---
    private final EnemiesAssetsManager enemiesAssets;

    public EnemySystem(CollisionSystem collisionSystem, EnemiesAssetsManager enemiesAssets) {
        this.collisionSystem = collisionSystem;
        this.enemiesAssets = enemiesAssets;
    }

    // --- Lifecycle ---

    /** Spawns enemies from the current map's spawn points. Runs once per map load. */
    public void spawnEnemiesIfNeeded() {
        TiledGameMap gameMap = collisionSystem.getGameMap();
        if (enemiesSpawned || gameMap == null) return;

        // crawlid
        for (Vector2 spawnPoint : gameMap.getCrawlidSpawns()) {
            Crawlid crawlid = new Crawlid(spawnPoint.x, spawnPoint.y, enemiesAssets.getCrawlidAssetBundle());
            enemies.add(crawlid);
            EventBus.getInstance().publish(GameEvent.ENEMY_SPAWNED, crawlid);
        }

        // mossfly
        for (Vector2 spawnPoint : gameMap.getMossflySpawns()) {
            Mossfly mossfly = new Mossfly(spawnPoint.x, spawnPoint.y, enemiesAssets.getMossflyAssetBundle());
            enemies.add(mossfly);
            EventBus.getInstance().publish(GameEvent.ENEMY_SPAWNED, mossfly);
        }

        // husk hornhead
        for (Vector2 spawnPoint : gameMap.getHuskHornheadSpawns()) {
            HuskHornhead huskHornhead = new HuskHornhead(spawnPoint.x, spawnPoint.y, enemiesAssets.getHuskHornheadAssetBundle());
            enemies.add(huskHornhead);
            EventBus.getInstance().publish(GameEvent.ENEMY_SPAWNED, huskHornhead);
        }

        // crystallized
        for (Vector2 spawnPoint : gameMap.getCrystallizedSpawns()) {
            Crystallized crystallized = new Crystallized(spawnPoint.x, spawnPoint.y, enemiesAssets.getCrystallizedAssetBundle());
            enemies.add(crystallized);
            EventBus.getInstance().publish(GameEvent.ENEMY_SPAWNED, crystallized);
        }

        // False Knight (boss)
        for (Vector2 spawnPoint : gameMap.getFalseKnightSpawns()) {
            for (Arena arena : gameMap.getArenas()) {
                if (arena.arenaBossType == BossType.FALSE_KNIGHT) {
                    FalseKnight falseKnight = new FalseKnight(spawnPoint.x, spawnPoint.y, enemiesAssets.getFalseKnightAssetBundle());
                    enemies.add(falseKnight);
                    arena.arenaBoss = falseKnight;
                    activeFalseKnight = falseKnight;
                    EventBus.getInstance().publish(GameEvent.ENEMY_SPAWNED, falseKnight);
                }
            }
        }

        // Zote (NPC)
        for (Vector2 spawnPoint : gameMap.getZoteSpawns()) {
            Zote zote = new Zote(spawnPoint.x, spawnPoint.y, enemiesAssets.getZoteAssetBundle());
            enemies.add(zote);
            activeZote = zote;
            EventBus.getInstance().publish(GameEvent.ENEMY_SPAWNED, zote);
        }

        enemiesSpawned = true;
    }

    /** Advances all enemies by one frame. */
    public void updateEnemies(float delta, Knight knight) {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();

            if (enemy.isDead() && !announcedDeaths.contains(enemy)) {
                announcedDeaths.add(enemy);
                EventBus.getInstance().publish(GameEvent.ENEMY_DIED, enemy);
            }

            if (enemy instanceof Crawlid) {
                updateCrawlid(delta, (Crawlid) enemy);
            } else if (enemy instanceof Mossfly) {
                updateMossfly(delta, (Mossfly) enemy, knight);
            } else if (enemy instanceof HuskHornhead) {
                updateHuskHornhead(delta, (HuskHornhead) enemy, knight);
            } else if (enemy instanceof Crystallized) {
                updateCrystallized(delta, (Crystallized) enemy, knight);
            } else if (enemy instanceof FalseKnight) {
                updateFalseKnight(delta, (FalseKnight) enemy);
            } else if (enemy instanceof Zote) {
                updateZote(delta, (Zote) enemy, knight);
            } else {
                enemy.update(delta);
            }
        }
    }

    /** Feeds the False Knight's AI decision system each frame when the fight is active. */
    public void updateFalseKnightAI(float delta, Knight knight) {
        if (activeFalseKnight == null) return;
        if (activeFalseKnight.isDead()) return;

        activeFalseKnight.decideNextAction(knight);
    }

    // --- Lasers ---

    /** Updates all active lasers and removes dead ones. */
    public void updateLasers(float delta) {
        Iterator<Laser> iterator = lasers.iterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.update(delta);
            if (laser.isDead()) {
                iterator.remove();
            }
        }
    }

    /** Creates a new Crystallized laser and adds it to the active list. */
    public void spawnCrystallizedLaser(Crystallized crystallized) {
        boolean facingRight = crystallized.getFacingDirection() == Direction.RIGHT;
        CrystallizedLaser laser = new CrystallizedLaser(
            crystallized.getLaserOriginX(),
            crystallized.getLaserOriginY(),
            facingRight,
            true
        );
        lasers.add(laser);
    }

    // --- Per-enemy updates ---

    private void updateCrawlid(float delta, Crawlid crawlid) {
        crawlid.setGrounded(false);

        Rectangle crawlidHitbox = crawlid.getBounds();
        Map<GridObject, Direction> collisions = collisionSystem.getOverlappingObjects(crawlidHitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (platform.isDeadly && !crawlid.isDead()) {
                crawlid.takeDamage(Crawlid.MAX_HEALTH, direction);
            }

            if (direction == Direction.UP) {
                float resolvedHitboxY = platform.y + platform.height;
                crawlid.onFloorCollision(resolvedHitboxY - Crawlid.HITBOX_Y_OFFSET);
            } else if (direction == Direction.DOWN) {
                float resolvedHitboxY = platform.y - crawlidHitbox.height;
                crawlid.onCeilingCollision(resolvedHitboxY - Crawlid.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                float resolvedHitboxX = platform.x - crawlidHitbox.width;
                crawlid.onWallCollision(resolvedHitboxX - Crawlid.HITBOX_X_OFFSET);
            } else {
                float resolvedHitboxX = platform.x + platform.width;
                crawlid.onWallCollision(resolvedHitboxX - Crawlid.HITBOX_X_OFFSET);
            }
        }

        if (collisionSystem.isOnFloor(crawlid.getBounds())) {
            crawlid.setGrounded(true);
        }

        if (crawlid.isGrounded() && !collisionSystem.hasFloorBelow(crawlid.getCliffProbe())) {
            crawlid.onHitWall();
        }

        crawlid.update(delta);
    }

    private void updateMossfly(float delta, Mossfly mossfly, Knight knight) {
        Rectangle mossflyHitbox = mossfly.getBounds();
        Map<GridObject, Direction> collisions = collisionSystem.getOverlappingObjects(mossflyHitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (platform.isDeadly && !mossfly.isDead()) {
                mossfly.takeDamage(Mossfly.MAX_HEALTH, direction);
            }

            if (direction == Direction.UP) {
                float resolvedHitboxY = platform.y + platform.height;
                mossfly.onFloorCollision(resolvedHitboxY - Mossfly.HITBOX_Y_OFFSET);
            } else if (direction == Direction.DOWN) {
                float resolvedHitboxY = platform.y - mossflyHitbox.height;
                mossfly.onCeilingCollision(resolvedHitboxY - Mossfly.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                float resolvedHitboxX = platform.x - mossflyHitbox.width;
                mossfly.onWallCollision(resolvedHitboxX - Mossfly.HITBOX_X_OFFSET);
            } else {
                float resolvedHitboxX = platform.x + platform.width;
                mossfly.onWallCollision(resolvedHitboxX - Mossfly.HITBOX_X_OFFSET);
            }
        }

        if (mossfly.isKnightDetected(knight) || mossfly.hasDetectedKnightAlready()) {
            mossfly.chaseKnight(knight);
        }

        mossfly.update(delta);
    }

    private void updateHuskHornhead(float delta, HuskHornhead huskHornhead, Knight knight) {
        huskHornhead.setGrounded(false);

        Rectangle hitbox = huskHornhead.getBounds();
        Map<GridObject, Direction> collisions = collisionSystem.getOverlappingObjects(hitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (platform.isDeadly && !huskHornhead.isDead()) {
                huskHornhead.takeDamage(HuskHornhead.MAX_HEALTH, direction);
            }

            if (direction == Direction.UP) {
                float resolvedHitboxY = platform.y + platform.height;
                huskHornhead.onFloorCollision(resolvedHitboxY - HuskHornhead.HITBOX_Y_OFFSET);
            } else if (direction == Direction.DOWN) {
                float resolvedHitboxY = platform.y - hitbox.height;
                huskHornhead.onCeilingCollision(resolvedHitboxY - HuskHornhead.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                float resolvedHitboxX = platform.x - hitbox.width;
                huskHornhead.onWallCollision(resolvedHitboxX - HuskHornhead.HITBOX_X_OFFSET);
            } else {
                float resolvedHitboxX = platform.x + platform.width;
                huskHornhead.onWallCollision(resolvedHitboxX - HuskHornhead.HITBOX_X_OFFSET);
            }
        }

        if (collisionSystem.isOnFloor(huskHornhead.getBounds())) {
            huskHornhead.setGrounded(true);
        }

        if (huskHornhead.isGrounded() && !collisionSystem.hasFloorBelow(huskHornhead.getCliffProbe())) {
            huskHornhead.onObstacleReached();
        }

        if (!huskHornhead.isCharging() && huskHornhead.isKnightVisible(knight)) {
            huskHornhead.startCharge();
        }

        huskHornhead.update(delta);
    }

    private void updateCrystallized(float delta, Crystallized crystallized, Knight knight) {
        crystallized.setGrounded(false);

        Rectangle hitbox = crystallized.getBounds();
        Map<GridObject, Direction> collisions = collisionSystem.getOverlappingObjects(hitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (platform.isDeadly && !crystallized.isDead()) {
                crystallized.takeDamage(Crystallized.MAX_HEALTH, direction);
            }

            if (direction == Direction.UP) {
                float resolvedHitboxY = platform.y + platform.height;
                crystallized.onFloorCollision(resolvedHitboxY - Crystallized.HITBOX_Y_OFFSET);
            } else if (direction == Direction.DOWN) {
                float resolvedHitboxY = platform.y - hitbox.height;
                crystallized.onCeilingCollision(resolvedHitboxY - Crystallized.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                float resolvedHitboxX = platform.x - hitbox.width;
                crystallized.onWallCollision(resolvedHitboxX - Crystallized.HITBOX_X_OFFSET);
            } else {
                float resolvedHitboxX = platform.x + platform.width;
                crystallized.onWallCollision(resolvedHitboxX - Crystallized.HITBOX_X_OFFSET);
            }
        }

        if (collisionSystem.isOnFloor(crystallized.getBounds())) {
            crystallized.setGrounded(true);
        }

        if (crystallized.isGrounded() && !collisionSystem.hasFloorBelow(crystallized.getCliffProbe())) {
            crystallized.onHitWall();
        }

        if (crystallized.isKnightVisible(knight)) {
            crystallized.startShooting();
        }

        if (crystallized.wantsToFireLaser()) {
            crystallized.consumeLaserFire();
            spawnCrystallizedLaser(crystallized);
        }

        crystallized.update(delta);
    }

    private void updateFalseKnight(float delta, FalseKnight falseKnight) {
        if (falseKnight.getState() == FalseKnight.State.STUNNED || falseKnight.getState() == FalseKnight.State.DEAD) {
            falseKnight.update(delta);
            return;
        }

        if (falseKnight.isNoclipEnabled()) {
            falseKnight.update(delta);
            return;
        }

        falseKnight.setGrounded(false);

        Rectangle hitbox = falseKnight.getBounds();
        Map<GridObject, Direction> collisions = collisionSystem.getOverlappingObjects(hitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (platform.isDeadly && !falseKnight.isDead()) {
                falseKnight.takeDamage(FalseKnight.MAX_HEALTH, direction);
            }

            if (direction == Direction.UP) {
                float resolvedHitboxY = platform.y + platform.height;
                falseKnight.onFloorCollision(resolvedHitboxY - FalseKnight.HITBOX_Y_OFFSET);
            } else if (direction == Direction.DOWN) {
                float resolvedHitboxY = platform.y - hitbox.height;
                falseKnight.onCeilingCollision(resolvedHitboxY - FalseKnight.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                float resolvedHitboxX = platform.x - hitbox.width;
                falseKnight.onWallCollision(resolvedHitboxX - FalseKnight.HITBOX_X_OFFSET);
            } else {
                float resolvedHitboxX = platform.x + platform.width;
                falseKnight.onWallCollision(resolvedHitboxX - FalseKnight.HITBOX_X_OFFSET);
            }
        }

        if (collisionSystem.isOnFloor(falseKnight.getBounds())) {
            falseKnight.setGrounded(true);
        }

        falseKnight.update(delta);
    }

    private void updateZote(float delta, Zote zote, Knight knight) {
        if (!zote.isTalking() && !zote.isAngry()) {
            zote.faceTowards(knight.getBounds().x);
        }

        zote.setGrounded(false);

        Rectangle hitbox = zote.getBounds();
        Map<GridObject, Direction> collisions = collisionSystem.getOverlappingObjects(hitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if (direction == Direction.UP) {
                float resolvedHitboxY = platform.y + platform.height;
                zote.onFloorCollision(resolvedHitboxY - Zote.HITBOX_Y_OFFSET);
            } else if (direction == Direction.DOWN) {
                float resolvedHitboxY = platform.y - hitbox.height;
                zote.onCeilingCollision(resolvedHitboxY - Zote.HITBOX_Y_OFFSET);
            } else if (direction == Direction.LEFT) {
                float resolvedHitboxX = platform.x - hitbox.width;
                zote.onWallCollision(resolvedHitboxX - Zote.HITBOX_X_OFFSET);
            } else {
                float resolvedHitboxX = platform.x + platform.width;
                zote.onWallCollision(resolvedHitboxX - Zote.HITBOX_X_OFFSET);
            }
        }

        if (collisionSystem.isOnFloor(zote.getBounds())) {
            zote.setGrounded(true);
        }

        zote.update(delta);
    }

    // --- Reset ---

    /** Clears all enemy and laser state (called on map change). */
    public void reset() {
        enemies.clear();
        announcedDeaths.clear();
        enemiesSpawned = false;
        activeFalseKnight = null;
        activeZote = null;
        for (Laser laser : lasers) {
            if (laser instanceof CrystallizedLaser) {
                ((CrystallizedLaser) laser).dispose();
            }
        }
        lasers.clear();
    }

    // --- Getters ---

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Laser> getLasers() {
        return lasers;
    }

    public FalseKnight getActiveFalseKnight() {
        return activeFalseKnight;
    }

    public Zote getActiveZote() {
        return activeZote;
    }
}
