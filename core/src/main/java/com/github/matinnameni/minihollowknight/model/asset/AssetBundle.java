package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.Collections;
import java.util.List;

/**
 * Base class for asset bundles.
 */
public abstract class AssetBundle {

    /** Shared asset manager used by all bundles. */
    protected final AssetManager manager;

    protected AssetBundle(AssetManager manager) {
        this.manager = manager;
    }

    /**
     * Queues all assets owned by this bundle into the {@link AssetManager}.
     */
    public abstract void queue();

    /**
     * Called after the assets queued by {@link #queue()} have finished loading.
     */
    public abstract void onLoaded();

    /**
     * Returns the unique registry key for this bundle.
     */
    public abstract String getKey();

    /**
     * Returns all asset paths queued by this bundle.
     * Used by the registry when unloading a bundle to free memory.
     */
    public List<String> getAssetPaths() {
        return Collections.emptyList();
    }

    /**
     * Builds an animation from a sprite sheet.
     */
    public Animation<TextureRegion> buildAnimation(String path, int columns, int rows, float frameDuration, Animation.PlayMode playMode) {
        Texture texture = new Texture(path);

        TextureRegion[][] split = TextureRegion.split(
            texture,
            texture.getWidth() / columns,
            texture.getHeight() / rows
        );

        int frameCount = columns * rows;
        TextureRegion[] frames = new TextureRegion[frameCount];

        int cols = split[0].length;

        for(int i = 0; i < frameCount; i++) {
            int row = i / cols;
            int column = i % cols;
            frames[i] = split[row][column];
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(playMode);

        return animation;
    }

    /**
     * Builds an animation from a frame sequence.
     */
    public Animation<TextureRegion> buildAnimation(String path, int frameCount, String suffix, int startFrame,
                                                   int endFrame, float frameDuration, Animation.PlayMode playMode) {
        Array<TextureRegion> keyFrames = new Array<>(frameCount);

        for (int i = startFrame; i <= endFrame; i++) {
            String frameNum = String.format("%03d", i);
            if(!Gdx.files.internal(path + frameNum + suffix).exists()) {
                frameNum = String.format("%04d", i);
            }

            keyFrames.add(
                new TextureRegion(
                    new Texture(path + frameNum + suffix)
                )
            );
        }

        return new Animation<>(frameDuration, keyFrames, playMode);
    }
}
