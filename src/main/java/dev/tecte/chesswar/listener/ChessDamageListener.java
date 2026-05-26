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
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
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
    public void preventNonBattleDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player participant)) {
            return;
        }

        if (gameManager.isParticipant(participant) && gameManager.phase() != GamePhase.BATTLE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleCombatInteraction(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attackerParticipant)) {
            return;
        }

        if (attackerParticipant.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (gameManager.isParticipant(attackerParticipant)) {
            event.setCancelled(true);
        }

        if (gameManager.phase() != GamePhase.BATTLE) {
            return;
        }

        ItemStack weaponItem = attackerParticipant.getInventory().getItemInMainHand();

        if (!PieceItemUtils.isPieceItem(weaponItem)) {
            return;
        }

        Optional<UUID> currentTurnPlayerId = gameManager.currentTurnPlayer();

        if (currentTurnPlayerId.isEmpty() || !currentTurnPlayerId.get().equals(attackerParticipant.getUniqueId())) {
            attackerParticipant.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (!boardManager.hasBoard()) {
            return;
        }

        Coordinate attackingCoordinate = null;

        for (var entry : gameManager.boardPieces().entrySet()) {
            if (attackerParticipant.getUniqueId().equals(entry.getValue().ownerId())) {
                attackingCoordinate = entry.getKey();
                break;
            }
        }

        if (attackingCoordinate == null) {
            return;
        }

        Coordinate targetCoordinate = null;
        Piece targetPiece = null;
        org.bukkit.entity.LivingEntity targetEntity = null;

        if (event.getEntity() instanceof Player targetParticipant) {
            if (!gameManager.isParticipant(targetParticipant)) {
                return;
            }

            for (var entry : gameManager.boardPieces().entrySet()) {
                if (targetParticipant.getUniqueId().equals(entry.getValue().ownerId())) {
                    targetCoordinate = entry.getKey();
                    targetPiece = entry.getValue();
                    targetEntity = targetParticipant;
                    break;
                }
            }
        } else if (event.getEntity() instanceof org.bukkit.entity.LivingEntity livingTarget) {
            NamespacedKey coordXKey = new NamespacedKey(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), "barracks_piece_x");
            NamespacedKey coordYKey = new NamespacedKey(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), "barracks_piece_y");

            Integer tx = livingTarget.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
            Integer ty = livingTarget.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);

            if (tx != null && ty != null) {
                targetCoordinate = Coordinate.of(tx, ty);
                targetPiece = gameManager.boardPieces().get(targetCoordinate);
                targetEntity = livingTarget;
            }
        }

        if (targetCoordinate == null || targetPiece == null || targetEntity == null) {
            return;
        }

        if (!moveValidator.canMove(attackingCoordinate, targetCoordinate)) {
            attackerParticipant.sendMessage(Component.text(
                    "그곳에 있는 적은 공격할 수 없는 범위에 있습니다!",
                    NamedTextColor.RED
            ));

            return;
        }

        Piece attackingPiece = gameManager.boardPieces().get(attackingCoordinate);

        if (targetPiece.team() == attackingPiece.team()) {
            attackerParticipant.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        event.setDamage(attackingPiece.type().baseDamage());
        event.setCancelled(false);
        targetEntity.setNoDamageTicks(0);

        // 통계 기록: 가한 피해
        gameManager.getStats(attackerParticipant.getUniqueId()).addDamageDealt(attackingPiece.type().baseDamage());
        
        if (targetEntity instanceof Player playerTarget) {
            gameManager.getStats(playerTarget.getUniqueId()).addDamageTaken(attackingPiece.type().baseDamage());
        }

        final Coordinate finalAttackingCoordinate = attackingCoordinate;
        final Coordinate finalTargetCoordinate = targetCoordinate;
        final Piece finalAttackingPiece = attackingPiece;
        final Piece finalTargetPiece = targetPiece;
        final org.bukkit.entity.LivingEntity finalTargetEntity = targetEntity;

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), () -> {
            finalTargetPiece.currentHealth(finalTargetEntity.getHealth());

            if (finalTargetEntity.getHealth() <= 0) {
                // 통계 기록: 킬
                gameManager.getStats(attackerParticipant.getUniqueId()).addKill();
                
                if (finalTargetEntity instanceof Player playerTarget) {
                    gameManager.getStats(playerTarget.getUniqueId()).addDeath();
                    playerTarget.sendMessage(Component.text(
                            "처치당했습니다! 관전자로 전환됩니다.",
                            NamedTextColor.DARK_RED
                    ));
                    playerTarget.setGameMode(GameMode.SPECTATOR);
                }

                attackerParticipant.sendMessage(Component.text(
                        "%s을(를) 처치했습니다!".formatted(finalTargetPiece.type().displayName()),
                        NamedTextColor.AQUA
                ));

                gameManager.removePiece(finalTargetCoordinate);
                gameManager.removePiece(finalAttackingCoordinate);
                gameManager.placePiece(finalTargetCoordinate, finalAttackingPiece);
                
                if (!(finalTargetEntity instanceof Player)) {
                    finalTargetEntity.remove(); // NPC 엔티티 제거
                }

                attackerParticipant.teleport(boardManager.currentBoard()
                        .toCenterLocation(finalTargetCoordinate)
                        .add(0, 1, 0));

                if (finalTargetPiece.type() == PieceType.KING) {
                    gameManager.win(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), boardManager, timerManager, finalAttackingPiece.team());
                    return;
                }
            }

            gameManager.finishTurn();
        });
    }
}
