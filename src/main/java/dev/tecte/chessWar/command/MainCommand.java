package dev.tecte.chessWar.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
@CommandAlias(CommandConstants.ROOT)
@SuppressWarnings("unused")
public class MainCommand extends BaseCommand {
    @Default
    @HelpCommand
    public void onHelp(@NotNull CommandHelp help) {
        help.showHelp();
    }
}
