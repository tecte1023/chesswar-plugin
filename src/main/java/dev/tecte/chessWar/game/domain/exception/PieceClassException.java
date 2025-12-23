package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Notifiable;
import lombok.NonNull;

/**
 * 게임 도메인 내에서 플레이어 전직(Class Change) 처리 시 발생하는 예외를 정의합니다.
 */
public class PieceClassException extends BusinessException implements Notifiable {
    private PieceClassException(@NonNull String message) {
        super(message);
    }

    /**
     * 폰을 전직 대상으로 선택했을 때 발생하는 예외를 생성합니다.
     *
     * @return 폰 선택 불가 예외
     */
    @NonNull
    public static PieceClassException pawnIsNotSelectable() {
        return new PieceClassException("폰으로는 전직할 수 없습니다.");
    }

    /**
     * 기물을 찾을 수 없을 때 발생하는 예외를 생성합니다.
     *
     * @return 기물 찾기 실패 예외
     */
    @NonNull
    public static PieceClassException pieceNotFound() {
        return new PieceClassException("해당 기물을 찾을 수 없습니다.");
    }
}
