package com.github.matinnameni.minihollowknight.model;

import com.github.matinnameni.minihollowknight.model.enemies.Enemy;

public interface Projectile extends Entity {
    /** Call when the projectile hits an enemy. */
    void onHitEnemy(Enemy enemy);

    /** @return true if the projectile should be removed from the game */
    boolean isDead();

    /** @return true if this projectile effects enemies. */
    boolean hasEffectOnEnemies();
}
