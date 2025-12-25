package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Notifiable;
import lombok.NonNull;

/**
 * 게임 도메인 내에서 플레이어의 기물 선택(Piece Selection) 및 참전 처리 시 발생하는 예외를 정의합니다.
 */
public class PieceClassException extends BusinessException implements Notifiable {
    private PieceClassException(@NonNull String message) {
        super(message);
    }

    /**
     * 폰을 참전 대상으로 선택했을 때 발생하는 예외를 생성합니다.
     *
     * @return 폰 선택 불가 예외
     */
    @NonNull
    public static PieceClassException pawnIsNotSelectable() {
        return new PieceClassException("폰으로는 참전할 수 없습니다.");
    }

    /**
     * 선택하려는 기물을 찾을 수 없을 때 발생하는 예외를 생성합니다.
     *
     * @return 기물 찾기 실패 예외
     */
    @NonNull
    public static PieceClassException pieceNotFound() {
        return new PieceClassException("해당 기물을 찾을 수 없습니다.");
    }

    /**
     * 이미 다른 플레이어가 참전 중인 기물을 선택하려 할 때 발생하는 예외를 생성합니다.
     *
     * @return 기물 중복 선택 예외
     */
    @NonNull
    public static PieceClassException alreadySelected() {
        return new PieceClassException("이미 참전 중인 기물입니다.");
    }
}
