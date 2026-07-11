package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.audio.Sound;
import com.github.matinnameni.minihollowknight.model.asset.SoundEffectAssetBundle;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.enums.Direction;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.event.EventListener;

/**
 * Plays sound effects (SFX) in response to gameplay events.
 */
public class SoundEffectManager implements EventListener {

    // --- Settings ---
    private final Settings settings;

    // --- Sounds ---
    private final Sound swordSlash;
    private final Sound downSlash;
    private final Sound heroDamage;
    private final Sound enemyDamage;
    private final Sound focusCharging;
    private final Sound focusHeal;
    private final Sound soulPickup;
    private final Sound breakableWallHit;
    private final Sound breakableWallDeath;

    /**
     * Sound instance id of the currently looping focus-charging sound,
     * or {@code -1} when the focus-charging loop is not playing.
     */
    private long focusChargingId = -1L;

    public SoundEffectManager(SoundEffectAssetBundle assets, Settings settings) {
        this.settings = settings;
        this.swordSlash = assets.getSwordSlash();
        this.downSlash = assets.getDownSlash();
        this.heroDamage = assets.getHeroDamage();
        this.enemyDamage = assets.getEnemyDamage();
        this.focusCharging = assets.getFocusCharging();
        this.focusHeal = assets.getFocusHeal();
        this.soulPickup = assets.getSoulPickup();
        this.breakableWallHit = assets.getBreakableWallHit();
        this.breakableWallDeath = assets.getBreakableWallDeath();

        EventBus bus = EventBus.getInstance();
        bus.subscribe(GameEvent.PLAYER_ATTACK_START, this);
        bus.subscribe(GameEvent.PLAYER_DAMAGED, this);
        bus.subscribe(GameEvent.ENEMY_DAMAGED, this);
        bus.subscribe(GameEvent.PLAYER_FOCUS_START, this);
        bus.subscribe(GameEvent.PLAYER_FOCUS_CANCEL, this);
        bus.subscribe(GameEvent.PLAYER_FOCUS_COMPLETE, this);
        bus.subscribe(GameEvent.PLAYER_SOUL_GAINED, this);
        bus.subscribe(GameEvent.BREAKABLE_WALL_HIT, this);
        bus.subscribe(GameEvent.BREAKABLE_WALL_DEATH, this);
    }

    // --- EventListener ---

    @Override
    public void onEvent(GameEvent event, Object payload) {
        switch (event) {
            case PLAYER_ATTACK_START:
                if (payload == Direction.DOWN) {
                    play(downSlash);
                } else {
                    play(swordSlash);
                }
                break;

            case PLAYER_DAMAGED:
                play(heroDamage);
                break;

            case ENEMY_DAMAGED:
                play(enemyDamage);
                break;

            case PLAYER_FOCUS_START:
                startFocusCharging();
                break;

            case PLAYER_FOCUS_CANCEL:
                stopFocusCharging();
                break;

            case PLAYER_FOCUS_COMPLETE:
                stopFocusCharging();
                play(focusHeal);
                break;

            case PLAYER_SOUL_GAINED:
                play(soulPickup);
                break;

            case BREAKABLE_WALL_HIT:
                play(breakableWallHit);
                break;

            case BREAKABLE_WALL_DEATH:
                play(breakableWallDeath);
                break;

            default:
                break;
        }
    }

    /**
     * Re-applies the current SFX volume / mute setting to any active sound.
     */
    public void applyVolumeSettings() {
        if (focusChargingId != -1L) {
            float vol = effectiveVolume();
            focusCharging.setVolume(focusChargingId, vol);
        }
    }

    public void update(float delta) {
        // no-op
    }

    public void dispose() {
        stopFocusCharging();

        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(GameEvent.PLAYER_ATTACK_START, this);
        bus.unsubscribe(GameEvent.PLAYER_DAMAGED, this);
        bus.unsubscribe(GameEvent.ENEMY_DAMAGED, this);
        bus.unsubscribe(GameEvent.PLAYER_FOCUS_START, this);
        bus.unsubscribe(GameEvent.PLAYER_FOCUS_CANCEL, this);
        bus.unsubscribe(GameEvent.PLAYER_FOCUS_COMPLETE, this);
        bus.unsubscribe(GameEvent.PLAYER_SOUL_GAINED, this);
        bus.subscribe(GameEvent.BREAKABLE_WALL_HIT, this);
        bus.subscribe(GameEvent.BREAKABLE_WALL_DEATH, this);
    }

    // --- Helpers ---

    /** Plays a one-shot sound at the current effective volume. */
    private void play(Sound sound) {
        if (sound == null) return;
        float vol = effectiveVolume();
        if (vol <= 0f) return;
        sound.play(vol);
    }

    /** Starts the focus-charging loop. */
    private void startFocusCharging() {
        if (focusCharging == null) return;
        if (focusChargingId != -1L) return;
        float vol = effectiveVolume();
        focusChargingId = focusCharging.loop(vol);
        if (vol <= 0f && focusChargingId != -1L) {
            focusCharging.setVolume(focusChargingId, 0f);
        }
    }

    /** Stops the focus-charging loop if it is currently playing. */
    private void stopFocusCharging() {
        if (focusChargingId != -1L && focusCharging != null) {
            focusCharging.stop(focusChargingId);
        }
        focusChargingId = -1L;
    }

    /** Current effective SFX volume (0 if disabled or muted). */
    private float effectiveVolume() {
        return settings.isSfxEnabled() ? settings.getSfxVolume() : 0f;
    }
}
