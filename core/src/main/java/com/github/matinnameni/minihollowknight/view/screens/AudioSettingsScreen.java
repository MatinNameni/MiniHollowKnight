package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.github.matinnameni.minihollowknight.controller.AudioSettingsController;
import com.github.matinnameni.minihollowknight.model.GameAssets;
import com.github.matinnameni.minihollowknight.model.Lang;
import com.github.matinnameni.minihollowknight.model.Settings;

/**
 * Audio settings screen with music toggle/volume, and SFX toggle/volume.
 */
public class AudioSettingsScreen extends AbstractScreen {
    private static final float FIELD_WIDTH = 350f;
    private static final float FIELD_HEIGHT = 40f;
    private static final float FIELD_SPACING = 20f;
    private static final float COLUMN_SPACING = 30f;
    private static final float SLIDER_WIDTH = 200f;

    private final Settings settings;
    private final AudioSettingsController controller;

    public AudioSettingsScreen(GameAssets assets, Settings settings, AudioSettingsController controller) {
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
        rootTable.defaults().space(FIELD_SPACING).expandX().center();

        rootTable.add(buildTitleLabel()).width(FIELD_WIDTH).colspan(2).spaceBottom(0).row();
        addSeparator(rootTable).space(0).width(FIELD_WIDTH).colspan(2).row();
        rootTable.add(buildSettingsColumns()).colspan(2).width(FIELD_WIDTH).row();
        rootTable.add(buildResetButton()).colspan(2).height(FIELD_HEIGHT).row();
        rootTable.add(buildBackButton()).colspan(2).height(FIELD_HEIGHT).padTop(30);

        stage.addActor(rootTable);
    }

    // --- Helpers ---

    /** Sets audio-settings background image */
    private void addBackground() {
        Image background = new Image(assets.getBackground());
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);
    }

    /** Builds the label that shows menu title */
    private Label buildTitleLabel() {
        Label titleLabel = new Label(Lang.get("audioSettings.title"), skin, "title");
        titleLabel.setAlignment(Align.center);
        titleLabel.setFontScale(1.5f);
        return titleLabel;
    }

    /** Adds the separator that goes bellow the menu title to the {@code wrapper} table. */
    private Cell<Image> addSeparator(Table wrapper) {
        float width = FIELD_WIDTH;
        float height = assets.getSeparator().getHeight() * (FIELD_WIDTH / assets.getSeparator().getWidth());

        return wrapper.add(new Image(assets.getSeparator()))
            .width(width)
            .height(height);
    }

    /** Sets toggle button text to on/off */
    private void updateToggleButtonText(TextButton button, boolean isEnabled) {
        if(isEnabled) {
            button.setText(Lang.get("audioSettings.enabled"));
        } else {
            button.setText(Lang.get("audioSettings.disabled"));
        }
    }

    /** Builds the table of audio settings rows. */
    private Table buildSettingsColumns() {
        Table columns = new Table();
        columns.defaults().top().expandX();

        Table leftColumn = new Table();
        leftColumn.defaults().space(FIELD_SPACING).expandX().fillX()
            .height(FIELD_HEIGHT).center();
        Table rightColumn = new Table();
        rightColumn.defaults().space(FIELD_SPACING).expandX().fillX()
            .height(FIELD_HEIGHT).center();

        leftColumn.add(new Label(Lang.get("audioSettings.music"), skin)).expandX().fillX().row();
        leftColumn.add(new Label(Lang.get("audioSettings.musicVolume"), skin)).expandX().fillX().row();
        leftColumn.add(new Label(Lang.get("audioSettings.sfx"), skin)).expandX().fillX().row();
        leftColumn.add(new Label(Lang.get("audioSettings.sfxVolume"), skin)).expandX().fillX();

        rightColumn.add(buildMusicToggle()).expandX().fillX().row();
        rightColumn.add(buildMusicVolumeSlider()).width(SLIDER_WIDTH).expandX().fillX().row();
        rightColumn.add(buildSfxToggle()).expandX().fillX().row();
        rightColumn.add(buildSfxVolumeSlider()).width(SLIDER_WIDTH).expandX().fillX().row();

        columns.add(leftColumn).spaceRight(COLUMN_SPACING);
        columns.add(rightColumn);
        return columns;
    }

    // --- Music ---

    /**
     * Builds the button that toggles music on or off.
     */
    private TextButton buildMusicToggle() {
        TextButton toggleButton = new TextButton("", skin);
        updateToggleButtonText(toggleButton, settings.isMusicEnabled());

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(toggleButton.getStyle());
        style.up = null;
        style.down = null;
        style.checked = null;
        style.over = null;
        toggleButton.setStyle(style);

        toggleButton.getLabel().setAlignment(Align.right);

        toggleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean newState = !settings.isMusicEnabled();
                updateToggleButtonText(toggleButton, newState);
                controller.onMusicToggled(newState);
            }
        });
        return toggleButton;
    }

    /**
     * Builds the slider that lets the player adjust music volume.
     */
    private Slider buildMusicVolumeSlider() {
        Slider slider = new Slider(0f, 1f, 0.01f, false, skin);
        slider.setValue(settings.getMusicVolume());

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onMusicVolumeChanged(slider.getValue());
            }
        });
        return slider;
    }

    // --- SFX ---

    /**
     * Builds the checkbox that toggles SFX on or off.
     */
    private TextButton buildSfxToggle() {
        TextButton toggleButton = new TextButton("", skin);
        updateToggleButtonText(toggleButton, settings.isMusicEnabled());

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(toggleButton.getStyle());
        style.up = null;
        style.down = null;
        style.checked = null;
        style.over = null;
        toggleButton.setStyle(style);

        toggleButton.getLabel().setAlignment(Align.right);

        toggleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean newState = !settings.isSfxEnabled();
                updateToggleButtonText(toggleButton, newState);
                controller.onSfxToggled(newState);
            }
        });
        return toggleButton;
    }

    /**
     * Builds the slider that lets the player adjust SFX volume.
     */
    private Slider buildSfxVolumeSlider() {
        Slider slider = new Slider(0f, 1f, 0.01f, false, skin);
        slider.setValue(settings.getSfxVolume());

        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onSfxVolumeChanged(slider.getValue());
            }
        });
        return slider;
    }

    /**
     * Builds the button that resets the audio settings.
     */
    private TextButton buildResetButton() {
        TextButton resetButton = new TextButton(Lang.get("audioSettings.reset"), skin);
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.resetAudioSettings();
            }
        });
        return resetButton;
    }

    // --- Navigation ---

    /**
     * Builds the back button that returns to the settings screen.
     */
    private TextButton buildBackButton() {
        TextButton button = new TextButton(Lang.get("audioSettings.back"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onBack();
            }
        });
        return button;
    }
}
