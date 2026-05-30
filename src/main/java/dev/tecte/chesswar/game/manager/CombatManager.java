package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardTargetSelectedEvent;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.TurnStartedEvent;
import dev.tecte.chesswar.game.policy.CombatPolicy;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceMoveEvent;
import dev.tecte.chesswar.piece.PieceType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CombatManager implements Listener {
    private final ChessWar plugin;
    private final CombatPolicy combatPolicy;

    public CombatManager(ChessWar plugin) {
        this.plugin = plugin;
        this.combatPolicy = new CombatPolicy(plugin.gameManager());
    }

    @EventHandler
    public void onTurnStart(TurnStartedEvent event) {
        updateInvulnerability(event.getPlayer());
    }

    public void updateInvulnerability(Player currentPlayer) {
        final var participant = plugin.gameManager().participants().get(currentPlayer.getUniqueId());
        if (participant == null) return;

        Piece attackerPiece = null;
        for (final Piece p : plugin.pieceManager().boardPieces().values()) {
            if (currentPlayer.getUniqueId().equals(p.ownerId()) && p.type() == PieceType.KING) {
                attackerPiece = p;
                break;
            }
        }

        if (attackerPiece == null) {
            attackerPiece = Piece.of(currentPlayer.getUniqueId(), participant.team(), PieceType.PAWN);
        }

        for (final Map.Entry<Coordinate, LivingEntity> entry : plugin.pieceManager().pieceEntities().entrySet()) {
            final Coordinate coord = entry.getKey();
            final LivingEntity living = entry.getValue();

            if (living == null || !living.isValid()) continue;

            final Piece targetPiece = plugin.pieceManager().boardPieces().get(coord);
            if (targetPiece == null) continue;

            final boolean canAttack = combatPolicy.canAttack(attackerPiece, targetPiece);
            living.setInvulnerable(!canAttack);
        }
    }

    public boolean canTakeDamage(Player participant) {
        return plugin.gameManager().isParticipant(participant) && plugin.gameManager().phase() == GamePhase.BATTLE;
    }

    public void handleAttack(Player attacker, LivingEntity victim) {
        if (attacker.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        final Optional<UUID> currentTurnPlayerId = plugin.gameManager().currentTurnPlayer();
        if (currentTurnPlayerId.isEmpty() || !currentTurnPlayerId.get().equals(attacker.getUniqueId())) {
            attacker.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (!plugin.boardManager().hasBoard()) {
            return;
        }

        final Coordinate attackingCoordinate = findAttackingCoordinate(attacker.getUniqueId());
        if (attackingCoordinate == null) {
            return;
        }

        final Optional<Coordinate> commandTarget = plugin.gameManager().findCommandTarget(attacker.getUniqueId());
        final Coordinate finalAttackingCoordinate = commandTarget.orElse(attackingCoordinate);

        final Coordinate targetCoordinate = findTargetCoordinate(victim);
        if (targetCoordinate == null) {
            return;
        }

        final Piece attackingPiece = plugin.pieceManager().boardPieces().get(finalAttackingCoordinate);
        final Piece targetPiece = plugin.pieceManager().boardPieces().get(targetCoordinate);

        if (targetPiece.team() == attackingPiece.team()) {
            handleCommanderOrFriendlyFire(attacker, attackingCoordinate, targetCoordinate, targetPiece, commandTarget);
            return;
        }

        if (!plugin.moveValidator().canMove(finalAttackingCoordinate, targetCoordinate)) {
            attacker.sendMessage(Component.text("그곳에 있는 적은 공격할 수 없는 범위에 있습니다!", NamedTextColor.RED));
            return;
        }

        performAttack(attacker, victim, targetCoordinate, finalAttackingCoordinate, attackingPiece, targetPiece);
    }

    public void handleMove(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        final Optional<UUID> currentTurnId = plugin.gameManager().currentTurnPlayer();
        if (currentTurnId.isEmpty() || !currentTurnId.get().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return;
        }

        if (!plugin.boardManager().hasBoard()) {
            player.sendMessage(Component.text("체스판이 설정되지 않았습니다!", NamedTextColor.RED));
            return;
        }

        final Coordinate to = plugin.boardManager().currentBoard().toCoordinate(player.getLocation());
        if (!to.isValid()) {
            player.sendMessage(Component.text("체스판 밖에서는 이동을 확정할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        final Coordinate from = findAttackingCoordinate(player.getUniqueId());
        if (from == null) {
            player.sendMessage(Component.text("보드 위에 당신의 기물이 없습니다!", NamedTextColor.RED));
            return;
        }

        final Optional<Coordinate> commandTarget = plugin.gameManager().findCommandTarget(player.getUniqueId());
        final Coordinate finalFrom = commandTarget.orElse(from);

        if (finalFrom.equals(to)) {
            player.sendMessage(Component.text("현재 위치와 같은 곳으로 이동할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        if (!plugin.moveValidator().canMove(finalFrom, to)) {
            player.sendMessage(Component.text("그곳으로는 이동할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        final Piece movingPiece = plugin.pieceManager().boardPieces().get(finalFrom);
        if (plugin.pieceManager().findPieceAt(to).isPresent()) {
            player.sendMessage(Component.text("그곳에 다른 기물이 있어 이동할 수 없습니다. (공격은 좌클릭으로 하세요!)", NamedTextColor.YELLOW));
            return;
        }

        plugin.pieceManager().removePiece(finalFrom);
        plugin.pieceManager().placePiece(to, movingPiece);

        if (commandTarget.isPresent()) {
            relocateNPCEntity(finalFrom, to);
            plugin.gameManager().clearCommandTarget(player.getUniqueId());
            player.sendMessage(Component.text(movingPiece.type().displayName() + " 기물을 " + to.x() + ", " + to.y() + " 좌표로 이동시켰습니다.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text(to.x() + ", " + to.y() + " 좌표로 이동했습니다.", NamedTextColor.GREEN));
        }

        Bukkit.getPluginManager().callEvent(new PieceMoveEvent(player));
        plugin.gameManager().nextTurn();
    }

    private Coordinate findAttackingCoordinate(UUID attackerId) {
        for (final Map.Entry<Coordinate, Piece> entry : plugin.pieceManager().boardPieces().entrySet()) {
            if (attackerId.equals(entry.getValue().ownerId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Coordinate findTargetCoordinate(LivingEntity victim) {
        if (victim instanceof Player targetParticipant) {
            if (!plugin.gameManager().isParticipant(targetParticipant)) {
                return null;
            }
            for (final Map.Entry<Coordinate, Piece> entry : plugin.pieceManager().boardPieces().entrySet()) {
                if (targetParticipant.getUniqueId().equals(entry.getValue().ownerId())) {
                    return entry.getKey();
                }
            }
        } else {
            final NamespacedKey coordXKey = new NamespacedKey(plugin, "barracks_piece_x");
            final NamespacedKey coordYKey = new NamespacedKey(plugin, "barracks_piece_y");
            final Integer tx = victim.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
            final Integer ty = victim.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);
            if (tx != null && ty != null) {
                return Coordinate.of(tx, ty);
            }
        }
        return null;
    }

    private void handleCommanderOrFriendlyFire(Player attacker, Coordinate myCoord, Coordinate targetCoord, Piece targetPiece, Optional<Coordinate> currentCommandTarget) {
        final Piece myPiece = plugin.pieceManager().boardPieces().get(myCoord);
        if (myPiece == null) return;

        if (combatPolicy.canCommand(myPiece, targetPiece)) {
            if (currentCommandTarget.isPresent() && currentCommandTarget.get().equals(targetCoord)) {
                plugin.gameManager().clearCommandTarget(attacker.getUniqueId());
                attacker.sendMessage(Component.text("%s 지휘를 해제했습니다.".formatted(targetPiece.type().displayName()), NamedTextColor.YELLOW));
                attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 0.5f);
                Bukkit.getPluginManager().callEvent(new BoardTargetSelectedEvent(attacker, null));
            } else {
                plugin.gameManager().registerCommandTarget(attacker.getUniqueId(), targetCoord);
                attacker.sendMessage(Component.text("%s을(를) 지휘 대상으로 선택했습니다!".formatted(targetPiece.type().displayName()), NamedTextColor.GOLD));
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                Bukkit.getPluginManager().callEvent(new BoardTargetSelectedEvent(attacker, targetCoord));
            }
        } else {
            attacker.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
        }
    }

    private void performAttack(Player attacker, LivingEntity victim, Coordinate targetCoord, Coordinate attackCoord, Piece attackingPiece, Piece targetPiece) {
        final double damage = attackingPiece.type().baseDamage();
        victim.setNoDamageTicks(0);
        victim.damage(damage, attacker);

        plugin.gameManager().stats(attacker.getUniqueId()).addDamageDealt(damage);
        if (victim instanceof Player playerTarget) {
            plugin.gameManager().stats(playerTarget.getUniqueId()).addDamageTaken(damage);
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            targetPiece.currentHealth(victim.getHealth());
            if (victim.getHealth() <= 0) {
                handleKill(attacker, victim, targetCoord, attackCoord, attackingPiece, targetPiece);
            }
            plugin.gameManager().clearCommandTarget(attacker.getUniqueId());
            plugin.gameManager().nextTurn();
        });
    }

    private void handleKill(Player attacker, LivingEntity victim, Coordinate targetCoord, Coordinate attackCoord, Piece attackingPiece, Piece targetPiece) {
        plugin.gameManager().stats(attacker.getUniqueId()).addKill();
        if (victim instanceof Player playerTarget) {
            plugin.gameManager().stats(playerTarget.getUniqueId()).addDeath();
            playerTarget.sendMessage(Component.text("처치당했습니다! 관전자로 전환됩니다.", NamedTextColor.DARK_RED));
            playerTarget.setGameMode(GameMode.SPECTATOR);
        }

        attacker.sendMessage(Component.text("%s을(를) 처치했습니다!".formatted(targetPiece.type().displayName()), NamedTextColor.AQUA));
        plugin.pieceManager().removePiece(targetCoord);
        plugin.pieceManager().removePiece(attackCoord);
        plugin.pieceManager().placePiece(targetCoord, attackingPiece);

        if (!(victim instanceof Player)) {
            victim.remove();
        }

        if (attackingPiece.isPlayerPiece()) {
            attacker.teleport(plugin.boardManager().currentBoard().toCenterLocation(targetCoord).add(0, 1, 0));
        } else {
            relocateNPCEntity(attackCoord, targetCoord);
        }

        if (targetPiece.type() == PieceType.KING) {
            plugin.gameManager().win(attackingPiece.team());
        }
    }

    private void relocateNPCEntity(Coordinate from, Coordinate to) {
        final NamespacedKey coordXKey = new NamespacedKey(plugin, "barracks_piece_x");
        final NamespacedKey coordYKey = new NamespacedKey(plugin, "barracks_piece_y");

        for (final Entity entity : plugin.boardManager().currentBoard().origin().getWorld().getEntities()) {
            final Integer ex = entity.getPersistentDataContainer().get(coordXKey, PersistentDataType.INTEGER);
            final Integer ey = entity.getPersistentDataContainer().get(coordYKey, PersistentDataType.INTEGER);

            if (ex != null && ey != null && from.x() == ex && from.y() == ey) {
                entity.teleport(plugin.boardManager().currentBoard().toCenterLocation(to));
                entity.getPersistentDataContainer().set(coordXKey, PersistentDataType.INTEGER, to.x());
                entity.getPersistentDataContainer().set(coordYKey, PersistentDataType.INTEGER, to.y());
                break;
            }
        }
    }
}
