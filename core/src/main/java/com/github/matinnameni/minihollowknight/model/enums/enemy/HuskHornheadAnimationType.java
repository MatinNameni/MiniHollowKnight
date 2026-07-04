package com.github.matinnameni.minihollowknight.model.enums.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;

public enum HuskHornheadAnimationType {
    IDLE("animation/Husk_Hornhead/Idle.png", 6, 1, 6, 1/8f, Animation.PlayMode.LOOP),
    WALK("animation/Husk_Hornhead/Walk.png", 7, 1, 7, 1/12f, Animation.PlayMode.LOOP),
    TURN("animation/Husk_Hornhead/Turn.png", 2, 1, 2, 1/10f, Animation.PlayMode.NORMAL),
    ATTACK_ANTICIPATE("animation/Husk_Hornhead/Attack Anticipate.png", 5, 1, 5, 1/12f, Animation.PlayMode.NORMAL),
    ATTACK_LUNGE("animation/Husk_Hornhead/Attack Lunge.png", 12, 1, 12, 1/18f, Animation.PlayMode.LOOP),
    DEATH_LAND("animation/Husk_Hornhead/Death Land.png", 8, 1, 8, 1/12f, Animation.PlayMode.NORMAL);

    public final String path;
    public final int frameCount;
    public final float frameDuration;
    public final Animation.PlayMode playMode;
    public final int rowCount;
    public final int columnCount;

    HuskHornheadAnimationType(String path, int frameCount, int rowCount,
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
