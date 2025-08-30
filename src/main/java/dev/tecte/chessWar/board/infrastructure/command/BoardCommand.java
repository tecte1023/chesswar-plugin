package dev.tecte.chessWar.board.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.command.CommandConstants;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
@CommandAlias(CommandConstants.ROOT)
@Subcommand("board")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@SuppressWarnings("unused")
public class BoardCommand extends BaseCommand {
    private final BoardService boardService;

    @Subcommand("create")
    @Description("새로운 체스판을 생성합니다.")
    public void onCreateBoard(@NotNull Player player) {
        boardService.createBoard(player);
        player.sendMessage("체스판이 생성되었습니다.");
    }

    @HelpCommand
    public void onHelp(@NotNull CommandHelp help) {
        help.showHelp();
    }
}
