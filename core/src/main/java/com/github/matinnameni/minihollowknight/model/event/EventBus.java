package com.github.matinnameni.minihollowknight.model.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton event bus.
 */
public class EventBus {
    private static EventBus instance;

    public static EventBus getInstance() {
        if (instance == null) instance = new EventBus();
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private final Map<GameEvent, List<EventListener>> listeners = new EnumMap<>(GameEvent.class);

    /**
     * Listeners added/removed while onEvent method is called for them,
     * go here and applied after the current loop finishes.
     */
    private final List<PendingChange> pendingChanges = new ArrayList<>();
    private boolean dispatching = false;

    /** Subscribe {@code listener} to {@code event}. */
    public void subscribe(GameEvent event, EventListener listener) {
        if (dispatching) {
            pendingChanges.add(new PendingChange(true, event, listener));
            return;
        }
        listenersFor(event).add(listener);
    }

    /** Unsubscribe {@code listener} from {@code event}. */
    public void unsubscribe(GameEvent event, EventListener listener) {
        if (dispatching) {
            pendingChanges.add(new PendingChange(false, event, listener));
            return;
        }
        List<EventListener> list = listeners.get(event);
        if (list != null) list.remove(listener);
    }

    /** Unsubscribe the {@code listener} from the events they subscribed to. */
    public void unsubscribeAll(EventListener listener) {
        for (GameEvent event : GameEvent.values()) {
            unsubscribe(event, listener);
        }
    }

    /** Publish {@code event} with no payload. */
    public void publish(GameEvent event) {
        publish(event, null);
    }

    /** Publish {@code event} with an optional {@code payload}. */
    public void publish(GameEvent event, Object payload) {
        List<EventListener> eventListeners = listeners.get(event);
        if (eventListeners == null || eventListeners.isEmpty()) return;

        dispatching = true;
        List<EventListener> listenersSnapshot = new ArrayList<>(eventListeners);
        for (EventListener l : listenersSnapshot) {
            l.onEvent(event, payload);
        }
        dispatching = false;

        applyPendingChanges();
    }

    // --- Helpers ---

    private List<EventListener> listenersFor(GameEvent event) {
        return listeners.computeIfAbsent(event, k -> new ArrayList<>());
    }

    private void applyPendingChanges() {
        for (PendingChange change : pendingChanges) {
            if (change.add) {
                listenersFor(change.event).add(change.listener);
            } else {
                List<EventListener> list = listeners.get(change.event);
                if (list != null) list.remove(change.listener);
            }
        }
        pendingChanges.clear();
    }

    private static final class PendingChange {
        final boolean add;
        final GameEvent event;
        final EventListener listener;

        public PendingChange(boolean add, GameEvent event, EventListener listener) {
            this.add = add;
            this.event = event;
            this.listener = listener;
        }
    }
}
