package dev.tecte.chessWar.team.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TeamColor {
    WHITE("백팀", NamedTextColor.WHITE),
    BLACK("흑팀", NamedTextColor.DARK_GRAY);

    private static final Map<String, TeamColor> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(TeamColor::name, Function.identity()));

    private final String name;
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
}
