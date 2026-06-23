package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;

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
}
