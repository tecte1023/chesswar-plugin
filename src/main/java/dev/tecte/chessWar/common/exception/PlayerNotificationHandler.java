package dev.tecte.chessWar.common.exception;

import dev.tecte.chessWar.common.notifier.PlayerNotifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * PlayerNotifiableException을 구현하는 모든 예외를 처리하는 범용 핸들러입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlayerNotificationHandler implements ExceptionHandler<PlayerNotifiableException> {
    private final PlayerNotifier playerNotifier;

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull PlayerNotifiableException e) {
        playerNotifier.notifyError(e.getPlayerId(), e.getMessage());
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Class<PlayerNotifiableException> getExceptionType() {
        return PlayerNotifiableException.class;
    }
}
