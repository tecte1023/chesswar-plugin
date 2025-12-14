package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Loggable;
import dev.tecte.chessWar.common.exception.Notifiable;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

/**
 * 게임 객체를 찾을 수 없을 때 발생하는 예외입니다.
 */
public class GameNotFoundException extends BusinessException implements Loggable, Notifiable {
    private static final Component NOTIFICATION = Component.text("진행 중인 게임 정보를 찾을 수 없습니다.");

    private GameNotFoundException(@NonNull String message) {
        super(message);
    }

    /**
     * 기물 소환 도중 게임을 찾을 수 없을 때 이 예외를 생성합니다.
     *
     * @return {@link GameNotFoundException}의 새 인스턴스
     */
    @NonNull
    public static GameNotFoundException duringPieceSpawning() {
        return new GameNotFoundException("Game not found during piece spawning.");
    }

    /**
     * 단계 전환 도중 게임을 찾을 수 없을 때 이 예외를 생성합니다.
     *
     * @param phase 전환하려는 단계
     * @return {@link GameNotFoundException}의 새 인스턴스
     */
    @NonNull
    public static GameNotFoundException duringPhaseTransition(@NonNull GamePhase phase) {
        return new GameNotFoundException("Game not found during phase transition: " + phase.name());
    }

    /**
     * 진행 중인 게임이 없는데 게임 관련 작업을 시도할 때 이 예외를 생성합니다.
     *
     * @return {@link GameNotFoundException}의 새 인스턴스
     */
    @NonNull
    public static GameNotFoundException noGameInProgress() {
        return new GameNotFoundException("No game currently in progress.");
    }

    @NonNull
    @Override
    public Component getNotificationComponent() {
        return NOTIFICATION;
    }
}
