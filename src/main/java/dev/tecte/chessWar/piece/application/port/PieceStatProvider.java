package dev.tecte.chessWar.piece.application.port;

import dev.tecte.chessWar.piece.application.port.dto.PieceStatsDto;
import lombok.NonNull;

/**
 * 기물의 스탯 정보를 제공하는 포트 인터페이스입니다.
 */
public interface PieceStatProvider {
    /**
     * 주어진 기물 식별자에 해당하는 기물의 스탯 정보를 가져옵니다.
     *
     * @param mobId 기물 식별자
     * @return 기물의 스탯 정보 (찾을 수 없는 경우 기본값 반환)
     */
    @NonNull
    PieceStatsDto getStats(@NonNull String mobId);
}
