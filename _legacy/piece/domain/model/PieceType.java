package dev.tecte.chessWar.piece.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 체스 기물의 종류를 나타내는 열거형입니다.
 */
@Getter
@Accessors(fluent = true)
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
            "아군을 치료하는 주교지만, 때로는 저격수처럼 은밀하게 멀리 있는 적을 노립니다.",
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
     * 이름(대소문자 무관)으로 해당 기물 타입을 찾습니다.
     *
     * @param name 기물 타입 이름
     * @return 찾은 기물 타입
     */
    @NonNull
    public static Optional<PieceType> from(@NonNull String name) {
        return Optional.ofNullable(LOOKUP.get(name.toUpperCase()));
    }

    /**
     * 기물의 이름 표시용 컴포넌트를 생성합니다.
     * <p>
     * 인자 없이 호출할 경우 접두어가 없는 기본 형태를 반환합니다.
     * 자세한 내용은 {@link #formattedName(Component)}를 참조하세요.
     *
     * @return 포맷팅된 컴포넌트
     */
    @NonNull
    public Component formattedName() {
        return formattedName(null);
    }

    /**
     * 기물의 이름 표시용 컴포넌트를 생성합니다.
     * <p>
     * 색상 정보가 포함되지 않은 기본 형식의 컴포넌트를 생성하여 반환합니다.
     * 호출하는 쪽에서 팀 색상이나 상황에 맞는 색상을 자유롭게 적용할 수 있도록
     * 내부 텍스트의 색상은 지정되지 않은 상태(Inherit)로 유지됩니다.
     * <p>
     * 단, 게임 전체의 시각적 일관성을 위해 괄호와 같은 고정 요소는 미리 스타일링되어 제공됩니다.
     *
     * @param prefix 이름 앞에 붙일 접두어 컴포넌트. null이면 생략됩니다.
     * @return 포맷팅된 컴포넌트
     */
    @NonNull
    public Component formattedName(@Nullable Component prefix) {
        return Component.text()
                .append(Component.text("[ "))
                .append(Component.text(symbol + " "))
                .append(prefix == null ? Component.empty() : prefix.appendSpace().decorate(TextDecoration.BOLD))
                .append(Component.text(displayName).decorate(TextDecoration.BOLD))
                .append(Component.text(" ]"))
                .build();
    }
}
