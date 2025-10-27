package dev.tecte.chessWar.infrastructure.exception;

import com.google.inject.Provider;
import dev.tecte.chessWar.common.annotation.HandleException;
import dev.tecte.chessWar.port.exception.ExceptionHandler;
import dev.tecte.chessWar.port.notifier.SenderNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * {@link HandleException} 어노테이션이 붙은 메서드에서 발생하는 예외를 가로채는 AOP 인터셉터입니다.
 * <p>
 * 발생한 예외를 처리할 수 있는 {@link ExceptionHandler} 구현체들을 찾아 처리를 위임합니다.
 * 만약 적절한 핸들러가 없다면, 처리되지 않은 예외로 간주하고 에러 로그를 남깁니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
public class ExceptionHandlingInterceptor implements MethodInterceptor {
    @Inject
    private Provider<Set<ExceptionHandler>> handlersProvider;
    @Inject
    private SenderNotifier notifier;

    /**
     * 대상 메서드를 실행하고, 발생하는 모든 예외를 가로채서 처리합니다.
     * <p>
     * 예외가 발생하면, 해당 예외를 지원하는 {@link ExceptionHandler}를 찾아 처리를 위임합니다.
     * 만약 적절한 핸들러가 없다면, 처리되지 않은 예외로 간주하고 에러 로그를 남긴 후 사용자에게 일반적인 오류 메시지를 보냅니다.
     *
     * @param invocation 가로챈 메서드 호출 정보
     * @return 메서드 실행 결과. 예외 발생 시 null을 반환합니다.
     * @throws Throwable 처리할 수 없는 예외가 발생할 경우
     */
    @Nullable
    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Exception e) {
            Set<ExceptionHandler> handlers = handlersProvider.get();
            CommandSender sender = findSenderArgument(invocation.getArguments());
            List<ExceptionHandler> supportingHandlers = handlers.stream()
                    .filter(handler -> handler.supports(e))
                    .toList();

            if (supportingHandlers.isEmpty()) {
                log.error("Unhandled exception caught in method {}:", invocation.getMethod().getName(), e);

                if (sender != null) {
                    notifier.notifyError(sender, "알 수 없는 오류가 발생했습니다.");
                }
            } else {
                supportingHandlers.forEach(handler -> handler.handle(e, sender));
            }

            return null;
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
}
