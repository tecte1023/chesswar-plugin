package dev.tecte.chesswar.piece;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PieceType {
    KING(
            "킹",
            "♚",
            "반드시 지켜야 하는 왕입니다. 걸음은 느리지만 강렬한 카리스마로 아군을 지휘하고 강화하며 전장을 승리로 이끕니다.",
            "모든 방향으로 1칸",
            200,
            5
    ),
    QUEEN(
            "퀸",
            "♛",
            "전장을 지배하는 최강의 여왕입니다. 공격과 방어, 기동성을 모두 갖춘 팔방미인입니다.",
            "모든 방향(직선+대각선)",
            100,
            25
    ),
    ROOK(
            "룩",
            "♜",
            "성벽처럼 단단한 전차입니다. 직선으로 시원하게 돌파하며 적의 진형을 무너뜨립니다.",
            "직선 방향",
            150,
            15
    ),
    BISHOP(
            "비숍",
            "♝",
            "아군을 치료하는 주교지만, 때로는 저격수처럼 은밀하게 멀리 있는 적을 노립니다.",
            "대각선 방향",
            80,
            20
    ),
    KNIGHT(
            "나이트",
            "♞",
            "말을 탄 기사입니다. 유일하게 다른 기물을 뛰어넘어, 혼란스러운 전장에서 적의 허를 찌릅니다.",
            "직선 방향으로 2칸 이동 후 옆으로 1칸 (뛰어넘기 가능)",
            100,
            20
    ),
    PAWN(
            "폰",
            "♟",
            "가장 기본적인 보병입니다. 상대 진영 끝까지 살아남으면 강력한 기물로 전직할 수 있습니다.",
            "이동: 전방 1칸 (첫 이동 시 2칸 가능) / 공격: 앞쪽 대각선 1칸",
            50,
            5
    );

    private final String displayName;
    private final String symbol;
    private final String description;
    private final String rangeDescription;
    private final double baseHealth;
    private final double baseDamage;
}
