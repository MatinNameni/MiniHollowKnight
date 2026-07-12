package com.github.matinnameni.minihollowknight.model.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the sound effects (SFX) used during gameplay.
 */
public class SoundEffectAssetBundle extends AssetBundle {

    public static final String KEY = "soundEffects";

    // --- Asset paths ---
    private static final String SWORD_SLASH = "Audio_Files/mage_knight_sword.wav";
    private static final String DOWN_SLASH = "Audio_Files/mage_knight_downslash.wav";
    private static final String HERO_DAMAGE = "Audio_Files/hero_damage.wav";
    private static final String ENEMY_DAMAGE = "Audio_Files/enemy_damage.wav";
    private static final String FOCUS_CHARGING = "Audio_Files/focus_health_charging.wav";
    private static final String FOCUS_HEAL = "Audio_Files/focus_health_heal.wav";
    private static final String SOUL_PICKUP = "Audio_Files/soul_pickup_1.wav";
    private static final String BREAKABLE_WALL_HIT = "Audio_Files/breakable_wall_hit_1.wav";
    private static final String BREAKABLE_WALL_DEATH = "Audio_Files/breakable_wall_death.wav";

    private static final String ZOTE_01 = "Audio_Files/Zote_01.wav";
    private static final String ZOTE_02 = "Audio_Files/Zote_02.wav";
    private static final String ZOTE_03 = "Audio_Files/Zote_03.wav";
    private static final String ZOTE_04 = "Audio_Files/Zote_04.wav";
    private static final String ZOTE_05 = "Audio_Files/Zote_05.wav";

    private final List<String> assetPaths = new ArrayList<>();

    public SoundEffectAssetBundle(AssetManager manager) {
        super(manager);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void queue() {
        manager.load(SWORD_SLASH, Sound.class);
        assetPaths.add(SWORD_SLASH);
        manager.load(DOWN_SLASH, Sound.class);
        assetPaths.add(DOWN_SLASH);
        manager.load(HERO_DAMAGE, Sound.class);
        assetPaths.add(HERO_DAMAGE);
        manager.load(ENEMY_DAMAGE, Sound.class);
        assetPaths.add(ENEMY_DAMAGE);
        manager.load(FOCUS_CHARGING, Sound.class);
        assetPaths.add(FOCUS_CHARGING);
        manager.load(FOCUS_HEAL, Sound.class);
        assetPaths.add(FOCUS_HEAL);
        manager.load(SOUL_PICKUP, Sound.class);
        assetPaths.add(SOUL_PICKUP);
        manager.load(BREAKABLE_WALL_HIT, Sound.class);
        assetPaths.add(BREAKABLE_WALL_HIT);
        manager.load(BREAKABLE_WALL_DEATH, Sound.class);
        assetPaths.add(BREAKABLE_WALL_DEATH);
        manager.load(ZOTE_01, Sound.class);
        assetPaths.add(ZOTE_01);
        manager.load(ZOTE_02, Sound.class);
        assetPaths.add(ZOTE_02);
        manager.load(ZOTE_03, Sound.class);
        assetPaths.add(ZOTE_03);
        manager.load(ZOTE_04, Sound.class);
        assetPaths.add(ZOTE_04);
        manager.load(ZOTE_05, Sound.class);
        assetPaths.add(ZOTE_05);
    }

    @Override
    public void onLoaded() {
        // Nothing extra to build after loading.
    }

    @Override
    public List<String> getAssetPaths() {
        return assetPaths;
    }

    // --- Accessors ---

    /** Default knight slash (left / right / up attack). */
    public Sound getSwordSlash() {
        return manager.get(SWORD_SLASH, Sound.class);
    }

    /** Down-facing slash (pogo-style). */
    public Sound getDownSlash() {
        return manager.get(DOWN_SLASH, Sound.class);
    }

    /** Played when the knight takes damage. */
    public Sound getHeroDamage() {
        return manager.get(HERO_DAMAGE, Sound.class);
    }

    /** Played when any enemy takes damage. */
    public Sound getEnemyDamage() {
        return manager.get(ENEMY_DAMAGE, Sound.class);
    }

    /** Looping sound played while the knight is focusing. */
    public Sound getFocusCharging() {
        return manager.get(FOCUS_CHARGING, Sound.class);
    }

    /** Played once when the focus heal completes. */
    public Sound getFocusHeal() {
        return manager.get(FOCUS_HEAL, Sound.class);
    }

    /** Played when the knight gains soul. */
    public Sound getSoulPickup() {
        return manager.get(SOUL_PICKUP, Sound.class);
    }

    /** Played when player hit a breakable wall. */
    public Sound getBreakableWallHit() {
        return manager.get(BREAKABLE_WALL_HIT, Sound.class);
    }

    /** Played when a breakable wall gets smashed. */
    public Sound getBreakableWallDeath() {
        return manager.get(BREAKABLE_WALL_DEATH, Sound.class);
    }

    public Sound getZote01() {
        return manager.get(ZOTE_01, Sound.class);
    }

    public Sound getZote02() {
        return manager.get(ZOTE_02, Sound.class);
    }

    public Sound getZote03() {
        return manager.get(ZOTE_03, Sound.class);
    }

    public Sound getZote04() {
        return manager.get(ZOTE_04, Sound.class);
    }

    public Sound getZote05() {
        return manager.get(ZOTE_05, Sound.class);
    }
}
