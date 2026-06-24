package dev.tecte.chesswar.piece;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@CommandAlias("chesswar|cw")
@RequiredArgsConstructor
public class PieceCommand extends BaseCommand {
    private final GameManager gameManager;

    @Subcommand("select")
    public void onSelectPiece(final Player player, final int x, final int y) {
        gameManager.selectPiece(player, Coordinate.of(x, y));
    }
}
