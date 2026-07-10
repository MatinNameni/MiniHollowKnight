package com.github.matinnameni.minihollowknight.model.enums;

public enum GameEnvironment {
    FORGOTTEN_CROSSROADS(1, "Forgotten Crossroads", "maps/forgotten_crossroads/spawnPoint.tmx"),
    GREENPATH(2, "Greenpath", "maps/greenpath/greenpath.tmx"),;

    public final int id;
    public final String name;
    public final String path;

    GameEnvironment(int id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    /** Returns the environment matching the given id, or null if not found. */
    public static GameEnvironment fromId(int id) {
        for (GameEnvironment environment : values()) {
            if (environment.id == id) return environment;
        }
        return null;
    }
}
