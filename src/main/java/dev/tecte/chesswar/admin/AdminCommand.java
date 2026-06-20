package dev.tecte.chesswar.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.economy.GoldSource;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@CommandAlias("chesswaradmin|cwa")
@CommandPermission("chesswar.admin")
@RequiredArgsConstructor
public class AdminCommand extends BaseCommand {
    private final GameManager gameManager;
    private final GameAnnouncer announcer;

    @Subcommand("damage")
    public void damagePiece(final Player player, final int x, final int y, final double amount) {
        final Coordinate coord = Coordinate.of(x, y);
        applyDamage(player, coord, amount);
    }

    @Subcommand("damagehere")
    public void damageHere(final Player player, final double amount) {
        final Coordinate coord = gameManager.pieceManager().findCoordinate(player);
        if (coord != null) {
            applyDamage(player, coord, amount);
        } else {
            announcer.announceAdminMessage(player, Component.text("§6[Admin] §c당신은 현재 기물 위에 서 있지 않습니다."));
        }
    }

    private void applyDamage(final Player player, final Coordinate coord, final double amount) {
        final dev.tecte.chesswar.piece.Piece piece = gameManager.pieceState().piece(coord);
        final LivingEntity entity = piece != null ? (piece.isPlayer() ? Bukkit.getPlayer(piece.id()) : (Bukkit.getEntity(piece.id()) instanceof LivingEntity e ? e : null)) : null;

        if (piece == null || entity == null) {
            announcer.announceAdminMessage(player, Component.text("§6[Admin] §c" + coord.x() + ", " + coord.y() + " 좌표에 기물이 존재하지 않습니다."));
            return;
        }

        entity.damage(amount, player);

        announcer.announceAdminMessage(player, Component.text("§6[Admin] §f" + coord.x() + ", " + coord.y() + " 좌표의 기물(" + piece.type().displayName() + ")에게 " + (int)amount + " 대미지를 입혔습니다."));
    }

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
