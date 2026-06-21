package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.github.matinnameni.minihollowknight.controller.SettingsController;
import com.github.matinnameni.minihollowknight.model.GameAssets;
import com.github.matinnameni.minihollowknight.model.Lang;
import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.model.enums.SupportedLanguage;

/**
 * Settings screen
 */
public class SettingsScreen extends AbstractScreen {
    private static final float FIELD_WIDTH = 250f;
    private static final float FIELD_HEIGHT = 40f;
    private static final float FIELD_SPACING = 20f;

    private final Settings settings;
    private final SettingsController controller;

    public SettingsScreen(GameAssets assets, Settings settings, SettingsController controller) {
        super(assets);
        this.settings = settings;
        this.controller = controller;
    }

    @Override
    public void show() {
        super.show();

        addBackground();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.defaults().space(FIELD_SPACING).expandX().fillX()
            .width(FIELD_WIDTH).height(FIELD_HEIGHT).center();

        rootTable.add(buildTitleLabel()).spaceBottom(0).row();
        rootTable.add(new Image(assets.getSeparator())).space(0).row();
        rootTable.add(buildLanguageSelectSection()).spaceTop(0).row();
        rootTable.add(buildBrightnessSection()).row();
        rootTable.add(buildAudioSettingsButton()).spaceTop(0).row();
        rootTable.add(buildKeyBindingsButton()).spaceTop(0).row();
        rootTable.add(buildBackButton()).padTop(30);

        stage.addActor(rootTable);
    }

    // --- Helpers ---

    /** Sets settings-menu background image */
    private void addBackground() {
        Image background = new Image(assets.getBackground());
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);
    }

    /** Builds the label that shows menu title */
    private Label buildTitleLabel() {
        Label titleLabel = new Label(Lang.get("settings.title"), skin);
        titleLabel.setAlignment(Align.center);
        return titleLabel;
    }

    // --- Audio ---

    /**
     * Builds the button that navigates to the audio settings sub-screen.
     */
    private TextButton buildAudioSettingsButton() {
        TextButton button = new TextButton(Lang.get("settings.audio"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onAudioSettings();
            }
        });
        return button;
    }

    // --- Key Bindings ---

    /**
     * Builds the button that navigates to the key bindings sub-screen.
     */
    private TextButton buildKeyBindingsButton() {
        TextButton button = new TextButton(Lang.get("settings.keyBindings"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onKeyBindings();
            }
        });
        return button;
    }

    // --- Display ---

    private Table buildLanguageSelectSection() {
        Table wrapper = new Table(skin);

        wrapper.add(new Label(Lang.get("settings.language"), skin)).expandX().grow().spaceRight(FIELD_SPACING);
        wrapper.add(buildLanguageSelect()).growY();

        return wrapper;
    }

    /**
     * Builds the select box that lets the player switch the active language
     */
    private SelectBox<SupportedLanguage> buildLanguageSelect() {
        SelectBox<SupportedLanguage> select = new SelectBox<>(skin);
        select.setItems(SupportedLanguage.values());
        select.setSelected(SupportedLanguage.fromShortName(settings.getLanguage()));

        select.getStyle().background = null;
        select.getList().setAlignment(Align.center);

        select.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onLanguageChanged(select.getSelected());
            }
        });
        return select;
    }

    /**
     * Builds the section containing a label and a slider for screen brightness.
     */
    private Table buildBrightnessSection() {
        Table wrapper = new Table(skin);

        wrapper.add(new Label(Lang.get("settings.brightness"), skin)).expandX().grow().spaceRight(FIELD_SPACING);
        wrapper.add(buildBrightnessSlider()).growY();

        return wrapper;
    }

    /**
     * Builds the slider that lets the player adjust screen brightness.
     */
    private Slider buildBrightnessSlider() {
        Slider slider = new Slider(0.2f, 1f, 0.01f, false, skin);
        slider.setValue(settings.getBrightness());

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onBrightnessChanged(slider.getValue());
            }
        });
        return slider;
    }

    // --- Navigation ---

    /**
     * Builds the back button that allows player to return to the main menu
     */
    private TextButton buildBackButton() {
        TextButton button = new TextButton(Lang.get("settings.back"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onBack();
            }
        });
        return button;
    }
}
