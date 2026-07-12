package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.github.matinnameni.minihollowknight.controller.CheatCodeRegistry;
import com.github.matinnameni.minihollowknight.controller.GuideController;
import com.github.matinnameni.minihollowknight.model.asset.AssetRegistry;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.localization.Lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Guide screen.
 */
public class GuideScreen extends AbstractScreen {

    // --- Layout constants ---

    private static final float CONTENT_WIDTH = 600f;
    private static final float TITLE_FONT_SCALE = 1.5f;
    private static final float SECTION_TITLE_FONT_SCALE = 1.25f;
    private static final float ROW_HEIGHT = 36f;
    private static final float ROW_SPACING = 8f;
    private static final float SECTION_WIDTH = 500f;
    private static final float SECTION_SPACING = 28f;
    private static final float KEY_CHIP_WIDTH = 110f;
    private static final float KEY_CHIP_HEIGHT = 32f;
    private static final float PAD_TOP = 40f;
    private static final float PAD_BOTTOM = 30f;
    private static final float BACK_BUTTON_WIDTH = 220f;
    private static final float BACK_BUTTON_HEIGHT = 40f;
    private static final float SCROLL_BODY_MAX_HEIGHT_RATIO = 0.70f;
    private static final int DEFAULT_STARTING_MASKS = 5;

    private final Settings settings;
    private final GuideController controller;

    private final List<CheatCodeRegistry.CheatCode> cheats;

    public GuideScreen(AssetRegistry registry, Settings settings, GuideController controller) {
        super(registry);
        this.settings = settings;
        this.controller = controller;
        this.cheats = new ArrayList<>(new CheatCodeRegistry().all());
    }

    @Override
    public void show() {
        super.show();

        addBackground();

        stage.setDebugAll(false);

        Table root = new Table();
        root.setFillParent(true);
        root.top().center();
        root.defaults().center();

        root.add(buildTitle()).padTop(PAD_TOP).row();
        root.add(buildSeparator()).width(CONTENT_WIDTH).padTop(8f).padBottom(4f).row();
        root.add(buildScrollableBody())
            .width(CONTENT_WIDTH)
            .maxHeight(stage.getHeight() * SCROLL_BODY_MAX_HEIGHT_RATIO)
            .padTop(8f)
            .padBottom(12f)
            .grow()
            .row();
        root.add(buildBackButton())
            .expand()
            .height(BACK_BUTTON_HEIGHT)
            .padBottom(PAD_BOTTOM)
            .row();

        stage.addActor(root);
    }

    // --- Background / title / separator ---

