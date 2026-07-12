package com.github.matinnameni.minihollowknight.controller;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.model.asset.SoundEffectAssetBundle;
import com.github.matinnameni.minihollowknight.model.data.Settings;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Zote;
import com.github.matinnameni.minihollowknight.model.enums.KnightState;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.event.EventListener;
import com.github.matinnameni.minihollowknight.model.localization.Lang;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controller for the Zote NPC.
 */
public class ZoteController implements EventListener {
    // --- Constants ---
    public static final float INTERACTION_AREA_WIDTH = 200f;
    public static final float INTERACTION_AREA_HEIGHT = 150f;
    public static final float TYPEWRITER_CHARS_PER_SECOND = 60f;
    /** Number of introductory lines played the first time the player meets Zote. */
    private static final int INTRO_LINE_COUNT = 3;

    private final Knight knight;
    private final Settings settings;
    private final SoundEffectAssetBundle sfxAssets;

    /** Active Zote NPC; set when the map has a Zote spawn. */
    private Zote zote;

    /** True after the knight has overlapped the interaction area this frame. */
    private boolean knightInRange = false;

    // --- Dialogue box state machine ---

    private boolean dialogueOpen = false;
    private boolean introFinished = false;

    /** Index of the next intro line to play (0-based). */
    private int introIndex = 0;

    /** Precept index used to cycle through the Precepts in order; randomized on first use. */
    private int nextPreceptIndex = 0;
    private final Random random = new Random();

    /** The full text of the line currently being shown. */
    private String currentLine = "";

    /** How many characters of {@link #currentLine} are visible right now. */
    private int revealedChars = 0;

    /** Accumulator for the typewriter effect. */
    private float typewriterAccumulator = 0f;

    /** Cached voice SFX pool. */
    private final List<Sound> voicePool = new ArrayList<>();

    public ZoteController(Knight knight, Settings settings, SoundEffectAssetBundle sfxAssets) {
        this.knight = knight;
        this.settings = settings;
        this.sfxAssets = sfxAssets;

        buildVoicePool();
        EventBus.getInstance().subscribe(GameEvent.ENEMY_DAMAGED, this);
    }

    private void buildVoicePool() {
        if (sfxAssets == null) return;
        voicePool.add(sfxAssets.getZote01());
        voicePool.add(sfxAssets.getZote02());
        voicePool.add(sfxAssets.getZote03());
        voicePool.add(sfxAssets.getZote04());
        voicePool.add(sfxAssets.getZote05());
    }

    /**
     * Called by {@link GameScreenController} when a new map is loaded.
     * Binds this controller to the Zote NPC and interaction rectangle on
     * that map (or unbinds it if the map has no Zote).
     */
    public void bindToMap(TiledGameMap map, Zote zote) {
        this.zote = zote;

        // If the player leaves a map mid-dialogue (e.g., via a transfer
        // trigger), make sure we close the dialogue box cleanly.
        if (zote == null && dialogueOpen) {
            closeDialogue();
        }
    }

    // --- Per-frame update ---

    public void update(float delta) {
        if (zote == null || getZoteInteractionArea() == null) {
            knightInRange = false;
            return;
        }

        knightInRange = knight.getBounds().overlaps(getZoteInteractionArea());

        if (dialogueOpen) {
            if (revealedChars < currentLine.length()) {
                typewriterAccumulator += delta;
                float secondsPerChar = 1f / TYPEWRITER_CHARS_PER_SECOND;
                while (typewriterAccumulator >= secondsPerChar
                    && revealedChars < currentLine.length()) {
                    revealedChars++;
                    typewriterAccumulator -= secondsPerChar;
                }
            }
        }
    }

    // --- Interaction Box ---

    public Rectangle getZoteInteractionArea() {
        if (zote == null) return null;

        float areaX = zote.getBounds().x - INTERACTION_AREA_WIDTH / 2f;
        float areaY = zote.getBounds().y;

        return new Rectangle(
            areaX,
            areaY,
            INTERACTION_AREA_WIDTH,
            INTERACTION_AREA_HEIGHT
        );
    }

    // --- Input hooks ---

    /**
     * Called by the input layer when the interaction key has just been
     * pressed. Opens the dialogue if the knight is in range and no
     * dialogue is already open.
     */
    public void onInteractKeyPressed() {
        if (zote == null || getZoteInteractionArea() == null) return;
        if (dialogueOpen) return;
        if (!knightInRange) return;

        openDialogue();
    }

