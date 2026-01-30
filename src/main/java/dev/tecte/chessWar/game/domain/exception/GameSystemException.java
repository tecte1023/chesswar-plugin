package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.NotifiableSystemException;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 게임 도메인에서 발생하는 사용자 알림이 필요한 시스템 예외입니다.
 * <p>
 * 이 예외는 시스템 로그를 기록하고, 사용자에게도 알림 메시지를 전달합니다.
 */
public class GameSystemException extends NotifiableSystemException {
    private GameSystemException(@NonNull String internalMessage, @NonNull String userMessage) {
        super(internalMessage, userMessage);
    }

    private GameSystemException(
            @NonNull String internalMessage,
            @NonNull String userMessage,
            @Nullable Throwable cause
    ) {
        super(internalMessage, userMessage, cause);
    }

    /**
     * 기물 선택에 실패했을 때 발생합니다.
     *
     * @param pieceId 선택 대상 기물의 식별자
     * @param cause   실패 원인
     * @return 생성된 예외
     */
    @NonNull
    public static GameSystemException pieceSelectionFailed(@NonNull UUID pieceId, @NonNull Throwable cause) {
        return new GameSystemException(
                "Failed to select piece [Target: %s]".formatted(pieceId),
                "시스템 오류로 해당 기물로 참전하는 데 실패했습니다.",
                cause
        );
    }

    /**
     * 게임 시작이 중단되었을 때 발생합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static GameSystemException gameStartInterrupted() {
        return new GameSystemException(
                "Game start process interrupted [Reason: Game instance missing during piece registration]",
                "시스템 오류로 게임을 시작하는 데 실패했습니다."
        );
    }

    /**
     * 게임 단계 전환이 중단되었을 때 발생합니다.
     *
     * @param targetPhase 전환 대상 단계
     * @return 생성된 예외
     */
    @NonNull
    public static GameSystemException gameTransitionInterrupted(@NonNull GamePhase targetPhase) {
        return new GameSystemException(
                "Game phase transition interrupted [Target: %s, Reason: Game instance missing]"
                        .formatted(targetPhase.name()),
                "시스템 오류로 게임을 진행하는 데 실패했습니다."
        );
    }
}
