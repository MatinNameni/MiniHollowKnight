package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.matinnameni.minihollowknight.controller.StartGameController;
import com.github.matinnameni.minihollowknight.model.GameAssets;
import com.github.matinnameni.minihollowknight.model.GameData;
import com.github.matinnameni.minihollowknight.model.Lang;
import com.github.matinnameni.minihollowknight.model.enums.GameEnvironment;

/**
 * Start-game screen
 */
public class StartGameScreen extends AbstractScreen {
    private static final float SLOT_WIDTH  = 800f;
    private static final float SLOT_HEIGHT = 100f;
    private static final float SLOT_SPACING = 20f;
    private static final float SLOT_PAD_TOP = 40f;
    private static final float TITLE_FONT_SCALE = 1.6f;
    private static final float SLOT_NUM_FONT_SCALE = 1.4f;
    private static final float AREA_FONT_SCALE = 1.0f;
    private static final float DETAIL_FONT_SCALE = 0.75f;
    private static final float RESET_BUTTON_WIDTH = 200f;
    private static final float RESET_BUTTON_SPACING = 50f;
    private static final float RESET_BUTTON_HEIGHT = 32f;
    private static final float BACK_BUTTON_HEIGHT = RESET_BUTTON_HEIGHT;
    private static final float FLEUR_WIDTH = SLOT_WIDTH;
    private static final float FLEUR_HEIGHT = SLOT_HEIGHT;
    private static final float FLEUR_OVERLAP = 40f;
    private static final float SOUL_BAR_SIZE = 120f;
    private static final float MASK_ICON_SIZE = 25f;
    private static final float SOUL_BAR_OVERLAP = 40f;
    private static final float MASK_ICON_OVERLAP = 30f;



    private final StartGameController controller;

    public StartGameScreen(GameAssets assets, StartGameController controller) {
        super(assets);
        this.controller = controller;
    }

    @Override
    public void show() {
        super.show();

        // Reload slot data every time the screen is shown
        controller.loadAllSlots();

        addBackground();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().center();

        rootTable.add(buildTitle()).padTop(SLOT_PAD_TOP).row();
        rootTable.add(buildSeparator()).width(SLOT_WIDTH).row();

        // Save slots
        for (int slotId = 1; slotId <= StartGameController.TOTAL_SLOTS; slotId++) {
            rootTable.add(buildSlotRow(slotId))
                .width(SLOT_WIDTH)
                .height(SLOT_HEIGHT)
                .space(SLOT_SPACING)
                .row();
        }

        rootTable.add(buildBackButton()).height(BACK_BUTTON_HEIGHT).padTop(24f).row();

        stage.addActor(rootTable);
    }
    // --- Helpers ---

