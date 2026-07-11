package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.controller.PauseMenuController;
import com.github.matinnameni.minihollowknight.model.localization.Lang;
import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;

/**
 * The in-game pause menu overlay.
 */
public class PauseOverlay {
    // Constants
    private static final float BUTTON_HEIGHT = 40f;
    private static final float BUTTON_SPACING = 16f;
    private static final float BUTTON_FONT_SCALE = 1.2f;
    private static final float TOP_FLEUR_WIDTH = 250f;
    private static final float BOTTOM_FLEUR_WIDTH = 180f;
    private static final float DIM_ALPHA = 0.6f;

    private final PauseMenuController controller;
    private final MenuAssetBundle assets;
    private final Skin skin;

    private Stage stage;
    private ShapeRenderer dimRenderer;

    public PauseOverlay(PauseMenuController controller, MenuAssetBundle assets) {
        this.controller = controller;
        this.assets = assets;
        this.skin = assets.createSkin();
    }

    /** Builds the {@link #stage} and its actors. */
    public void init() {
        stage = new Stage(new ScreenViewport());
        dimRenderer = new ShapeRenderer();

        // Root table
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        // Pause table
        Table pauseTable = new Table();
        pauseTable.center();
        pauseTable.defaults().height(BUTTON_HEIGHT).space(BUTTON_SPACING);

        pauseTable.add(buildTopFleur()).width(TOP_FLEUR_WIDTH).row();
        pauseTable.add(buildContinueButton()).row();
        pauseTable.add(buildOptionsButton()).row();
        pauseTable.add(buildQuitToMenuButton()).row();
        pauseTable.add(buildBottomFleur()).width(BOTTOM_FLEUR_WIDTH);

        // Cheat codes table
        Table cheatCodesTable = new Table();
        cheatCodesTable.defaults().height(BUTTON_HEIGHT).space(BUTTON_SPACING);

        cheatCodesTable.add(buildShowCheatCodesButton());

        rootTable.add(pauseTable).grow().fill().row();
        rootTable.add(cheatCodesTable).row();
        stage.addActor(rootTable);
    }

    public Stage getStage() {
        return stage;
    }

    public void update(float delta) {
        stage.act(delta);
    }

    public void draw() {
        drawDimOverlay();
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        dimRenderer.dispose();
    }

    // --- Buttons ---

    private Image buildTopFleur() {
        Image separator = new Image(assets.getPauseOverlayTopFleur());
        separator.setScaling(Scaling.fillX);
        return separator;
    }

    private TextButton buildContinueButton() {
        TextButton button = new TextButton(Lang.get("pause.continue"), skin);
        button.getLabel().setFontScale(BUTTON_FONT_SCALE);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onContinue();
            }
        });
        return button;
    }

    private TextButton buildOptionsButton() {
        TextButton button = new TextButton(Lang.get("pause.options"), skin);
        button.getLabel().setFontScale(BUTTON_FONT_SCALE);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onOptions();
            }
        });
        return button;
    }

    private TextButton buildQuitToMenuButton() {
        TextButton button = new TextButton(Lang.get("pause.quitToMenu"), skin);
        button.getLabel().setFontScale(BUTTON_FONT_SCALE);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onQuitToMenu();
            }
        });
        return button;
    }

    private Image buildBottomFleur() {
        Image separator = new Image(assets.getPauseOverlayBottomFleur());
        separator.setScaling(Scaling.fillX);
        return separator;
    }

    private TextButton buildShowCheatCodesButton() {
        TextButton button = new TextButton(Lang.get("pause.showCheatCodes"), skin);
        button.getLabel().setFontScale(BUTTON_FONT_SCALE);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onShowCheatCodes();
            }
        });
        return button;
    }

    // --- Helpers ---

    /** Draws a semi-transparent black rectangle covering the whole screen. */
    private void drawDimOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        dimRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);
        dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dimRenderer.setColor(0f, 0f, 0f, DIM_ALPHA);
        dimRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        dimRenderer.end();
    }
}
