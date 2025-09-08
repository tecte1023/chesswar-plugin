package dev.tecte.chessWar.board.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Singleton
@CommandAlias(CommandConstants.ROOT)
@Subcommand("board")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BoardCommand extends BaseCommand {
    private final BoardService boardService;

    @Subcommand("create")
    @Description("새로운 체스판을 생성합니다.")
    @SuppressWarnings("unused")
    public void onCreateBoard(@NonNull Player player) {
        boardService.createBoard(player);
        player.sendMessage("체스판이 생성되었습니다.");
    }

    @HelpCommand
    @SuppressWarnings("unused")
    public void onHelp(@NonNull CommandHelp help) {
        help.showHelp();
    }
}
