package dev.tecte.chesswar.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@CommandAlias("chesswaradmin|cwa")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
public class AdminCommand extends BaseCommand {
    private final GameManager gameManager;

    @Subcommand("start")
    public void startGame(final Player player) {
        gameManager.startGame(player);
    }

    @Subcommand("reset")
    public void resetGame(final Player player) {
        gameManager.reset();
    }

    @Subcommand("record")
    public void recordStats(final Player player) {
        gameManager.provideRecordBook(player);
    }

    @Subcommand("move")
    public void movePiece(final Player player) {
        gameManager.movePiece(player);
    }

    @Subcommand("setup")
    public void setupBoard(final Player player) {
        gameManager.setupBoard(player);
    }

    @Subcommand("join")
    public void joinGame(final Player player, final Team team) {
        gameManager.join(player, team);
    }
}
