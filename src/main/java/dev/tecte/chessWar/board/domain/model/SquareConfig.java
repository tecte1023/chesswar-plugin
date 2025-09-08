package dev.tecte.chessWar.board.domain.model;

import lombok.Builder;
import org.bukkit.Material;

/**
 * 체스판 격자 칸의 정적 설정 정보를 담는 데이터 객체입니다.
 *
 * @param rowCount   행 수
 * @param colCount   열 수
 * @param width      칸의 너비
 * @param height     칸의 높이
 * @param blackBlock 검은색 칸을 구성하는 블록 종류
 * @param whiteBlock 흰색 칸을 구성하는 블록 종류
 */
@Builder
public record SquareConfig(
        int rowCount,
        int colCount,
        int width,
        int height,
        Material blackBlock,
        Material whiteBlock
) {
}
