package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;

public class TiledMapAssetBundle extends AssetBundle {

    public static final String KEY = "tiledmap";

    // --- Asset paths ---
    private static final String COLOSSEUM_DOOR_PREFIX = "sprites/Architecture & Environment/Area specfic architecture/Colosseum/colosseum_dungeon_door";

    // --- Animation ---
    private Animation<TextureRegion> doorOpenAnimation;
    private Animation<TextureRegion> doorCloseAnimation;
    private static final String COLOSSEUM_DOOR_SUFFIX = ".png";
    private static final int COLOSSEUM_DOOR_FRAME_COUNT = 7;
    private static final float COLOSSEUM_DOOR_FRAME_DURATION = 1/15f;
    private static final Animation.PlayMode COLOSSEUM_DOOR_OPEN_PLAYMODE = Animation.PlayMode.NORMAL;
    private static final Animation.PlayMode COLOSSEUM_DOOR_CLOSE_PLAYMODE = Animation.PlayMode.REVERSED;

    private final List<String> assetPaths = new ArrayList<>();

    public TiledMapAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public void queue() {
        assetPaths.clear();
    }

    @Override
    public void onLoaded() {
        doorOpenAnimation = buildAnimation(COLOSSEUM_DOOR_PREFIX, COLOSSEUM_DOOR_FRAME_COUNT, COLOSSEUM_DOOR_SUFFIX,
            0, COLOSSEUM_DOOR_FRAME_COUNT - 1, COLOSSEUM_DOOR_FRAME_DURATION, COLOSSEUM_DOOR_OPEN_PLAYMODE);

        doorCloseAnimation = buildAnimation(COLOSSEUM_DOOR_PREFIX, COLOSSEUM_DOOR_FRAME_COUNT, COLOSSEUM_DOOR_SUFFIX,
            0, COLOSSEUM_DOOR_FRAME_COUNT - 1, COLOSSEUM_DOOR_FRAME_DURATION, COLOSSEUM_DOOR_CLOSE_PLAYMODE);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public List<String> getAssetPaths() {
        return assetPaths;
    }

    // --- Getters ---

    public Animation<TextureRegion> getDoorOpenAnimation() {
        return doorOpenAnimation;
    }

    public Animation<TextureRegion> getDoorCloseAnimation() {
        return doorCloseAnimation;
    }
}
