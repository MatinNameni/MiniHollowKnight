package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.github.matinnameni.minihollowknight.model.GameAssets;
import com.github.matinnameni.minihollowknight.controller.MainMenuController;
import com.github.matinnameni.minihollowknight.model.Settings;

/**
 * Main-menu screen
 */
public class MainMenuScreen extends AbstractScreen {
    private static final float BUTTON_WIDTH  = 250f;
    private static final float BUTTON_HEIGHT =  40f;
    private static final float BUTTON_SPACING = 10f;
    private static final float LOGO_MIN_SIZE = 150f;
    private static final float LOGO_PAD_TOP  =  50f;
    private static final float LOGO_PAD_BOTTOM =  20f;
    private static final float MENU_PAD_BOTTOM =  50f;

    private final MainMenuController controller;
    private final Settings settings;

    public MainMenuScreen(GameAssets assets, MainMenuController controller, Settings settings) {
        super(assets);
        this.controller = controller;
        this.settings = settings;
    }

    public MainMenuScreen(GameAssets assets, MainMenuController controller) {
        this(assets, controller, new Settings());
    }

    @Override
    public void show() {
        super.show();

        addBackground();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.defaults().space(30);
        rootTable.add(buildLogoSection()).grow().center().row();
        rootTable.add(buildButtonsSection()).expandX().center().padBottom(MENU_PAD_BOTTOM);

        stage.addActor(rootTable);
    }

    // --- Helpers ---

    /** Sets main-menu background image */
    private void addBackground() {
        Image background = new Image(assets.getBackground());
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);
    }

    /**
     * Builds the section that shows game logo
     * @return the Table containing the image
     */
    private Table buildLogoSection() {
        Table wrapper = new Table();
        Image logo = new Image(assets.getLogo());
        logo.setScaling(Scaling.fit);
        wrapper.add(logo)
            .grow()
            .minSize(LOGO_MIN_SIZE)
            .padTop(LOGO_PAD_TOP)
            .padBottom(LOGO_PAD_BOTTOM)
            .center();
        return wrapper;
    }

    /**
     * Builds main-menu buttons section
     * @return the Table containing the buttons
     */
    private Table buildButtonsSection() {
        Table wrapper = new Table();
        wrapper.defaults()
            .space(BUTTON_SPACING)
            .width(BUTTON_WIDTH)
            .height(BUTTON_HEIGHT)
            .center();
        wrapper.add(menuButton("Start Game", controller::onStartGame)).row();
        wrapper.add(menuButton("Settings", controller::onSettings)).row();
        wrapper.add(menuButton("Guide", controller::onGuide)).row();
        wrapper.add(menuButton("Achievements", controller::onAchievements)).row();
        wrapper.add(menuButton("Quit Game", controller::onQuit)).row();
        return wrapper;
    }

    /** Creates a button matching the main menu style */
    private TextButton menuButton(String label, Runnable action) {
        TextButton button = new TextButton(label, skin);
        button.pad(10);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return button;
    }
}
