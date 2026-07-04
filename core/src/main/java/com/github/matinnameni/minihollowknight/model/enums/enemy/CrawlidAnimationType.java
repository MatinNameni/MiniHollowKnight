package com.github.matinnameni.minihollowknight.model.enums.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;

public enum CrawlidAnimationType {
    WALK("animation/Crawlid/Walk.png", 4, 1, 4, 1/15f, Animation.PlayMode.LOOP),
    TURN("animation/Crawlid/Turn.png", 2, 1, 2, 1/15f, Animation.PlayMode.NORMAL),
    DEATH_LAND("animation/Crawlid/Death Land.png", 2, 1, 2, 1/15f, Animation.PlayMode.NORMAL),
    DEATH_AIR("animation/Crawlid/Death Air.png", 3, 1, 3, 1/15f, Animation.PlayMode.NORMAL);

    public final String path;
    public final int frameCount;
    public final float frameDuration;
    public final Animation.PlayMode playMode;
    public final int rowCount;
    public final int columnCount;

    CrawlidAnimationType(String path, int frameCount, int rowCount,
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
