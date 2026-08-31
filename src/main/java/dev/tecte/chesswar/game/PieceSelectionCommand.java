package dev.tecte.chesswar.game;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.Coordinate;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("chesswar|cw")
@RequiredArgsConstructor
public final class PieceSelectionCommand extends BaseCommand {
    @NotNull
    private final PieceSelectionPhaseManager manager;

    @Private
    @Subcommand("select")
    public void onSelect(@NotNull final Player player, final int x, final int y) {
        if (!Coordinate.isValid(x, y)) {
            return;
        }

        manager.trySelectPiece(player, Coordinate.of(x, y));
    }
}
