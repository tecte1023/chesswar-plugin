package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Notifiable;
import lombok.NonNull;

/**
 * 게임 도메인 내에서 플레이어의 기물 선택 및 참전 처리 시 발생하는 예외를 정의합니다.
 */
public class PieceSelectionException extends BusinessException implements Notifiable {
    private PieceSelectionException(@NonNull String message) {
        super(message);
    }

    /**
     * 폰을 참전 대상으로 선택했을 때 발생하는 예외를 생성합니다.
     * <p>
     * 폰은 플레이어가 직접 조작할 수 없는 기물이므로 참전이 제한됩니다.
     *
     * @return 폰 선택 불가 예외
     */
    @NonNull
    public static PieceSelectionException pawnIsNotSelectable() {
        return new PieceSelectionException("폰으로는 참전할 수 없습니다.");
    }

    /**
     * 선택하려는 기물을 찾을 수 없을 때 발생하는 예외를 생성합니다.
     *
     * @return 기물 찾기 실패 예외
     */
    @NonNull
    public static PieceSelectionException pieceNotFound() {
        return new PieceSelectionException("해당 기물을 찾을 수 없습니다.");
    }

    /**
     * 이미 다른 플레이어가 참전 중인 기물을 선택하려 할 때 발생하는 예외를 생성합니다.
     *
     * @return 기물 중복 선택 예외
     */
    @NonNull
    public static PieceSelectionException alreadySelected() {
        return new PieceSelectionException("이미 참전 중인 기물입니다.");
    }
}
