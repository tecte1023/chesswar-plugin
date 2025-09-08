package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;

/**
 * 체스판의 정적 설정 정보를 담는 데이터 객체입니다.
 *
 * @param squareConfig      격자 칸의 설정
 * @param innerBorderConfig 내부 테두리의 설정
 * @param frameConfig       외부 테두리의 설정
 */
@Builder
public record BoardConfig(
        SquareConfig squareConfig,
        BorderConfig innerBorderConfig,
        BorderConfig frameConfig
) {
}
