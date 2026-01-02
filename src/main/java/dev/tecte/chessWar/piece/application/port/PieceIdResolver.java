package dev.tecte.chessWar.piece.application.port;

import dev.tecte.chessWar.piece.domain.model.PieceType;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

/**
 * 기물의 속성(팀, 타입)을 기반으로 외부 시스템에서 사용하는 식별자를 찾아주는 포트 인터페이스입니다.
 * <p>
 * 애플리케이션 계층은 구체적인 기술을 알 필요 없이, 이 인터페이스를 통해 필요한 식별자를 얻을 수 있습니다.
 */
public interface PieceIdResolver {
    /**
     * 기물의 팀과 타입에 대응하는 식별자를 찾아 반환합니다.
     *
     * @param teamColor 팀 색상
     * @param type      기물 타입
     * @return 찾아낸 식별자 문자열
     */
    @NonNull
    String resolveId(@NonNull TeamColor teamColor, @NonNull PieceType type);
}
