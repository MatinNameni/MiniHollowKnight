package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.github.matinnameni.minihollowknight.model.Settings;
import com.github.matinnameni.minihollowknight.view.screens.GameScreen;

import java.util.function.BooleanSupplier;

/**
 * Dedicated input processor for the {@link GameScreen}.
 */
public class GameScreenInputProcessor extends InputAdapter {

    private final Settings settings;
    private final CheatCodeRegistry cheats;
    private final CheatCodeRegistry.Context cheatContext;

    /** Gate callbacks that decide whether a given toggle is allowed right now. */
    private final BooleanSupplier canTogglePause;
    private final BooleanSupplier canToggleInventory;
    private final Runnable onTogglePause;
    private final Runnable onToggleInventory;
    private final Runnable onToggleDebug;

    public GameScreenInputProcessor(Settings settings,
                                    CheatCodeRegistry cheats,
                                    CheatCodeRegistry.Context cheatContext,
                                    BooleanSupplier canTogglePause,
                                    BooleanSupplier canToggleInventory,
                                    Runnable onTogglePause,
                                    Runnable onToggleInventory,
                                    Runnable onToggleDebug) {
        this.settings = settings;
        this.cheats = cheats;
        this.cheatContext = cheatContext;
        this.canTogglePause = canTogglePause;
        this.canToggleInventory = canToggleInventory;
        this.onTogglePause = onTogglePause;
        this.onToggleInventory = onToggleInventory;
        this.onToggleDebug = onToggleDebug;
    }

    @Override
    public boolean keyDown(int keycode) {
        // --- Menu toggles (user-configurable keys) ---
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
