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
    private static final float FIELD_WIDTH = 250f;
    private static final float FIELD_HEIGHT = 40f;
    private static final float FIELD_SPACING = 10f;
    private static final float SLIDER_WIDTH = 150f;

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
        rootTable.defaults().space(FIELD_SPACING).expandX().fillX()
            .width(FIELD_WIDTH).height(FIELD_HEIGHT).center();

        rootTable.add(buildTitleLabel()).spaceBottom(0).row();
        rootTable.add(new Image(assets.getSeparator())).space(0).row();
        rootTable.add(buildMusicToggleSection()).spaceTop(0).row();
        rootTable.add(buildMusicVolumeSection()).row();
        rootTable.add(buildSfxToggleSection()).row();
        rootTable.add(buildSfxVolumeSection()).row();
        rootTable.add(buildBackButton()).padTop(30);

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
        Label titleLabel = new Label(Lang.get("audioSettings.title"), skin);
        titleLabel.setAlignment(Align.center);
        return titleLabel;
    }

    private void updateToggleButtonText(TextButton button, boolean isEnabled) {
        if(isEnabled) {
            button.setText(Lang.get("audioSettings.enabled"));
        } else {
            button.setText(Lang.get("audioSettings.disabled"));
        }
    }

    // --- Music ---

    /**
     * Builds the row containing a label and a checkbox to toggle music on/off.
     */
    private Table buildMusicToggleSection() {
        Table wrapper = new Table(skin);

        wrapper.add(new Label(Lang.get("audioSettings.music"), skin)).spaceRight(FIELD_SPACING);
        wrapper.add(buildMusicToggle()).expandX().grow();

        return wrapper;
    }

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
     * Builds the section containing a label and a slider for music volume.
     */
    private Table buildMusicVolumeSection() {
        Table wrapper = new Table(skin);

        wrapper.add(new Label(Lang.get("audioSettings.musicVolume"), skin)).expandX().grow().spaceRight(FIELD_SPACING);
        wrapper.add(buildMusicVolumeSlider()).growY().right().width(SLIDER_WIDTH);

        return wrapper;
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
     * Builds the section containing a label and a checkbox to toggle SFX on/off.
     */
    private Table buildSfxToggleSection() {
        Table wrapper = new Table(skin);

        wrapper.add(new Label(Lang.get("audioSettings.sfx"), skin)).spaceRight(FIELD_SPACING);
        wrapper.add(buildSfxToggle()).expandX().grow();

        return wrapper;
    }

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
     * Builds the section containing a label and a slider for SFX volume.
     */
    private Table buildSfxVolumeSection() {
        Table wrapper = new Table(skin);

        wrapper.add(new Label(Lang.get("audioSettings.sfxVolume"), skin)).expandX().grow().spaceRight(FIELD_SPACING);
        wrapper.add(buildSfxVolumeSlider()).growY().right().width(SLIDER_WIDTH);

        return wrapper;
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
