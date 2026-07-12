package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.controller.input.KnightInputProcessor;
import com.github.matinnameni.minihollowknight.controller.system.CameraSystem;
import com.github.matinnameni.minihollowknight.controller.system.CombatSystem;
import com.github.matinnameni.minihollowknight.controller.system.CollisionSystem;
import com.github.matinnameni.minihollowknight.controller.system.EnemySystem;
import com.github.matinnameni.minihollowknight.controller.system.KnightPhysicsSystem;
import com.github.matinnameni.minihollowknight.controller.system.ProjectileSystem;
import com.github.matinnameni.minihollowknight.controller.system.WorldSystem;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.event.EventListener;
import com.github.matinnameni.minihollowknight.model.asset.EnemiesAssetsManager;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.entity.enemies.FalseKnight;
import com.github.matinnameni.minihollowknight.model.laser.Laser;
import com.github.matinnameni.minihollowknight.model.localization.Lang;
import com.github.matinnameni.minihollowknight.model.projectile.Shockwave;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.enums.BossType;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;
import com.github.matinnameni.minihollowknight.model.object.GridObject;
import com.github.matinnameni.minihollowknight.model.projectile.HowlingWraiths;
import com.github.matinnameni.minihollowknight.model.projectile.Projectile;
import com.github.matinnameni.minihollowknight.model.projectile.VengefulSpirit;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin coordinator that delegates to specialized subsystems.
 */
public class GameScreenController implements EventListener {

    // --- Dependencies ---
    private final Knight knight;
    private final EnemiesAssetsManager enemiesAssets;

    // --- Subsystems ---
    private final KnightInputProcessor knightInputProcessor;
    private final CollisionSystem collisionSystem;
    private final KnightPhysicsSystem knightPhysicsSystem;
    private final EnemySystem enemySystem;
    private final ProjectileSystem projectileSystem;
    private final CombatSystem combatSystem;
    private final CameraSystem cameraSystem;
    private final WorldSystem worldSystem;

    // --- Respawn callback ---
    private Runnable onPlayerDied;

    private GameEnvironment pendingTransfer = null;

    public GameScreenController(Knight knight, EnemiesAssetsManager enemiesAssets) {
        this.knight = knight;
        this.enemiesAssets = enemiesAssets;

        // Build subsystems
        this.knightInputProcessor = new KnightInputProcessor(knight, knight.getSettings());
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
        EventBus.getInstance().subscribe(GameEvent.PLAYER_DIED, this);
        EventBus.getInstance().subscribe(GameEvent.CAMERA_SHAKE, this);
    }

    // --- Respawn callback ---

