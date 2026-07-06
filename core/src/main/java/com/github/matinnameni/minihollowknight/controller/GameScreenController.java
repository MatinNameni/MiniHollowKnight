package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.event.EventBus;
import com.github.matinnameni.minihollowknight.event.GameEvent;
import com.github.matinnameni.minihollowknight.event.EventListener;
import com.github.matinnameni.minihollowknight.model.*;
import com.github.matinnameni.minihollowknight.model.asset.EnemiesAssetsManager;
import com.github.matinnameni.minihollowknight.model.enemies.Crawlid;
import com.github.matinnameni.minihollowknight.model.enemies.Crystallized;
import com.github.matinnameni.minihollowknight.model.enemies.CrystallizedLaser;
import com.github.matinnameni.minihollowknight.model.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.enemies.FalseKnight;
import com.github.matinnameni.minihollowknight.model.enemies.HuskHornhead;
import com.github.matinnameni.minihollowknight.model.enemies.Mossfly;
import com.github.matinnameni.minihollowknight.model.enemies.Shockwave;
import com.github.matinnameni.minihollowknight.model.enums.BossType;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;
import com.github.matinnameni.minihollowknight.view.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.screens.GameScreen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameScreenController implements EventListener {
    private ScreenNavigator navigator;
    private Settings settings;
    private GameData gameData;
    private Knight knight;
    private TiledGameMap gameMap;
    private final EnemiesAssetsManager enemiesAssets;

    // --- Projectiles ---
    private final List<Projectile> projectiles = new ArrayList<>();

    // --- Enemies ---
    private final List<Enemy> enemies = new ArrayList<>();
    private boolean enemiesSpawned = false;
    private final Set<Enemy> attackedEnemiesThisSwing = new HashSet<>();
    private final Set<BreakableWall> attackedWallsThisSwing = new HashSet<>();

    // --- Lasers ---
    private final List<Laser> lasers = new ArrayList<>();

    // --- False Knight boss reference ---
    private FalseKnight activeFalseKnight;

    // --- Camera control ---
    private static final float CAMERA_LERP = 4f;
    private final Vector2 cameraTarget = new Vector2();

    // --- Camera shake state ---
    private float cameraShakeIntensity = 0f;

    // --- Display text ---
    private String displayText;

    public GameScreenController(ScreenNavigator navigator, Settings settings,
                                GameData gameData, Knight knight, EnemiesAssetsManager enemiesAssets) {
        this.navigator = navigator;
        this.settings = settings;
        this.gameData = gameData;
        this.knight = knight;
        this.enemiesAssets = enemiesAssets;

        EventBus.getInstance().subscribe(GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST, this);
        EventBus.getInstance().subscribe(GameEvent.PLAYER_HOWLING_WRAITHS_CAST, this);
        EventBus.getInstance().subscribe(GameEvent.PLAYER_NAIL_HIT, this);

        // False Knight events
        EventBus.getInstance().subscribe(GameEvent.FALSE_KNIGHT_FIGHT_STARTED, this);
        EventBus.getInstance().subscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
    }

    public void initializeCameraTarget() {
        cameraTarget.set(knight.getPosition().x + Knight.WIDTH / 2f,
            knight.getPosition().y + Knight.HEIGHT / 2f);
    }

    public void update(float delta, OrthographicCamera camera) {
        delta = Math.min(delta, 0.05f);

        spawnEnemiesIfNeeded();

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

        resolveProjectilesHit();

        updateEnemies(delta);

        resolveKnightEnemyContact();

        updateLasers(delta);

        resolveLaserKnightContact(delta);

        // False Knight
        updateFalseKnightAI(delta);
        resolveFalseKnightAttackHitboxes();

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
            } else if(projectile instanceof Shockwave) {
                updateShockwave(delta, (Shockwave) projectile);
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

    private void updateShockwave(float delta, Shockwave shockwave) {
        Rectangle shockwaveBounds = shockwave.getBounds();
        for (GridObject platform : gameMap.getColliders()) {
            if (shockwaveBounds.y >= platform.y &&
                shockwaveBounds.y + shockwaveBounds.height <= platform.y + platform.height &&
                shockwaveBounds.overlaps(platform)) {
                shockwave.onHitWall();
                break;
            }
        }

        shockwave.update(delta);
    }

    // --- Enemies ---

    /** Returns the list of active enemies. */
    public List<Enemy> getEnemies() {
        return enemies;
    }

    /** Spawns enemies per spawn points in the current map. */
    private void spawnEnemiesIfNeeded() {
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
            for(Arena arena : gameMap.getArenas()) {
                if (arena.arenaBossType == BossType.FALSE_KNIGHT) {
                    FalseKnight falseKnight = new FalseKnight(spawnPoint.x, spawnPoint.y, enemiesAssets.getFalseKnightAssetBundle());
                    enemies.add(falseKnight);
                    arena.arenaBoss = falseKnight;
                    activeFalseKnight = falseKnight;
                    EventBus.getInstance().publish(GameEvent.ENEMY_SPAWNED, falseKnight);
                }
            }
        }

        enemiesSpawned = true;
    }

    /** Updates all active enemies. */
    private void updateEnemies(float delta) {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();

            if (enemy instanceof Crawlid) {
                updateCrawlid(delta, (Crawlid) enemy);
            } else if (enemy instanceof Mossfly) {
                updateMossfly(delta, (Mossfly) enemy);
            } else if (enemy instanceof HuskHornhead) {
                updateHuskHornhead(delta, (HuskHornhead) enemy);
            } else if (enemy instanceof Crystallized) {
                updateCrystallized(delta, (Crystallized) enemy);
            } else if (enemy instanceof FalseKnight) {
                updateFalseKnight(delta, (FalseKnight) enemy);
            } else {
                enemy.update(delta);
            }
        }
    }

    private void updateCrawlid(float delta, Crawlid crawlid) {
        crawlid.setGrounded(false);

        Rectangle crawlidHitbox = crawlid.getBounds();
        Map<GridObject, Direction> collisions = getOverlappingObjects(crawlidHitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if(platform.isDeadly && !crawlid.isDead()) {
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

        // Floor proximity check
        if (isOnFloor(crawlid.getBounds())) {
            crawlid.setGrounded(true);
        }

        // Cliff edge detection
        if (crawlid.isGrounded() && !hasFloorBelow(crawlid.getCliffProbe())) {
            crawlid.onHitWall();
        }

        crawlid.update(delta);
    }

    private void updateMossfly(float delta, Mossfly mossfly) {
        Rectangle mossflyHitbox = mossfly.getBounds();
        Map<GridObject, Direction> collisions = getOverlappingObjects(mossflyHitbox);

        for (Map.Entry<GridObject, Direction> entry : collisions.entrySet()) {
            GridObject platform = entry.getKey();
            Direction direction = entry.getValue();

            if(platform.isDeadly && !mossfly.isDead()) {
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

    private void updateHuskHornhead(float delta, HuskHornhead huskHornhead) {
        huskHornhead.setGrounded(false);

        Rectangle hitbox = huskHornhead.getBounds();
        Map<GridObject, Direction> collisions = getOverlappingObjects(hitbox);

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

        // Floor proximity check
        if (isOnFloor(huskHornhead.getBounds())) {
            huskHornhead.setGrounded(true);
        }

        // Cliff edge detection
        if (huskHornhead.isGrounded() && !hasFloorBelow(huskHornhead.getCliffProbe())) {
            huskHornhead.onObstacleReached();
        }

        // Vision
        if (!huskHornhead.isCharging() && huskHornhead.isKnightVisible(knight)) {
            huskHornhead.startCharge();
        }

        huskHornhead.update(delta);
    }

    private void updateCrystallized(float delta, Crystallized crystallized) {
        crystallized.setGrounded(false);

        Rectangle hitbox = crystallized.getBounds();
        Map<GridObject, Direction> collisions = getOverlappingObjects(hitbox);

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

        // Floor proximity check
        if (isOnFloor(crystallized.getBounds())) {
            crystallized.setGrounded(true);
        }

        // Cliff edge detection
        if (crystallized.isGrounded() && !hasFloorBelow(crystallized.getCliffProbe())) {
            crystallized.onHitWall();
        }

        // Vision
        if (crystallized.isKnightVisible(knight)) {
            crystallized.startShooting();
        }

        // Check if the crystallized wants to fire a laser
        if (crystallized.wantsToFireLaser()) {
            crystallized.consumeLaserFire();
            boolean facingRight = crystallized.getFacingDirection() == Direction.RIGHT;
            CrystallizedLaser laser = new CrystallizedLaser(
                crystallized.getLaserOriginX(),
                crystallized.getLaserOriginY(),
                facingRight,
                true
            );
            lasers.add(laser);
        }

        crystallized.update(delta);
    }

    // --- False Knight ---

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
        Map<GridObject, Direction> collisions = getOverlappingObjects(hitbox);

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

        // Floor proximity check
        if (isOnFloor(falseKnight.getBounds())) {
            falseKnight.setGrounded(true);
        }

        falseKnight.update(delta);
    }

    /**
     * Feeds the False Knight's AI decision system each frame when the fight
     * is active and the boss is idle.
     */
    private void updateFalseKnightAI(float delta) {
        if (activeFalseKnight == null) return;
        if (activeFalseKnight.isDead()) return;

        // Feed AI
        activeFalseKnight.decideNextAction(knight);

        // Collect camera shake from boss
        float shake = activeFalseKnight.getCameraShakeIntensity();
        if (shake > cameraShakeIntensity) {
            cameraShakeIntensity = shake;
        }

        // Spawn shockwaves
        if (activeFalseKnight.wantsShockwave()) {
            activeFalseKnight.consumeShockwave();
            spawnShockwaves(activeFalseKnight);
        }
    }

    /** Resolves damage from the False Knight's special attack hitboxes against the Knight. */
    private void resolveFalseKnightAttackHitboxes() {
        if (activeFalseKnight == null) return;
        if (knight.isInvincible() || knight.isDead()) return;

        Rectangle knightHitbox = knight.getBounds();

        // Mace slam hitbox
        Rectangle slamHitbox = activeFalseKnight.getSlamHitbox();
        if (slamHitbox != null && knightHitbox.overlaps(slamHitbox)) {
            Direction knockback = (knightHitbox.x < slamHitbox.x) ? Direction.LEFT : Direction.RIGHT;
            knight.takeDamage(knockback);
            return;
        }

        // Jump attack hitbox
        Rectangle jumpHitbox = activeFalseKnight.getJumpAttackHitbox();
        if (jumpHitbox != null && knightHitbox.overlaps(jumpHitbox)) {
            Direction knockback = (knightHitbox.x < jumpHitbox.x) ? Direction.LEFT : Direction.RIGHT;
            knight.takeDamage(knockback);
        }
    }

    // --- Shockwaves ---

    /** Spawns two shockwaves from the boss's position. */
    private void spawnShockwaves(FalseKnight falseKnight) {
        float originX = (falseKnight.getFacingDirection() == Direction.RIGHT) ?
            falseKnight.getBounds().x + FalseKnight.HITBOX_WIDTH :
            falseKnight.getBounds().x;
        float originY = falseKnight.getBounds().y;

        Shockwave wave = new Shockwave(originX, originY, falseKnight.getFacingDirection(),
            enemiesAssets.getFalseKnightAssetBundle());

        projectiles.add(wave);
    }

    // --- Lasers ---

    /** Returns the list of active Crystallized lasers for rendering. */
    public List<Laser> getLasers() {
        return lasers;
    }

    /** Updates all active lasers and removes dead ones. */
    private void updateLasers(float delta) {
        Iterator<Laser> iterator = lasers.iterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.update(delta);
            if (laser.isDead()) {
                iterator.remove();
            }
        }
    }

    /** Resolves contact damage between the Knight and all active lasers. */
    private void resolveLaserKnightContact(float delta) {
        if (knight.isInvincible() || knight.isDead()) return;

        Rectangle knightHitbox = knight.getBounds();

        for (Laser laser : lasers) {
            if (!laser.isActive()) continue;
            if (!knightHitbox.overlaps(laser.getBounds())) continue;

            Direction knockback = (knight.getBounds().x < laser.getBounds().x) ? Direction.LEFT : Direction.RIGHT;
            knight.takeDamage(knockback);
            break; // only take damage from one laser per frame
        }
    }

    /** Resolves contact damage between the Knight and any living enemy. */
    private void resolveKnightEnemyContact() {
        if (knight.isInvincible() || knight.isDead()) return;

        Rectangle knightHitbox = knight.getBounds();

        for (Enemy enemy : enemies) {
            if (!enemy.canDamagePlayer()) continue;
            if (!knightHitbox.overlaps(enemy.getBounds())) continue;

            Direction knockback = (knight.getBounds().x < enemy.getBounds().x) ? Direction.LEFT : Direction.RIGHT;
            knight.takeDamage(knockback);
            break;
        }
    }

    /** @return true if there is a floor collider directly beneath {@code probe}. */
    private boolean hasFloorBelow(Rectangle probe) {
        for (GridObject platform : gameMap.getColliders()) {
            if (platform.isDeadly) continue;
            if (probe.overlaps(platform)) return true;
        }
        return false;
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
        } else if (event == GameEvent.PLAYER_NAIL_HIT && payload instanceof Enemy) {
            resolveNailHitOnEnemy((Enemy) payload);
        }  else if (event == GameEvent.FALSE_KNIGHT_FIGHT_STARTED) {
            displayText = "False Knight";
        } else if (event == GameEvent.FALSE_KNIGHT_DEFEATED) {
            activeFalseKnight = null;
        }
    }

    /** Damages {@code enemy} when the Knight's nail hits it. */
    private void resolveNailHitOnEnemy(Enemy enemy) {
        enemy.takeDamage(Knight.SLASH_DAMAGE, knight.getAttackDirection());
        knight.gainSoul(Knight.SLASH_SOUL_GAIN);

        // pogo
        if(knight.getAttackDirection() == Direction.DOWN) {
            knight.onDownAttackBounce();
        }
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST, this);
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_HOWLING_WRAITHS_CAST, this);
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_NAIL_HIT, this);
        EventBus.getInstance().unsubscribe(GameEvent.FALSE_KNIGHT_FIGHT_STARTED, this);
        EventBus.getInstance().unsubscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
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

        boolean lockedOnArea = false;

        for (Arena arena : gameMap.getArenas()) {
            if (knight.getBounds().overlaps(arena)) {
                float arenaHalfWidth = arena.width / 2f;
                float arenaHalfHeight = arena.height / 2f;
                float arenaCenterX = arena.x + arenaHalfWidth;
                float arenaCenterY = arena.y + arenaHalfHeight;

                cameraTarget.x = arenaCenterX;
                cameraTarget.y = arenaCenterY;

                // lock the camera on center of the arena
                camera.position.x += (cameraTarget.x - camera.position.x) * CAMERA_LERP * delta;
                camera.position.y += (cameraTarget.y - camera.position.y) * CAMERA_LERP * delta;

                lockedOnArea = true;

                // start the boss fight if it's a boss arena
                if (activeFalseKnight != null && !activeFalseKnight.isFightStarted()) {
                    activeFalseKnight.startFight();
                }

                break;
            }
        }

         if (!lockedOnArea) {
             cameraTarget.x = knightPos.x + Knight.WIDTH / 2f;
             cameraTarget.y = knightPos.y + Knight.HEIGHT / 2f;

             // for smoother display, the camera stays a little behind the knight
             camera.position.x += (cameraTarget.x - camera.position.x) * CAMERA_LERP * delta;
             camera.position.y += (cameraTarget.y - camera.position.y) * CAMERA_LERP * delta;

             // Clamp camera
             float halfWidth = camera.viewportWidth / 2f;
             float halfHeight = camera.viewportHeight / 2f;

            // Normal map clamping
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

    /** Resolves the Knight's nail attack if he's in attacking state. */
    public void resolveNailAttack() {
        if(knight.getState() != KnightState.ATTACKING) {
            attackedEnemiesThisSwing.clear();
            attackedWallsThisSwing.clear();
            return;
        }

        resolvePlatformAttack();
        resolveBreakableWallAttack();
        resolveEnemyAttack();
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

    /** Resolves collisions between the Knight's attack hitbox and all breakable walls. */
    private void resolveBreakableWallAttack() {
        if (gameMap == null) return;

        Rectangle attackHitbox = knight.getAttackHitbox();
        if (attackHitbox == null) return;

        for (BreakableWall wall : gameMap.getBreakableWalls()) {
            if (wall.isPassable()) continue;
            if (attackedWallsThisSwing.contains(wall)) continue;
            if (!attackHitbox.overlaps(wall.getBounds())) continue;

            attackedWallsThisSwing.add(wall);
            boolean stateChanged = wall.hit();

            if (stateChanged && wall.isPassable()) {
                gameMap.removeBreakableWallCollider(wall);
                gameMap.removeBlackMask();
            }
        }
    }

    /** Resolves collision between projectiles and game entities. */
    public void resolveProjectilesHit() {
        for (Projectile projectile : projectiles) {
            if (projectile.hasEffectOnEnemies()) {
                for (Enemy enemy : enemies) {
                    if (enemy.isDead()) continue;
                    if (!enemy.getBounds().overlaps(projectile.getBounds())) continue;
                    projectile.onHitEnemy(enemy);
                }
            } else if (projectile.hasEffectOnKnight()) {
                if (!projectile.getBounds().overlaps(knight.getBounds())) continue;
                projectile.onHitKnight(knight);
            }
        }
    }

    /** Resolves collisions between the Knight's attack hitbox and enemies. */
    private void resolveEnemyAttack() {
        Rectangle attackHitbox = knight.getAttackHitbox();
        if (attackHitbox == null) return;

        for (Enemy enemy : enemies) {
            if (enemy.isDead() || attackedEnemiesThisSwing.contains(enemy)) continue;
            if (!attackHitbox.overlaps(enemy.getBounds())) continue;

            attackedEnemiesThisSwing.add(enemy);
            knight.onNailHit(enemy);
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

    /** @return the active False Knight boss, or null. */
    public FalseKnight getActiveFalseKnight() {
        return activeFalseKnight;
    }

    public String getDisplayText() {
        return displayText;
    }

    // --- Setters ---

    /** Should be called after each time a new map was loaded in {@link GameScreen}. */
    public void setGameMap(TiledGameMap gameMap) {
        this.gameMap = gameMap;
        this.enemies.clear();
        this.enemiesSpawned = false;
        this.attackedEnemiesThisSwing.clear();
        this.activeFalseKnight = null;
        this.cameraShakeIntensity = 0f;
        for (Laser laser : lasers) {
            if (laser instanceof CrystallizedLaser) {
                ((CrystallizedLaser) laser).dispose();
            }
        }
        this.lasers.clear();
    }

    public void resetDisplayText() {
        displayText = null;
    }
}
