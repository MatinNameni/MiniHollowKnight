package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.model.asset.AssetRegistry;
import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.view.navigator.UiManager;

/**
 * Base class for all screens.
 */
public abstract class AbstractScreen implements Screen {
    protected final AssetRegistry registry;
    protected Stage stage;
    protected Skin skin;
    private ShapeRenderer overlayRenderer;

    public AbstractScreen(AssetRegistry registry) {
        this.registry = registry;
    }

    /** Accessor for the menu bundle. */
    protected MenuAssetBundle menuAssets() {
        return (MenuAssetBundle) registry.get(MenuAssetBundle.KEY);
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = menuAssets().createSkin();
        Gdx.input.setInputProcessor(stage);
        overlayRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
        drawBrightnessOverlay();
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
        overlayRenderer.dispose();
    }

    /**
     * Draws a semi-transparent black overlay on top of everything to simulate lower brightness.
     */
    private void drawBrightnessOverlay() {
        UiManager uiManager = UiManager.getInstance();
        if (uiManager == null) return;

        Settings settings = uiManager.getSettings();
        if (settings == null) return;

        float brightness = settings.getBrightness();
        if (brightness >= 0.99f) return;

        float alpha = 1f - brightness;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        overlayRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);
        overlayRenderer.begin(ShapeRenderer.ShapeType.Filled);
        overlayRenderer.setColor(0, 0, 0, alpha);
        overlayRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        overlayRenderer.end();
    }
}
