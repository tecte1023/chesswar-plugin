package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardTargetSelectedEvent;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceMoveEvent;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class CombatManager {
    private final ChessWar plugin;
    private final GameManager gameManager;
    private final BoardManager boardManager;
    private final PieceManager pieceManager;
    private final MoveValidator moveValidator;
    private final TimerManager timerManager;

    public boolean canTakeDamage(Player participant) {
        return gameManager.isParticipant(participant) && gameManager.phase() == GamePhase.BATTLE;
    }

    public void handleAttack(Player attacker, LivingEntity victim) {
        if (attacker.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Optional<UUID> currentTurnPlayerId = gameManager.currentTurnPlayer();
        if (currentTurnPlayerId.isEmpty() || !currentTurnPlayerId.get().equals(attacker.getUniqueId())) {
            attacker.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (!boardManager.hasBoard()) {
            return;
        }

        Coordinate attackingCoordinate = findAttackingCoordinate(attacker.getUniqueId());
        if (attackingCoordinate == null) {
            return;
        }

        Optional<Coordinate> commandTarget = gameManager.getCommandTarget(attacker.getUniqueId());
        Coordinate finalAttackingCoordinate = commandTarget.orElse(attackingCoordinate);

        Coordinate targetCoordinate = findTargetCoordinate(victim);
        if (targetCoordinate == null) {
            return;
        }

        Piece attackingPiece = pieceManager.boardPieces().get(finalAttackingCoordinate);
        Piece targetPiece = pieceManager.boardPieces().get(targetCoordinate);

        if (targetPiece.team() == attackingPiece.team()) {
            handleCommanderOrFriendlyFire(attacker, attackingCoordinate, targetCoordinate, targetPiece, commandTarget);
            return;
        }

        if (!moveValidator.canMove(finalAttackingCoordinate, targetCoordinate)) {
            attacker.sendMessage(Component.text("그곳에 있는 적은 공격할 수 없는 범위에 있습니다!", NamedTextColor.RED));
            return;
        }

        performAttack(attacker, victim, targetCoordinate, finalAttackingCoordinate, attackingPiece, targetPiece);
    }

    public void handleMove(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Optional<UUID> currentTurnId = gameManager.currentTurnPlayer();
        if (currentTurnId.isEmpty() || !currentTurnId.get().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (!boardManager.hasBoard()) {
            player.sendMessage(Component.text("체스판이 설정되지 않았습니다!", NamedTextColor.RED));
            return;
        }

        Coordinate to = boardManager.currentBoard().toCoordinate(player.getLocation());
        if (!to.isValid()) {
            player.sendMessage(Component.text("체스판 밖에서는 이동을 확정할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        Coordinate from = findAttackingCoordinate(player.getUniqueId());
        if (from == null) {
            player.sendMessage(Component.text("보드 위에 당신의 기물이 없습니다!", NamedTextColor.RED));
            return;
        }

        Optional<Coordinate> commandTarget = gameManager.getCommandTarget(player.getUniqueId());
        Coordinate finalFrom = commandTarget.orElse(from);

        if (finalFrom.equals(to)) {
            player.sendMessage(Component.text("현재 위치와 같은 곳으로 이동할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        if (!moveValidator.canMove(finalFrom, to)) {
            player.sendMessage(Component.text("그곳으로는 이동할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        Piece movingPiece = pieceManager.boardPieces().get(finalFrom);
        if (pieceManager.findPieceAt(to).isPresent()) {
            player.sendMessage(Component.text("그곳에 다른 기물이 있어 이동할 수 없습니다. (공격은 좌클릭으로 하세요!)", NamedTextColor.YELLOW));
            return;
        }

        pieceManager.removePiece(finalFrom);
        pieceManager.placePiece(to, movingPiece);

        if (commandTarget.isPresent()) {
            relocateNPCEntity(finalFrom, to);
            gameManager.clearCommandTarget(player.getUniqueId());
            player.sendMessage(Component.text(movingPiece.type().displayName() + " 기물을 " + to.x() + ", " + to.y() + " 좌표로 이동시켰습니다.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text(to.x() + ", " + to.y() + " 좌표로 이동했습니다.", NamedTextColor.GREEN));
        }

        Bukkit.getPluginManager().callEvent(new PieceMoveEvent(player));
        gameManager.nextTurn(pieceManager);
    }

    private Coordinate findAttackingCoordinate(UUID attackerId) {
        for (var entry : pieceManager.boardPieces().entrySet()) {
            if (attackerId.equals(entry.getValue().ownerId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Coordinate findTargetCoordinate(LivingEntity victim) {
        if (victim instanceof Player targetParticipant) {
            if (!gameManager.isParticipant(targetParticipant)) {
                return null;
            }
            for (var entry : pieceManager.boardPieces().entrySet()) {
                if (targetParticipant.getUniqueId().equals(entry.getValue().ownerId())) {
                    return entry.getKey();
                }
            }
        } else {
            NamespacedKey coordXKey = new NamespacedKey(plugin, "barracks_piece_x");
            NamespacedKey coordYKey = new NamespacedKey(plugin, "barracks_piece_y");
            Integer tx = victim.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
            Integer ty = victim.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);
            if (tx != null && ty != null) {
                return Coordinate.of(tx, ty);
            }
        }
        return null;
    }

    private void handleCommanderOrFriendlyFire(Player attacker, Coordinate myCoord, Coordinate targetCoord, Piece targetPiece, Optional<Coordinate> currentCommandTarget) {
        Piece myPiece = pieceManager.boardPieces().get(myCoord);
        if (myPiece != null && myPiece.type() == PieceType.KING) {
            if (currentCommandTarget.isPresent() && currentCommandTarget.get().equals(targetCoord)) {
                gameManager.clearCommandTarget(attacker.getUniqueId());
                attacker.sendMessage(Component.text("%s 지휘를 해제했습니다.".formatted(targetPiece.type().displayName()), NamedTextColor.YELLOW));
                attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 0.5f);
                Bukkit.getPluginManager().callEvent(new BoardTargetSelectedEvent(attacker, null));
            } else if (!targetPiece.isPlayerPiece()) {
                gameManager.setCommandTarget(attacker.getUniqueId(), targetCoord);
                attacker.sendMessage(Component.text("%s을(를) 지휘 대상으로 선택했습니다!".formatted(targetPiece.type().displayName()), NamedTextColor.GOLD));
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                Bukkit.getPluginManager().callEvent(new BoardTargetSelectedEvent(attacker, targetCoord));
            } else {
                attacker.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
            }
        } else {
            attacker.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
        }
    }

    private void performAttack(Player attacker, LivingEntity victim, Coordinate targetCoord, Coordinate attackCoord, Piece attackingPiece, Piece targetPiece) {
        double damage = attackingPiece.type().baseDamage();
        victim.setNoDamageTicks(0);
        victim.damage(damage, attacker);

        gameManager.getStats(attacker.getUniqueId()).addDamageDealt(damage);
        if (victim instanceof Player playerTarget) {
            gameManager.getStats(playerTarget.getUniqueId()).addDamageTaken(damage);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            targetPiece.currentHealth(victim.getHealth());
            if (victim.getHealth() <= 0) {
                handleKill(attacker, victim, targetCoord, attackCoord, attackingPiece, targetPiece);
            }
            gameManager.clearCommandTarget(attacker.getUniqueId());
            gameManager.nextTurn(pieceManager);
        });
    }

    private void handleKill(Player attacker, LivingEntity victim, Coordinate targetCoord, Coordinate attackCoord, Piece attackingPiece, Piece targetPiece) {
        gameManager.getStats(attacker.getUniqueId()).addKill();
        if (victim instanceof Player playerTarget) {
            gameManager.getStats(playerTarget.getUniqueId()).addDeath();
            playerTarget.sendMessage(Component.text("처치당했습니다! 관전자로 전환됩니다.", NamedTextColor.DARK_RED));
            playerTarget.setGameMode(GameMode.SPECTATOR);
        }

        attacker.sendMessage(Component.text("%s을(를) 처치했습니다!".formatted(targetPiece.type().displayName()), NamedTextColor.AQUA));
        pieceManager.removePiece(targetCoord);
        pieceManager.removePiece(attackCoord);
        pieceManager.placePiece(targetCoord, attackingPiece);

        if (!(victim instanceof Player)) {
            victim.remove();
        }

        if (attackingPiece.isPlayerPiece()) {
            attacker.teleport(boardManager.currentBoard().toCenterLocation(targetCoord).add(0, 1, 0));
        } else {
            relocateNPCEntity(attackCoord, targetCoord);
        }

        if (targetPiece.type() == PieceType.KING) {
            gameManager.win(plugin, boardManager, pieceManager, timerManager, attackingPiece.team());
        }
    }

    private void relocateNPCEntity(Coordinate from, Coordinate to) {
        NamespacedKey coordXKey = new NamespacedKey(plugin, "barracks_piece_x");
        NamespacedKey coordYKey = new NamespacedKey(plugin, "barracks_piece_y");

        for (Entity entity : boardManager.currentBoard().origin().getWorld().getEntities()) {
            Integer ex = entity.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
            Integer ey = entity.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);

            if (ex != null && ey != null && from.x() == ex && from.y() == ey) {
                entity.teleport(boardManager.currentBoard().toCenterLocation(to));
                entity.getPersistentDataContainer().set(coordXKey, PersistentDataType.INTEGER, to.x());
                entity.getPersistentDataContainer().set(coordYKey, PersistentDataType.INTEGER, to.y());
                break;
            }
        }
    }
}
