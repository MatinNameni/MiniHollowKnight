package com.github.matinnameni.minihollowknight.model.map;

import com.github.matinnameni.minihollowknight.model.asset.AssetRegistry;
import com.github.matinnameni.minihollowknight.model.asset.TiledMapAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;

/**
 * Creates and loads a {@link TiledGameMap} using the given {@code path}
 */
public class MapLoader {
    public static TiledGameMap loadMap(GameEnvironment environment, TiledMapAssetBundle mapAssets) {
        return new TiledGameMap(environment, mapAssets);
    }
}
