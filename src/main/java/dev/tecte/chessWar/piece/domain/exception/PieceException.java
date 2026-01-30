package dev.tecte.chessWar.piece.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import lombok.NonNull;

/**
 * 기물 도메인 규칙 위반 시 발생하는 비즈니스 예외입니다.
 * <p>
 * 이 예외는 로그를 남기지 않고, 사용자에게 알림 메시지만 전달합니다.
 */
public class PieceException extends BusinessException {
    private PieceException(@NonNull String message) {
        super(message);
    }

    /**
     * 폰을 참전 대상으로 선택했을 때 발생합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static PieceException cannotSelectPawn() {
        return new PieceException("폰으로는 참전할 수 없습니다.");
    }

    /**
     * 이미 다른 플레이어가 참전 중인 기물을 선택하려 할 때 발생합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static PieceException alreadySelected() {
        return new PieceException("이미 참전 중인 기물입니다.");
    }
}
