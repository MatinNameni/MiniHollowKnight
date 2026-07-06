package com.github.matinnameni.minihollowknight.model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.matinnameni.minihollowknight.model.asset.TiledMapAssetBundle;

public class Door extends GridObject implements Entity {

    // --- Constants ---

    // Size
    private float WIDTH = 132f;
    private float HEIGHT = 145f;

    // Bounds
    private static final float X_OFFSET = -20f;
    private static final float Y_OFFSET = -5f;

    // --- State ---

    private DoorState currentState = DoorState.OPEN;

    private final Animation<TextureRegion> openAnimation;
    private final Animation<TextureRegion> closeAnimation;
    private float stateTime = 0f;
    private boolean isSolid = false;

    public Door(float x, float y, float width, float height, TiledMapAssetBundle mapAssets) {
        super(x, y, width, height);
        this.openAnimation = mapAssets.getDoorOpenAnimation();
        this.closeAnimation = mapAssets.getDoorCloseAnimation();
    }

    @Override
    public void update(float delta) {
        stateTime += delta;

        switch (currentState) {
            case CLOSING:
                if (closeAnimation.isAnimationFinished(stateTime)) {
                    currentState = DoorState.CLOSED;
                    isSolid = true;
                }
                break;
            case OPENING:
                if (openAnimation.isAnimationFinished(stateTime)) {
                    currentState = DoorState.OPEN;
                    isSolid = false;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion> animation = getCurrentAnimation();
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(stateTime);

        batch.draw(frame,
            getX() + X_OFFSET, getY() + Y_OFFSET,
            WIDTH, HEIGHT);
    }

    @Override
    public Vector2 getPosition() {
        return new Vector2(getX(), getY());
    }

    @Override
    public Rectangle getBounds() {
        return this;
    }

    public void closeDoor() {
        if (currentState == DoorState.OPEN || currentState == DoorState.OPENING) {
            currentState = DoorState.CLOSING;
            stateTime = 0f;
            isSolid = true;
        }
    }

    public void openDoor() {
        if (currentState == DoorState.CLOSED || currentState == DoorState.CLOSING) {
            currentState = DoorState.OPENING;
            stateTime = 0f;
        }
    }

    public boolean isSolid() { return isSolid; }

    // --- Helpers ---

    private Animation<TextureRegion> getCurrentAnimation() {
        switch (currentState) {
            case OPENING, OPEN: return openAnimation;
            case CLOSING, CLOSED: return closeAnimation;
            default: return null;
        }
    }

    // --- Inner types ---

    public enum DoorState {
        OPEN,
        CLOSING,
        CLOSED,
        OPENING
    }
}
