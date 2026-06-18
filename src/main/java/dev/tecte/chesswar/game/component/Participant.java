package dev.tecte.chesswar.game.component;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.team.Team;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "of")
public class Participant {
    @NotNull
    private final UUID playerId;
    @NotNull
    private final String playerName;
    @NotNull
    private final Team team;
    @NotNull
    private final Statistics statistics;
    private boolean ready;
    @NotNull
    private GameMode originalGameMode;
    @Nullable
    private Double originalHealth;
    @Nullable
    private Double originalAttackDamage;

    @NotNull
    public static Participant of(
            @NotNull final UUID playerId,
            @NotNull final String playerName,
            @NotNull final Team team,
            @NotNull final GameMode originalGameMode
    ) {
        return new Participant(
                playerId,
                playerName,
                team,
                new Statistics(),
                false,
                originalGameMode,
                null,
                null
        );
    }

    @Nullable
    public Piece getPiece(@NotNull final PieceState state) {
        final Coordinate coord = state.coordinate(playerId);
        return coord != null ? state.piece(coord) : null;
    }
}
