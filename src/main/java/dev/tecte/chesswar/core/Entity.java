package dev.tecte.chesswar.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class Entity {
    @Getter private final UUID id;

    private final Map<Class<? extends Component>, Component> components = new HashMap<>();

    public <T extends Component> void attach(final Class<T> type, final T component) {
        components.put(type, component);
        component.onAttach(this);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(final Class<T> type) {
        return (T) components.get(type);
    }

    public <T extends Component> boolean hasComponent(final Class<T> type) {
        return components.containsKey(type);
    }
}
