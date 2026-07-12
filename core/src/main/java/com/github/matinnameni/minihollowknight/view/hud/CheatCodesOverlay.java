package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.controller.CheatCodeRegistry;
import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;
import com.github.matinnameni.minihollowknight.model.localization.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * In-game overlay that lists every registered cheat code.
 */
public class CheatCodesOverlay {

    // --- Layout constants ---

    private static final float CONTENT_WIDTH = 600f;
    private static final float TITLE_FONT_SCALE = 1.6f;
    private static final float ROW_SPACING = 8f;
    private static final float SECTION_SPACING = 28f;
    private static final float KEY_CHIP_WIDTH = 110f;
    private static final float KEY_CHIP_HEIGHT = 32f;
    private static final float PAD_TOP = 40f;
    private static final float PAD_BOTTOM = 30f;
    private static final float BACK_BUTTON_WIDTH = 220f;
    private static final float BACK_BUTTON_HEIGHT = 40f;
    private static final float SCROLL_BODY_MAX_HEIGHT_RATIO = 0.70f;
    private static final float DIM_ALPHA = 1f;

    private final MenuAssetBundle assets;
    private final Skin skin;
    private final Runnable onClose;

    private final List<CheatCodeRegistry.CheatCode> cheats;

    private Stage stage;
    private ShapeRenderer dimRenderer;

    public CheatCodesOverlay(MenuAssetBundle assets, Runnable onClose) {
        this.assets = assets;
        this.skin = assets.createSkin();
        this.onClose = onClose;
        this.cheats = new ArrayList<>(new CheatCodeRegistry().all());
    }

    public void init() {
        stage = new Stage(new ScreenViewport());
        dimRenderer = new ShapeRenderer();

        Table root = new Table();
        root.setFillParent(true);
        root.top().center();
        root.defaults().center();

        root.add(buildTitle()).padTop(PAD_TOP).row();
        root.add(buildSeparator()).width(CONTENT_WIDTH).padTop(8f).padBottom(4f).row();
        root.add(buildScrollableBody())
            .width(CONTENT_WIDTH)
            .maxHeight(stage.getHeight() * SCROLL_BODY_MAX_HEIGHT_RATIO)
            .padTop(8f).padBottom(12f).grow().row();
        root.add(buildBackButton())
            .expand()
            .height(BACK_BUTTON_HEIGHT)
            .padBottom(PAD_BOTTOM).row();

        stage.addActor(root);
    }

    public Stage getStage() { return stage; }

    public void update(float delta) { stage.act(delta); }

    public void draw() {
        drawDimOverlay();
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        if (stage != null) stage.dispose();
        if (dimRenderer != null) dimRenderer.dispose();
        if (skin != null) skin.dispose();
    }

    private Label buildTitle() {
        Label title = new Label(Lang.get("guide.section.cheats"), skin, "title");
        title.setAlignment(Align.center);
        title.setFontScale(TITLE_FONT_SCALE);
        return title;
    }

    private Image buildSeparator() {
        Image separator = new Image(assets.getSeparator());
        separator.setScaling(Scaling.fillX);
        return separator;
    }

    private ScrollPane buildScrollableBody() {
        Table body = new Table();
        body.top().center();
        body.defaults().width(CONTENT_WIDTH).center();
        body.add(buildCheatsSection()).padBottom(SECTION_SPACING).row();

        ScrollPane scroll = new ScrollPane(body, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.getStyle().background = null;
        return scroll;
    }

    private Table buildCheatsSection() {
        Table section = new Table();
        section.top().center();
        section.defaults().width(CONTENT_WIDTH).center();

        Table list = new Table();
        list.defaults().space(ROW_SPACING).width(CONTENT_WIDTH).center();

        for (CheatCodeRegistry.CheatCode cheat : cheats) {
            list.add(buildCheatRow(cheat)).row();
        }

        section.add(list).growX().row();
        return section;
    }

    private Table buildCheatRow(CheatCodeRegistry.CheatCode cheat) {
        Table row = new Table();
        row.defaults().width(300f).top().left();

        // Left column
        Table left = new Table();
        left.defaults().left().space(8f);
        TextButton keyChip = buildKeyChip("Ctrl + " + Input.Keys.toString(cheat.keyCode));
        left.add(keyChip).width(KEY_CHIP_WIDTH + 30f).height(KEY_CHIP_HEIGHT).growX().left().row();

        // Right column
        Label desc = new Label(Lang.get(cheatDescriptionKey(cheat.name)), skin);
        desc.setWrap(true);
        desc.setColor(Color.WHITE);
        desc.setAlignment(Align.center);

        row.add(left).width(220f).top().padRight(12f).padTop(8f);
        row.add(desc).growX().fillX().padLeft(12f).padTop(8f);
        return row;
    }

    private TextButton buildKeyChip(String keyName) {
        TextButton chip = new TextButton(keyName, skin, "KeyBindings");
        chip.setDisabled(true);
        chip.setColor(Color.WHITE);
        chip.getLabel().setAlignment(Align.center);
        return chip;
    }

    private TextButton buildBackButton() {
        TextButton button = new TextButton(Lang.get("guide.back"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClose.run();
            }
        });
        return button;
    }

    private static String cheatDescriptionKey(String cheatName) {
        switch (cheatName) {
            case "Boss Arena Teleport": return "guide.cheat.bossTeleport.desc";
            case "Noclip": return "guide.cheat.noclip.desc";
            case "Emergency Heal": return "guide.cheat.emergencyHeal.desc";
            case "Refill Soul Vessel": return "guide.cheat.refillSoul.desc";
            case "God Mode": return "guide.cheat.godMode.desc";
            case "Insta-Kill": return "guide.cheat.instaKill.desc";
            default: return "guide.cheat.unknown.desc";
        }
    }

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
