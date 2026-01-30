package dev.tecte.chessWar.piece.domain.model;

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
 * 기물이 게임 내에서 수행하는 역할을 정의하는 열거형입니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum PieceRole {
    HERO("영웅"),
    UNIT("일반");

    private static final Map<String, PieceRole> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(PieceRole::name, Function.identity()));

    private final String displayName;

    /**
     * 이름(대소문자 무관)으로 해당 기물 역할을 찾습니다.
     *
     * @param name 기물 역할 이름
     * @return 찾은 기물 역할
     */
    @NonNull
    public static Optional<PieceRole> from(@NonNull String name) {
        return Optional.ofNullable(LOOKUP.get(name.toUpperCase()));
    }
}
