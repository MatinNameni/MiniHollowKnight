package com.github.matinnameni.minihollowknight.model;

import com.badlogic.gdx.Input;
import com.github.matinnameni.minihollowknight.model.enums.SupportedLanguage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All game's configurable settings.
 */
public class Settings {
    // --- Audio ---
    private float musicVolume  = 1.0f; // 0..1
    private float sfxVolume    = 1.0f; // 0..1
    private boolean musicEnabled = true;
    private boolean sfxEnabled   = true;

    // --- Display ---
    private float brightness = 1.0f; // 0..1
    private String language = SupportedLanguage.ENGLISH.shortName;

    // --- Key bindings ---
    private int keyLeft = Input.Keys.LEFT;
    private int keyRight = Input.Keys.RIGHT;
    private int keyUp = Input.Keys.UP;
    private int keyDown = Input.Keys.DOWN;
    private int keyJump = Input.Keys.Z;
    private int keyAttack = Input.Keys.X;
    private int keyDash = Input.Keys.C;
    private int keyFocus = Input.Keys.A;
    private int keyCast = Input.Keys.S;
    private int keyInteract = Input.Keys.E;
    private int keyInventory = Input.Keys.I;
    private int keyPause = Input.Keys.ESCAPE;

    /** Returns a deep copy from this {@link Settings} */
    public Settings copy() {
        Settings settingDup = new Settings();
        settingDup.musicVolume = this.musicVolume;
        settingDup.sfxVolume = this.sfxVolume;
        settingDup.musicEnabled = this.musicEnabled;
        settingDup.sfxEnabled = this.sfxEnabled;
        settingDup.brightness = this.brightness;
        settingDup.language = this.language;
        settingDup.keyLeft = this.keyLeft;
        settingDup.keyRight = this.keyRight;
        settingDup.keyUp = this.keyUp;
        settingDup.keyDown = this.keyDown;
        settingDup.keyJump = this.keyJump;
        settingDup.keyAttack = this.keyAttack;
        settingDup.keyDash = this.keyDash;
        settingDup.keyFocus = this.keyFocus;
        settingDup.keyCast = this.keyCast;
        settingDup.keyInteract = this.keyInteract;
        settingDup.keyInventory = this.keyInventory;
        settingDup.keyPause = this.keyPause;
        return settingDup;
    }

    /** Resets all key bindings to defaults. */
    public void resetKeys() {
        keyLeft = Input.Keys.LEFT;
        keyRight = Input.Keys.RIGHT;
        keyUp = Input.Keys.UP;
        keyDown = Input.Keys.DOWN;
        keyJump = Input.Keys.Z;
        keyAttack = Input.Keys.X;
        keyDash = Input.Keys.C;
        keyFocus = Input.Keys.A;
        keyCast = Input.Keys.S;
        keyInteract = Input.Keys.E;
        keyInventory = Input.Keys.I;
        keyPause = Input.Keys.ESCAPE;
    }

    /** Resets all audio volumes to defaults. */
    public void resetVolumes() {
        musicEnabled = true;
        musicVolume = 1.0f;
        sfxEnabled = true;
        sfxVolume = 1.0f;
    }

    /**
     * @return a {@link Map} containing all game button keycodes,
     * keyed with their title
     */
    public Map<String, Integer> getKeys() {
        Map<String, Integer> keys = new LinkedHashMap<>();
        keys.put(Input.Keys.toString(keyLeft), keyLeft);
        keys.put(Input.Keys.toString(keyRight), keyRight);
        keys.put(Input.Keys.toString(keyUp), keyUp);
        keys.put(Input.Keys.toString(keyDown), keyDown);
        keys.put(Input.Keys.toString(keyJump), keyJump);
        keys.put(Input.Keys.toString(keyAttack), keyAttack);
        keys.put(Input.Keys.toString(keyDash), keyDash);
        keys.put(Input.Keys.toString(keyFocus), keyFocus);
        keys.put(Input.Keys.toString(keyCast), keyCast);
        keys.put(Input.Keys.toString(keyInteract), keyInteract);
        keys.put(Input.Keys.toString(keyInventory), keyInventory);
        keys.put(Input.Keys.toString(keyPause), keyPause);
        return keys;
    }

    // --- Getters ---

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public float getBrightness() {
        return brightness;
    }

    public String getLanguage() {
        return language;
    }

    public int getKeyLeft() {
        return keyLeft;
    }

    public int getKeyRight() {
        return keyRight;
    }

    public int getKeyUp() {
        return keyUp;
    }

    public int getKeyDown() {
        return keyDown;
    }

    public int getKeyJump() {
        return keyJump;
    }

    public int getKeyAttack() {
        return keyAttack;
    }

    public int getKeyDash() {
        return keyDash;
    }

    public int getKeyFocus() {
        return keyFocus;
    }

    public int getKeyCast() {
        return keyCast;
    }

    public int getKeyInteract() {
        return keyInteract;
    }

    public int getKeyInventory() {
        return keyInventory;
    }

    public int getKeyPause() {
        return keyPause;
    }

    // --- Setters ---

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public void setSfxEnabled(boolean sfxEnabled) {
        this.sfxEnabled = sfxEnabled;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setKeyLeft(int keyLeft) {
        this.keyLeft = keyLeft;
    }

    public void setKeyRight(int keyRight) {
        this.keyRight = keyRight;
    }

    public void setKeyUp(int keyUp) {
        this.keyUp = keyUp;
    }

    public void setKeyDown(int keyDown) {
        this.keyDown = keyDown;
    }

    public void setKeyJump(int keyJump) {
        this.keyJump = keyJump;
    }

    public void setKeyAttack(int keyAttack) {
        this.keyAttack = keyAttack;
    }

    public void setKeyDash(int keyDash) {
        this.keyDash = keyDash;
    }

    public void setKeyFocus(int keyFocus) {
        this.keyFocus = keyFocus;
    }

    public void setKeyCast(int keyCast) {
        this.keyCast = keyCast;
    }

    public void setKeyInteract(int keyInteract) {
        this.keyInteract = keyInteract;
    }

    public void setKeyInventory(int keyInventory) {
        this.keyInventory = keyInventory;
    }

    public void setKeyPause(int keyPause) {
        this.keyPause = keyPause;
    }
}
