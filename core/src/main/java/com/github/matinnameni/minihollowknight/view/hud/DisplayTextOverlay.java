package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;

public class DisplayTextOverlay {
    // --- Constants ---

    private static final float DISPLAY_HOLDUP = 2.5f;
    private static final float DISPLAY_DURATION = 3.5f;
    private static final float FONT_SCALE = 2.5f;
    private static final float LABEL_PAD_BOTTOM = 50f;
    private static final float LABEL_PAD_RIGHT = 80f;

    // --- State ---
    private String bodyText;
    private boolean isDisplaying = false;

    // --- Stage ---
    private Stage stage;
    private final Skin skin;
    private Label displayLabel;

    // --- Timers ---
    private float holdupTime = 0f;
    private float displayTime = 0f;

    // --- Dependencies ---
    private MenuAssetBundle assets;

    public DisplayTextOverlay(MenuAssetBundle assets) {
        this.assets = assets;
        this.skin = assets.createSkin();
    }

    /** Builds the {@link #stage} and its actors. */
    public void init() {
        stage = new Stage(new ScreenViewport());

        // Root table
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.defaults().expand();

        rootTable.add(buildDisplayLabel())
            .bottom()
            .right()
            .padBottom(LABEL_PAD_BOTTOM)
            .padRight(LABEL_PAD_RIGHT);

        stage.addActor(rootTable);
    }

    public void update(float delta) {
        holdupTime += delta;

        if (holdupTime > DISPLAY_HOLDUP) {
            displayTime += delta;
        }

        isDisplaying = (displayTime <= DISPLAY_DURATION) && (displayTime >= 0);

        if(isDisplaying) {
            stage.act(delta);
        }
    }

    public void draw() {
        if (holdupTime <= DISPLAY_HOLDUP) {
            return;
        }

        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    public void startDisplay() {
        isDisplaying = true;
    }

    // --- Display label ---

    private Label buildDisplayLabel() {
        displayLabel = new Label(bodyText, skin);
        displayLabel.setFontScale(FONT_SCALE);
        return displayLabel;
    }

    // --- Getters ---

    public String getBodyText() {
        return bodyText;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isDisplaying() {
        return isDisplaying;
    }

    // --- Setters ---

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;

        if (stage != null && displayLabel != null) {
            displayLabel.setText(bodyText != null ? bodyText : "");
        }
    }
}
