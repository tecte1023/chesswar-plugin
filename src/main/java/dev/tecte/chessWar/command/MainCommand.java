package dev.tecte.chessWar.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import jakarta.inject.Singleton;
import lombok.NonNull;

@Singleton
@CommandAlias(CommandConstants.ROOT)
public class MainCommand extends BaseCommand {
    @Default
    @HelpCommand
    public void onHelp(@NonNull CommandHelp help) {
        help.showHelp();
    }
}
