package dev.tecte.chesswar.core;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@CommandAlias("chesswar|cw")
public abstract class ChessWarCommand extends BaseCommand {
    private static final Component PREFIX = Component.text("[ChessWar] ", NamedTextColor.GRAY);

    protected void reply(final CommandSender sender, final Component message) {
        sender.sendMessage(PREFIX.append(message));
    }
}
