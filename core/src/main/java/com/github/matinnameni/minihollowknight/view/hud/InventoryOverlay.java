package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.controller.InventoryController;
import com.github.matinnameni.minihollowknight.model.Lang;
import com.github.matinnameni.minihollowknight.model.asset.CharmAssetBundle;
import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.CharmType;

/**
 * In-game inventory overlay.
 */
public class InventoryOverlay {

    // --- Layout constants ---
    private static final float DIM_ALPHA = 0.65f;
    private static final float TITLE_FONT_SCALE = 1.6f;
    private static final float BODY_FONT_SCALE = 1.0f;
    private static final float SMALL_FONT_SCALE = 0.85f;
    private static final float NOTCH_LABEL_FONT_SCALE = 1.1f;
    private static final float CLOSE_BUTTON_FONT_SCALE = 1.1f;

    private static final float CHARM_ICON_SIZE = 96f;
    private static final float CHARM_CELL_PAD = 12f;
    private static final float GRID_COLUMNS = 4;

    private static final float PANEL_WIDTH = 450f;
    private static final float CONTENT_PAD = 28f;
    private static final float SECTION_SPACE = 18f;

    // Equipped / selected highlight colors.
    private static final Color EQUIPPED_TINT = Color.DARK_GRAY;
    private static final Color SELECTED_TINT = Color.WHITE;
    private static final Color LOCKED_TINT = Color.BLACK;
    private static final Color NORMAL_TINT = Color.WHITE;

    // --- Dependencies ---
    private final InventoryController controller;
    private final CharmAssetBundle charmAssets;
    private final MenuAssetBundle menuAssets;
    private final Skin skin;

    // --- Scene2d ---
    private Stage stage;
    private ShapeRenderer dimRenderer;

    // --- Dynamic widgets ---
    private Table charmGridContainer;
    private Label nameLabel;
    private Label descriptionLabel;
    private Label notchStatusLabel;
    private TextButton closeButton;

    public InventoryOverlay(InventoryController controller,
                            CharmAssetBundle charmAssets,
                            MenuAssetBundle menuAssets) {
        this.controller = controller;
        this.charmAssets = charmAssets;
        this.menuAssets = menuAssets;
        this.skin = menuAssets.createSkin();
    }

    /** Builds the {@link Stage} and its actors. Call once before {@link #draw()}. */
    public void init() {
        stage = new Stage(new ScreenViewport());
        dimRenderer = new ShapeRenderer();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(CONTENT_PAD);
        root.defaults().space(SECTION_SPACE);

        // Title row
        root.add(buildHeaderRow()).growX().row();

        // Main content: charm grid (left) + description panel (right)
        Table body = new Table();
        body.defaults().top().space(CONTENT_PAD);
        body.add(buildCharmGridSection()).expand().fill();
        body.add(buildDescriptionPanel()).expand().fill().left().width(PANEL_WIDTH).growY();
        root.add(body).grow().row();

        // Footer: close button
        root.add(buildFooterRow()).growX().padTop(SECTION_SPACE);

        stage.addActor(root);

        // Initialize the description panel with the default selection.
        refreshSelection();
        refreshNotchStatus();
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
        if (stage != null) stage.dispose();
        if (dimRenderer != null) dimRenderer.dispose();
        if (skin != null) skin.dispose();
    }

    // ------------------------------------------------------------------
    //  Section builders
    // ------------------------------------------------------------------

    /** Title on the left, notch meter on the right. */
    private Table buildHeaderRow() {
        Table header = new Table();
        header.defaults().top();

        Label title = new Label(Lang.get("charm.inventory"), skin);
        title.setFontScale(TITLE_FONT_SCALE);
        header.add(title).left().expandX();

        notchStatusLabel = new Label("", skin);
        notchStatusLabel.setFontScale(NOTCH_LABEL_FONT_SCALE);
        header.add(notchStatusLabel).right();

        return header;
    }

    /** The scrollable grid of charm cells. */
    private Table buildCharmGridSection() {
        Table section = new Table();
        section.defaults().top();

        charmGridContainer = new Table();
        rebuildCharmGrid();

        section.add(charmGridContainer).expand().fill();
        return section;
    }

    /** Right-hand description panel. */
    private Table buildDescriptionPanel() {
        Table panel = new Table();
        panel.defaults().pad(CONTENT_PAD);
        panel.center();

        nameLabel = new Label("", skin);
        nameLabel.setFontScale(TITLE_FONT_SCALE);
        nameLabel.setColor(EQUIPPED_TINT);
        panel.add(nameLabel).growX().row();

        descriptionLabel = new Label("", skin);
        descriptionLabel.setFontScale(BODY_FONT_SCALE);
        descriptionLabel.setWrap(true);
        panel.add(descriptionLabel).growX().top().padTop(SECTION_SPACE).row();

        return panel;
    }

