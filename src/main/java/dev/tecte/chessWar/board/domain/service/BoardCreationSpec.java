package dev.tecte.chessWar.board.domain.service;

import dev.tecte.chessWar.board.domain.model.FrameConfig;
import dev.tecte.chessWar.board.domain.model.InnerBorderConfig;
import dev.tecte.chessWar.board.domain.model.Orientation;
import dev.tecte.chessWar.board.domain.model.SquareConfig;
import lombok.Builder;
import org.bukkit.util.Vector;

/**
 * 체스판 생성을 위한 모든 조건을 상세히 기술하는 생성 명세입니다.
 * 이 객체는 불변이며, 생성될 체스판의 모든 매개변수를 캡슐화합니다.
 *
 * @param gridAnchor        격자의 기준점 (a1: 좌측 하단 모서리)
 * @param orientation       격자의 방향
 * @param squareConfig      격자 칸의 설정
 * @param innerBorderConfig 내부 테두리의 설정
 * @param frameConfig       외부 테두리의 설정
 */
@Builder
public record BoardCreationSpec(
        Vector gridAnchor,
        Orientation orientation,
        SquareConfig squareConfig,
        InnerBorderConfig innerBorderConfig,
        FrameConfig frameConfig
) {
}
