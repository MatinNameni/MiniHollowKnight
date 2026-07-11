package com.github.matinnameni.minihollowknight.model.enums;

import com.github.matinnameni.minihollowknight.model.localization.Lang;

public enum MenuStyle {
    CLASSIC("menuStyle.classic"),
    LIFEBLOOD("menuStyle.lifeblood"),
    HIDDEN_DREAMS("menuStyle.hiddenDreams");

    public final String style;

    MenuStyle(String style) {
        this.style = style;
    }

    @Override
    public String toString() {
        return Lang.get(this.style);
    }
}
