package dev.tecte.chessWar.game.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.game.application.GameService;
import dev.tecte.chessWar.infrastructure.command.CommandConstants;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

/**
 * 게임과 관련된 모든 플레이어 명령어를 처리하는 클래스입니다.
 */
@Singleton
@CommandAlias(CommandConstants.ROOT)
@Subcommand("game")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class GameCommand extends BaseCommand {
    private final GameService gameService;

    /**
     * 게임을 시작하는 명령어입니다.
     *
     * @param sender 명령어를 실행한 주체
     */
    @Subcommand("start")
    @Description("게임을 시작합니다.")
    public void onGameStart(@NonNull CommandSender sender) {
        gameService.startGame(sender);
    }

    /**
     * 진행 중인 게임을 강제로 중단합니다.
     *
     * @param sender 명령어를 실행한 주체
     */
    @Subcommand("stop")
    @Description("진행 중인 게임을 중단합니다.")
    public void onGameStop(@NonNull CommandSender sender) {
        gameService.stopGame(sender);
    }
}
