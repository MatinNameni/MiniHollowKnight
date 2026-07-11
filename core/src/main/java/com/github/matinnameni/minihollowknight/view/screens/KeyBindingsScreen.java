package com.github.matinnameni.minihollowknight.view.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import com.github.matinnameni.minihollowknight.controller.KeyBindingsController;
import com.github.matinnameni.minihollowknight.model.asset.AssetRegistry;
import com.github.matinnameni.minihollowknight.model.localization.Lang;
import com.github.matinnameni.minihollowknight.model.data.Settings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Key bindings screen.
 */
public class KeyBindingsScreen extends AbstractScreen {
    private static final float FIELD_WIDTH = 400f;
    private static final float FIELD_HEIGHT = 36f;
    private static final float FIELD_SPACING = 30f;
    private static final float COLUMN_SPACING = 30f;
    private static final float KEY_BUTTON_WIDTH = 90f;
    private static final float KEY_BUTTON_HEIGHT = 60f;

    private final Settings settings;
    private final KeyBindingsController controller;

    /** The rebind button currently waiting for a key press, or null if none. */
    private TextButton listeningButton;

    /** Contains the TextButtons in this menu. keyed with their title. */
    Map<String, TextButton> buttons = new LinkedHashMap<>();

    public KeyBindingsScreen(AssetRegistry registry, Settings settings, KeyBindingsController controller) {
        super(registry);
        this.settings = settings;
        this.controller = controller;
    }

    @Override
    public void show() {
        super.show();

        if(controller.shouldDrawBackground()) { addBackground(); }
        installKeyCaptureProcessor();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.defaults().space(FIELD_SPACING).expandX().center();

        rootTable.add(buildTitleLabel()).width(FIELD_WIDTH).height(FIELD_HEIGHT).colspan(2).spaceBottom(0).row();
        rootTable.add(buildSeparator()).width(FIELD_WIDTH).colspan(2).spaceTop(0).row();
        rootTable.add(buildBindingsColumns()).colspan(2).spaceTop(0).row();
        rootTable.add(buildResetButton()).height(FIELD_HEIGHT).colspan(2).row();
        rootTable.add(buildBackButton()).height(FIELD_HEIGHT).colspan(2).padTop(30);

        stage.addActor(rootTable);
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(null);
    }

    // --- Helpers ---

    /** Sets key-bindings background image */
    private void addBackground() {
        Image background = new Image(menuAssets().getBackground());
        background.setScaling(Scaling.fill);
        background.setFillParent(true);
        stage.addActor(background);
    }

    /** Builds the label that shows menu title */
    private Label buildTitleLabel() {
        Label titleLabel = new Label(Lang.get("keyBindings.title"), skin, "title");
        titleLabel.setAlignment(Align.center);
        titleLabel.setFontScale(1.5f);
        return titleLabel;
    }

    /** Builds the separator under the title. */
    private Image buildSeparator() {
        Image separator = new Image(menuAssets().getSeparator());
        separator.setScaling(Scaling.fillX);
        return separator;
    }

