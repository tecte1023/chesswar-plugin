package dev.tecte.chesswar.core;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

@CommandAlias("chesswar|cw")
public abstract class ChessWarCommand extends BaseCommand {
    private static final Component PREFIX = Component.text("[ChessWar] ", NamedTextColor.GRAY);

    protected void reply(final Player player, final Component message) {
        player.sendMessage(PREFIX.append(message));
    }
}
