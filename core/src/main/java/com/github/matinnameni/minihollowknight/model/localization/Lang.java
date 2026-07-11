package com.github.matinnameni.minihollowknight.model.localization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import com.github.matinnameni.minihollowknight.model.enums.SupportedLanguage;

import java.util.MissingResourceException;
import java.util.Locale;

/**
 * Loads and exposes the game's active translation bundle.
 */
public final class Lang {
    private static final String BASE_PATH = "i18n/Strings";

    private static I18NBundle bundle;

    private Lang() { }

    /** Loads the bundle for language {@code language}. */
    public static void load(SupportedLanguage language) {
        FileHandle baseFileHandle = Gdx.files.internal(BASE_PATH);
        Locale locale = new Locale(language.shortName);
        bundle = I18NBundle.createBundle(baseFileHandle, locale, "UTF-8");
    }

    /** Returns the translated string for {@code key} in the currently loaded language. */
    public static String get(String key) {
        if (bundle == null) {
            throw new IllegalStateException("Lang.load() must be called before Lang.get().");
        }
        return bundle.get(key);
    }

    /** Returns the translated, formatted string for {@code key}. */
    public static String format(String key, Object... args) {
        if (bundle == null) {
            throw new IllegalStateException("Lang.load() must be called before Lang.format().");
        }
        return bundle.format(key, args);
    }
}
