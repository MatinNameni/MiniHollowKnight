package com.github.matinnameni.minihollowknight.model.enemies;

public abstract class Boss extends Enemy {
    public Boss(float x, float y) {
        super(x, y);
    }

    /** @return which phase is the boss currently at. */
    public abstract int getPhase();
}
