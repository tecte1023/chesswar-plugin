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
     * 이름(대소문자 무관)으로 해당 팀 색상을 찾습니다.
     *
     * @param name 팀 색상 이름
     * @return 찾은 팀 색상
     */
    @NonNull
    public static Optional<TeamColor> from(@NonNull String name) {
        return Optional.ofNullable(LOOKUP.get(name.toUpperCase()));
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
