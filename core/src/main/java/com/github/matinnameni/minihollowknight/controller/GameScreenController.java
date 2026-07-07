package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.controller.system.*;
import com.github.matinnameni.minihollowknight.controller.system.CameraSystem;
import com.github.matinnameni.minihollowknight.controller.system.CollisionSystem;
import com.github.matinnameni.minihollowknight.controller.system.CombatSystem;
import com.github.matinnameni.minihollowknight.controller.system.EnemySystem;
import com.github.matinnameni.minihollowknight.controller.system.KnightPhysicsSystem;
import com.github.matinnameni.minihollowknight.controller.system.ProjectileSystem;
import com.github.matinnameni.minihollowknight.controller.system.WorldSystem;
import com.github.matinnameni.minihollowknight.event.EventBus;
import com.github.matinnameni.minihollowknight.event.GameEvent;
import com.github.matinnameni.minihollowknight.event.EventListener;
import com.github.matinnameni.minihollowknight.model.*;
import com.github.matinnameni.minihollowknight.model.asset.EnemiesAssetsManager;
import com.github.matinnameni.minihollowknight.model.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.enemies.FalseKnight;
import com.github.matinnameni.minihollowknight.model.enemies.Shockwave;
import com.github.matinnameni.minihollowknight.model.enums.BossType;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

import java.util.List;

/**
 * Thin coordinator that delegates to specialized subsystems.
 */
public class GameScreenController implements EventListener {

    // --- Dependencies ---
    private final Knight knight;
    private final EnemiesAssetsManager enemiesAssets;

    // --- Subsystems ---
    private final CollisionSystem collisionSystem;
    private final KnightPhysicsSystem knightPhysicsSystem;
    private final EnemySystem enemySystem;
    private final ProjectileSystem projectileSystem;
    private final CombatSystem combatSystem;
    private final CameraSystem cameraSystem;
    private final WorldSystem worldSystem;

    public GameScreenController(Knight knight, EnemiesAssetsManager enemiesAssets) {
        this.knight = knight;
        this.enemiesAssets = enemiesAssets;

        // Build subsystems
        this.collisionSystem = new CollisionSystem();
        this.knightPhysicsSystem = new KnightPhysicsSystem(collisionSystem);
        this.enemySystem = new EnemySystem(collisionSystem, enemiesAssets);
        this.projectileSystem = new ProjectileSystem(collisionSystem);
        this.combatSystem = new CombatSystem(collisionSystem);
        this.cameraSystem = new CameraSystem();
        this.worldSystem = new WorldSystem();

        // Subscribe to events
        EventBus.getInstance().subscribe(GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST, this);
        EventBus.getInstance().subscribe(GameEvent.PLAYER_HOWLING_WRAITHS_CAST, this);
        EventBus.getInstance().subscribe(GameEvent.PLAYER_NAIL_HIT, this);
        EventBus.getInstance().subscribe(GameEvent.FALSE_KNIGHT_FIGHT_STARTED, this);
        EventBus.getInstance().subscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
    }

    // --- Initialization ---

    public void initializeCameraTarget() {
        cameraSystem.initializeCameraTarget(knight);
    }

    // --- Main loop ---

    public void update(float delta, OrthographicCamera camera) {
        delta = Math.min(delta, 0.05f);

        TiledGameMap gameMap = collisionSystem.getGameMap();

        // 1. Spawn enemies
        enemySystem.spawnEnemiesIfNeeded();

        // 2. Resolve nail attack
        combatSystem.resolveNailAttack(knight);
        combatSystem.resolveEnemyAttack(knight, enemySystem.getEnemies());

        // 3. Reset knight grounded / wall state before collision pass
        knight.setGrounded(false);
        knight.setHittingWall(false);

        // 4. Resolve knight collisions with world geometry
        knightPhysicsSystem.resolveCollisions(knight);

        // 5. Wall / floor proximity
        if (collisionSystem.isAdjacentToWall(knight.getBounds())) {
            knight.setHittingWall(true);
        }
        if (collisionSystem.isOnFloor(knight.getBounds())) {
            knight.setGrounded(true);
        }

        // 6. Knight logic update
        knight.update(delta);

        // 7. Projectiles
        projectileSystem.updateProjectiles(delta);
        projectileSystem.resolveProjectilesHit(enemySystem.getEnemies(), knight);

        // 8. Enemies
        enemySystem.updateEnemies(delta, knight);

        // 9. Knight and enemy contact damage
        combatSystem.resolveKnightEnemyContact(knight, enemySystem.getEnemies());

        // 10. Lasers
        enemySystem.updateLasers(delta);
        combatSystem.resolveLaserKnightContact(knight, enemySystem.getLasers());

        // 11. False Knight AI + attack hitboxes
        FalseKnight falseKnight = enemySystem.getActiveFalseKnight();
        enemySystem.updateFalseKnightAI(delta, knight);

        // Collect camera shake from boss
        if (falseKnight != null) {
            float shake = falseKnight.getCameraShakeIntensity();
            cameraSystem.addShake(shake);
        }

        // Spawn shockwaves if False Knight wants them
        if (falseKnight != null && falseKnight.wantsShockwave()) {
            falseKnight.consumeShockwave();
            spawnShockwaves(falseKnight);
        }

        combatSystem.resolveFalseKnightAttackHitboxes(knight, falseKnight);

        // 12. Doors
        if (gameMap != null) {
            worldSystem.updateDoors(delta, knight, gameMap, knightPhysicsSystem);
        }

        // 13. Camera
        if (gameMap != null) {
            cameraSystem.updateCamera(delta, camera, knight, gameMap);

            // Arena boss-fight start check
            for (Arena arena : gameMap.getArenas()) {
                if (knight.getBounds().overlaps(arena) && arena.contains(knight.getBounds())) {
                    if (falseKnight != null && !falseKnight.isFightStarted() &&
                        arena.haveBoss && arena.arenaBossType == BossType.FALSE_KNIGHT) {
                        falseKnight.startFight();
                    }
                }
            }
        }
    }

