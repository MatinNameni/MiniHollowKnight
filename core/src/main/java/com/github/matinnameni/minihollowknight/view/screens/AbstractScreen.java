package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.model.GameAssets;

/**
 * Base class for all screens.
 */
public abstract class AbstractScreen implements Screen {
    protected final GameAssets assets;
    protected Stage stage;
    protected Skin skin;

    public AbstractScreen(GameAssets assets) {
        this.assets = assets;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = assets.createSkin();
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        if (stage != null) {
            stage.clear();
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