    /**
     * Sets a callback that is invoked when the player dies (masks reach zero).
     * The GameScreen uses this to start the death/respawn sequence.
     */
    public void setOnPlayerDied(Runnable callback) {
        this.onPlayerDied = callback;
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

        // 6. Process player input
        knightInputProcessor.processInput(delta);

        // 7. Knight logic update (timers, state machine, physics)
        knight.update(delta);

        // 8. Projectiles
        projectileSystem.updateProjectiles(delta);
        projectileSystem.resolveProjectilesHit(enemySystem.getEnemies(), knight);

        // 9. Enemies
        enemySystem.updateEnemies(delta, knight);

        // 10. Knight and enemy contact damage
        combatSystem.resolveKnightEnemyContact(knight, enemySystem.getEnemies());
        combatSystem.resolveSharpShadowDashDamage(knight, enemySystem.getEnemies());

        // 11. Lasers
        enemySystem.updateLasers(delta);
        combatSystem.resolveLaserKnightContact(knight, enemySystem.getLasers());

        // 12. False Knight AI + attack hitboxes
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

        // 13. Doors
        if (gameMap != null) {
            worldSystem.updateDoors(delta, knight, gameMap, knightPhysicsSystem);
        }

        // 14. Camera
        if (gameMap != null) {
            cameraSystem.updateCamera(delta, camera, knight, gameMap);

            // Arena boss-fight start check
            for (var arena : gameMap.getArenas()) {
                if (knight.getBounds().overlaps(arena) && arena.contains(knight.getBounds())) {
                    if (falseKnight != null && !falseKnight.isFightStarted() &&
                        arena.haveBoss && arena.arenaBossType == BossType.FALSE_KNIGHT) {
                        falseKnight.startFight();
                    }
                }
            }

            // 15. Area-transfer check
            if (pendingTransfer == null) {
                Rectangle knightBounds = knight.getBounds();
                for (GridObject collider : gameMap.getColliders()) {
                    if (collider.transferTo == null) continue;
                    if (!knightBounds.overlaps(collider)) continue;
                    GameEnvironment target = resolveTransferTarget(collider.transferTo);
                    if (target != null) {
                        pendingTransfer = target;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Resolves a {@code transferTo} string (from the Tiled map) to a
     * {@link GameEnvironment}. Matches case-insensitively against both the
     * enum name and the environment's display name.
     *
     * @return the matching environment, or {@code null} if no match.
     */
    private static GameEnvironment resolveTransferTarget(String transferTo) {
        if (transferTo == null) return null;
        String normalized = transferTo.trim().toLowerCase();
        for (GameEnvironment env : GameEnvironment.values()) {
            if (env.name().toLowerCase().equals(normalized)) return env;
            if (env.name != null && env.name.toLowerCase().equals(normalized)) return env;
        }
        return null;
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
            projectileSystem.addProjectile(new VengefulSpirit(info.x, info.y, info.direction, info.assets, info.isShadow, info.damageMultiplier));
            EventBus.getInstance().publish(GameEvent.CAMERA_SHAKE);
        } else if (event == GameEvent.PLAYER_HOWLING_WRAITHS_CAST && payload instanceof HowlingWraiths.SpawnInfo) {
            HowlingWraiths.SpawnInfo info = (HowlingWraiths.SpawnInfo) payload;
            projectileSystem.addProjectile(new HowlingWraiths(info.x, info.y, info.assets, info.isShadow, info.damageMultiplier));
            EventBus.getInstance().publish(GameEvent.CAMERA_SHAKE);
        } else if (event == GameEvent.PLAYER_NAIL_HIT && payload instanceof Enemy) {
            combatSystem.resolveNailHitOnEnemy(knight, (Enemy) payload);
        } else if (event == GameEvent.FALSE_KNIGHT_FIGHT_STARTED) {
            worldSystem.setDisplayText(Lang.get("boss.falseKnight"));
            TiledGameMap gameMap = collisionSystem.getGameMap();
            if (gameMap != null) {
                worldSystem.closeAllDoors(gameMap);
            }
        } else if (event == GameEvent.FALSE_KNIGHT_DEFEATED) {
            TiledGameMap gameMap = collisionSystem.getGameMap();
            if (gameMap != null) {
                worldSystem.openAllDoors(gameMap);
            }
            // "Game Completed" achievement
            EventBus.getInstance().publish(GameEvent.GAME_COMPLETED);
        } else if (event == GameEvent.PLAYER_DIED) {
            if (onPlayerDied != null) {
                onPlayerDied.run();
            }
        } else if (event == GameEvent.CAMERA_SHAKE) {
            if (payload instanceof Float) {
                cameraSystem.addShake((Float) payload);
            } else {
                cameraSystem.addShake(CameraSystem.DEFAULT_SHAKE);
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

    public Color getCurrentBackgroundColor(TiledGameMap gameMap) {
        switch (gameMap.getCurrentEnvironment()) {
            case FORGOTTEN_CROSSROADS:
                return new Color(
                    1/255f, 0/255f, 35/255f, 0.8f
                );
            case GREENPATH:
                return new Color(
                    0/255f, 16/255f, 4/255f, 0.8f
                );
            default:
                return Color.BLACK;
        }
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
        // Clear any pending transfer so a stale one from the old map doesn't
        // immediately re-trigger after a map swap.
        pendingTransfer = null;
    }

    /**
     * @return the environment the player should transfer to, or {@code null}
     *         if no transfer is pending.
     */
    public GameEnvironment consumePendingTransfer() {
        GameEnvironment target = pendingTransfer;
        pendingTransfer = null;
        return target;
    }

    public void forceStartBossFight(TiledGameMap gameMap) {
        gameMap.removeBlackMask();
        EventBus.getInstance().publish(GameEvent.FALSE_KNIGHT_FIGHT_STARTED);
    }

    // --- Lifecycle ---

    public void dispose() {
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_VENGEFUL_SPIRIT_CAST, this);
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_HOWLING_WRAITHS_CAST, this);
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_NAIL_HIT, this);
        EventBus.getInstance().unsubscribe(GameEvent.FALSE_KNIGHT_FIGHT_STARTED, this);
        EventBus.getInstance().unsubscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
        EventBus.getInstance().unsubscribe(GameEvent.PLAYER_DIED, this);
        EventBus.getInstance().unsubscribe(GameEvent.CAMERA_SHAKE, this);
    }
}
