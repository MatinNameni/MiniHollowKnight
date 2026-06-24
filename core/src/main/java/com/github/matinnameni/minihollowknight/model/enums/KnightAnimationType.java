package com.github.matinnameni.minihollowknight.model.enums;

import com.badlogic.gdx.graphics.g2d.Animation;

public enum KnightAnimationType {
    IDLE("animation/Idle.png", 9, 1, 9, 1/15f, Animation.PlayMode.LOOP),
    IDLE_HURT("animation/Idle Hurt.png", 12, 1, 12, 1/15f, Animation.PlayMode.LOOP),
    RUN("animation/Run_", 13, ".png", 3, 12, 1/15f, Animation.PlayMode.LOOP),
    RUN_TO_IDLE("animation/Run To Idle_", 6, ".png", 0, 5, 1/15f, Animation.PlayMode.LOOP),
    AIR_BORNE("animation/Airborne.png", 12, 1, 12, 1/15f, Animation.PlayMode.NORMAL),
    DOUBLE_JUMP("animation/Double Jump.png", 8, 1, 8, 1/15f, Animation.PlayMode.LOOP),
    FALL("animation/Fall_", 6, ".png", 0, 5, 1/15f, Animation.PlayMode.LOOP),
    LANDING("animation/Landing.png", 4, 1, 4, 1/15f, Animation.PlayMode.LOOP),
    SLASH("animation/SlashAlt.png", 5, 1, 5, 1/15f, Animation.PlayMode.NORMAL),
    UP_SLASH("animation/UpSlash.png", 5, 1, 5, 1/15f, Animation.PlayMode.LOOP),
    DOWN_SLASH("animation/DownSlash.png", 5, 1, 5, 1/15f, Animation.PlayMode.LOOP),
    WALL_JUMP("animation/Walljump.png", 9, 1, 9, 1/15f, Animation.PlayMode.NORMAL),
    WALL_SLIDE("animation/Wall Slide.png", 4, 1, 4, 1/15f, Animation.PlayMode.LOOP),
    DASH("animation/Dash.png", 12, 1, 12, 1/15f, Animation.PlayMode.NORMAL),
    FOCUS("animation/Focus_", 7, ".png", 0, 6, 1/15f, Animation.PlayMode.LOOP),
    FOCUS_START("animation/Focus Start.png", 3, 1, 3, 1/15f, Animation.PlayMode.NORMAL),
    FOCUS_GET("animation/Focus Get.png", 6, 1, 6, 1/15f, Animation.PlayMode.NORMAL),
    FOCUS_END("animation/Focus End.png", 3, 1, 3, 1/15f, Animation.PlayMode.NORMAL),
    FIREBALL_CAST("animation/Fireball Cast.png", 9, 1, 9, 1/15f, Animation.PlayMode.NORMAL),
    SCREAM("animation/Scream.png", 7, 1, 7, 1/15f, Animation.PlayMode.NORMAL),
    DEATH("animation/Death.png", 18, 1, 18, 1/15f, Animation.PlayMode.NORMAL);


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

    KnightAnimationType(String path, int frameCount, int rowCount,
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

    KnightAnimationType(String path, int frameCount, String suffix, int startFrameNum,
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