    private Table buildFooterRow() {
        Table footer = new Table();
        footer.defaults().center();

        closeButton = new TextButton(Lang.get("charm.close"), skin);
        closeButton.getLabel().setFontScale(CLOSE_BUTTON_FONT_SCALE);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onClose();
            }
        });
        footer.add(closeButton).right();

        return footer;
    }

    //  --- Charm grid ---

    /** Rebuilds every charm cell from scratch. Call whenever state changes. */
    private void rebuildCharmGrid() {
        charmGridContainer.clear();
        charmGridContainer.defaults().grow().expand().fill();

        Table allCharmsSegment = new Table();
        allCharmsSegment.defaults().space(CHARM_CELL_PAD);

        int column = 0;
        for (CharmType charm : controller.allCharms()) {
            Actor cell = buildCharmCell(charm);
            allCharmsSegment.add(cell).size(
                CHARM_ICON_SIZE + 2 * CHARM_CELL_PAD,
                CHARM_ICON_SIZE + 2 * CHARM_CELL_PAD + 24f
            );

            column++;
            if (column >= GRID_COLUMNS) {
                allCharmsSegment.row();
                column = 0;
            }
        }

        charmGridContainer.add(allCharmsSegment).right();
    }

    /** Builds a single charm cell. */
    private Actor buildCharmCell(final CharmType charm) {
        boolean collected = controller.isCollected(charm);
        boolean equipped = controller.isEquipped(charm);
        boolean selected = (controller.getSelectedCharm() == charm);

        Texture iconTex = charmAssets.getIcon(charm);
        Image icon;
        if (iconTex != null) {
            icon = new Image(new TextureRegion(iconTex));
        } else {
            // Defensive fallback: if the texture hasn't been loaded yet (e.g.
            // the bundle wasn't finished loading), render an empty cell.
            icon = new Image();
        }
        icon.setScaling(Scaling.fit);

        // Tint by state.
        if (!collected) {
            icon.setColor(LOCKED_TINT);
        } else if (equipped) {
            icon.setColor(EQUIPPED_TINT);
        } else if (selected) {
            icon.setColor(SELECTED_TINT);
        } else {
            icon.setColor(NORMAL_TINT);
        }

        // Wrap in a stack so we can overlay a lock label for uncollected charms.
        Stack stack = new Stack();

        Container<Image> iconWrap = new Container<>(icon);
        iconWrap.size(CHARM_ICON_SIZE, CHARM_ICON_SIZE).fill(true);

        stack.add(iconWrap);

        Table cell = new Table();
        cell.add(iconWrap).size(CHARM_ICON_SIZE, CHARM_ICON_SIZE).row();

        // Tint the whole cell when selected by drawing a colored background.
        if (selected) {
            cell.setBackground(makeSolidDrawable(SELECTED_TINT, 0.25f));
        } else if (collected && equipped) {
            cell.setBackground(makeSolidDrawable(EQUIPPED_TINT, 0.15f));
        }

        // Click handling
        cell.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (controller.getSelectedCharm() != charm) {
                    controller.onSelect(charm);
                } else {
                    controller.onToggleCharm(charm);
                }
                rebuildCharmGrid();
                refreshSelection();
                refreshNotchStatus();
            }
        });

        return cell;
    }

    //  --- Refresh helpers ---

    /** Updates the description panel to match the currently selected charm. */
    private void refreshSelection() {
        CharmType selected = controller.getSelectedCharm();
        if (selected == null) {
            nameLabel.setText("");
            nameLabel.setColor(Color.GRAY);
            descriptionLabel.setText("");
            return;
        }

        nameLabel.setText(controller.nameOf(selected));

        StringBuilder text = new StringBuilder();
        text.append(Lang.get("charm.cost")).append(": ").append(controller.notchCost(selected)).append("\n\n");
        text.append(controller.descriptionOf(selected)).append("\n\n");

        descriptionLabel.setText(text.toString());
    }

    /** Updates the "notches used / total" label in the header. */
    private void refreshNotchStatus() {
        notchStatusLabel.setText(Lang.get("charm.notches") + ": " + controller.usedNotches()
            + " / " + controller.totalNotches());
    }

    // --- Drawing helpers ---

    /** Draws the dimmed background that sits between the game world and the overlay. */
    private void drawDimOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        dimRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);
        dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dimRenderer.setColor(0f, 0f, 0f, DIM_ALPHA);
        dimRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        dimRenderer.end();
    }

    /** Builds a solid-color {@link Drawable} with the given tint and alpha. */
    private static Drawable makeSolidDrawable(Color color, float alpha) {
        // Build a 1x1 white texture once and reuse it via a static cache.
        return SolidDrawableCache.get(color, alpha);
    }

    /**
     * Tiny cache that hands out 1x1 white {@link Drawable}s tinted to a
     * requested color.
     */
    private static final class SolidDrawableCache {
        private static Texture whiteTexture;
        private static TextureRegionDrawable baseDrawable;
        private static final java.util.Map<String, Drawable> cache = new java.util.HashMap<>();

        static Drawable get(Color color, float alpha) {
            String key = color.r + "," + color.g + "," + color.b + "," + alpha;
            Drawable d = cache.get(key);
            if (d == null) {
                if (whiteTexture == null) {
                    whiteTexture = makeWhiteTexture();
                    baseDrawable = new TextureRegionDrawable(new TextureRegion(whiteTexture));
                }
                // tint() returns a new SpriteDrawable with the requested color
                // baked in, so it can be used directly as a Table background.
                d = baseDrawable.tint(new Color(color.r, color.g, color.b, alpha));
                cache.put(key, d);
            }
            return d;
        }

        private static Texture makeWhiteTexture() {
            com.badlogic.gdx.graphics.Pixmap pm =
                new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pm.setColor(Color.WHITE);
            pm.fill();
            Texture t = new Texture(pm);
            pm.dispose();
            return t;
        }
    }
}
