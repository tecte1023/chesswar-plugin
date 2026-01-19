package dev.tecte.chessWar.game.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 게임의 진행 단계를 나타내는 열거형입니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GamePhase {
    PIECE_SELECTION("기물 선택"),
    TURN_ORDER_SELECTION("행동 순서 결정"),
    BATTLE("전투"),
    ENDED("종료");

    private final String displayName;

    private static final Map<String, GamePhase> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(GamePhase::name, Function.identity()));

    /**
     * 문자열로부터 {@link GamePhase}를 찾습니다. 대소문자를 구분하지 않습니다.
     *
     * @param value 게임 단계 이름
     * @return 해당하는 게임 단계가 있으면 {@link Optional}로 반환, 없으면 빈 {@link Optional}
     */
    @NonNull
    public static Optional<GamePhase> from(@NonNull String value) {
        return Optional.ofNullable(LOOKUP.get(value.toUpperCase()));
    }

    /**
     * 게임의 초기 단계를 반환합니다.
     *
     * @return 초기 게임 단계
     */
    @NonNull
    public static GamePhase initial() {
        return PIECE_SELECTION;
    }
}
