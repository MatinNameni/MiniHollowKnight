package com.github.matinnameni.minihollowknight;

import com.badlogic.gdx.Game;
import com.github.matinnameni.minihollowknight.view.UiManager;

public class Main extends Game {
    @Override
    public void create() {
        UiManager.init(this);
        UiManager.getInstance().goToMainMenu();
    }
}
