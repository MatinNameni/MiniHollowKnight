package com.github.matinnameni.minihollowknight.model.laser;

import com.github.matinnameni.minihollowknight.model.entity.Entity;

public interface Laser extends Entity {

    /** @return true if this laser is being shot. */
    boolean isActive();

    /** @return true if this laser isn't being fired anymore. */
    boolean isDead();
}
