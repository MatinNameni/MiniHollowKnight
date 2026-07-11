package com.github.matinnameni.minihollowknight.model.projectile;

import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.entity.Entity;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.enums.Direction;

public interface Projectile extends Entity {
    /** @return the damage this projectile applies on impact. */
    float getDamage();

    /** @return true if the projectile should be removed from the game */
    boolean isDead();

    /** @return the direction this projectile was shot at. */
    Direction getDirection();

    /** Called when the projectile hits an enemy. */
    void onHitEnemy(Enemy enemy);

    /** @return true if this projectile effects enemies. */
    boolean hasEffectOnEnemies();

    /** Called when the projectile hits the Knight. */
    void onHitKnight(Knight knight);

    /** @return true if this projectile effects the Knight. */
    boolean hasEffectOnKnight();
}
