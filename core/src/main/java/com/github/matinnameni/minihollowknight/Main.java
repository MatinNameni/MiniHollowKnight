package com.github.matinnameni.minihollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.matinnameni.minihollowknight.view.navigator.UiManager;

public class Main extends Game {
    @Override
    public void create() {
        // Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        setCursor();
        UiManager.init(this);
        UiManager.getInstance().goToMainMenu();
    }

    @Override
    public void dispose() {
        super.dispose();
        UiManager uiManager = UiManager.getInstance();
        if (uiManager != null) {
            if (uiManager.getDatabase() != null) {
                uiManager.getDatabase().close();
            }
            uiManager.dispose();
        }
    }

    private void setCursor() {
        Pixmap originalPixmap = new Pixmap(Gdx.files.internal("sprites/Backend/Cursor.png"));

        int targetSize = 64;
        Pixmap scaledPixmap = new Pixmap(targetSize, targetSize, Pixmap.Format.RGBA8888);

        int offsetX = (targetSize - originalPixmap.getWidth()) / 2;
        int offsetY = (targetSize - originalPixmap.getHeight()) / 2;
        scaledPixmap.drawPixmap(originalPixmap, offsetX, offsetY);

        Cursor customCursor = Gdx.graphics.newCursor(scaledPixmap, offsetX, offsetY);
        Gdx.graphics.setCursor(customCursor);

        originalPixmap.dispose();
        scaledPixmap.dispose();
    }
}
