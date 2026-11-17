package dev.tecte.chesswar.game;

import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.core.ChessWarCommand;
import dev.tecte.chesswar.core.Messages;
import dev.tecte.chesswar.piece.PieceLifecycle;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@Subcommand("game")
@RequiredArgsConstructor
public final class GameCommand extends ChessWarCommand {
    private final BoardContext board;
    private final PieceLifecycle pieceLifecycle;
    private final GameLifecycle gameLifecycle;

    @Subcommand("start")
    public void onStart(final CommandSender sender) {
        if (!board.isSetup()) {
            Messages.reply(sender, Component.text("체스판 기준점이 설정되지 않았습니다.", NamedTextColor.RED));
            return;
        }

        GameStartResult result = gameLifecycle.start(pieceLifecycle.playerPieces());
        Component message = switch (result) {
            case SUCCESS -> Component.text("게임이 시작되었습니다.", NamedTextColor.GREEN);
            case ALREADY_STARTED -> Component.text("이미 게임이 진행 중입니다.", NamedTextColor.RED);
            case NO_PLAYERS -> Component.text("기물에 배정된 플레이어가 없습니다.", NamedTextColor.RED);
        };
        Messages.reply(sender, message);
    }

    @Subcommand("stop")
    public void onStop(final CommandSender sender) {
        if (!gameLifecycle.isRunning()) {
            Messages.reply(sender, Component.text("진행 중인 게임이 없습니다.", NamedTextColor.RED));
            return;
        }

        gameLifecycle.stop();
    }
}
