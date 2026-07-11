package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.github.matinnameni.minihollowknight.model.achievement.AchievementManager;
import com.github.matinnameni.minihollowknight.model.asset.AchievementAssetBundle;
import com.github.matinnameni.minihollowknight.model.asset.AssetRegistry;
import com.github.matinnameni.minihollowknight.model.localization.Lang;
import com.github.matinnameni.minihollowknight.model.enums.AchievementType;
import com.github.matinnameni.minihollowknight.view.navigator.ScreenNavigator;

/**
 * Achievements screen, reachable from the main menu.
 */
public class AchievementsScreen extends AbstractScreen {

    private static final float TITLE_FONT_SCALE = 1.6f;
    private static final float CARD_WIDTH = 720f;
    private static final float CARD_HEIGHT = 96f;
    private static final float CARD_SPACING = 14f;
    private static final float ICON_SIZE = 72f;
    private static final float NAME_FONT_SCALE = 1.15f;
    private static final float DESC_FONT_SCALE = 0.9f;
    private static final float STATUS_FONT_SCALE = 0.85f;
    private static final float PAD_TOP = 40f;

    /** Tint applied to the icon of a locked achievement. */
    private static final Color LOCKED_ICON_TINT = new Color(0.32f, 0.32f, 0.34f, 0.75f);

    private final ScreenNavigator navigator;
    private final AchievementManager manager;
    private final AchievementAssetBundle achievementAssets;

    public AchievementsScreen(AssetRegistry registry,
                              ScreenNavigator navigator,
                              AchievementManager manager,
                              AchievementAssetBundle achievementAssets) {
        super(registry);
        this.navigator = navigator;
        this.manager = manager;
        this.achievementAssets = achievementAssets;
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
        root.add(buildSeparator()).width(CARD_WIDTH).padTop(8f).row();
        root.add(buildProgressLabel()).padTop(10f).padBottom(6f).row();
        root.add(buildAchievementsList()).width(CARD_WIDTH).maxHeight(CARD_HEIGHT * 6f).padTop(4f).row();
        root.add(buildBackButton()).padTop(22f).padBottom(28f).row();

        stage.addActor(root);
    }

    // --- Helpers -----------------------------------------------------------------

    /** Sets the menu background image. */
    private void addBackground() {
        Image background = new Image(menuAssets().getBackground());
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);
    }

    /** Builds the screen title. */
    private Label buildTitle() {
        Label title = new Label(Lang.get("achievements.title"), skin, "title");
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

    /** Builds the "X / N unlocked" progress label. */
    private Label buildProgressLabel() {
        int unlocked = manager != null ? manager.unlockedCount() : 0;
        int total = AchievementType.values().length;
        String text = Lang.get("achievements.progress") + ": " + unlocked + " / " + total;
        Label label = new Label(text, skin);
        label.setAlignment(Align.center);
        label.setFontScale(STATUS_FONT_SCALE);
        label.setColor(Color.WHITE);
        return label;
    }

    /** Builds the scrollable list of achievement cards. */
    private ScrollPane buildAchievementsList() {
        Table list = new Table();
        list.defaults().width(CARD_WIDTH).height(CARD_HEIGHT).space(CARD_SPACING);

        for (AchievementType type : AchievementType.values()) {
            list.add(buildCard(type)).row();
        }

        ScrollPane scroll = new ScrollPane(list, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false); // vertical scroll only
        scroll.getStyle().background = null;
        return scroll;
    }

    /** Builds a single achievement card. */
    private Table buildCard(AchievementType type) {
        boolean unlocked = (manager != null) && manager.isUnlocked(type);

        Table card = new Table();
        card.defaults().pad(8f);

        // --- Icon ---
        Image icon = new Image(new TextureRegionDrawable(achievementAssets.getIcon(type)));
        icon.setScaling(Scaling.fit);
        icon.setColor(unlocked ? Color.WHITE : LOCKED_ICON_TINT);
        Table iconWrap = new Table();
        iconWrap.add(icon).size(ICON_SIZE);

        // --- Text column ---
        Table text = new Table();
        text.left().top();
        text.defaults().left().padLeft(4f);

        Label name = new Label(Lang.get(type.displayName), skin);
        name.setFontScale(NAME_FONT_SCALE);
        name.setColor(unlocked ? Color.WHITE : new Color(0.6f, 0.6f, 0.62f, 1f));

        Label description = new Label(Lang.get(type.description), skin);
        description.setFontScale(DESC_FONT_SCALE);
        description.setWrap(true);
        description.setColor(unlocked ? new Color(0.85f, 0.85f, 0.85f, 1f) : new Color(0.5f, 0.5f, 0.52f, 1f));

        Label status = new Label(
            unlocked ? Lang.get("achievements.unlocked") : Lang.get("achievements.locked"),
            skin
        );
        status.setFontScale(STATUS_FONT_SCALE);
        status.setColor(unlocked
            ? new Color(0.55f, 0.85f, 0.55f, 1f)
            : new Color(0.7f, 0.55f, 0.3f, 1f));

        text.add(name).left().row();
        text.add(description).left().growX().padTop(2f).row();
        text.add(status).left().padTop(4f);

        card.add(iconWrap).size(ICON_SIZE).top().padTop(6f).padLeft(12f);
        card.add(text).grow().fill().padRight(16f).padLeft(8f);

        // Dim the whole card slightly when locked for extra "grayscale" feel.
        if (!unlocked) {
            card.setColor(new Color(0.8f, 0.8f, 0.82f, 1f));
        }
        return card;
    }

    /** Builds the back button that returns to the main menu. */
    private TextButton buildBackButton() {
        TextButton button = new TextButton(Lang.get("achievements.back"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                navigator.goToMainMenu();
            }
        });
        return button;
    }
}
