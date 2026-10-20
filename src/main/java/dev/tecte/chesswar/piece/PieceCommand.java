package dev.tecte.chesswar.piece;

import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.core.ChessWarCommand;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@Subcommand("piece")
@RequiredArgsConstructor
public final class PieceCommand extends ChessWarCommand {
    private final BoardContext board;
    private final PieceLifecycle pieceLifecycle;

    @Subcommand("spawn")
    public void onSpawn(final CommandSender sender, @Default("0") final int gridX, @Default("0") final int gridY) {
        if (!board.isSetup()) {
            reply(sender, Component.text("체스판 기준점이 설정되지 않았습니다.", NamedTextColor.RED));
            return;
        }

        if (!board.isInBounds(gridX, gridY)) {
            reply(sender, Component.text("유효하지 않은 체스판 좌표입니다.", NamedTextColor.RED));
            return;
        }

        pieceLifecycle.spawn(gridX, gridY);
        reply(sender, Component.text("기물이 (" + gridX + ", " + gridY + ")에 소환되었습니다.", NamedTextColor.GREEN));
    }

    @Subcommand("clear")
    public void onClear(final CommandSender sender) {
        pieceLifecycle.clear();
        reply(sender, Component.text("소환된 기물들이 모두 정리되었습니다.", NamedTextColor.GREEN));
    }
}
