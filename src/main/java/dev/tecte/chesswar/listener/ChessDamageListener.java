package dev.tecte.chesswar.listener;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.GameManager;
import dev.tecte.chesswar.game.GamePhase;
import dev.tecte.chesswar.game.TimerManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceItemUtils;
import dev.tecte.chesswar.piece.PieceType;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ChessDamageListener implements Listener {
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final MoveValidator moveValidator;
    private final TimerManager timerManager;

    @EventHandler
    public void onGlobalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (gameManager.isParticipant(player) && gameManager.phase() != GamePhase.BATTLE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (gameManager.isParticipant(attacker)) {
            event.setCancelled(true);
        }

        if (gameManager.phase() != GamePhase.BATTLE) {
            return;
        }

        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (!PieceItemUtils.isPieceItem(item)) {
            return;
        }

        Optional<UUID> currentTurnId = gameManager.currentTurnPlayer();
        if (currentTurnId.isEmpty() || !currentTurnId.get().equals(attacker.getUniqueId())) {
            attacker.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!gameManager.isParticipant(victim)) {
            return;
        }

        if (!boardManager.hasBoard()) {
            return;
        }

        Coordinate from = null;

        for (var entry : gameManager.boardPieces().entrySet()) {
            if (attacker.getUniqueId().equals(entry.getValue().ownerId())) {
                from = entry.getKey();
                break;
            }
        }

        Coordinate to = null;

        for (var entry : gameManager.boardPieces().entrySet()) {
            if (victim.getUniqueId().equals(entry.getValue().ownerId())) {
                to = entry.getKey();
                break;
            }
        }

        if (from == null || to == null) {
            return;
        }

        if (!moveValidator.canMove(from, to)) {
            attacker.sendMessage(Component.text(
                    "그곳에 있는 적은 공격할 수 없는 범위에 있습니다!",
                    NamedTextColor.RED
            ));

            return;
        }

        Piece myPiece = gameManager.boardPieces().get(from);
        Piece target = gameManager.boardPieces().get(to);

        if (target.team() == myPiece.team()) {
            attacker.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        double damage = myPiece.type().baseDamage();

        if (event.isCritical()) {
            event.setDamage(damage);
        }

        event.setCancelled(false);

        final Coordinate finalFrom = from;
        final Coordinate finalTo = to;
        final Piece finalMyPiece = myPiece;
        final Piece finalTarget = target;

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), () -> {
            finalTarget.currentHealth(victim.getHealth());

            if (victim.getHealth() <= 0) {
                attacker.sendMessage(Component.text(
                        "%s을(를) 처치했습니다!".formatted(finalTarget.type().displayName()),
                        NamedTextColor.AQUA
                ));
                victim.sendMessage(Component.text("처치당했습니다! 관전자로 전환됩니다.", NamedTextColor.DARK_RED));
                victim.setGameMode(GameMode.SPECTATOR);
                gameManager.removePiece(finalTo);
                gameManager.removePiece(finalFrom);
                gameManager.placePiece(finalTo, finalMyPiece);
                attacker.teleport(boardManager.currentBoard().toCenterLocation(finalTo).add(0, 1, 0));

                if (finalTarget.type() == PieceType.KING) {
                    gameManager.win(finalMyPiece.team());
                    timerManager.stopTimer();
                    return;
                }
            }

            finishTurn();
        });
    }

    private void finishTurn() {
        gameManager.nextTurn();
        timerManager.startTurnTimer();
    }
}
