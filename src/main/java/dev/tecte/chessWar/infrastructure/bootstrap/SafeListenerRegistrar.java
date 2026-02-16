package dev.tecte.chessWar.infrastructure.bootstrap;

import dev.tecte.chessWar.ChessWar;
import dev.tecte.chessWar.common.event.NotifiableEvent;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 모든 리스너를 예외 처리 안전망과 함께 등록합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
public class SafeListenerRegistrar {
    @Inject
    public SafeListenerRegistrar(
            @NonNull ChessWar plugin,
            @NonNull PluginManager pluginManager,
            @NonNull ExceptionDispatcher exceptionDispatcher,
            @NonNull Set<Listener> listeners
    ) {
        listeners.forEach(listener -> registerSafeEvents(plugin, pluginManager, exceptionDispatcher, listener));
    }

    private void registerSafeEvents(
            @NonNull ChessWar plugin,
            @NonNull PluginManager pluginManager,
            @NonNull ExceptionDispatcher exceptionDispatcher,
            @NonNull Listener listener
    ) {
        for (Method method : listener.getClass().getMethods()) {
            EventHandler handlerAnnotation = method.getAnnotation(EventHandler.class);

            if (handlerAnnotation == null) {
                continue;
            }

            Class<?>[] parameters = method.getParameterTypes();

            if (parameters.length != 1 || !Event.class.isAssignableFrom(parameters[0])) {
                log.atWarn().log("Invalid EventHandler method: {}.{}",
                        listener.getClass().getName(), method.getName());
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) parameters[0];

            pluginManager.registerEvent(
                    eventClass,
                    listener,
                    handlerAnnotation.priority(),
                    createSafeExecutor(method, exceptionDispatcher),
                    plugin,
                    handlerAnnotation.ignoreCancelled()
            );
        }
    }

    @NonNull
    private EventExecutor createSafeExecutor(@NonNull Method method, @NonNull ExceptionDispatcher exceptionDispatcher) {
        return (listener, event) -> {
            try {
                method.invoke(listener, event);
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                CommandSender sender = (event instanceof NotifiableEvent notifiable) ? notifiable.sender() : null;

                if (cause instanceof Exception exception) {
                    exceptionDispatcher.dispatch(exception, sender, "Listener " + method.getName());
                } else {
                    log.atError().setCause(cause).log("Fatal error in listener: {}.{}",
                            listener.getClass().getName(), method.getName());
                }
            }
        };
    }
}
