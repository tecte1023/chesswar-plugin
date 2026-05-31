package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.BoardTargetSelectedEvent;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.policy.CombatPolicy;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceMoveEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CombatManager {
    private final ChessWar plugin;
    private final CombatPolicy combatPolicy;

    public CombatManager(final ChessWar plugin) {
        this.plugin = plugin;
        combatPolicy = new CombatPolicy(plugin.gameManager());
    }

    public void updateInvulnerability(final Player currentPlayer) {
        final Participant participant = plugin.gameManager().participants().get(currentPlayer.getUniqueId());

        if (participant == null) {
            return;
        }

        final Optional<Coordinate> commandTarget = plugin.gameManager().findCommandTarget(currentPlayer.getUniqueId());
        final Piece attackerPiece = commandTarget
                .map(target -> plugin.pieceManager().boardPieces().get(target))
                .orElseGet(() -> combatPolicy.getRepresentativeAttacker(participant, plugin.pieceManager()));

        if (attackerPiece == null) {
            return;
        }

        for (final Map.Entry<Coordinate, LivingEntity> entry : plugin.pieceManager().pieceEntities().entrySet()) {
            final Coordinate coord = entry.getKey();
            final LivingEntity living = entry.getValue();

            if (living == null || !living.isValid()) {
                continue;
            }

            final Piece targetPiece = plugin.pieceManager().boardPieces().get(coord);

            if (targetPiece == null) {
                continue;
            }

            final boolean canAttack = combatPolicy.canAttack(attackerPiece, targetPiece);

            living.setInvulnerable(!canAttack);
        }
    }

    public boolean canTakeDamage(final Player participant) {
        return plugin.gameManager().isParticipant(participant) && plugin.gameManager().phase() == GamePhase.BATTLE;
    }

    public void handleAttack(final Player attacker, final LivingEntity victim) {
        if (!isPlayerTurn(attacker)) {
            return;
        }

        if (!plugin.boardManager().hasBoard()) {
            return;
        }

        final Coordinate attackingCoordinate = plugin.pieceManager().findCoordinateByEntity(attacker).orElse(null);

        if (attackingCoordinate == null) {
            return;
        }

        final Optional<Coordinate> commandTarget = plugin.gameManager().findCommandTarget(attacker.getUniqueId());
        final Coordinate finalAttackingCoordinate = commandTarget.orElse(attackingCoordinate);
        final Coordinate targetCoordinate = plugin.pieceManager().findCoordinateByEntity(victim).orElse(null);

        if (targetCoordinate == null) {
            return;
        }

        final Piece attackingPiece = plugin.pieceManager().boardPieces().get(finalAttackingCoordinate);
        final Piece targetPiece = plugin.pieceManager().boardPieces().get(targetCoordinate);

        if (attackingPiece == null || targetPiece == null) {
            return;
        }

        if (targetPiece.team() == attackingPiece.team()) {
            handleCommanderOrFriendlyFire(
                    attacker,
                    attackingCoordinate,
                    targetCoordinate,
                    targetPiece,
                    commandTarget.orElse(null)
            );

            return;
        }

        if (!plugin.moveValidator().canMove(finalAttackingCoordinate, targetCoordinate)) {
            attacker.sendMessage(Component.text(
                    "그곳에 있는 적은 공격할 수 없는 범위에 있습니다!",
                    NamedTextColor.RED
            ));

            return;
        }

        performAttack(attacker, victim, targetCoordinate, finalAttackingCoordinate, attackingPiece, targetPiece);
    }

    public void handleMove(final Player player) {
        if (!isPlayerTurn(player)) {
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

        final Coordinate from = plugin.pieceManager().findCoordinateByEntity(player).orElse(null);

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

        if (movingPiece == null) {
            return;
        }

        if (plugin.pieceManager().findPieceAt(to).isPresent()) {
            player.sendMessage(Component.text(
                    "그곳에 다른 기물이 있어 이동할 수 없습니다. (공격은 좌클릭으로 하세요!)",
                    NamedTextColor.YELLOW
            ));

            return;
        }

        plugin.pieceManager().movePiece(finalFrom, to);

        if (commandTarget.isPresent()) {
            plugin.gameManager().clearCommandTarget(player.getUniqueId());
            player.sendMessage(Component.text(
                    movingPiece.type().displayName() + " 기물을 " + to.x() + ", " + to.y() + " 좌표로 이동시켰습니다.",
                    NamedTextColor.GREEN
            ));
        } else {
            player.sendMessage(Component.text(
                    to.x() + ", " + to.y() + " 좌표로 이동했습니다.",
                    NamedTextColor.GREEN
            ));
        }

        Bukkit.getPluginManager().callEvent(new PieceMoveEvent(player));
        plugin.gameManager().nextTurn();
    }

    private boolean isPlayerTurn(final Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }

        final Optional<UUID> currentTurnPlayerId = plugin.gameManager().currentTurnPlayer();

        if (currentTurnPlayerId.isEmpty() || !currentTurnPlayerId.get().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED));
            return false;
        }

        return true;
    }

    private void handleCommanderOrFriendlyFire(
            final Player attacker,
            final Coordinate myCoord,
            final Coordinate targetCoord,
            final Piece targetPiece,
            final Coordinate currentCommandTarget
    ) {
        final Piece myPiece = plugin.pieceManager().boardPieces().get(myCoord);

        if (myPiece == null) {
            return;
        }

        if (!combatPolicy.canCommand(myPiece, targetPiece)) {
            attacker.sendMessage(Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED));
            return;
        }

        if (targetCoord.equals(currentCommandTarget)) {
            plugin.gameManager().clearCommandTarget(attacker.getUniqueId());
            attacker.sendMessage(Component.text(
                    "%s 지휘를 해제했습니다.".formatted(targetPiece.type().displayName()),
                    NamedTextColor.YELLOW
            ));
            attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 0.5f);
            Bukkit.getPluginManager().callEvent(new BoardTargetSelectedEvent(attacker, null));

            return;
        }

        plugin.gameManager().registerCommandTarget(attacker.getUniqueId(), targetCoord);
        attacker.sendMessage(Component.text(
                "%s을(를) 지휘 대상으로 선택했습니다!".formatted(targetPiece.type().displayName()),
                NamedTextColor.GOLD
        ));
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        Bukkit.getPluginManager().callEvent(new BoardTargetSelectedEvent(attacker, targetCoord));
    }

    private void performAttack(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate targetCoord,
            final Coordinate attackCoord,
            final Piece attackingPiece,
            final Piece targetPiece
    ) {
        final double damage = attackingPiece.type().baseDamage();

        victim.setNoDamageTicks(0);
        victim.damage(damage, attacker);
        plugin.gameManager().stats(attacker.getUniqueId()).addDamageDealt(damage);

        if (victim instanceof Player playerTarget) {
            plugin.gameManager().stats(playerTarget.getUniqueId()).addDamageTaken(damage);
        }

        victim.getWorld().spawnParticle(
                Particle.CRIT,
                victim.getLocation().add(0, 1, 0),
                10, 0.5, 0.5, 0.5, 0.1
        );
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        targetPiece.currentHealth(victim.getHealth());

        if (victim.getHealth() <= 0) {
            handleKill(attacker, victim, targetCoord, attackCoord, attackingPiece, targetPiece);
        }

        plugin.gameManager().clearCommandTarget(attacker.getUniqueId());
        plugin.gameManager().nextTurn();
    }

    private void handleKill(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate targetCoord,
            final Coordinate attackCoord,
            final Piece attackingPiece,
            final Piece targetPiece
    ) {
        plugin.gameManager().stats(attacker.getUniqueId()).addKill();

        if (victim instanceof Player playerTarget) {
            plugin.gameManager().eliminate(playerTarget.getUniqueId());
        }

        attacker.sendMessage(Component.text(
                "%s을(를) 처치했습니다!".formatted(targetPiece.type().displayName()),
                NamedTextColor.AQUA
        ));
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        victim.getWorld().spawnParticle(
                Particle.EXPLOSION,
                victim.getLocation().add(0, 1, 0),
                1, 0, 0, 0, 0
        );
        plugin.pieceManager().capturePiece(attackCoord, targetCoord);

        if (!(victim instanceof Player)) {
            victim.remove();
        }

        if (combatPolicy.isWinConditionMet(targetPiece)) {
            plugin.gameManager().win(attackingPiece.team());
        }
    }
}
