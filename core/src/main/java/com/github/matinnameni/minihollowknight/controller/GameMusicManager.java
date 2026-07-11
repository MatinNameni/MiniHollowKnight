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

    /** Duration of a crossfade between two tracks. */
    private static final float FADE_DURATION = 1.5f;

    // --- State ---
    private GameEnvironment currentEnvironment;
    private boolean bossFightActive = false;

    /** The track that should be playing once any transition completes. */
    private Music currentTrack;

    /**
     * The track being faded out during a crossfade, or {@code null} when no
     * transition is in progress (or when the outgoing track has fully faded
     * and been stopped).
     */
    private Music outgoingTrack;

    /** True while a fade is in progress. */
    private boolean transitioning = false;

    /** Elapsed time in the current transition, in seconds. */
    private float fadeTimer = 0f;

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

    /**
     * Advances any in-progress fade. MUST be called every frame from the game
     * loop (e.g. from {@code GameScreen.render}).
     */
    public void update(float delta) {
        if (!transitioning) return;

        fadeTimer += delta;
        float progress = Math.min(1f, fadeTimer / FADE_DURATION);
        float vol = currentVolume();

        // Fade the new track in.
        if (currentTrack != null) {
            currentTrack.setVolume(vol * progress);
        }
        // Fade the old track out.
        if (outgoingTrack != null) {
            outgoingTrack.setVolume(vol * (1f - progress));
        }

        if (progress >= 1f) {
            if (outgoingTrack != null) {
                outgoingTrack.stop();
                outgoingTrack = null;
            }
            transitioning = false;
        }
    }

    /** Re-applies the current music volume / mute setting to the active tracks. */
    public void applyVolumeSettings() {
        float vol = currentVolume();
        if (!transitioning) {
            if (currentTrack != null) {
                currentTrack.setVolume(vol);
            }
        } else {
            float progress = Math.min(1f, fadeTimer / FADE_DURATION);
            if (currentTrack != null) {
                currentTrack.setVolume(vol * progress);
            }
            if (outgoingTrack != null) {
                outgoingTrack.setVolume(vol * (1f - progress));
            }
        }
    }

    /** Stops all tracks immediately and clears transition state. */
    public void stop() {
        if (currentTrack != null) {
            currentTrack.stop();
            currentTrack = null;
        }
        if (outgoingTrack != null) {
            outgoingTrack.stop();
            outgoingTrack = null;
        }
        transitioning = false;
        fadeTimer = 0f;
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

        if (!transitioning) {
            if (desired == currentTrack) {
                if (currentTrack != null) {
                    currentTrack.setVolume(currentVolume());
                }
                return;
            }

            // Start a new transition.
            if (currentTrack == null && desired != null) {
                currentTrack = desired;
                desired.setVolume(0f);
                desired.play();
            } else if (currentTrack != null && desired == null) {
                outgoingTrack = currentTrack;
                currentTrack = null;
            } else {
                outgoingTrack = currentTrack;
                currentTrack = desired;
                desired.setVolume(0f);
                desired.play();
            }
            fadeTimer = 0f;
            transitioning = true;
            return;
        }

        if (desired == currentTrack) {
            return;
        }

        if (desired == outgoingTrack) {
            if (currentTrack != null) {
                currentTrack.stop();
            }
            currentTrack = outgoingTrack;
            outgoingTrack = null;
            transitioning = false;
            currentTrack.setVolume(currentVolume());
            return;
        }

        if (outgoingTrack != null) {
            outgoingTrack.stop();
        }
        outgoingTrack = currentTrack;
        currentTrack = desired;
        if (desired != null) {
            desired.setVolume(0f);
            desired.play();
        }
        fadeTimer = 0f;
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
