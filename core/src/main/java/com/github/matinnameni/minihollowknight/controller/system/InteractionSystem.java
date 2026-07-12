package com.github.matinnameni.minihollowknight.controller.system;

import com.badlogic.gdx.math.Rectangle;
import com.github.matinnameni.minihollowknight.controller.input.KnightInputProcessor;
import com.github.matinnameni.minihollowknight.model.data.GameData;
import com.github.matinnameni.minihollowknight.model.entity.Knight;
import com.github.matinnameni.minihollowknight.model.enums.CharmType;
import com.github.matinnameni.minihollowknight.model.event.EventBus;
import com.github.matinnameni.minihollowknight.model.event.GameEvent;
import com.github.matinnameni.minihollowknight.model.map.TiledGameMap;

public class InteractionSystem {
    public void resolveVoidHeartItemInteraction (Knight knight, TiledGameMap gameMap, GameData gameData,
                                                 KnightInputProcessor knightInputProcessor) {
        if (gameMap == null) return;

        Rectangle interactionArea = gameMap.getVoidHeartInteractionArea();

        if (interactionArea == null) return;
        if (!knightInputProcessor.interactionKeyHeld()) return;
        if (gameMap.isVoidHeartRemoved()) return;
        if (!knight.getBounds().overlaps(interactionArea)) return;

        gameMap.removeVoidHeartCharm();
        gameData.collectedCharms.add(CharmType.VOID_HEART);
    }
}
