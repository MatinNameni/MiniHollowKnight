package com.github.matinnameni.minihollowknight.model.object;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;

public class BreakableWall {

    // --- Constants ---

    // Textures prefix
    private static final String FORGOTTEN_CROSSROADS_TEXTURES_PREFIX =
        "sprites/Architecture & Environment/Area specfic architecture/Forgotten Crossroads/";

    // Size
    private static final float WIDTH_FILLER = 10f;
    private static final float HEIGHT_FILLER = 25f;

    // Position offset
    private static final float X_OFFSET = -5f;
    private static final float Y_OFFSET = -15f;

    // --- Geometry ---
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    // --- Textures ---
    private Texture textureDefault;
    private Texture textureCracked;
    private Texture textureBroken;

    // --- State ---
    private int hitCount;
    private final int hitsToBreak;
    private State state;
    private final float scaleX;
    private final float scaleY;

    // --- Collider ---
    private final GridObject collider;

    public BreakableWall(float x, float y, float width, float height, float scaleX, float scaleY,
                         GameEnvironment environment, String textureDefault, String textureCracked,
                         String textureBroken, int hitsToBreak) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        initTextures(environment, textureDefault, textureCracked, textureBroken);
        this.hitsToBreak = Math.max(1, hitsToBreak);
        this.hitCount = 0;
        this.state = State.INTACT;
        this.collider = new GridObject(x, y, width, height, false, false);
    }

    // --- Hit logic ---

    /**
     * Apply one nail hit on this wall.
     *
     * @return true if the wall's state changed this hit.
     */
    public boolean hit() {
        if (state == State.BROKEN) return false;

        hitCount++;

        if (hitCount >= hitsToBreak) {
            state = State.BROKEN;
            EventBus.getInstance().publish(GameEvent.BREAKABLE_WALL_DEATH);
        } else {
            state = State.CRACKED;
            EventBus.getInstance().publish(GameEvent.BREAKABLE_WALL_HIT);
        }
        return true;
    }

    /** @return true when the wall has been fully destroyed. */
    public boolean isPassable() {
        return state == State.BROKEN;
    }

    /** @return the current collider. */
    public GridObject getCollider() {
        return collider;
    }

    /** Returns an axis-aligned bounding box. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // --- Rendering ---

    public void render(SpriteBatch batch) {
        Texture tex;
        switch (state) {
            case CRACKED:
                tex = textureCracked;
                break;
            case BROKEN:
                tex = textureBroken;
                break;
            default:
                tex = textureDefault;
        }

        float drawX = x + X_OFFSET;
        float drawY = y + Y_OFFSET;
        float drawWidth = width + WIDTH_FILLER;
        float drawHeight = height + HEIGHT_FILLER;

        batch.draw(tex,
            drawX, drawY,
            drawWidth / 2, drawHeight / 2,
            drawWidth, drawHeight,
            scaleX, scaleY,
            0,
            0 , 0,
            tex.getWidth(), tex.getHeight(),
            false, false);
    }

    // --- Lifecycle ---

    public void dispose() {
        textureDefault.dispose();
        textureCracked.dispose();
        textureBroken.dispose();
    }

    // --- Helpers ---

    private void initTextures(GameEnvironment environment, String textureDefault,
                              String textureCracked, String textureBroken) {
        String prefix = "";
        switch (environment) {
            case FORGOTTEN_CROSSROADS:
                prefix = FORGOTTEN_CROSSROADS_TEXTURES_PREFIX;
                break;
        }
        this.textureDefault = new Texture(prefix + textureDefault);
        this.textureCracked = new Texture(prefix + textureCracked);
        this.textureBroken  = new Texture(prefix + textureBroken);
    }

    // --- State ---

    public enum State {
        INTACT,
        CRACKED,
        BROKEN
    }
}
