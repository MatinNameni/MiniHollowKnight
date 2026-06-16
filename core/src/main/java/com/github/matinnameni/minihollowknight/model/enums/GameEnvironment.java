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
}
