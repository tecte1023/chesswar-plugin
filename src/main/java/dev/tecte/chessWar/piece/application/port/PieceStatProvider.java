package dev.tecte.chessWar.piece.application.port;

import dev.tecte.chessWar.piece.application.port.dto.PieceStatsDto;
import dev.tecte.chessWar.piece.domain.model.PieceSpec;
import lombok.NonNull;

/**
 * 기물의 스탯 정보를 제공하는 포트 인터페이스입니다.
 */
public interface PieceStatProvider {
    /**
     * 주어진 기물 스펙에 해당하는 기물의 스탯 정보를 가져옵니다.
     *
     * @param spec 기물 스펙
     * @return 기물의 스탯 정보 (찾을 수 없는 경우 기본값 반환)
     */
    @NonNull
    PieceStatsDto getStats(@NonNull PieceSpec spec);
}
