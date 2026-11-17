package dev.tecte.chesswar.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

public final class SimulationEventBus {
    private final Map<Class<? extends SimulationEvent>, List<Consumer<SimulationEvent>>> listeners = new HashMap<>();
    private final Queue<SimulationEvent> pendingEvents = new ArrayDeque<>();
    private boolean publishing;

    @SuppressWarnings("unchecked")
    public <T extends SimulationEvent> void subscribe(final Class<T> eventType, final Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, type -> new ArrayList<>())
                 .add(event -> listener.accept((T) event));
    }

    public void publish(final SimulationEvent event) {
        pendingEvents.add(event);

        if (publishing) {
            return;
        }

        publishing = true;
        try {
            while (!pendingEvents.isEmpty()) {
                dispatch(pendingEvents.poll());
            }
        } finally {
            pendingEvents.clear();
            publishing = false;
        }
    }

    private void dispatch(final SimulationEvent event) {
        List<Consumer<SimulationEvent>> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null) {
            return;
        }

        for (Consumer<SimulationEvent> listener : eventListeners) {
            listener.accept(event);
        }
    }
}
