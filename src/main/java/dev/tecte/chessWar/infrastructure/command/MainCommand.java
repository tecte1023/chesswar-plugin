package dev.tecte.chessWar.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import jakarta.inject.Singleton;
import lombok.NonNull;

/**
 * 루트 명령어를 처리하는 클래스입니다.
 */
@Singleton
@CommandAlias(CommandConstants.ROOT_ALIAS)
public class MainCommand extends BaseCommand {
    /**
     * 도움말을 표시합니다.
     *
     * @param help 커맨드 도움말
     */
    @Default
    @HelpCommand
    @SuppressWarnings("unused")
    public void help(@NonNull CommandHelp help) {
        help.showHelp();
    }
}
