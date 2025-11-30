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
    PAWN("폰"),
    KNIGHT("나이트"),
    BISHOP("비숍"),
    ROOK("룩"),
    QUEEN("퀸"),
    KING("킹");

    private static final Map<String, PieceType> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(PieceType::name, Function.identity()));

    private final String displayName;

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
