package com.github.matinnameni.minihollowknight.controller;

import com.github.matinnameni.minihollowknight.model.entity.enemies.Boss;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.event.EventListener;
import com.github.matinnameni.minihollowknight.model.achievement.AchievementManager;
import com.github.matinnameni.minihollowknight.model.entity.enemies.Enemy;
import com.github.matinnameni.minihollowknight.model.entity.enemies.FalseKnight;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.enums.AchievementType;

/**
 * The controller fot achievements
 */
public class AchievementController implements EventListener {

    public AchievementController() {
        EventBus bus = EventBus.getInstance();
        bus.subscribe(GameEvent.ENEMY_DIED, this);
        bus.subscribe(GameEvent.FALSE_KNIGHT_DEFEATED, this);
        bus.subscribe(GameEvent.GAME_COMPLETED, this);
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        AchievementManager manager = AchievementManager.getInstance();
        if (manager == null) return;

        switch (event) {
            case ENEMY_DIED:
                onEnemyDied(payload, manager);
                break;

            case FALSE_KNIGHT_DEFEATED:
                onFalseKnightDefeated(manager);
                break;

            case GAME_COMPLETED:
                onGameCompleted(manager);
                break;

            default:
                break;
        }
    }

    // --- Event handlers ---

    /**
     * Records an enemy kill (drives the True Hunter achievement) and bumps the
     * per-slot kill counter.
     */
    private void onEnemyDied(Object payload, AchievementManager manager) {
        if (!(payload instanceof Enemy)) return;
        Enemy enemy = (Enemy) payload;

        // Always record the kill type (bosses count toward True Hunter too).
        manager.recordEnemyKill(enemy);

        GameData data = manager.getActiveGameData();
        if (data != null && !(enemy instanceof Boss)) {
            data.enemiesKilled++;
        }
    }

    /**
     * Unlocks "Defeat False Knight" and bumps the boss counter. The False
     * Knight is also recorded as a killed enemy type (for True Hunter) by the
     * {@code ENEMY_DIED} event that the EnemySystem publishes for the same
     * death.
     */
    private void onFalseKnightDefeated(AchievementManager manager) {
        manager.unlock(AchievementType.DEFEAT_FALSE_KNIGHT);

        GameData data = manager.getActiveGameData();
        if (data != null) {
            data.bossesDefeated++;
        }
    }

    /** Unlocks Completion (and Speedrun if the run was fast enough). */
    private void onGameCompleted(AchievementManager manager) {
        manager.unlockCompletion();
    }
}
