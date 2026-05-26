package dev.tecte.chesswar.listener;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.event.ChessCommandTargetSelectedEvent;
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

        Optional<Coordinate> commandTarget = gameManager.getCommandTarget(attackerParticipant.getUniqueId());
        Coordinate finalAttackingCoordinate = (commandTarget.isPresent()) ? commandTarget.get() : attackingCoordinate;

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

        Piece attackingPiece = gameManager.boardPieces().get(finalAttackingCoordinate);

        if (targetPiece.team() == attackingPiece.team()) {
            Piece myOriginalPiece = gameManager.boardPieces().get(attackingCoordinate);

            if (myOriginalPiece != null && myOriginalPiece.type() == PieceType.KING) {
                if (commandTarget.isPresent() && commandTarget.get().equals(targetCoordinate)) {
                    // 동일 대상 클릭 시 지휘 해제 (Toggle OFF)
                    gameManager.clearCommandTarget(attackerParticipant.getUniqueId());
                    attackerParticipant.sendMessage(Component.text(
                            "%s 지휘를 해제했습니다.".formatted(targetPiece.type().displayName()),
                            NamedTextColor.YELLOW
                    ));
                    attackerParticipant.playSound(attackerParticipant.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 0.5f);
                    Bukkit.getPluginManager().callEvent(new ChessCommandTargetSelectedEvent(attackerParticipant, null));
                } else if (!targetPiece.isPlayerPiece()) {
                    // 새로운 NPC 클릭 시 지휘 대상 지정/변경
                    gameManager.setCommandTarget(attackerParticipant.getUniqueId(), targetCoordinate);
                    attackerParticipant.sendMessage(Component.text(
                            "%s을(를) 지휘 대상으로 선택했습니다!".formatted(targetPiece.type().displayName()),
                            NamedTextColor.GOLD
                    ));
                    attackerParticipant.playSound(attackerParticipant.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    Bukkit.getPluginManager().callEvent(new ChessCommandTargetSelectedEvent(attackerParticipant, targetCoordinate));
                } else {
                    attackerParticipant.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
                }
            } else {
                attackerParticipant.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
            }
            return;
        }

        if (!moveValidator.canMove(finalAttackingCoordinate, targetCoordinate)) {
            attackerParticipant.sendMessage(Component.text(
                    "그곳에 있는 적은 공격할 수 없는 범위에 있습니다!",
                    NamedTextColor.RED
            ));

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

        final Coordinate capturedFinalAttackingCoordinate = finalAttackingCoordinate;
        final Coordinate capturedFinalTargetCoordinate = targetCoordinate;
        final Piece capturedFinalAttackingPiece = attackingPiece;
        final Piece capturedFinalTargetPiece = targetPiece;
        final org.bukkit.entity.LivingEntity capturedFinalTargetEntity = targetEntity;

        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), () -> {
            capturedFinalTargetPiece.currentHealth(capturedFinalTargetEntity.getHealth());

            if (capturedFinalTargetEntity.getHealth() <= 0) {
                // 통계 기록: 킬
                gameManager.getStats(attackerParticipant.getUniqueId()).addKill();
                
                if (capturedFinalTargetEntity instanceof Player playerTarget) {
                    gameManager.getStats(playerTarget.getUniqueId()).addDeath();
                    playerTarget.sendMessage(Component.text(
                            "처치당했습니다! 관전자로 전환됩니다.",
                            NamedTextColor.DARK_RED
                    ));
                    playerTarget.setGameMode(GameMode.SPECTATOR);
                }

                attackerParticipant.sendMessage(Component.text(
                        "%s을(를) 처치했습니다!".formatted(capturedFinalTargetPiece.type().displayName()),
                        NamedTextColor.AQUA
                ));

                gameManager.removePiece(capturedFinalTargetCoordinate);
                gameManager.removePiece(capturedFinalAttackingCoordinate);
                gameManager.placePiece(capturedFinalTargetCoordinate, capturedFinalAttackingPiece);
                
                if (!(capturedFinalTargetEntity instanceof Player)) {
                    capturedFinalTargetEntity.remove(); // NPC 엔티티 제거
                }

                if (capturedFinalAttackingPiece.isPlayerPiece()) {
                    attackerParticipant.teleport(boardManager.currentBoard()
                            .toCenterLocation(capturedFinalTargetCoordinate)
                            .add(0, 1, 0));
                } else {
                    // NPC 기물이 처치 후 이동
                    NamespacedKey coordXKey = new NamespacedKey(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), "barracks_piece_x");
                    NamespacedKey coordYKey = new NamespacedKey(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), "barracks_piece_y");

                    for (org.bukkit.entity.Entity entity : boardManager.currentBoard().origin().getWorld().getEntities()) {
                        Integer ex = entity.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
                        Integer ey = entity.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);

                        if (ex != null && ey != null && capturedFinalAttackingCoordinate.x() == ex && capturedFinalAttackingCoordinate.y() == ey) {
                            entity.teleport(boardManager.currentBoard().toCenterLocation(capturedFinalTargetCoordinate));
                            entity.getPersistentDataContainer().set(coordXKey, PersistentDataType.INTEGER, capturedFinalTargetCoordinate.x());
                            entity.getPersistentDataContainer().set(coordYKey, PersistentDataType.INTEGER, capturedFinalTargetCoordinate.y());
                            break;
                        }
                    }
                }

                if (capturedFinalTargetPiece.type() == PieceType.KING) {
                    gameManager.win(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), boardManager, timerManager, capturedFinalAttackingPiece.team());
                    return;
                }
            }

            gameManager.clearCommandTarget(attackerParticipant.getUniqueId());
            gameManager.finishTurn();
        });
    }
}
