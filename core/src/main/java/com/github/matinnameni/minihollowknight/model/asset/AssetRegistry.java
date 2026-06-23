package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry that maps string keys to {@link AssetBundle} instances.
 */
public class AssetRegistry {
    private final AssetManager manager = new AssetManager();
    private final Map<String, AssetBundle> bundles = new LinkedHashMap<>();

    public AssetManager getManager() {
        return manager;
    }

    /**
     * Registers a bundle under its own key.
     */
    public void register(AssetBundle bundle) {
        bundles.put(bundle.getKey(), bundle);
    }

    /**
     * Queues and loads the specified bundle.
     *
     * @param key bundle key to load
     */
    public void loadBundle(String key) {
        AssetBundle bundle = bundles.get(key);
        if(bundle != null) {
            bundle.queue();
            manager.finishLoading();
            bundle.onLoaded();
        }
    }

    /**
     * Unloads the specified bundle, freeing its assets from memory.
     *
     * @param key bundle key to unload
     */
    public void unloadBundle(String key) {
        AssetBundle bundle = bundles.get(key);
        if (bundle != null) {
            for (String path : bundle.getAssetPaths()) {
                if (manager.isLoaded(path)) {
                    manager.unload(path);
                }
            }
        }
    }

    /**
     * Returns the bundle registered under the given key.
     *
     * @param key the bundle key.
     */
    public AssetBundle get(String key) {
        return bundles.get(key);
    }

    /**
     * Checks whether a bundle is registered under the given key.
     */
    public boolean has(String key) {
        return bundles.containsKey(key);
    }

    /**
     * Disposes all loaded assets and clears the registry.
     */
    public void dispose() {
        manager.dispose();
        bundles.clear();
    }
}
