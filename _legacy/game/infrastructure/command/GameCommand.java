package dev.tecte.chessWar.game.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chessWar.game.application.GameFlowCoordinator;
import dev.tecte.chessWar.infrastructure.command.CommandRouting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

/**
 * 게임 진행 관련 명령어를 처리합니다.
 */
@Singleton
@CommandAlias(CommandRouting.ROOT_ALIAS)
@Subcommand("game")
@RequiredArgsConstructor(onConstructor_ = @Inject)
@SuppressWarnings("unused")
public class GameCommand extends BaseCommand {
    private final GameFlowCoordinator gameFlowCoordinator;

    /**
     * 게임을 시작합니다.
     *
     * @param sender 행위자
     */
    @Subcommand("start")
    @Description("게임을 시작합니다.")
    public void start(@NonNull CommandSender sender) {
        gameFlowCoordinator.startGame(sender);
    }

    /**
     * 진행 중인 게임을 중단합니다.
     *
     * @param sender 행위자
     */
    @Subcommand("stop")
    @Description("진행 중인 게임을 중단합니다.")
    public void stop(@NonNull CommandSender sender) {
        gameFlowCoordinator.stopGame(sender);
    }
}
