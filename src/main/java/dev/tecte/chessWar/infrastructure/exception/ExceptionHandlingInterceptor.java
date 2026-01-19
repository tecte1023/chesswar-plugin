package dev.tecte.chessWar.infrastructure.exception;

import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.port.exception.ExceptionDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * {@link HandleException} 어노테이션이 붙은 메서드에서 발생하는 예외를 가로채는 AOP 인터셉터입니다.
 * <p>
 * 실제 예외 처리 로직은 {@link ExceptionDispatcher}에 위임합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
public class ExceptionHandlingInterceptor implements MethodInterceptor {
    @Inject
    private ExceptionDispatcher dispatcher;

    /**
     * 대상 메서드를 실행하고, 발생하는 모든 예외를 가로채서 {@link ExceptionDispatcher}에 처리를 위임합니다.
     *
     * @param invocation 가로챈 메서드 호출 정보
     * @return 메서드 실행 결과. 예외 발생 시 null 또는 어노테이션에 지정된 대체 값을 반환합니다.
     * @throws Throwable 처리할 수 없는 예외가 발생할 경우
     */
    @Nullable
    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Exception e) {
            Method method = invocation.getMethod();
            CommandSender sender = findSenderArgument(invocation.getArguments());

            dispatcher.dispatch(e, sender, "Method " + method.getName());

            return getFallbackReturnValue(method.getReturnType(), method.getAnnotation(HandleException.class));
        }
    }

    @Nullable
    private CommandSender findSenderArgument(@NonNull Object @NonNull [] args) {
        for (Object arg : args) {
            if (arg instanceof CommandSender sender) {
                return sender;
            }
        }

        return null;
    }

    @Nullable
    private Object getFallbackReturnValue(@NonNull Class<?> returnType, @Nullable HandleException annotation) {
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }

        if (returnType == Optional.class) {
            return Optional.empty();
        }

        if (returnType == boolean.class || returnType == Boolean.class) {
            return annotation != null && annotation.fallbackBoolean();
        }

        double fbDouble = annotation != null ? annotation.fallbackNumber() : 0.0;

        if (returnType == double.class || returnType == Double.class) {
            return fbDouble;
        } else if (returnType == float.class || returnType == Float.class) {
            return (float) fbDouble;
        }

        int fbInt = annotation != null ? annotation.fallbackInt() : 0;

        if (returnType == int.class || returnType == Integer.class) {
            return fbInt;
        } else if (returnType == long.class || returnType == Long.class) {
            return (long) fbInt;
        } else if (returnType == short.class || returnType == Short.class) {
            return (short) fbInt;
        } else if (returnType == byte.class || returnType == Byte.class) {
            return (byte) fbInt;
        } else if (returnType == char.class || returnType == Character.class) {
            return (char) fbInt;
        }

        return null;
    }
}
