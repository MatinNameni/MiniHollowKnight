package com.github.matinnameni.minihollowknight.model.charm;

import com.github.matinnameni.minihollowknight.model.localization.Lang;
import com.github.matinnameni.minihollowknight.model.enums.CharmType;

/**
 * Static metadata for every {@link CharmType}: display name, short description
 * and notch cost.
 */
public final class CharmCatalog {

    private CharmCatalog() { }

    /** Display name for {@code charm}. */
    public static String nameOf(CharmType charm) {
        switch (charm) {
            case SOUL_CATCHER: return Lang.get("charm.soulCatcher");
            case DASHMASTER: return Lang.get("charm.dashmaster");
            case UNBREAKABLE_STRENGTH: return Lang.get("charm.unbreakableStrength");
            case QUICK_SLASH: return Lang.get("charm.quickSlash");
            case QUICK_FOCUS: return Lang.get("charm.quickFocus");
            case HEAVY_BLOW: return Lang.get("charm.heavyBlow");
            case SHARP_SHADOW: return Lang.get("charm.sharpShadow");
            case VOID_HEART: return Lang.get("charm.voidHeart");
            default: throw new IllegalArgumentException("Unknown charm: " + charm);
        }
    }

    /**
     * Short gameplay description for {@code charm}, matching the spec's wording
     * (section "Charms &amp; Inventory System").
     */
    public static String descriptionOf(CharmType charm) {
        switch (charm) {
            case SOUL_CATCHER:
                return Lang.get("charm.soulCatcherDescription");
            case DASHMASTER:
                return Lang.get("charm.dashmasterDescription");
            case UNBREAKABLE_STRENGTH:
                return Lang.get("charm.unbreakableStrengthDescription");
            case QUICK_SLASH:
                return Lang.get("charm.quickSlashDescription");
            case QUICK_FOCUS:
                return Lang.get("charm.quickFocusDescription");
            case HEAVY_BLOW:
                return Lang.get("charm.heavyBlowDescription");
            case SHARP_SHADOW:
                return Lang.get("charm.sharpShadowDescription");
            case VOID_HEART:
                return Lang.get("charm.voidHeartDescription");
            default: throw new IllegalArgumentException("Unknown charm: " + charm);
        }
    }

    /** Notch cost for {@code charm} (delegates to {@link CharmType#notches}). */
    public static int notchCost(CharmType charm) {
        return charm.notches;
    }

    /** Whether {@code charm} is granted through a special in-world unlock. */
    public static boolean isUnlockable(CharmType charm) {
        return charm == CharmType.VOID_HEART;
    }
}
