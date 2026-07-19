package dev.tecte.chesswar.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.game.WaitingPhaseManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("chesswar|cw")
@Subcommand("admin game")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
public final class GameAdminCommand extends BaseCommand {
    @NotNull
    private final WaitingPhaseManager waitingPhaseManager;

    @Subcommand("setstart")
    public void onSetStart(@NotNull final Player player) {
        waitingPhaseManager.forceBindStartTrigger(player.getLocation());
    }

    @Subcommand("start")
    public void onStart(@NotNull final CommandSender sender) {
        waitingPhaseManager.tryStartGame();
    }

    @Subcommand("stop")
    public void onStop(@NotNull final CommandSender sender) {
        waitingPhaseManager.tryStopGame();
    }
}
