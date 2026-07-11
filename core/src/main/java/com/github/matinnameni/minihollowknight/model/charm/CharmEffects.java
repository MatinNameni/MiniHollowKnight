package com.github.matinnameni.minihollowknight.model.charm;

import com.github.matinnameni.minihollowknight.model.enums.CharmType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Resolves the currently equipped {@link CharmType}s into concrete gameplay modifiers.
 */
public final class CharmEffects {

    // --- Constants ---

    /** An effect set with every charm unequipped */
    public static final CharmEffects NONE = new CharmEffects(Collections.emptySet());

    // Soul Catcher
    private static final float SOUL_CATCHER_MULTIPLIER = 1.5f;

    // Dashmaster
    private static final float DASHMASTER_COOLDOWN_MULTIPLIER = 0.5f;

    // Unbreakable Strength
    private static final float UNBREAKABLE_STRENGTH_DAMAGE_MULTIPLIER = 1.5f;

    // Quick Slash
    private static final float QUICK_SLASH_DURATION_MULTIPLIER = 0.6f;
    private static final float QUICK_FOCUS_TIME_MULTIPLIER = 0.6f;

    // Heavy Blow
    private static final float HEAVY_BLOW_KNOCKBACK_MULTIPLIER = 1.8f;

    // Sharp Shadow
    private static final float SHARP_SHADOW_DASH_DURATION_MULTIPLIER = 1.4f;

    // Void Heart
    private static final float VOID_HEART_SPELL_DAMAGE_MULTIPLIER = 1.5f;

    private final Set<CharmType> equipped;

    private CharmEffects(Set<CharmType> equipped) {
        this.equipped = equipped;
    }

    /** Builds a {@link CharmEffects} snapshot from the currently equipped charms. */
    public static CharmEffects of(Set<CharmType> equippedCharms) {
        if (equippedCharms == null || equippedCharms.isEmpty()) return NONE;
        return new CharmEffects(EnumSet.copyOf(equippedCharms));
    }

    public boolean isEquipped(CharmType charm) {
        return equipped.contains(charm);
    }

    // --- Soul Catcher ---

    public float getSoulGainMultiplier() {
        return isEquipped(CharmType.SOUL_CATCHER) ? SOUL_CATCHER_MULTIPLIER : 1f;
    }

    // --- Dashmaster ---

    public float getDashCooldownMultiplier() {
        return isEquipped(CharmType.DASHMASTER) ? DASHMASTER_COOLDOWN_MULTIPLIER : 1f;
    }

    // --- Unbreakable Strength ---

    public float getNailDamageMultiplier() {
        return isEquipped(CharmType.UNBREAKABLE_STRENGTH) ? UNBREAKABLE_STRENGTH_DAMAGE_MULTIPLIER : 1f;
    }

    // --- Quick Slash ---

    public float getAttackDurationMultiplier() {
        return isEquipped(CharmType.QUICK_SLASH) ? QUICK_SLASH_DURATION_MULTIPLIER : 1f;
    }

    public float getFocusChannelTimeMultiplier() {
        return isEquipped(CharmType.QUICK_FOCUS) ? QUICK_FOCUS_TIME_MULTIPLIER : 1f;
    }

    // --- Heavy Blow ---

    public float getEnemyKnockbackMultiplier() {
        return isEquipped(CharmType.HEAVY_BLOW) ? HEAVY_BLOW_KNOCKBACK_MULTIPLIER : 1f;
    }

    // --- Sharp Shadow ---

    public boolean hasSharpShadow() {
        return isEquipped(CharmType.SHARP_SHADOW);
    }

    public float getDashDurationMultiplier() {
        return hasSharpShadow() ? SHARP_SHADOW_DASH_DURATION_MULTIPLIER : 1f;
    }

    // --- Void Heart ---

    public boolean hasVoidHeart() {
        return isEquipped(CharmType.VOID_HEART);
    }

    public float getSpellDamageMultiplier() {
        return hasVoidHeart() ? VOID_HEART_SPELL_DAMAGE_MULTIPLIER : 1f;
    }
}
