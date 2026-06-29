package com.github.matinnameni.minihollowknight.model;

public interface Projectile extends Entity {
    /** Call when the projectile hits an enemy. */
    void onHitEnemy();

    /** @return true if the projectile should be removed from the game */
    boolean isDead();
}
