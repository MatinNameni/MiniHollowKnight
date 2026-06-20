package com.github.matinnameni.minihollowknight.model.enums;

public enum SupportedLanguage {
    ENGLISH("en", "English"),
    FRENCH("fr", "Français");

    public final String shortName;
    public final String displayName;

    SupportedLanguage(String shortName, String displayName) {
        this.shortName = shortName;
        this.displayName = displayName;
    }

    public static SupportedLanguage fromShortName(String shortName) {
        for (SupportedLanguage lang : values()) {
            if (lang.shortName.equals(shortName)) return lang;
        }
        return ENGLISH;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