    /** Sets the menu background image. */
    private void addBackground() {
        Image background = new Image(menuAssets().getBackground());
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);
    }

    /** Builds the screen title. */
    private Label buildTitle() {
        Label title = new Label(Lang.get("guide.title"), skin, "title");
        title.setAlignment(Align.center);
        title.setFontScale(TITLE_FONT_SCALE);
        return title;
    }

    /** Builds the separator under the title. */
    private Image buildSeparator() {
        Image separator = new Image(menuAssets().getSeparator());
        separator.setScaling(Scaling.fillX);
        return separator;
    }

    /** Builds the separator used bottom of each section. */
    private Image buildSectionsSeparator() {
        Image separator = new Image(menuAssets().getPauseOverlayBottomFleur());
        separator.setScaling(Scaling.fillY);
        return separator;
    }

    // --- Scrollable body ---

    /** Builds the scroll pane that hosts the three guide sections. */
    private ScrollPane buildScrollableBody() {
        Table body = new Table();
        body.top().center();
        body.defaults().width(CONTENT_WIDTH).center();

        body.add(buildControlsSection()).padBottom(SECTION_SPACING).row();
        body.add(buildSectionsSeparator()).width(CONTENT_WIDTH).height(50f).padTop(2f).padBottom(SECTION_SPACING).row();
        body.add(buildAbilitiesSection()).padBottom(SECTION_SPACING).row();
        body.add(buildSectionsSeparator()).width(CONTENT_WIDTH).height(50f).padTop(2f).padBottom(SECTION_SPACING).row();
        body.add(buildCheatsSection()).padBottom(SECTION_SPACING).row();

        ScrollPane scroll = new ScrollPane(body, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.getStyle().background = null;
        return scroll;
    }

    // --- Section 1: Controls ---

    /**
     * Builds the Controls section.
     */
    private Table buildControlsSection() {
        Table section = new Table();
        section.top().center();

        section.add(buildSectionTitle(Lang.get("guide.section.controls"))).growX().center().padBottom(20f).row();
        section.add(buildControlsTable()).growX().row();

        return section;
    }

    /** Builds the 2-column table of action and current key binding. */
    private Table buildControlsTable() {
        Table table = new Table();
        table.defaults().width(SECTION_WIDTH).space(ROW_SPACING).height(ROW_HEIGHT).center();

        table.add(buildActionRow(Lang.get("keyBindings.left"), settings.getKeyLeft())).row();
        table.add(buildActionRow(Lang.get("keyBindings.right"), settings.getKeyRight())).row();
        table.add(buildActionRow(Lang.get("keyBindings.up"), settings.getKeyUp())).row();
        table.add(buildActionRow(Lang.get("keyBindings.down"), settings.getKeyDown())).row();
        table.add(buildActionRow(Lang.get("keyBindings.jump"), settings.getKeyJump())).row();
        table.add(buildActionRow(Lang.get("keyBindings.attack"), settings.getKeyAttack())).row();
        table.add(buildActionRow(Lang.get("keyBindings.dash"), settings.getKeyDash())).row();
        table.add(buildActionRow(Lang.get("keyBindings.focus"), settings.getKeyFocus())).row();
        table.add(buildActionRow(Lang.get("keyBindings.cast"), settings.getKeyCast())).row();
        table.add(buildActionRow(Lang.get("keyBindings.interact"), settings.getKeyInteract())).row();
        table.add(buildActionRow(Lang.get("keyBindings.inventory"), settings.getKeyInventory())).row();
        table.add(buildActionRow(Lang.get("keyBindings.pause"), settings.getKeyPause())).row();

        return table;
    }

    private Table buildActionRow(String actionLabel, int keycode) {
        Table row = new Table();

        Label label = new Label(actionLabel, skin);
        label.setColor(Color.WHITE);
        label.setAlignment(Align.left);

        TextButton keyChip = buildKeyChip(Input.Keys.toString(keycode));

        row.add(label).grow().expand().fill().left().padRight(12f);
        row.add(keyChip).width(KEY_CHIP_WIDTH).height(KEY_CHIP_HEIGHT).right().padLeft(12f);
        return row;
    }

    private TextButton buildKeyChip(String keyName) {
        TextButton chip = new TextButton(keyName, skin, "KeyBindings");
        chip.setDisabled(true);
        chip.setColor(Color.WHITE);
        chip.getLabel().setAlignment(Align.center);
        return chip;
    }

    // --- Section 2: Abilities & Knight ---

    /** Builds the "Abilities & Knight" section. */
    private Table buildAbilitiesSection() {
        Table section = new Table();
        section.top().center();
        section.defaults().width(SECTION_WIDTH).center();

        section.add(buildSectionTitle(Lang.get("guide.section.abilities")))
            .center().padBottom(20f).row();

        Table list = new Table();
        list.defaults().space(ROW_SPACING).width(SECTION_WIDTH).center();

        // HP / Masks
        list.add(buildInfoRow(
            Lang.get("guide.ability.masks.name"),
            Lang.format("guide.ability.masks.desc", DEFAULT_STARTING_MASKS)
        )).row();

        // Soul Vessel
        list.add(buildInfoRow(
            Lang.get("guide.ability.soulVessel.name"),
            Lang.format("guide.ability.soulVessel.desc",
                (int) Knight.SLASH_SOUL_GAIN,
                (int) GameData.MAX_SOUL)
        )).row();

        // Focus / Heal
        list.add(buildInfoRow(
            Lang.get("guide.ability.focus.name"),
            Lang.format("guide.ability.focus.desc",
                Knight.FOCUS_CHANNEL_TIME,
                (int) Knight.FOCUS_SOUL_COST)
        )).row();

        // Vengeful Spirit
        list.add(buildInfoRow(
            Lang.get("guide.ability.vengefulSpirit.name"),
            Lang.format("guide.ability.vengefulSpirit.desc",
                (int) Knight.VENGEFUL_SPIRIT_SOUL_COST,
                (int) Knight.VENGEFUL_SPIRIT_DAMAGE)
        )).row();

        // Howling Wraiths
        list.add(buildInfoRow(
            Lang.get("guide.ability.howlingWraiths.name"),
            Lang.format("guide.ability.howlingWraiths.desc",
                (int) Knight.VENGEFUL_SPIRIT_SOUL_COST,
                (int) Knight.HOWLING_WRAITHS_DAMAGE)
        )).row();

        // Dash
        list.add(buildInfoRow(
            Lang.get("guide.ability.dash.name"),
            Lang.get("guide.ability.dash.desc")
        )).row();

        // Double Jump
        list.add(buildInfoRow(
            Lang.get("guide.ability.doubleJump.name"),
            Lang.get("guide.ability.doubleJump.desc")
        )).row();

        // Wall Slide / Wall Jump
        list.add(buildInfoRow(
            Lang.get("guide.ability.wallSlide.name"),
            Lang.get("guide.ability.wallSlide.desc")
        )).row();

        // Pogo Jump
        list.add(buildInfoRow(
            Lang.get("guide.ability.pogo.name"),
            Lang.get("guide.ability.pogo.desc")
        )).row();

        section.add(list).growX().row();
        return section;
    }

    // --- Section 3: Cheat Codes ---

    /** Builds the "Cheat Codes" section, listing every registered cheat. */
    private Table buildCheatsSection() {
        Table section = new Table();
        section.top().center();
        section.defaults().width(SECTION_WIDTH).center();

        section.add(buildSectionTitle(Lang.get("guide.section.cheats")))
            .center().padBottom(20f).row();

        Table list = new Table();
        list.defaults().space(ROW_SPACING).width(SECTION_WIDTH).center();

        for (CheatCodeRegistry.CheatCode cheat : cheats) {
            list.add(buildCheatRow(cheat)).row();
        }

        section.add(list).growX().row();
        return section;
    }

    /** Builds a single cheat row */
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

    // --- Helpers ---

    /** Builds a generic "name on the left, description on the right" row. */
    private Table buildInfoRow(String name, String description) {
        Table row = new Table();
        row.defaults().top().left();

        Label nameLabel = new Label(name, skin);
        nameLabel.setColor(Color.WHITE);
        nameLabel.setAlignment(Align.left);

        Label descLabel = new Label(description, skin);
        descLabel.setWrap(true);
        descLabel.setColor(Color.WHITE);
        descLabel.setAlignment(Align.left);

        row.add(nameLabel).width(200f).top().padRight(12f).padTop(6f);
        row.add(descLabel).growX().fillX().padLeft(12f).padTop(6f);
        return row;
    }

    /** Builds a section title label. */
    private Label buildSectionTitle(String text) {
        Label label = new Label(text, skin);
        label.setColor(Color.WHITE);
        label.setFontScale(SECTION_TITLE_FONT_SCALE);
        label.setAlignment(Align.center);
        return label;
    }

    // --- Navigation ---

    /** Builds the back button that returns to the main menu. */
    private TextButton buildBackButton() {
        TextButton button = new TextButton(Lang.get("guide.back"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                controller.onBack();
            }
        });
        return button;
    }

    // --- Helpers ---

    private static String cheatDescriptionKey(String cheatName) {
        switch (cheatName) {
            case "Boss Arena Teleport":return "guide.cheat.bossTeleport.desc";
            case "Noclip":return "guide.cheat.noclip.desc";
            case "Emergency Heal":return "guide.cheat.emergencyHeal.desc";
            case "Refill Soul Vessel":return "guide.cheat.refillSoul.desc";
            case "God Mode":return "guide.cheat.godMode.desc";
            case "Insta-Kill":return "guide.cheat.instaKill.desc";
            default: return "guide.cheat.unknown.desc";
        }
    }
}
