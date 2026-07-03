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

    // sprite sheet
    public final int rowCount;
    public final int columnCount;

    // frame photos
    public final String suffix;
    public final int startFrameNum;
    public final int endFrameNum;

    CrawlidAnimationType(String path, int frameCount, int rowCount,
                        int columnCount, float frameDuration, Animation.PlayMode playMode) {
        this.path = path;
        this.frameCount = frameCount;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.suffix = null;
        startFrameNum = 0;
        endFrameNum = frameCount - 1;
        this.frameDuration = frameDuration;
        this.playMode = playMode;
    }

    CrawlidAnimationType(String path, int frameCount, String suffix, int startFrameNum,
                        int endFrameNum, float frameDuration, Animation.PlayMode playMode) {
        this.path = path;
        this.frameCount = frameCount;
        rowCount = -1;
        columnCount = -1;
        this.suffix = suffix;
        this.startFrameNum = startFrameNum;
        this.endFrameNum = endFrameNum;
        this.frameDuration = frameDuration;
        this.playMode = playMode;
    }

    public boolean isSpritesheet() {
        return suffix == null;
    }

    public float getDuration() {
        return frameDuration * frameCount;
    }
}
