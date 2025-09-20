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

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TeamColor {
    WHITE("백", NamedTextColor.WHITE),
    BLACK("흑", NamedTextColor.BLACK);

    private static final Map<String, TeamColor> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(TeamColor::name, Function.identity()));

    private final String name;
    private final NamedTextColor textColor;

    @NonNull
    public static Optional<TeamColor> from(@NonNull String value) {
        return Optional.ofNullable(LOOKUP.get(value.toUpperCase()));
    }
}
