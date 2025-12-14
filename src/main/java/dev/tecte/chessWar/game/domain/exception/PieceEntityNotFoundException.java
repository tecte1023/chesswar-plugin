package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Notifiable;
import lombok.NonNull;

/**
 * Piece 객체는 존재하지만, 실제 월드 상의 엔티티를 찾을 수 없을 때 발생하는 예외입니다.
 */
public class PieceEntityNotFoundException extends BusinessException implements Notifiable {
    private PieceEntityNotFoundException(@NonNull String message) {
        super(message);
    }

    /**
     * 기물의 엔티티를 찾을 수 없을 때 이 예외를 생성합니다.
     *
     * @return 생성된 {@link PieceEntityNotFoundException} 인스턴스
     */
    @NonNull
    public static PieceEntityNotFoundException entityNotFound() {
        return new PieceEntityNotFoundException("해당 기물의 정보를 찾을 수 없습니다.");
    }
}
