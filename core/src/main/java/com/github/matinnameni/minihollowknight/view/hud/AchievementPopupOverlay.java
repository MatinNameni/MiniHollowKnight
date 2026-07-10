package com.github.matinnameni.minihollowknight.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.matinnameni.minihollowknight.event.EventBus;
import com.github.matinnameni.minihollowknight.event.GameEvent;
import com.github.matinnameni.minihollowknight.event.EventListener;
import com.github.matinnameni.minihollowknight.model.Lang;
import com.github.matinnameni.minihollowknight.model.asset.AchievementAssetBundle;
import com.github.matinnameni.minihollowknight.model.asset.MenuAssetBundle;
import com.github.matinnameni.minihollowknight.model.enums.AchievementType;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * In-game popup that slides in whenever an achievement is unlocked.
 */
public class AchievementPopupOverlay implements EventListener {

    // --- Timing ---
    private static final float SLIDE_IN_DURATION = 0.35f;
    private static final float HOLD_DURATION = 3.0f;
    private static final float SLIDE_OUT_DURATION = 0.4f;
    private static final float BETWEEN_POPUPS = 0.15f;

    // --- Layout ---
    private static final float BANNER_WIDTH = 420f;
    private static final float BANNER_HEIGHT = 92f;
    private static final float ICON_SIZE = 64f;
    private static final float MARGIN_TOP = 24f;
    private static final float HEADER_FONT_SCALE = 0.85f;
    private static final float NAME_FONT_SCALE = 1.1f;
    private static final float DESC_FONT_SCALE = 0.8f;

    private final AchievementAssetBundle achievementAssets;
    private final Skin skin;

    private Stage stage;
    private Table currentBanner;
    private boolean dismissing = false;

    /** Achievements waiting to be shown while another banner is on screen. */
    private final Queue<AchievementType> pending = new ArrayDeque<>();

    /** Time until the next pending achievement should be shown. */
    private float cooldown = 0f;

    public AchievementPopupOverlay(AchievementAssetBundle achievementAssets, MenuAssetBundle menuAssets) {
        this.achievementAssets = achievementAssets;
        this.skin = menuAssets.createSkin();
    }

    /** Builds the stage and subscribes to popup events. */
    public void init() {
        stage = new Stage(new ScreenViewport());
        EventBus.getInstance().subscribe(GameEvent.UI_ACHIEVEMENT_POPUP, this);
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event != GameEvent.UI_ACHIEVEMENT_POPUP) return;
        if (payload instanceof AchievementType) {
            pending.add((AchievementType) payload);
        }
    }

    /** Advances the animation and spawns queued popups. */
    public void update(float delta) {
        if (stage == null) return;

        stage.act(delta);

        // If nothing is currently shown, try to spawn the next queued popup.
        if (currentBanner == null && cooldown > 0f) {
            cooldown -= delta;
        }
        if (currentBanner == null && cooldown <= 0f && !pending.isEmpty()) {
            showBanner(pending.poll());
        }
    }

    /** Draws the current banner (if any) on top of everything. */
    public void draw() {
        if (stage == null) return;
        stage.draw();
    }

    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(GameEvent.UI_ACHIEVEMENT_POPUP, this);
        if (stage != null) stage.dispose();
    }

    // --- Banner lifecycle ---

    /** Builds, positions and animates in the banner for {@code type}. */
    private void showBanner(AchievementType type) {
        currentBanner = buildBanner(type);

        float screenWidth = stage.getWidth();
        // Start just above the visible area.
        float startY = stage.getHeight(); // top edge
        float targetY = stage.getHeight() - BANNER_HEIGHT - MARGIN_TOP;
        float targetX = (screenWidth - BANNER_WIDTH) / 2f;

        currentBanner.setPosition(targetX, startY);
        stage.addActor(currentBanner);

        dismissing = false;
        // Slide in, hold, then slide out, then remove + show next.
        currentBanner.addAction(
            Actions.sequence(
                Actions.moveTo(targetX, targetY, SLIDE_IN_DURATION),
                Actions.delay(HOLD_DURATION),
                Actions.run(this::markDismissing),
                Actions.moveTo(targetX, startY, SLIDE_OUT_DURATION),
                Actions.run(this::finishCurrent)
            )
        );
    }

    private void markDismissing() {
        dismissing = true;
    }

    /** Removes the current banner and arms the cooldown before the next one. */
    private void finishCurrent() {
        if (currentBanner != null) {
            currentBanner.remove();
            currentBanner = null;
        }
        dismissing = false;
        cooldown = BETWEEN_POPUPS;
    }

    /** Builds the banner table for a single achievement. */
    private Table buildBanner(AchievementType type) {
        Table banner = new Table();
        banner.setSize(BANNER_WIDTH, BANNER_HEIGHT);
        banner.defaults().pad(6f);

        // Icon
        Texture iconTexture = achievementAssets.getIcon(type);
        Image icon = new Image(new TextureRegionDrawable(new TextureRegion(iconTexture)));
        icon.setScaling(Scaling.fit);
        icon.setColor(Color.WHITE);
        banner.add(icon).size(ICON_SIZE).left().padLeft(10f);

        // Text column
        Table text = new Table();
        text.left().top();
        text.defaults().left().padLeft(6f);

        Label header = new Label(Lang.get("achievements.popup.title"), skin);
        header.setFontScale(HEADER_FONT_SCALE);
        header.setColor(new Color(0.85f, 0.78f, 0.45f, 1f));
        text.add(header).left().row();

        Label name = new Label(Lang.get(type.displayName), skin);
        name.setFontScale(NAME_FONT_SCALE);
        name.setColor(Color.WHITE);
        text.add(name).left().row();

        Label description = new Label(Lang.get(type.description), skin);
        description.setFontScale(DESC_FONT_SCALE);
        description.setColor(new Color(0.82f, 0.82f, 0.82f, 1f));
        text.add(description).left();

        banner.add(text).grow().fill().padRight(12f);

        return banner;
    }

    /** @return {@code true} while a banner is currently animating on screen. */
    public boolean isShowing() {
        return currentBanner != null;
    }

    /** @return {@code true} while the banner is sliding out. */
    public boolean isDismissing() {
        return dismissing;
    }
}