    /**
     * Installs an {@link InputAdapter} ahead of the stage that intercepts the next key press
     * while a rebind button is in "listening" state, and otherwise lets input fall through to the stage.
     */
    private void installKeyCaptureProcessor() {
        InputAdapter keyCapture = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (listeningButton == null) return false;
                applyNewKeycode(listeningButton, keycode);
                return true;
            }
        };
        InputMultiplexer multiplexer = new InputMultiplexer(keyCapture, stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    // --- Bindings layout ---

    /**
     * Builds the table of key binding rows.
     */
    private Table buildBindingsColumns() {
        Table columns = new Table();
        columns.defaults().top().expandX();

        Table leftColumn = new Table();
        leftColumn.defaults().space(FIELD_SPACING).expandX().fillX()
            .height(FIELD_HEIGHT).center();
        Table rightColumn = new Table();
        rightColumn.defaults().space(FIELD_SPACING).expandX().fillX()
            .height(FIELD_HEIGHT).center();

        leftColumn.add(buildBindingRow("keyBindings.up", settings::getKeyUp, controller::onKeyUpChanged)).row();
        leftColumn.add(buildBindingRow("keyBindings.down", settings::getKeyDown, controller::onKeyDownChanged)).row();
        leftColumn.add(buildBindingRow("keyBindings.jump", settings::getKeyJump, controller::onKeyJumpChanged)).row();
        leftColumn.add(buildBindingRow("keyBindings.attack", settings::getKeyAttack, controller::onKeyAttackChanged)).row();
        leftColumn.add(buildBindingRow("keyBindings.dash", settings::getKeyDash, controller::onKeyDashChanged)).row();
        leftColumn.add(buildBindingRow("keyBindings.inventory", settings::getKeyInventory, controller::onKeyInventoryChanged)).row();

        rightColumn.add(buildBindingRow("keyBindings.left", settings::getKeyLeft, controller::onKeyLeftChanged)).row();
        rightColumn.add(buildBindingRow("keyBindings.right", settings::getKeyRight, controller::onKeyRightChanged)).row();
        rightColumn.add(buildBindingRow("keyBindings.focus", settings::getKeyFocus, controller::onKeyFocusChanged)).row();
        rightColumn.add(buildBindingRow("keyBindings.cast", settings::getKeyCast, controller::onKeyCastChanged)).row();
        rightColumn.add(buildBindingRow("keyBindings.interact", settings::getKeyInteract, controller::onKeyInteractChanged)).row();
        rightColumn.add(buildBindingRow("keyBindings.pause", settings::getKeyPause, controller::onKeyPauseChanged)).row();

        columns.add(leftColumn).spaceRight(COLUMN_SPACING);
        columns.add(rightColumn);
        return columns;
    }

    /**
     * Builds a single row containing an action label and a button showing/capturing its bound key.
     */
    private Table buildBindingRow(String labelKey, IntSupplier currentKeycode, IntConsumer onChanged) {
        Table wrapper = new Table(skin);

        wrapper.add(new Label(Lang.get(labelKey), skin)).expandX().grow().spaceRight(FIELD_SPACING);
        wrapper.add(buildKeyButton(currentKeycode, onChanged)).width(KEY_BUTTON_WIDTH).height(KEY_BUTTON_HEIGHT).right();

        return wrapper;
    }

    /**
     * Builds the button that shows the currently bound key and enters "listening" mode when tapped,
     * capturing the next key press as the new binding.
     */
    private TextButton buildKeyButton(IntSupplier currentKeycode, IntConsumer onChanged) {
        TextButton keyButton = new TextButton(Input.Keys.toString(currentKeycode.getAsInt()), skin, "KeyBindings");
        BindingState state = new BindingState(currentKeycode.getAsInt(), onChanged);
        keyButton.setUserObject(state);

        keyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listeningButton == keyButton) return;
                startListening(keyButton);
            }
        });

        buttons.put(keyButton.getText().toString(), keyButton);

        return keyButton;
    }

    /** Puts {@code button} into "press any key" mode, prompting the player for new input. */
    private void startListening(TextButton button) {
        if (listeningButton != null) {
            stopListening(listeningButton);
        }
        listeningButton = button;
        button.setText(Lang.get("keyBindings.pressKey"));
    }

    /** Exits "listening" mode for {@code button}, restoring its label to its current bound key's name. */
    private void stopListening(TextButton button) {
        BindingState state = (BindingState) button.getUserObject();
        button.setText(Input.Keys.toString(state.keycode));
        if (listeningButton == button) {
            listeningButton = null;
        }
    }

    /** Applies {@code keycode} as the new binding for {@code button} and exits listening mode. */
    private void applyNewKeycode(TextButton button, int keycode) {
        BindingState state = (BindingState) button.getUserObject();
        TextButton conflictKey;
        if((conflictKey = controller.getConflictKey(buttons, button, keycode)) != null) {
            int conflictKeyNewKeycode = state.keycode;
            state.keycode = keycode;
            state.onChanged.accept(keycode);
            stopListening(button);
            applyNewKeycode(conflictKey, conflictKeyNewKeycode);
        } else {
            state.keycode = keycode;
            state.onChanged.accept(keycode);
            stopListening(button);
        }
    }

    /**
     * Builds the button that resets all key bindings.
     */
    private TextButton buildResetButton() {
        TextButton resetButton = new TextButton(Lang.get("keyBindings.reset"), skin);
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listeningButton = null;
                controller.resetKeyBindings();
            }
        });
        return resetButton;
    }

    // --- Navigation ---

    /**
     * Builds the back button that returns to the settings screen.
     */
    private TextButton buildBackButton() {
        TextButton button = new TextButton(Lang.get("keyBindings.back"), skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                controller.onBack();
            }
        });
        return button;
    }

    // --- Inner class ---

    /** Holds a binding button's current keycode and the callback used to persist a new one. */
    private static final class BindingState {
        int keycode;
        final IntConsumer onChanged;

        BindingState(int keycode, IntConsumer onChanged) {
            this.keycode = keycode;
            this.onChanged = onChanged;
        }
    }
}
