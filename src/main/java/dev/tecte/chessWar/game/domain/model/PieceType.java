package dev.tecte.chessWar.game.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 체스 기물의 종류를 나타내는 열거형입니다.
 */
@Getter
@RequiredArgsConstructor
public enum PieceType {
    PAWN(
            "폰",
            "♟",
            "가장 기본적인 보병입니다. 상대 진영 끝까지 살아남으면 강력한 기물로 전직할 수 있습니다.",
            "이동: 전방 1칸 (첫 이동 시 2칸 가능) / 공격: 앞쪽 대각선 1칸"
    ),

    KNIGHT(
            "나이트",
            "♞",
            "말을 탄 기사입니다. 유일하게 다른 기물을 뛰어넘어, 혼란스러운 전장에서 적의 허를 찌릅니다.",
            "직선 방향으로 2칸 이동 후 옆으로 1칸 (뛰어넘기 가능)"
    ),

    BISHOP(
            "비숍",
            "♝",
            "아군을 치료하는 성직자이지만, 때로는 저격수처럼 은밀하게 멀리 있는 적을 노립니다.",
            "대각선 방향"
    ),

    ROOK(
            "룩",
            "♜",
            "성벽처럼 단단한 전차입니다. 직선으로 시원하게 돌파하며 적의 진형을 무너뜨립니다.",
            "직선 방향"
    ),

    QUEEN(
            "퀸",
            "♛",
            "전장을 지배하는 최강의 여왕입니다. 공격과 방어, 기동성을 모두 갖춘 팔방미인입니다.",
            "모든 방향(직선+대각선)"
    ),

    KING(
            "킹",
            "♚",
            "반드시 지켜야 하는 왕입니다. 걸음은 느리지만 강렬한 카리스마로 아군을 지휘하고 강화하며 전장을 승리로 이끕니다.",
            "모든 방향으로 1칸"
    );

    private static final Map<String, PieceType> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(PieceType::name, Function.identity()));

    private final String displayName;
    private final String symbol;
    private final String description;
    private final String rangeDescription;

    /**
     * 문자열 이름(대소문자 무관)으로 해당 기물 타입을 조회합니다.
     *
     * @param value 조회할 기물 타입의 이름
     * @return 해당 이름과 일치하는 {@link PieceType}을 담은 {@link Optional}, 없으면 빈 {@link Optional}
     */
    @NonNull
    public static Optional<PieceType> from(@NonNull String value) {
        return Optional.ofNullable(LOOKUP.get(value.toUpperCase()));
    }
}
