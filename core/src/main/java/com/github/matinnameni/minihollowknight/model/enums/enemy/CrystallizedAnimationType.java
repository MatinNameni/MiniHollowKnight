package com.github.matinnameni.minihollowknight.model.enums.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;

public enum CrystallizedAnimationType {
    IDLE("animation/Crystallized/Idle.png", 5, 1, 5, 1/10f, Animation.PlayMode.LOOP),
    RUN("animation/Crystallized/Run.png", 6, 1, 6, 1/12f, Animation.PlayMode.LOOP),
    TURN("animation/Crystallized/Turn.png", 3, 1, 3, 1/10f, Animation.PlayMode.NORMAL),
    SHOOT("animation/Crystallized/Shoot.png", 7, 1, 7, 1/12f, Animation.PlayMode.NORMAL),
    DEATH_LAND("animation/Crystallized/Death Land.png", 3, 1, 3, 1/12f, Animation.PlayMode.NORMAL),
    DEATH_AIR("animation/Crystallized/Death Air.png", 3, 1, 3, 1/12f, Animation.PlayMode.NORMAL);

    public final String path;
    public final int frameCount;
    public final float frameDuration;
    public final Animation.PlayMode playMode;
    public final int rowCount;
    public final int columnCount;

    CrystallizedAnimationType(String path, int frameCount, int rowCount,
                              int columnCount, float frameDuration, Animation.PlayMode playMode) {
        this.path = path;
        this.frameCount = frameCount;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.frameDuration = frameDuration;
        this.playMode = playMode;
    }

    public float getDuration() {
        return frameDuration * frameCount;
    }
}
