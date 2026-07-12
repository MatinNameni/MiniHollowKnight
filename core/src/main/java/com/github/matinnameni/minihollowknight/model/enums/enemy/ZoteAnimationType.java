package com.github.matinnameni.minihollowknight.model.enums.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;

public enum ZoteAnimationType {
    IDLE("animation/Zote/Idle.png",5, 1, 5, 1 / 8f,  Animation.PlayMode.LOOP),
    TALK("animation/Zote/Talk.png",5, 1, 5, 1 / 12f, Animation.PlayMode.LOOP),
    TURN ("animation/Zote/Turn.png",2, 1, 2, 1 / 10f, Animation.PlayMode.NORMAL),
    FALL ("animation/Zote/Fall.png", 5, 1, 5, 1 / 10f, Animation.PlayMode.NORMAL),
    GET_UP ("animation/Zote/Get Up.png", 4, 1, 4, 1 / 10f, Animation.PlayMode.NORMAL),
    ATTACK ("animation/Zote/Attack.png", 4, 1, 4, 1 / 10f, Animation.PlayMode.NORMAL),
    ROLL ("animation/Zote/Roll.png", 3, 1, 3, 1 / 10f, Animation.PlayMode.NORMAL);

    public final String path;
    public final int frameCount;
    public final float frameDuration;
    public final Animation.PlayMode playMode;
    public final int rowCount;
    public final int columnCount;

    ZoteAnimationType(String path, int frameCount, int rowCount,
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
