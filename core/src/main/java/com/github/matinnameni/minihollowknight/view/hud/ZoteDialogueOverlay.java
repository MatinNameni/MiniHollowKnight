package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.controller.ZoteController;
import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;
import com.github.matinnameni.minihollowknight.model.localization.Lang;

/**
 * In-game dialogue overlay for the Zote NPC.
 */
public class ZoteDialogueOverlay {

    // --- Layout (screen-space, in pixels) ---

    private static final float BOX_WIDTH_RATIO = 0.70f;
    private static final float BOX_HEIGHT = 180f;
    private static final float BOX_MARGIN_BOTTOM = 40f;
    private static final float BOX_PAD = 20f;

    private static final float NAME_LABEL_FONT_SCALE = 1.5f;
    private static final float BODY_LABEL_FONT_SCALE = 1.0f;
    private static final float HINT_FONT_SCALE = 0.85f;

    private static final float PROMPT_FONT_SCALE = 1.0f;

    private static final float DIM_ALPHA = 0.45f;

    // --- Colors ---

    private static final Color BOX_FILL_COLOR = new Color(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0.85f);
    private static final Color BODY_COLOR = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.85f);
    private static final Color HINT_COLOR = new Color(0.7f, 0.7f, 0.7f, 1f);
    private static final Color PROMPT_COLOR = new Color(1f, 1f, 1f, 0.95f);

    // --- Dependencies ---

    private final MenuAssetBundle menuAssets;
    private final Skin skin;
    private final ZoteController controller;

    // --- Scene2d ---

    private Stage stage;
    private Label nameLabel;
    private Label bodyLabel;
    private Label promptLabel;
    private Table dialogueRoot;
    private ShapeRenderer boxRenderer;

    public ZoteDialogueOverlay(MenuAssetBundle menuAssets, ZoteController controller) {
        this.menuAssets = menuAssets;
        this.skin = menuAssets.createSkin();
        this.controller = controller;
    }

    /** Builds the stage actors. Call once before {@link #draw()}. */
    public void init() {
        if (stage == null) {
            stage = new Stage(new ScreenViewport());
        } else {
            stage.clear();
        }
        if (boxRenderer == null) {
            boxRenderer = new ShapeRenderer();
        }

        // --- Dialogue box ---

        dialogueRoot = new Table();
        dialogueRoot.setFillParent(true);
        dialogueRoot.bottom().center();
        dialogueRoot.defaults().center();
        dialogueRoot.align(Align.bottom | Align.center);

        nameLabel = new Label(Lang.get("zote.name"), skin);
        nameLabel.setFontScale(NAME_LABEL_FONT_SCALE);
        nameLabel.setColor(Color.WHITE);
        dialogueRoot.add(nameLabel).left().padLeft(10f).padBottom(10f).row();

        Table box = new Table();
        box.defaults().pad(BOX_PAD).left();

        bodyLabel = new Label("", skin);
        bodyLabel.setFontScale(BODY_LABEL_FONT_SCALE);
        bodyLabel.setColor(BODY_COLOR);
        bodyLabel.setWrap(true);
        bodyLabel.setAlignment(Align.left | Align.top);
        box.add(bodyLabel).growX().row();

        float boxWidth = stage.getWidth() * BOX_WIDTH_RATIO;
        dialogueRoot.add(box).width(boxWidth).height(BOX_HEIGHT).padBottom(BOX_MARGIN_BOTTOM);
        dialogueRoot.setVisible(false);
        stage.addActor(dialogueRoot);

        // --- Interact prompt ---

        promptLabel = new Label(Lang.get("zote.interactPrompt"), skin);
        promptLabel.setFontScale(PROMPT_FONT_SCALE);
        promptLabel.setColor(PROMPT_COLOR);
        promptLabel.setAlignment(Align.center);
        promptLabel.setVisible(false);
        stage.addActor(promptLabel);
    }

    public Stage getStage() {
        return stage;
    }

    public void setInteractPromptPosition(float screenX, float screenY) {
        if (promptLabel == null) return;
        promptLabel.setPosition(
            screenX - promptLabel.getWidth() / 2f,
            screenY - promptLabel.getHeight() / 2f
        );
    }

    public void update(float delta) {
        if (stage == null) return;
        stage.act(delta);

        // --- Dialogue box visibility + body text ---
        boolean dialogueOpen = controller.isDialogueOpen();
        dialogueRoot.setVisible(dialogueOpen);
        if (dialogueOpen) {
            bodyLabel.setText(controller.getVisibleText());
        }

        // --- Interact prompt visibility ---
        promptLabel.setVisible(controller.shouldShowInteractPrompt());
    }

    /** Draws the dialogue box and prompt (if visible). */
    public void draw() {
        if (stage == null) return;

        if (controller.isDialogueOpen()) {
            drawDimBackground();
            drawBoxBackground();
        }

        stage.draw();
    }

    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
            init();
        }
    }

    public void dispose() {
        if (stage != null) stage.dispose();
        if (boxRenderer != null) boxRenderer.dispose();
        if (skin != null) skin.dispose();
    }

    // --- Drawing helpers ---

    /** Dims the world behind the dialogue box. */
    private void drawDimBackground() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        boxRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);
        boxRenderer.begin(ShapeRenderer.ShapeType.Filled);
        boxRenderer.setColor(0f, 0f, 0f, DIM_ALPHA);
        boxRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        boxRenderer.end();
    }

    /** Draws the decorative box behind the dialogue text. */
    private void drawBoxBackground() {
        float boxWidth = Gdx.graphics.getWidth() * BOX_WIDTH_RATIO;
        float boxX = (Gdx.graphics.getWidth() - boxWidth) / 2f;
        float boxY = BOX_MARGIN_BOTTOM;
        float boxH = BOX_HEIGHT;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        boxRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);
        boxRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fill
        boxRenderer.setColor(BOX_FILL_COLOR);
        boxRenderer.rect(boxX, boxY, boxWidth, boxH);

        boxRenderer.end();
    }
}
