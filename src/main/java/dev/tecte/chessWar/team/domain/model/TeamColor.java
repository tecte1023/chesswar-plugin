package dev.tecte.chessWar.team.domain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 체스 게임의 팀 색상을 나타내는 열거형입니다.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum TeamColor {
    WHITE("백팀", NamedTextColor.WHITE),
    BLACK("흑팀", NamedTextColor.DARK_GRAY);

    private static final Map<String, TeamColor> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(TeamColor::name, Function.identity()));

    private final String displayName;
    private final NamedTextColor textColor;

    /**
     * 문자열 값으로부터 {@link TeamColor}를 찾습니다.
     *
     * @param value 팀 색상을 나타내는 문자열 (대소문자 구분 없음)
     * @return 해당 팀 색상의 {@link TeamColor}를 담은 {@link Optional}, 없으면 빈 {@link Optional}
     */
    @NonNull
    public static Optional<TeamColor> from(@NonNull String value) {
        return Optional.ofNullable(LOOKUP.get(value.toUpperCase()));
    }

    /**
     * 현재 팀 색상의 반대편 팀 색상을 반환합니다.
     *
     * @return 반대편 팀 색상
     */
    @NonNull
    public TeamColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
