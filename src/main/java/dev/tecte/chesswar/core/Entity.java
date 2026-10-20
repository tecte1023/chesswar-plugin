package dev.tecte.chesswar.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public final class Entity {
    @Getter
    private final int id;
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();

    public <T extends Component> void attach(final Class<T> type, final T component) {
        components.put(type, component);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T component(final Class<T> type) {
        return (T) components.get(type);
    }

    public <T extends Component> boolean hasComponent(final Class<T> type) {
        return components.containsKey(type);
    }
}
