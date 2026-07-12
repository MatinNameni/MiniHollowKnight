package com.github.matinnameni.minihollowknight.controller.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.github.matinnameni.minihollowknight.controller.CheatCodeRegistry;
import com.github.matinnameni.minihollowknight.controller.ZoteController;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.view.screens.GameScreen;

import java.util.function.BooleanSupplier;

/**
 * Dedicated input processor for the {@link GameScreen}.
 */
public class GameScreenInputProcessor extends InputAdapter {

    private final Settings settings;
    private final CheatCodeRegistry cheats;
    private final CheatCodeRegistry.Context cheatContext;
    private final ZoteController zoteController;

    /** Gate callbacks that decide whether a given toggle is allowed right now. */
    private final BooleanSupplier canTogglePause;
    private final BooleanSupplier canToggleInventory;
    private final Runnable onTogglePause;
    private final Runnable onToggleInventory;
    private final Runnable onToggleDebug;

    public GameScreenInputProcessor(Settings settings,
                                    CheatCodeRegistry cheats,
                                    CheatCodeRegistry.Context cheatContext,
                                    ZoteController zoteController,
                                    BooleanSupplier canTogglePause,
                                    BooleanSupplier canToggleInventory,
                                    Runnable onTogglePause,
                                    Runnable onToggleInventory,
                                    Runnable onToggleDebug) {
        this.settings = settings;
        this.cheats = cheats;
        this.cheatContext = cheatContext;
        this.zoteController = zoteController;
        this.canTogglePause = canTogglePause;
        this.canToggleInventory = canToggleInventory;
        this.onTogglePause = onTogglePause;
        this.onToggleInventory = onToggleInventory;
        this.onToggleDebug = onToggleDebug;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (zoteController != null && zoteController.isDialogueOpen()) {
            if (keycode == Keys.ENTER || keycode == Keys.NUMPAD_ENTER
                || keycode == settings.getKeyInteract()) {
                zoteController.onAdvanceKeyPressed();
                return true;
            }
            if (keycode == settings.getKeyPause()) {
                zoteController.onCloseKeyPressed();
                return true;
            }
            return false;
        }

        // --- Menu toggles ---
        if (keycode == settings.getKeyPause()) {
            if (canTogglePause.getAsBoolean()) {
                onTogglePause.run();
            }
            return true;
        }
        if (keycode == settings.getKeyInventory()) {
            if (canToggleInventory.getAsBoolean()) {
                onToggleInventory.run();
            }
            return true;
        }

        // --- Zote interact ---
        if (keycode == settings.getKeyInteract() && zoteController != null) {
            zoteController.onInteractKeyPressed();
        }

        // --- Debug toggle ---
        if (keycode == Keys.F1) {
            onToggleDebug.run();
            return true;
        }

        // --- Cheat codes (Ctrl + key) ---
        if (isCtrlHeld()) {
            CheatCodeRegistry.CheatCode cheat = cheats.forKey(keycode);
            if (cheat != null) {
                cheat.activate(cheatContext);
                return true;
            }
        }

        return false;
    }

    /** @return true if either left or right Ctrl is currently held. */
    private static boolean isCtrlHeld() {
        return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
    }
}
