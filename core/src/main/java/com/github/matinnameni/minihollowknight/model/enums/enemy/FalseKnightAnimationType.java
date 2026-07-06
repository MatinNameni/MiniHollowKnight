package com.github.matinnameni.minihollowknight.model.enums.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;

public enum FalseKnightAnimationType {
    // --- Core movement ---
    IDLE("animation/False_knight/Idle.png", 5, 1, 5, 1 / 10f, Animation.PlayMode.LOOP),
    TURN("animation/False_knight/Turn.png", 2, 1, 2, 1 / 10f, Animation.PlayMode.NORMAL),
    RUN("animation/False_knight/Run.png", 5, 1, 5, 1 / 10f, Animation.PlayMode.LOOP),
    RUN_ANTIC("animation/False_knight/Run Antic.png", 2, 1, 2, 1 / 10f, Animation.PlayMode.NORMAL),

    // --- Jump / aerial ---
    JUMP("animation/False_knight/Jump.png", 4, 1, 4, 1 / 10f, Animation.PlayMode.NORMAL),
    JUMP_ANTIC("animation/False_knight/Jump Antic.png", 3, 1, 3, 1 / 10f, Animation.PlayMode.NORMAL),
    JUMP_ATTACK("animation/False_knight/Jump Attack.png", 8, 1, 8, 1 / 10f, Animation.PlayMode.NORMAL),
    LAND("animation/False_knight/Land.png", 5, 1, 5, 1 / 10f, Animation.PlayMode.NORMAL),

    // --- Mace attacks ---
    ATTACK("animation/False_knight/Attack.png", 3, 1, 3, 1 / 10f, Animation.PlayMode.NORMAL),
    ATTACK_ANTIC("animation/False_knight/Attack Antic.png", 6, 1, 6, 1 / 10f, Animation.PlayMode.NORMAL),
    ATTACK_RECOVER("animation/False_knight/Attack Recover.png", 5, 1, 5, 1 / 10f, Animation.PlayMode.NORMAL),

    // --- Stun / recover ---
    STUN_RECOVER("animation/False_knight/Stun Recover.png", 6, 1, 6, 1 / 10f, Animation.PlayMode.NORMAL),

    // --- Death sequence ---
    DEATH_HIT("animation/False_knight/DeathHit.png", 3, 1, 3, 1 / 10f, Animation.PlayMode.NORMAL),
    DEATH_FALL("animation/False_knight/DeathFall.png", 3, 1, 3, 1 / 10f, Animation.PlayMode.NORMAL),
    DEATH_LAND("animation/False_knight/DeathLand.png", 11, 1, 11, 1 / 10f, Animation.PlayMode.NORMAL),

    // --- Body (exposed maggot inside armor during stun) ---
    BODY("animation/False_knight/Body.png", 5, 1, 5, 1 / 10f, Animation.PlayMode.LOOP);

    public final String path;
    public final int frameCount;
    public final float frameDuration;
    public final Animation.PlayMode playMode;
    public final int rowCount;
    public final int columnCount;

    FalseKnightAnimationType(String path, int frameCount, int rowCount,
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
