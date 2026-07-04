package com.github.matinnameni.minihollowknight.model.asset;

import java.util.HashMap;
import java.util.Map;

public class EnemiesAssetsManager {
    private static EnemiesAssetsManager instance;
    private AssetRegistry registry;
    private Map<String, AssetBundle> enemiesAssets = new HashMap<>();
    private boolean crawlidAssetsLoaded = false;
    private boolean mossflyAssetsLoaded = false;
    private boolean huskHornheadAssetsLoaded = false;
    private boolean crystallizedAssetsLoaded = false;

    private EnemiesAssetsManager(AssetRegistry registry) {
        this.registry = registry;
    }

    public static EnemiesAssetsManager getInstance(AssetRegistry registry) {
        if (instance == null) {
            instance = new EnemiesAssetsManager(registry);
        }
        return instance;
    }

    /** Registers all enemies asset bundles. */
    public void initAssets() {
        registry.register(new CrawlidAssetBundle(registry.getManager()));
        registry.register(new MossflyAssetBundle(registry.getManager()));
        registry.register(new HuskHornheadAssetBundle(registry.getManager()));
        registry.register(new CrystallizedAssetBundle(registry.getManager()));
    }

    /** Loads all enemies asset bundles if they haven't been loaded yet. */
    public void loadBundles() {
        if (!crawlidAssetsLoaded) {
            registry.loadBundle(CrawlidAssetBundle.KEY);
            crawlidAssetsLoaded = true;
        }

        if (!mossflyAssetsLoaded) {
            registry.loadBundle(MossflyAssetBundle.KEY);
            mossflyAssetsLoaded = true;
        }

        if (!huskHornheadAssetsLoaded) {
            registry.loadBundle(HuskHornheadAssetBundle.KEY);
            huskHornheadAssetsLoaded = true;
        }

        if (!crystallizedAssetsLoaded) {
            registry.loadBundle(CrystallizedAssetBundle.KEY);
            crystallizedAssetsLoaded = true;
        }
    }

    // --- Enemies asset bundles ---

    public CrawlidAssetBundle getCrawlidAssetBundle() {
        return (CrawlidAssetBundle) registry.get(CrawlidAssetBundle.KEY);
    }

    public MossflyAssetBundle getMossflyAssetBundle() {
        return (MossflyAssetBundle) registry.get(MossflyAssetBundle.KEY);
    }

    public HuskHornheadAssetBundle getHuskHornheadAssetBundle() {
        return (HuskHornheadAssetBundle) registry.get(HuskHornheadAssetBundle.KEY);
    }

    public CrystallizedAssetBundle getCrystallizedAssetBundle() {
        return (CrystallizedAssetBundle) registry.get(CrystallizedAssetBundle.KEY);
    }
}
