package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.asset.HudAssetBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * The in-game HUD.
 */
public class GameHud {
    // Constants
    private static final float MARGIN_LEFT = 80f;
    private static final float ORB_MARGIN_TOP = 60f;
    private static final float MASK_MARGIN_TOP = 75f;
    private static final float MASK_OVERLAP = 25f;
    private static final float ORB_MASK_OVERLAP = 100f;

    private Stage stage;
    private SoulBarWidget soulBar;
    private List<MaskWidget> maskWidgets = new ArrayList<>();

    private final Knight knight;

    private final HudAssetBundle assets;

    public GameHud(Knight knight, HudAssetBundle assets) {
        this.knight = knight;
        this.assets = assets;
    }

    /**
     * Initializes the {@link #stage} actors and its layout.
     * Should be called after Knight data has been initialized.
     */
    public void init() {
        stage = new Stage(new ScreenViewport());

        // Soul bar
        soulBar = new SoulBarWidget(assets);
        stage.addActor(soulBar);

        // Masks
        int maxMasks = knight.getMaxMasks();
        for (int i = 0; i < maxMasks; i++) {
            MaskWidget mask = new MaskWidget(assets);
            stage.addActor(mask);
            maskWidgets.add(mask);
        }

        layout();
        syncInitialState();
    }

    /** Positions all HUD elements. */
    public void layout() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Soul orb
        float orbX = MARGIN_LEFT;
        float orbY = screenHeight - ORB_MARGIN_TOP - soulBar.getHeight();
        soulBar.setPosition(orbX, orbY);

        // Masks
        float maskStartX = orbX + soulBar.getWidth() - ORB_MASK_OVERLAP;
        float maskHeight = maskWidgets.isEmpty() ? 0 : maskWidgets.getFirst().getHeight();

        for (int i = 0; i < maskWidgets.size(); i++) {
            MaskWidget mask = maskWidgets.get(i);
            float maskX = maskStartX + i * (mask.getWidth() - MASK_OVERLAP);
            float maskY = screenHeight - MASK_MARGIN_TOP - maskHeight;
            mask.setPosition(maskX, maskY);
        }

        stage.getViewport().update((int) screenWidth, (int) screenHeight, true);
    }

    /** Syncs the HUD to the Knight's current health/soul. */
    private void syncInitialState() {
        int masks = knight.getMasks();
        int maxMasks = knight.getMaxMasks();

        for (int i = 0; i < maxMasks; i++) {
            if (i < masks) {
                maskWidgets.get(i).forceState(MaskWidget.MaskState.FILLED);
            } else {
                maskWidgets.get(i).forceState(MaskWidget.MaskState.EMPTY);
            }
        }
        soulBar.updateSoul(knight.getSoul());
    }

    public void update(float delta) {
        int currentMasks = knight.getMasks();

        // Update masks
        for (int i = 0; i < maskWidgets.size(); i++) {
            MaskWidget mask = maskWidgets.get(i);
            if (i < currentMasks) {
                if (mask.getState() == MaskWidget.MaskState.EMPTY ||
                    mask.getState() == MaskWidget.MaskState.BREAKING) {
                    mask.fill(true);
                }
            } else {
                if (mask.getState() == MaskWidget.MaskState.FILLED ||
                    mask.getState() == MaskWidget.MaskState.REFILLING) {
                    mask.breakMask(true);
                }
            }
        }

        // Update soul bar
        soulBar.updateSoul(knight.getSoul());

        // Tick the stage
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        layout();
    }

    public void dispose() {
        stage.dispose();
    }
}
