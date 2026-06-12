package dev.tecte.chesswar.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.economy.GoldSource;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("chesswaradmin|cwa")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
public class AdminCommand extends BaseCommand {
    private final GameManager gameManager;
    private final GameAnnouncer announcer;

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

    @Subcommand("setstart")
    public void setStartButton(final Player player) {
        gameManager.setStartButtonBindingMode(player);
    }

    @Subcommand("removestart")
    public void removeStartButton(final Player player) {
        gameManager.removeStartButton(player);
    }

    @Subcommand("join")
    public void joinGame(final Player player, final Team team) {
        gameManager.join(player, team);
    }

    @Subcommand("shop")
    public void openShop(final Player player) {
        gameManager.openShop(player);
    }

    @Subcommand("kingrequired")
    public void setKingRequired(final Player player, final boolean required) {
        gameManager.context().kingRequired(required);
        announcer.announceAdminMessage(player, Component.text("§6[Admin] §f킹 필수 배정 여부가 " + (required ? "§a활성화" : "§c비활성화") + "§f되었습니다."));
    }

    @Subcommand("addgold")
    public void addGold(final Player player, final int amount) {
        gameManager.economyManager().addGold(player.getUniqueId(), amount, GoldSource.STEAL);
        announcer.announceAdminMessage(player, Component.text("§6[Admin] §f" + amount + " 골드를 지급했습니다."));
    }

    @Subcommand("spendgold")
    public void spendGold(final Player player, final int amount) {
        if (gameManager.economyManager().spendGold(player.getUniqueId(), amount)) {
            announcer.announceAdminMessage(player, Component.text("§6[Admin] §f" + amount + " 골드를 소비했습니다."));
        }
    }
}
