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
 * 게임 생명 주기의 진행 단계를 정의합니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum GamePhase {
    SETUP("준비"),
    PIECE_SELECTION("기물 선택"),
    TURN_ORDER_SELECTION("턴 순서 결정"),
    BATTLE("전투"),
    ENDED("종료");

    private final String displayName;

    private static final Map<String, GamePhase> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(GamePhase::name, Function.identity()));

    /**
     * 이름 기반으로 게임 단계를 검색합니다.
     *
     * @param name 단계 이름
     * @return 찾은 단계
     */
    @NonNull
    public static Optional<GamePhase> from(@NonNull String name) {
        return Optional.ofNullable(LOOKUP.get(name.toUpperCase()));
    }
}
