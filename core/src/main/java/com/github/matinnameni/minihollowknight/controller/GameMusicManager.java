package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.audio.Music;
import com.github.matinnameni.minihollowknight.model.asset.GameMusicAssetBundle;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.event.EventListener;

/**
 * Coordinates the gameplay music tracks.
 */
public class GameMusicManager implements EventListener {

    // --- State ---
    private GameEnvironment currentEnvironment;
    private boolean bossFightActive = false;

    /** The track currently playing, or {@code null} when nothing is playing. */
    private Music currentTrack;

    // --- Tracks ---
    private final Music crossroadsMusic;
    private final Music greenpathMusic;
    private final Music falseKnightMusic;

    // --- Settings ---
    private final Settings settings;

    public GameMusicManager(GameMusicAssetBundle assets, Settings settings) {
        this.settings = settings;
        this.crossroadsMusic = assets.getCrossroadsMusic();
        this.greenpathMusic = assets.getGreenpathMusic();
        this.falseKnightMusic = assets.getFalseKnightMusic();

        crossroadsMusic.setLooping(true);
        greenpathMusic.setLooping(true);
        falseKnightMusic.setLooping(true);

        EventBus bus = EventBus.getInstance();
        bus.subscribe(GameEvent.FALSE_KNIGHT_FIGHT_STARTED, this);
        bus.subscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
    }

    public void onEnvironmentChanged(GameEnvironment environment) {
        this.currentEnvironment = environment;
        this.bossFightActive = false;
        updateTrack();
    }

    /** Re-applies the current music volume / mute setting to the active track. */
    public void applyVolumeSettings() {
        if (currentTrack != null) {
            currentTrack.setVolume(currentVolume());
        }
    }

    /** Stops and clears the active track without releasing the music assets. */
    public void stop() {
        if (currentTrack != null) {
            currentTrack.stop();
            currentTrack = null;
        }
    }

    public void dispose() {
        EventBus bus = EventBus.getInstance();
        bus.unsubscribe(GameEvent.FALSE_KNIGHT_FIGHT_STARTED, this);
        bus.unsubscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
        stop();
    }

    // --- EventListener ---

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event == GameEvent.FALSE_KNIGHT_FIGHT_STARTED) {
            bossFightActive = true;
            updateTrack();
        } else if (event == GameEvent.FALSE_KNIGHT_DEFEATED) {
            bossFightActive = false;
            updateTrack();
        }
    }

    // --- Helpers ---

    /**
     * Swaps to the desired track for the current state, stopping the old one
     * first if any. No-op when the correct track is already playing.
     */
    private void updateTrack() {
        Music desired = desiredTrack();
        if (desired == currentTrack) {
            if (currentTrack != null) {
                currentTrack.setVolume(currentVolume());
            }
            return;
        }

        if (currentTrack != null) {
            currentTrack.stop();
        }

        currentTrack = desired;
        if (desired != null) {
            desired.setVolume(currentVolume());
            desired.play();
        }
    }

    /**
     * @return the track that should be playing right now, based on
     *         the current environment and boss-fight state, or {@code null}
     *         if no track applies.
     */
    private Music desiredTrack() {
        if (bossFightActive) {
            return falseKnightMusic;
        }
        if (currentEnvironment == GameEnvironment.FORGOTTEN_CROSSROADS) {
            return crossroadsMusic;
        }
        if (currentEnvironment == GameEnvironment.GREENPATH) {
            return greenpathMusic;
        }
        return null;
    }

    /** Current effective music volume. */
    private float currentVolume() {
        return settings.isMusicEnabled() ? settings.getMusicVolume() : 0f;
    }
}
