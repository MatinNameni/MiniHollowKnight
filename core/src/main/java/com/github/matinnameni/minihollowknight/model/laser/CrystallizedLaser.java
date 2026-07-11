package com.github.matinnameni.minihollowknight.model.laser;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CrystallizedLaser implements Laser {

    // --- Constants ---

    // Timing
    /** How long the thin warning line is visible before the beam activates. */
    public static final float TELEGRAPH_DURATION = 0.6f;

    /** How long the beam takes to grow from minimum to maximum height. */
    public static final float EXPAND_DURATION = 0.3f;

    /** How long the beam stays at full power before fading. */
    public static final float HOLD_DURATION = 0.5f;

    /** How long the beam takes to shrink back to nothing. */
    public static final float FADE_DURATION = 0.25f;

    // Size
    /** Maximum beam height in world units. */
    public static final float MAX_HEIGHT = 40f;

    /** Minimum beam height during telegraph. */
    public static final float TELEGRAPH_HEIGHT = 4f;

    /** Beam length. */
    public static final float LENGTH = 2000f;

    // Colors
    private static final Color TELEGRAPH_COLOR = new Color(0.6f, 0.8f, 1.0f, 0.3f);
    private static final Color BEAM_CORE_COLOR = new Color(0.85f, 0.92f, 1.0f, 0.95f);
    private static final Color BEAM_GLOW_COLOR = new Color(0.4f, 0.65f, 1.0f, 0.45f);

    // --- State ---

    private final Vector2 origin = new Vector2();
    private final boolean facingRight;
    private final State initialState;

    private State state;
    private float stateTime = 0f;
    private float currentHeight;
    private boolean active; // true when the beam can deal damage

    // --- Rendering ---

    private Texture coreTexture;
    private Texture glowTexture;

    public CrystallizedLaser(float originX, float originY, boolean facingRight, boolean startTelegraph) {
        this.origin.set(originX, originY);
        this.facingRight = facingRight;
        this.initialState = startTelegraph ? State.TELEGRAPH : State.EXPANDING;
        this.state = initialState;
        this.stateTime = 0f;
        this.currentHeight = TELEGRAPH_HEIGHT;
        this.active = false;
        createTextures();
    }

    // --- Laser ---

    @Override
    public void update(float deltaTime) {
        stateTime += deltaTime;

        switch (state) {
            case TELEGRAPH:
                currentHeight = TELEGRAPH_HEIGHT;
                active = false;
                if (stateTime >= TELEGRAPH_DURATION) {
                    enterState(State.EXPANDING);
                }
                break;

            case EXPANDING:
                active = true;
                float expandProgress = Math.min(stateTime / EXPAND_DURATION, 1f);
                currentHeight = expandProgress * (MAX_HEIGHT - TELEGRAPH_HEIGHT);
                if (stateTime >= EXPAND_DURATION) {
                    enterState(State.HOLDING);
                }
                break;

            case HOLDING:
                active = true;
                currentHeight = MAX_HEIGHT;
                if (stateTime >= HOLD_DURATION) {
                    enterState(State.FADING);
                }
                break;

            case FADING:
                float fadeProgress = Math.min(stateTime / FADE_DURATION, 1f);
                currentHeight = MAX_HEIGHT * (1f - fadeProgress);
                active = fadeProgress < 0.5f;
                if (stateTime >= FADE_DURATION) {
                    enterState(State.DEAD);
                }
                break;

            case DEAD:
                active = false;
                break;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (state == State.DEAD) return;

        float drawX = facingRight ? origin.x : origin.x - LENGTH;
        float drawY = origin.y - currentHeight / 2f;
        float drawWidth = LENGTH;
        float drawHeight = currentHeight;

        float initialR = batch.getColor().r;
        float initialG = batch.getColor().g;
        float initialB = batch.getColor().b;
        float initalAlpha = batch.getColor().a;

        // Draw glow layer
        if (state != State.TELEGRAPH) {
            float glowExtra = currentHeight * 0.6f;
            float glowY = origin.y - (currentHeight + glowExtra) / 2f;
            float glowHeight = currentHeight + glowExtra;
            batch.setColor(BEAM_GLOW_COLOR);
            batch.draw(glowTexture, drawX, glowY, drawWidth, glowHeight);
        }

        // Draw core beam
        if (state == State.TELEGRAPH) {
            batch.setColor(TELEGRAPH_COLOR);
        } else {
            batch.setColor(BEAM_CORE_COLOR);
        }
        batch.draw(coreTexture, drawX, drawY, drawWidth, drawHeight);

        batch.setColor(initialR, initialG, initialB, initalAlpha);
    }

    @Override
    public Vector2 getPosition() {
        return origin;
    }

    // --- Collision ---

    @Override
    public Rectangle getBounds() {
        float drawX = facingRight ? origin.x : origin.x - LENGTH;
        float drawY = origin.y - currentHeight / 2f;
        return new Rectangle(drawX, drawY, LENGTH, currentHeight);
    }

    // --- Getters ---

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isDead() {
        return state == State.DEAD;
    }

    public State getState() {
        return state;
    }

    // --- Lifecycle ---

    public void dispose() {
        if (coreTexture != null) coreTexture.dispose();
        if (glowTexture != null) glowTexture.dispose();
    }

    // --- Helpers ---

    private void enterState(State newState) {
        state = newState;
        stateTime = 0f;
    }

    /** Creates simple 1x1 pixel textures used for drawing the laser beam. */
    private void createTextures() {
        // Core: white 1x1 pixel
        Pixmap corePx = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        corePx.setColor(Color.WHITE);
        corePx.fill();
        coreTexture = new Texture(corePx);
        corePx.dispose();

        // Glow: white 1x1 pixel
        Pixmap glowPx = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        glowPx.setColor(Color.WHITE);
        glowPx.fill();
        glowTexture = new Texture(glowPx);
        glowPx.dispose();
    }

    // --- Inner type ---

    public enum State {
        TELEGRAPH, // Thin warning line
        EXPANDING, // Beam growing from thin to full height
        HOLDING, // Beam at full power
        FADING, // Beam shrinking
        DEAD // Laser is done
    }
}
