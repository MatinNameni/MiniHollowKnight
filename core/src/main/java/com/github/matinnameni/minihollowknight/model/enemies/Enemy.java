package com.github.matinnameni.minihollowknight.model.enemies;

import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.Entity;
import com.github.matinnameni.minihollowknight.model.enums.Direction;

public abstract class Enemy implements Entity {

    // --- State ---
    protected Vector2 position = new Vector2();
    protected Vector2 velocity = new Vector2();
    protected float health;

    public Enemy(float x, float y) {
        this.position.set(x, y);
    }

    /** Applies {@code damage} to this enemy's health. */
    public abstract void takeDamage(float damage, Direction knockbackDirection);

    /** @return true while the enemy's hitbox should be able to damage the player on contact. */
    public abstract boolean canDamagePlayer();

    /** @return true if this enemy is dead. */
    public boolean isDead() {
        return health <= 0f;
    }

    // --- Getters ---

    @Override
    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }
}
