package dev.tecte.chessWar.piece.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Notifiable;
import lombok.NonNull;

import java.util.UUID;

/**
 * 기물을 찾을 수 없을 때 발생하는 범용 도메인 예외입니다.
 * <p>
 * 선택 단계, 전투 단계, 렌더링 등 다양한 상황에서 기물이 존재하지 않거나 소실되었을 때 사용합니다.
 */
public class PieceNotFoundException extends BusinessException implements Notifiable {
    private PieceNotFoundException(@NonNull String message) {
        super(message);
    }

    /**
     * 월드 상에서 기물 엔티티를 찾을 수 없을 때(렌더링 등) 예외를 생성합니다.
     */
    public static PieceNotFoundException entityMissing() {
        return new PieceNotFoundException("월드에서 해당 기물을 찾을 수 없습니다.");
    }

    /**
     * 기물 선택 과정에서 해당 기물을 찾을 수 없을 때 예외를 생성합니다.
     */
    public static PieceNotFoundException targetMissing() {
        return new PieceNotFoundException("선택하려는 기물이 존재하지 않거나 찾을 수 없습니다.");
    }
}
