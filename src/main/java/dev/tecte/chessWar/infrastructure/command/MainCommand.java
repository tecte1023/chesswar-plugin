package dev.tecte.chessWar.infrastructure.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import jakarta.inject.Singleton;
import lombok.NonNull;

/**
 * 플러그인의 루트 명령어를 처리하는 클래스입니다.
 * 다른 서브커맨드가 없는 경우 기본적으로 도움말을 표시합니다.
 */
@Singleton
@CommandAlias(CommandConstants.ROOT)
public class MainCommand extends BaseCommand {
    @Default
    @HelpCommand
    @SuppressWarnings("unused")
    public void onHelp(@NonNull CommandHelp help) {
        help.showHelp();
    }
}
