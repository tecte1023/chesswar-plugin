package dev.tecte.chesswar.admin.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.BoardManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("chesswar|cw")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
public final class BoardAdminCommand extends BaseCommand {
    @NotNull
    private final BoardManager boardManager;

    @Subcommand("admin board view")
    public void onViewBoard(@NotNull final Player player, @Nullable @Optional final Boolean state) {
        if (state == null) {
            boardManager.toggleBoardVisibility(player);
        } else {
            boardManager.setBoardVisibility(player, state);
        }
    }
}
