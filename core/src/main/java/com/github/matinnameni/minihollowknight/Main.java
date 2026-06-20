package com.github.matinnameni.minihollowknight;

import com.badlogic.gdx.Game;
import com.github.matinnameni.minihollowknight.view.UiManager;

public class Main extends Game {
    @Override
    public void create() {
        UiManager.init(this);
        UiManager.getInstance().goToMainMenu();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (UiManager.getInstance() != null && UiManager.getInstance().getDatabase() != null) {
            UiManager.getInstance().getDatabase().close();
        }
    }
}
