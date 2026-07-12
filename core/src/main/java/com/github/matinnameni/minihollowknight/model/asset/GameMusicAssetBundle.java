package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the background-music tracks used during gameplay
 */
public class GameMusicAssetBundle extends AssetBundle {

    public static final String KEY = "gameMusic";

    // --- Asset paths ---
    private static final String CROSSROADS_MUSIC = "Audio_Files/S19 Crossroads Main.wav";
    private static final String GREENPATH_MUSIC = "Audio_Files/S5 Green Path Main.wav";
    private static final String FALSE_KNIGHT_MUSIC = "Audio_Files/04. False Knight.mp3";
    private static final String END_GAME_MUSIC = "Audio_Files/Royal_HollowKnight_Theme.wav";

    private final List<String> assetPaths = new ArrayList<>();

    public GameMusicAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void queue() {
        manager.load(CROSSROADS_MUSIC, Music.class);
        assetPaths.add(CROSSROADS_MUSIC);
        manager.load(GREENPATH_MUSIC, Music.class);
        assetPaths.add(GREENPATH_MUSIC);
        manager.load(FALSE_KNIGHT_MUSIC, Music.class);
        assetPaths.add(FALSE_KNIGHT_MUSIC);
        manager.load(END_GAME_MUSIC, Music.class);
        assetPaths.add(END_GAME_MUSIC);
    }

    @Override
    public void onLoaded() {
        // Nothing extra to build after loading.
    }

    @Override
    public List<String> getAssetPaths() {
        return assetPaths;
    }

    // --- Accessors ---

    public Music getCrossroadsMusic() {
        return manager.get(CROSSROADS_MUSIC, Music.class);
    }

    public Music getGreenpathMusic() {
        return manager.get(GREENPATH_MUSIC, Music.class);
    }

    public Music getFalseKnightMusic() {
        return manager.get(FALSE_KNIGHT_MUSIC, Music.class);
    }

    public Music getEndGameMusic() {
        return manager.get(END_GAME_MUSIC, Music.class);
    }
}