    // --- Shockwave helper ---

    private void spawnShockwaves(FalseKnight falseKnight) {
        Shockwave wave =
            new Shockwave(
                (falseKnight.getFacingDirection() == Direction.RIGHT) ?
                    falseKnight.getBounds().x + FalseKnight.HITBOX_WIDTH :
                    falseKnight.getBounds().x,
                falseKnight.getBounds().y,
                falseKnight.getFacingDirection(),
                enemiesAssets.getFalseKnightAssetBundle()
            );
        projectileSystem.addProjectile(wave);
    }

    // --- EventListener ---

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event == GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST && payload instanceof VengefulSpirit.SpawnInfo) {
            VengefulSpirit.SpawnInfo info = (VengefulSpirit.SpawnInfo) payload;
            projectileSystem.addProjectile(new VengefulSpirit(info.x, info.y, info.direction, info.assets));
        } else if (event == GameEvent.PLAYER_HOWLING_WRAITHS_CAST && payload instanceof HowlingWraiths.SpawnInfo) {
            HowlingWraiths.SpawnInfo info = (HowlingWraiths.SpawnInfo) payload;
            projectileSystem.addProjectile(new HowlingWraiths(info.x, info.y, info.assets));
        } else if (event == GameEvent.PLAYER_NAIL_HIT && payload instanceof Enemy) {
            combatSystem.resolveNailHitOnEnemy(knight, (Enemy) payload);
        } else if (event == GameEvent.FALSE_KNIGHT_FIGHT_STARTED) {
            worldSystem.setDisplayText(com.github.matinnameni.minihollowknight.model.Lang.get("boss.falseKnight"));
            TiledGameMap gameMap = collisionSystem.getGameMap();
            if (gameMap != null) {
                worldSystem.closeAllDoors(gameMap);
            }
        } else if (event == GameEvent.FALSE_KNIGHT_DEFEATED) {
            TiledGameMap gameMap = collisionSystem.getGameMap();
            if (gameMap != null) {
                worldSystem.openAllDoors(gameMap);
            }
        }
    }

    // --- Getters ---

    public List<Projectile> getProjectiles() {
        return projectileSystem.getProjectiles();
    }

    public List<Enemy> getEnemies() {
        return enemySystem.getEnemies();
    }

    public List<Laser> getLasers() {
        return enemySystem.getLasers();
    }

    public Vector2 getCameraTarget() {
        return cameraSystem.getCameraTarget();
    }

    public String getDisplayText() {
        return worldSystem.getDisplayText();
    }

    public void resetDisplayText() {
        worldSystem.resetDisplayText();
    }

    // --- Map change ---

    /** Should be called after each time a new map was loaded in GameScreen. */
    public void setGameMap(TiledGameMap gameMap) {
        collisionSystem.setGameMap(gameMap);
        enemySystem.reset();
        projectileSystem.reset();
        combatSystem.reset();
        cameraSystem.reset();
        worldSystem.reset();
    }

    // --- Lifecycle ---

    public void dispose() {
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST, this);
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_HOWLING_WRAITHS_CAST, this);
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_NAIL_HIT, this);
        EventBus.getInstance().unsubscribe(GameEvent.FALSE_KNIGHT_FIGHT_STARTED, this);
        EventBus.getInstance().unsubscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
    }
}
