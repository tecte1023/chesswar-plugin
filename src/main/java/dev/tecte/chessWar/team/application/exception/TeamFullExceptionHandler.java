package dev.tecte.chessWar.team.application.exception;

import dev.tecte.chessWar.common.exception.ExceptionHandler;
import dev.tecte.chessWar.common.notifier.PlayerNotifier;
import dev.tecte.chessWar.team.domain.exception.TeamFullException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * {@link TeamFullException}을 처리하여 플레이어에게 팀이 가득 찼다는 알림을 보냅니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamFullExceptionHandler implements ExceptionHandler<TeamFullException> {
    private final PlayerNotifier playerNotifier;

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull TeamFullException e) {
        playerNotifier.notifyError(e.getPlayerId(), e.getMessage());
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Class<TeamFullException> getExceptionType() {
        return TeamFullException.class;
    }
}
