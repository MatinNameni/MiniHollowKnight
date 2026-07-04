package com.github.matinnameni.minihollowknight.model.enums.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;

public enum MossflyAnimationType {
    SHAKE("animation/Mossfly/Shake.png", 3, 1, 3, 1/5f, Animation.PlayMode.LOOP),
    APPEAR("animation/Mossfly/Appear.png", 6, 1, 6, 1/15f, Animation.PlayMode.NORMAL),
    TURN_TO_FLY("animation/Mossfly/TurnToFly.png", 3, 1, 3, 1/8f, Animation.PlayMode.NORMAL),
    FLY("animation/Mossfly/Fly.png", 4, 1, 4, 1/10f, Animation.PlayMode.LOOP),
    DEATH_AIR("animation/Mossfly/Death Air.png", 4, 1, 4, 1/10f, Animation.PlayMode.NORMAL),
    DEATH_LAND("animation/Mossfly/Death Land.png", 2, 1, 2, 1/10f, Animation.PlayMode.NORMAL);

    public final String path;
    public final int frameCount;
    public final float frameDuration;
    public final Animation.PlayMode playMode;
    public final int rowCount;
    public final int columnCount;

    MossflyAnimationType(String path, int frameCount, int rowCount,
                                int columnCount, float frameDuration, Animation.PlayMode playMode) {
        this.path = path;
        this.frameCount = frameCount;
        this.frameDuration = frameDuration;
        this.playMode = playMode;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public float getDuration() {
        return frameDuration * frameCount;
    }
}
