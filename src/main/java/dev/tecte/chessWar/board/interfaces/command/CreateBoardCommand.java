package dev.tecte.chessWar.board.interfaces.command;

import dev.tecte.chessWar.board.application.BoardService;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class CreateBoardCommand implements CommandExecutor {
    private final BoardService boardService;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
            boardService.createAndRenderBoard(player);
            player.sendMessage("체스판이 생성되었습니다.");
            return true;
        }

        return false;
    }
}