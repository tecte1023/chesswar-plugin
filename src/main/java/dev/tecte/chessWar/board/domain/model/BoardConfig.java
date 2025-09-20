package dev.tecte.chessWar.board.domain.model;

import lombok.NonNull;

import java.util.Objects;

/**
 * 체스판의 정적 설정 정보를 담는 데이터 객체입니다.
 *
 * @param squareConfig      격자 칸의 설정
 * @param innerBorderConfig 내부 테두리의 설정
 * @param frameConfig       외부 테두리의 설정
 */
public record BoardConfig(
        SquareConfig squareConfig,
        InnerBorderConfig innerBorderConfig,
        FrameConfig frameConfig
) {
    private static final BoardConfig DEFAULTS = new BoardConfig(
            SquareConfig.ofDefault(),
            InnerBorderConfig.ofDefault(),
            FrameConfig.ofDefault()
    );

    public BoardConfig {
        Objects.requireNonNull(squareConfig, "squareConfig must not be null");
        Objects.requireNonNull(innerBorderConfig, "innerBorderConfig must not be null");
        Objects.requireNonNull(frameConfig, "frameConfig must not be null");
    }

    /**
     * 기본값으로 채워진 {@link BoardConfig} 인스턴스를 반환합니다.
     *
     * @return 기본 설정 값을 가진 {@link BoardConfig} 객체
     */
    @NonNull
    public static BoardConfig ofDefaults() {
        return DEFAULTS;
    }
}
