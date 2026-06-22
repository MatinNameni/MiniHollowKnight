package com.github.matinnameni.minihollowknight.model.enums;

public enum GameEnvironment {
    FORGOTTEN_CROSSROADS(1, "Forgotton Crossroads"),
    GREENPATH(2, "Greenpath");

    public final int id;
    public final String name;

    GameEnvironment(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /** Returns the environment matching the given id, or null if not found. */
    public static GameEnvironment fromId(int id) {
        for (GameEnvironment environment : values()) {
            if (environment.id == id) return environment;
        }
        return null;
    }
}
