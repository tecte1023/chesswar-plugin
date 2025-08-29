package dev.tecte.chessWar.bootstrap;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import dev.tecte.chessWar.board.infrastructure.command.BoardCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Singleton
@CommandAlias("chesswar|cw")
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
@SuppressWarnings("unused")
public class MainCommand extends BaseCommand {
    private final BoardCommand boardCommand;

    @Default
    @HelpCommand
    public void onHelp(@NotNull CommandHelp help) {
        help.showHelp();
    }
}
