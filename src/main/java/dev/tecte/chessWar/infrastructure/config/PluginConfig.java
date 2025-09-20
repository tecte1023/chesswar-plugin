package dev.tecte.chessWar.infrastructure.config;

import dev.tecte.chessWar.board.domain.model.BoardConfig;
import lombok.NonNull;
import lombok.With;

import java.util.Objects;

/**
 * 플러그인의 모든 정적 설정 정보를 담는 불변 데이터 객체입니다.
 * 각 모듈의 설정 객체를 필드로 포함합니다.
 *
 * @param boardConfig 체스판 모듈의 정적 설정
 */
@With
public record PluginConfig(BoardConfig boardConfig) {
    private static final PluginConfig DEFAULTS = new PluginConfig(BoardConfig.ofDefaults());

    public PluginConfig {
        Objects.requireNonNull(boardConfig, "boardConfig must not be null");
    }

    @NonNull
    public static PluginConfig ofDefaults() {
        return DEFAULTS;
    }
}
