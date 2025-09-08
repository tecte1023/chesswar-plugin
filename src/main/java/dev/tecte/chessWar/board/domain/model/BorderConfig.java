package dev.tecte.chessWar.board.domain.model;

import org.bukkit.Material;

/**
 * 체스판 테두리의 정적 설정 정보를 담는 데이터 객체입니다.
 *
 * @param thickness 테두리의 두께
 * @param block     테두리를 구성하는 블록의 종류
 */
public record BorderConfig(int thickness, Material block) {
}
