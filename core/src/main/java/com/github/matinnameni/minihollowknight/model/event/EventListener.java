package com.github.matinnameni.minihollowknight.model.event;

/**
 * Interface for objects that want to react to game events
 */
public interface EventListener {
    void onEvent(GameEvent event, Object payload);
}
