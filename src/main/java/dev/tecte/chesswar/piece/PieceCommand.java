package dev.tecte.chesswar.piece;

import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.ChessWarCommand;
import dev.tecte.chesswar.core.Messages;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Subcommand("piece")
@RequiredArgsConstructor
public final class PieceCommand extends ChessWarCommand {
    private final BoardContext board;
    private final PieceLifecycle pieceLifecycle;

    @Subcommand("spawn")
    public void onSpawn(
            final CommandSender sender,
            @Default("0") final int gridX,
            @Default("0") final int gridY,
            @Default("WHITE") final Team team,
            @Default("PAWN") final PieceType type
    ) {
        GridPoint gridPos = resolveGridPos(sender, gridX, gridY);
        if (gridPos == null) {
            return;
        }

        SpawnResult result = pieceLifecycle.spawn(gridPos, team, type);
        Component message = switch (result) {
            case SUCCESS -> Component.text("기물이 (" + gridX + ", " + gridY + ")에 소환되었습니다.", NamedTextColor.GREEN);
            case ALREADY_OCCUPIED -> Component.text("해당 좌표에 이미 기물이 존재합니다.", NamedTextColor.RED);
        };
        Messages.reply(sender, message);
    }

    @Subcommand("setup")
    public void onSetup(final CommandSender sender) {
        if (!board.isSetup()) {
            Messages.reply(sender, Component.text("체스판 기준점이 설정되지 않았습니다.", NamedTextColor.RED));
            return;
        }

        PieceSetupResult result = pieceLifecycle.setupPieces();
        Component message = switch (result) {
            case SUCCESS -> Component.text("32개 기물이 체스판에 배치되었습니다.", NamedTextColor.GREEN);
            case ALREADY_INITIALIZED -> Component.text("체스판에 이미 기물이 배치되어 있습니다.", NamedTextColor.RED);
        };
        Messages.reply(sender, message);
    }

    @Subcommand("bind")
    public void onBind(final Player player, @Default("0") final int gridX, @Default("0") final int gridY) {
        GridPoint gridPos = resolveGridPos(player, gridX, gridY);
        if (gridPos == null) {
            return;
        }

        BindResult result = pieceLifecycle.bindPlayer(gridPos, player.getUniqueId());
        Component message = switch (result) {
            case SUCCESS -> Component.text("기물이 (" + gridX + ", " + gridY + ")에 배정되었습니다.", NamedTextColor.GREEN);
            case PLAYER_ALREADY_BOUND -> Component.text("이미 배정받은 기물이 있습니다.", NamedTextColor.RED);
            case PIECE_NOT_FOUND -> Component.text("해당 좌표에 기물이 없습니다.", NamedTextColor.RED);
            case ALREADY_BOUND -> Component.text("다른 플레이어가 배정받은 기물입니다.", NamedTextColor.RED);
        };
        Messages.reply(player, message);
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
            Messages.reply(sender, Component.text("체스판 범위를 벗어났습니다. (0~7)", NamedTextColor.RED));
            return null;
        }

        return gridPos;
    }
}
