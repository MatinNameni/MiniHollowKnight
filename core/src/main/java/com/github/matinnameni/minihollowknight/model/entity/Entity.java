package com.github.matinnameni.minihollowknight.model.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** Contract for every live object in the game world. */
public interface Entity {
    /** Advances the entity's logic by {@code deltaTime} seconds. */
    void update(float deltaTime);

    /** Draws the entity. */
    void render(SpriteBatch batch);

    /** World-space position. */
    Vector2 getPosition();

    /** Axis-aligned bounding box used for collision detection. */
    Rectangle getBounds();
}
