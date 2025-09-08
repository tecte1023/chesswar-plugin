package dev.tecte.chessWar.infrastructure.config;

import dev.tecte.chessWar.board.domain.model.BoardConfig;
import lombok.NonNull;
import lombok.With;

/**
 * 플러그인의 모든 정적 설정 정보를 담는 불변 데이터 객체입니다.
 * 각 모듈의 설정 객체를 필드로 포함합니다.
 *
 * @param boardConfig 체스판 모듈의 정적 설정
 */
@With
public record PluginConfig(BoardConfig boardConfig) {
    /**
     * 비어있는 초기 정적 설정 객체를 생성합니다.
     *
     * @return 모든 필드가 null로 초기화된 {@link PluginConfig} 인스턴스
     */
    @NonNull
    public static PluginConfig empty() {
        return new PluginConfig(null);
    }
}
