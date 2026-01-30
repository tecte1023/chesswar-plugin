package dev.tecte.chessWar.board.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.board.application.BoardService;
import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 * 체스판 관련 명령어를 처리하는 클래스입니다.
 */
@Singleton
@CommandAlias(CommandConstants.ROOT_ALIAS)
@Subcommand("board")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class BoardCommand extends BaseCommand {
    private final BoardService boardService;

    /**
     * 새로운 체스판을 생성합니다.
     *
     * @param player 명령어를 실행한 플레이어
     */
    @Subcommand("create")
    @Description("새로운 체스판을 생성합니다.")
    public void create(@NonNull Player player) {
        boardService.createBoard(player);
    }
}
