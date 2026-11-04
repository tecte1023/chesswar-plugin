package dev.tecte.chesswar.piece;

import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.ChessWarCommand;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.Messages;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@Subcommand("piece")
@RequiredArgsConstructor
public final class PieceCommand extends ChessWarCommand {
    private final BoardContext board;
    private final PieceLifecycle pieceLifecycle;

    @Subcommand("spawn")
    public void onSpawn(final CommandSender sender, @Default("0") final int gridX, @Default("0") final int gridY) {
        GridPoint gridPos = resolveGridPos(sender, gridX, gridY);
        if (gridPos == null) {
            return;
        }

        if (pieceLifecycle.hasPieceAt(gridPos)) {
            Messages.reply(sender, Component.text("해당 좌표에 이미 기물이 존재합니다.", NamedTextColor.RED));
            return;
        }

        pieceLifecycle.spawn(gridPos);
        Messages.reply(sender, Component.text("기물이 (" + gridX + ", " + gridY + ")에 소환되었습니다.", NamedTextColor.GREEN));
    }

    @Subcommand("bind")
    public void onBind(final Player player, @Default("0") final int gridX, @Default("0") final int gridY) {
        GridPoint gridPos = resolveGridPos(player, gridX, gridY);
        if (gridPos == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        if (pieceLifecycle.hasPlayerPiece(playerId)) {
            Messages.reply(player, Component.text("이미 배정된 기물이 존재합니다.", NamedTextColor.RED));
            return;
        }

        Entity piece = pieceLifecycle.pieceAt(gridPos);
        if (piece == null) {
            Messages.reply(player, Component.text("해당 좌표에 배정할 기물이 존재하지 않습니다.", NamedTextColor.RED));
            return;
        }

        if (piece.hasComponent(PlayerComponent.class)) {
            Messages.reply(player, Component.text("해당 기물은 이미 다른 플레이어에게 배정되어 있습니다.", NamedTextColor.RED));
            return;
        }

        pieceLifecycle.bindPlayer(piece, playerId);
        Messages.reply(player, Component.text("기물이 (" + gridX + ", " + gridY + ")에 배정되었습니다.", NamedTextColor.GREEN));
    }

    @Subcommand("clear")
    public void onClear(final CommandSender sender) {
        pieceLifecycle.clear();
        Messages.reply(sender, Component.text("소환된 기물들이 모두 정리되었습니다.", NamedTextColor.GREEN));
    }

    private GridPoint resolveGridPos(final CommandSender sender, final int gridX, final int gridY) {
        if (!board.isSetup()) {
            Messages.reply(sender, Component.text("체스판 기준점이 설정되지 않았습니다.", NamedTextColor.RED));
            return null;
        }

        var gridPos = new GridPoint(gridX, gridY);
        if (!board.isInBounds(gridPos)) {
            Messages.reply(sender, Component.text("유효하지 않은 체스판 좌표입니다.", NamedTextColor.RED));
            return null;
        }

        return gridPos;
    }
}