    /**
     * Called by the input layer when the advance key (Enter) has just
     * been pressed. If the typewriter is still mid-line, snap to the full
     * line; otherwise advance to the next line or close the box.
     */
    public void onAdvanceKeyPressed() {
        if (!dialogueOpen) return;

        if (revealedChars < currentLine.length()) {
            revealedChars = currentLine.length();
            typewriterAccumulator = 0f;
            return;
        }

        advanceToNextLine();
    }

    /** Called by the input layer when the player pressed Escape / Pause while the box is open. */
    public void onCloseKeyPressed() {
        if (dialogueOpen) {
            closeDialogue();
        }
    }

    // --- Dialogue state machine ---

    private void openDialogue() {
        dialogueOpen = true;
        zote.startTalking();
        EventBus.getInstance().publish(GameEvent.UI_SHOW_DIALOGUE);

        if (!introFinished) {
            currentLine = Lang.get("zote.line." + (introIndex + 1));
        } else {
            currentLine = Lang.get("zote.precept." + nextPreceptKey());
        }

        revealedChars = 0;
        typewriterAccumulator = 0f;
        playRandomVoice();
    }

    private void advanceToNextLine() {
        if (!introFinished) {
            introIndex++;
            if (introIndex >= INTRO_LINE_COUNT) {
                introFinished = true;
                nextPreceptIndex = 0;
                closeDialogue();
                return;
            }
            currentLine = Lang.get("zote.line." + (introIndex + 1));
        } else {
            nextPreceptIndex = (nextPreceptIndex + 1) % PRECEPT_KEYS.length;
            closeDialogue();
            return;
        }

        revealedChars = 0;
        typewriterAccumulator = 0f;
        playRandomVoice();
    }

    private void closeDialogue() {
        dialogueOpen = false;
        currentLine = "";
        revealedChars = 0;
        typewriterAccumulator = 0f;
        if (zote != null) {
            zote.stopTalking();
        }
        EventBus.getInstance().publish(GameEvent.UI_HIDE_DIALOGUE);
    }

    /** @return the precept index (1-based) that should be shown next. */
    private int nextPreceptKey() {
        return nextPreceptIndex + 1;
    }

    /** Plays a random voice clip from the pool. */
    private void playRandomVoice() {
        if (voicePool.isEmpty()) return;
        float vol = settings.isSfxEnabled() ? settings.getSfxVolume() : 0f;
        if (vol <= 0f) return;

        Sound clip = voicePool.get(random.nextInt(voicePool.size()));
        if (clip != null) {
            clip.play(vol);
        }
    }

    // --- EventListener ---

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event == GameEvent.ENEMY_DAMAGED && payload == zote && dialogueOpen) {
            closeDialogue();
        }
    }

    // --- Getters ---

    /** @return true if a dialogue box should currently be rendered. */
    public boolean isDialogueOpen() {
        return dialogueOpen;
    }

    /** @return true if the "press E" prompt should be shown above Zote. */
    public boolean shouldShowInteractPrompt() {
        return knightInRange && !dialogueOpen && zote != null && zote.getState() == Zote.State.IDLE;
    }

    /** @return the visible substring of the current line. */
    public String getVisibleText() {
        if (currentLine == null || currentLine.isEmpty()) return "";
        int end = Math.min(revealedChars, currentLine.length());
        return currentLine.substring(0, end);
    }

    /** @return true when the typewriter has revealed every character of the current line. */
    public boolean isLineFullyRevealed() {
        return revealedChars >= currentLine.length();
    }

    /** @return the active Zote NPC (may be {@code null} on maps without one). */
    public Zote getZote() {
        return zote;
    }

    /** @return the knight's current X position (used to position the E prompt). */
    public float getKnightX() {
        return knight.getPosition().x;
    }

    /** @return the knight's current Y position (used to position the E prompt). */
    public float getKnightY() {
        return knight.getPosition().y;
    }

    // --- Lifecycle ---

    public void dispose() {
        EventBus.getInstance().unsubscribe(GameEvent.ENEMY_DAMAGED, this);
    }

    // --- Precept key table ---

    public static final int PRECEPT_COUNT = 20;

    /** Valid precept keys. */
    public static final int[] PRECEPT_KEYS = buildPreceptKeys();

    private static int[] buildPreceptKeys() {
        int[] keys = new int[PRECEPT_COUNT];
        for (int i = 0; i < PRECEPT_COUNT; i++) {
            keys[i] = i + 1;
        }
        return keys;
    }
}