    /** Sets start-game background image */
    private void addBackground() {
        Image background = new Image(assets.getBackground());
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);
    }

    /** Builds the label that shows menu title */
    private Label buildTitle() {
        Label title = new Label(Lang.get("startGame.title"), skin, "title");
        title.setAlignment(Align.center);
        title.setFontScale(TITLE_FONT_SCALE);
        return title;
    }

    /** Builds the separator under the title. */
    private Image buildSeparator() {
        Image separator = new Image(assets.getSeparator());
        separator.setScaling(Scaling.fillX);
        return separator;
    }

    /** Builds one save-slot row. */
    private Table buildSlotRow(int slotId) {
        GameData data = controller.getSlotData(slotId);
        boolean occupied = (data != null);

        Table slotRow = new Table();

        Table saveSlotTable = new Table();

        if(occupied) {
            saveSlotTable.setBackground(
                new TextureRegionDrawable(assets.getSaveSlotBG(data))
            );
        }

        // slot number
        Label numLabel = new Label(slotId + ".", skin, "title");
        numLabel.setAlignment(Align.center);
        numLabel.setFontScale(SLOT_NUM_FONT_SCALE);
        numLabel.setColor(Color.WHITE);
        saveSlotTable.add(numLabel).width(50f).expandY().center().padLeft(12f);

        // area info or "New Game"
        if (occupied) {
            saveSlotTable.add(buildOccupiedInfo(data)).expand().fill().padLeft(14f).padRight(14f);
        } else {
            Label emptyLabel = new Label(Lang.get("startGame.newGame"), skin);
            emptyLabel.setAlignment(Align.left);
            emptyLabel.setFontScale(AREA_FONT_SCALE);
            emptyLabel.setColor(Color.WHITE);
            saveSlotTable.add(emptyLabel).expand().fill();
        }

        Table resetButtonTable = new Table();

        // reset button (if save slot isn't empty)
        if (occupied) {
            resetButtonTable.add(buildResetButton(slotId)).height(RESET_BUTTON_HEIGHT).right();
        } else {
            TextButton disabledButton = new TextButton(Lang.get("startGame.reset"), skin);
            disabledButton.setDisabled(true);
            disabledButton.setVisible(false);
            resetButtonTable.add(disabledButton).height(RESET_BUTTON_HEIGHT).right();
        }

        Stack saveSlotWithFleur = addFleurTo(saveSlotTable);

        // Clicking the row starts the game
        saveSlotWithFleur.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.onSlotSelected(slotId);
            }
        });

        slotRow.add(saveSlotWithFleur).expand().fill();
        slotRow.add(resetButtonTable).width(RESET_BUTTON_WIDTH).spaceLeft(RESET_BUTTON_SPACING);

        return slotRow;
    }

    /** Builds the fleur image that sits on the top edge of each save slot. */
    private Image buildFleur() {
        Image fleur = new Image(assets.getProfileFleur());
        fleur.setScaling(Scaling.fit);
        fleur.setColor(Color.WHITE);
        return fleur;
    }

    /**
     * Adds a fleur to the given {@code slotTable}.
     * @return the {@link Stack} containing the slot and the fleur.
     */
    private Stack addFleurTo(Table slotTable) {
        Image fleur = buildFleur();

        Stack stack = new Stack();
        stack.add(slotTable);

        Table fleurWrapper = new Table();
        fleurWrapper.top().center();
        fleurWrapper.add(fleur)
            .width(FLEUR_WIDTH)
            .height(FLEUR_HEIGHT)
            .padTop(-FLEUR_OVERLAP)
            .padLeft(-FLEUR_OVERLAP);
        stack.add(fleurWrapper);

        return stack;
    }

    /**
     * Builds the info section for an occupied slot.
     */
    private Table buildOccupiedInfo(GameData data) {
        Table info = new Table();

        // Left Table (containing profile HUD)
        Table leftTable = new Table();
        leftTable.top().left();
        leftTable.add(buildProfileHUD(data));

        // Right table (containing area name and detail line)
        Table rightTable = new Table();
        rightTable.top().right();

        // Area name
        String areaName = resolveAreaName(data.currentEnvironment);
        Label areaLabel = new Label(areaName, skin);
        areaLabel.setAlignment(Align.right);
        areaLabel.setFontScale(AREA_FONT_SCALE);
        areaLabel.setColor(new Color(Color.WHITE));
        rightTable.add(areaLabel).padTop(25f).right().row();

        // Detail line: masks / deaths / play time
        String detail = buildDetailLine(data);
        Label detailLabel = new Label(detail, skin);
        detailLabel.setAlignment(Align.right);
        detailLabel.setFontScale(DETAIL_FONT_SCALE);
        detailLabel.setColor(new Color(Color.WHITE));
        rightTable.add(detailLabel).right().padTop(20f);

        // Add tables to info
        info.add(leftTable);
        info.add(rightTable).expand().fill();

        return info;
    }

    /** Builds the profile HUD (soul bar + masks) for an occupied save slot. */
    private Table buildProfileHUD(GameData data) {
        Table hud = new Table();
        hud.top().center();

        Image soulBar = new Image(assets.getProfileSoul());
        soulBar.setScaling(Scaling.fit);

        hud.add(soulBar).size(SOUL_BAR_SIZE).padBottom(5f).padRight(-SOUL_BAR_OVERLAP);

        Table masksRow = new Table();
        for (int i = 0; i < data.masks; i++) {
            Image mask = new Image(assets.getProfileMask());
            mask.setScaling(Scaling.fit);
            masksRow.add(mask).size(MASK_ICON_SIZE).spaceRight(2f);
        }
        hud.add(masksRow).padTop(-MASK_ICON_OVERLAP).center();

        return hud;
    }

    /** Resolves the environment id to a human-readable area name. */
    private String resolveAreaName(int environmentId) {
        GameEnvironment environment = GameEnvironment.fromId(environmentId);
        if (environment != null) return environment.name;
        return Lang.get("startGame.unknownArea");
    }

    /** Formats a compact detail string for an occupied slot. */
    private String buildDetailLine(GameData data) {
        return formatPlayTime(data.playTimeSeconds);
    }

    /** Formats seconds into HH:MM:SS. */
    private String formatPlayTime(float seconds) {
        int totalSecs = (int) seconds;
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        return String.format("%02dH %02dM", hours, minutes);
    }

    /** Reset button for a single slot. */
    private TextButton buildResetButton(int slotId) {
        TextButton button = new TextButton(Lang.get("startGame.reset"), skin);
        button.getLabel().setFontScale(0.7f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onResetSlot(slotId);
            }
        });
        return button;
    }

    /** Back button that navigates to the main menu. */
    private TextButton buildBackButton() {
        TextButton button = new TextButton(Lang.get("startGame.back"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onBack();
            }
        });
        return button;
    }
}
