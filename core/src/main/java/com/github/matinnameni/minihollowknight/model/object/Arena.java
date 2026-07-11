package com.github.matinnameni.minihollowknight.model.object;

import com.github.matinnameni.minihollowknight.model.entity.enemies.Boss;
import com.github.matinnameni.minihollowknight.model.enums.BossType;

public class Arena extends GridObject {
    public boolean haveBoss = false;
    public BossType arenaBossType;
    public Boss arenaBoss;

    public Arena(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public Arena(float x, float y, float width, float height, boolean haveBoss) {
        super(x, y, width, height);
        this.haveBoss = haveBoss;
    }

    public void resolveBossName(String bossName) {
        if (bossName == null || bossName.isEmpty()) {
            return;
        }

        switch (bossName) {
            case "falseKnight":
                arenaBossType = BossType.FALSE_KNIGHT;
                break;
            default:
                arenaBossType = null;
        }
    }
}
