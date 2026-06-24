package dev.tecte.chesswar.board;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@NoArgsConstructor(staticName = "create")
public final class BoardComponent {
    @Nullable
    private Board board;

    public boolean isEmpty() {
        return this.board == null;
    }

    public boolean hasBoard() {
        return board != null;
    }
}
