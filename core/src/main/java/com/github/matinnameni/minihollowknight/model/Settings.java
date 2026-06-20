package com.github.matinnameni.minihollowknight.model;

import com.badlogic.gdx.Input;
import com.github.matinnameni.minihollowknight.model.enums.SupportedLanguage;

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
    private int keyLeft   = Input.Keys.LEFT;
    private int keyRight  = Input.Keys.RIGHT;
    private int keyJump   = Input.Keys.Z;
    private int keyAttack = Input.Keys.X;
    private int keyDash   = Input.Keys.C;
    private int keyFocus  = Input.Keys.A;
    private int keyInteract = Input.Keys.E;
    private int keyInventory = Input.Keys.I;
    private int keyPause  = Input.Keys.ESCAPE;

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
        settingDup.keyJump = this.keyJump;
        settingDup.keyAttack = this.keyAttack;
        settingDup.keyDash = this.keyDash;
        settingDup.keyFocus = this.keyFocus;
        settingDup.keyInteract = this.keyInteract;
        settingDup.keyInventory = this.keyInventory;
        settingDup.keyPause = this.keyPause;
        return settingDup;
    }

    /** Resets all key bindings to defaults. */
    public void resetKeys() {
        keyLeft = Input.Keys.LEFT;
        keyRight = Input.Keys.RIGHT;
        keyJump = Input.Keys.Z;
        keyAttack = Input.Keys.X;
        keyDash = Input.Keys.C;
        keyFocus = Input.Keys.A;
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
