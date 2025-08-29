package dev.tecte.chessWar.board.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.board.application.BoardService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
@Subcommand("board")
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
@SuppressWarnings("unused")
public class BoardCommand extends BaseCommand {
    private final BoardService boardService;

    @Subcommand("create")
    @Description("새로운 체스판을 생성합니다.")
    public void onCreateBoard(@NotNull Player player) {
        boardService.createBoard(player);
    }

    @Default
    @HelpCommand
    public void onHelp(@NotNull CommandHelp help) {
        help.showHelp();
    }
}
