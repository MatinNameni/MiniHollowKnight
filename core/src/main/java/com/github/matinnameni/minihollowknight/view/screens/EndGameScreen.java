package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.matinnameni.minihollowknight.model.asset.AssetRegistry;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.enums.MenuStyle;
import com.github.matinnameni.minihollowknight.model.localization.Lang;
import com.github.matinnameni.minihollowknight.view.navigator.ScreenNavigator;
import com.github.matinnameni.minihollowknight.view.navigator.UiManager;

/**
 * End-game screen.
 */
public class EndGameScreen extends AbstractScreen {

    // --- Layout constants ---
    private static final float TITLE_FONT_SCALE = 1.8f;
    private static final float SUBTITLE_FONT_SCALE = 1.0f;
    private static final float STAT_LABEL_FONT_SCALE = 1.05f;
    private static final float STAT_VALUE_FONT_SCALE = 1.3f;

    private static final float PANEL_WIDTH = 560f;
    private static final float BUTTON_WIDTH = 240f;
    private static final float BUTTON_HEIGHT = 44f;
    private static final float BUTTON_SPACING = 14f;
    private static final float PAD_TOP = 60f;
    private static final float PAD_BOTTOM = 40f;

    private final ScreenNavigator navigator;
    private final GameData gameData;

    private final MenuStyle previousStyle;

    public EndGameScreen(AssetRegistry registry, ScreenNavigator navigator, GameData gameData) {
        super(registry);
        this.navigator = navigator;
        this.gameData = gameData;
        this.previousStyle = menuAssets().getMenuStyle();
    }

    @Override
    public void show() {
        super.show();
        addBackground();

        Table root = new Table();
        root.setFillParent(true);
        root.top().center();
        root.defaults().center();

        root.add(buildTitle()).padTop(PAD_TOP).row();
        root.add(buildSeparator()).width(PANEL_WIDTH).padTop(10f).row();
        root.add(buildSubtitle()).padTop(12f).padBottom(18f).row();
        root.add(buildStatsTable()).width(PANEL_WIDTH).padTop(6f).row();
        root.add(buildButtons()).padTop(34f).padBottom(PAD_BOTTOM).row();

        stage.addActor(root);
    }

    // --- Helpers ---

    /** Sets the menu background image. */
    private void addBackground() {
        menuAssets().setBackground(null);
        Image background = new Image(menuAssets().getBackground());
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);
    }

    /** Builds the "Victory" title. */
    private Label buildTitle() {
        Label title = new Label(Lang.get("endGame.title"), skin, "title");
        title.setAlignment(Align.center);
        title.setFontScale(TITLE_FONT_SCALE);
        return title;
    }

    /** Builds the thin separator image under the title. */
    private Image buildSeparator() {
        Image separator = new Image(menuAssets().getSeparator());
        separator.setScaling(Scaling.fillX);
        return separator;
    }

    /** Builds the flavor subtitle shown beneath the separator. */
    private Label buildSubtitle() {
        Label subtitle = new Label(Lang.get("endGame.subtitle"), skin);
        subtitle.setAlignment(Align.center);
        subtitle.setFontScale(SUBTITLE_FONT_SCALE);
        subtitle.setColor(Color.WHITE);
        return subtitle;
    }

    /** Builds the stats section. */
    private Table buildStatsTable() {
        Table stats = new Table();
        stats.defaults().pad(6f);

        stats.add(buildStatRow(Lang.get("endGame.deaths") + ":", String.valueOf(gameData.totalDeaths))).row();
        stats.add(buildStatRow(Lang.get("endGame.enemiesKilled") + ":", String.valueOf(gameData.enemiesKilled))).row();
        stats.add(buildStatRow(Lang.get("endGame.playTime") + ":", formatPlayTime(gameData.playTimeSeconds))).row();

        return stats;
    }

    /** Builds a single label/value row. */
    private Table buildStatRow(String label, String value) {
        Table row = new Table();
        row.defaults().pad(4f);

        Label nameLabel = new Label(label, skin);
        nameLabel.setFontScale(STAT_LABEL_FONT_SCALE);
        nameLabel.setColor(Color.WHITE);
        nameLabel.setAlignment(Align.left);

        Label valueLabel = new Label(value, skin);
        valueLabel.setFontScale(STAT_VALUE_FONT_SCALE);
        valueLabel.setColor(Color.WHITE);
        valueLabel.setAlignment(Align.right);

        row.add(nameLabel).left().growX();
        row.add(valueLabel).right().growX();
        return row;
    }

    /** Builds the Restart + Main Menu buttons. */
    private Table buildButtons() {
        Table buttons = new Table();
        buttons.defaults().grow().height(BUTTON_HEIGHT).space(BUTTON_SPACING);

        buttons.add(menuButton(Lang.get("endGame.restart"), this::onRestart)).row();
        buttons.add(menuButton(Lang.get("endGame.mainMenu"), this::onMainMenu)).row();
        return buttons;
    }

    /** Creates a skin-styled button that runs {@code action} on click. */
    private TextButton menuButton(String label, Runnable action) {
        TextButton button = new TextButton(label, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return button;
    }

    // --- Button actions ---

    /** Resets the save slot and starts a fresh run. */
    private void onRestart() {
        if (gameData != null) {
            gameData.resetToNewGame();
        }
        navigator.goToGame(gameData);
    }

    /** Returns the player to the main menu. */
    private void onMainMenu() {
        if (gameData != null) {
            gameData.resetToNewGame();
        }
        menuAssets().setBackground(previousStyle);
        navigator.goToMainMenu();
        UiManager.getInstance().playMenuMusic();
    }

    // --- Formatting ---

    /** Formats a play-time in seconds as {@code HH:MM:SS}. */
    private static String formatPlayTime(float seconds) {
        int totalSecs = Math.max(0, (int) seconds);
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int secs = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
