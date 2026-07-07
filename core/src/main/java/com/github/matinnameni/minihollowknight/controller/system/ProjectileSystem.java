package com.github.matinnameni.minihollowknight.controller.system;

import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.HowlingWraiths;
import com.github.matinnameni.minihollowknight.model.Knight;
import com.github.matinnameni.minihollowknight.model.Projectile;
import com.github.matinnameni.minihollowknight.model.VengefulSpirit;
import com.github.matinnameni.minihollowknight.model.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.enemies.Shockwave;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the lifecycle and hit-resolution of all projectiles.
 */
public class ProjectileSystem {

    private final CollisionSystem collisionSystem;
    private final List<Projectile> projectiles = new ArrayList<>();

    public ProjectileSystem(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }

    /** Adds a newly spawned projectile to the active list. */
    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    /** Advances all active projectiles by one frame and removes dead ones. */
    public void updateProjectiles(float delta) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();

            if (projectile instanceof VengefulSpirit) {
                updateVengefulSpirit(delta, (VengefulSpirit) projectile);
            } else if (projectile instanceof HowlingWraiths) {
                updateHowlingWraiths(delta, (HowlingWraiths) projectile);
            } else if (projectile instanceof Shockwave) {
                updateShockwave(delta, (Shockwave) projectile);
            }

            if (projectile.isDead()) {
                iterator.remove();
            }
        }
    }

    /** Resolves collisions between projectiles and enemies / knight. */
    public void resolveProjectilesHit(List<Enemy> enemies, Knight knight) {
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

    // --- Per-projectile updates ---

    private void updateVengefulSpirit(float delta, VengefulSpirit projectile) {
        if (projectile.isFlying()) {
            Rectangle projectileBounds = projectile.getBounds();
            for (var platform : collisionSystem.getGameMap().getColliders()) {
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
        for (var platform : collisionSystem.getGameMap().getColliders()) {
            if (shockwaveBounds.y >= platform.y &&
                shockwaveBounds.y + shockwaveBounds.height <= platform.y + platform.height &&
                shockwaveBounds.overlaps(platform)) {
                shockwave.onHitWall();
                break;
            }
        }

        shockwave.update(delta);
    }

    // --- Getters ---

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    /** Clears all projectiles (called on map change). */
    public void reset() {
        projectiles.clear();
    }
}
