package dev.tecte.chesswar.board;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@AllArgsConstructor
public final class BoardComponent {
    @NotNull
    private Board board;
}
