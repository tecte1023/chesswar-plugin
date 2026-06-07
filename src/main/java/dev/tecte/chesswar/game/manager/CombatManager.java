package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.CombatPolicy;
import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.piece.PieceType;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class CombatManager {
    private static final double DEFAULT_HEALTH = 20.0;
    private static final double DEFAULT_ATTACK_DAMAGE = 1.0;

    private static final float SOUND_VOLUME_ATTACK = 1.0f;
    private static final float SOUND_PITCH_ATTACK = 1.0f;
    private static final float SOUND_VOLUME_KILL = 1.0f;
    private static final float SOUND_PITCH_KILL = 0.5f;
    private static final float SOUND_VOLUME_COMMAND = 1.0f;
    private static final float SOUND_PITCH_COMMAND_OFF = 0.5f;
    private static final float SOUND_PITCH_COMMAND_ON = 1.0f;

    private static final int PARTICLE_COUNT_ATTACK = 10;
    private static final double PARTICLE_OFFSET = 0.5;
    private static final double PARTICLE_SPEED_ATTACK = 0.1;

    private static final Component ERROR_NOT_YOUR_TURN = Component.text("당신의 턴이 아닙니다!", NamedTextColor.RED);
    private static final Component ERROR_NO_BOARD = Component.text("체스판이 설정되지 않았습니다!", NamedTextColor.RED);
    private static final Component ERROR_OUT_OF_BOARD = Component.text("체스판 밖에서는 이동을 확정할 수 없습니다!", NamedTextColor.RED);
    private static final Component ERROR_NO_PIECE_ON_BOARD = Component.text("보드 위에 당신의 기물이 없습니다!", NamedTextColor.RED);
    private static final Component ERROR_SAME_POSITION = Component.text("현재 위치와 같은 곳으로 이동할 수 없습니다!", NamedTextColor.RED);
    private static final Component ERROR_INVALID_MOVE_RANGE = Component.text("그곳으로는 이동할 수 없습니다!", NamedTextColor.RED);
    private static final Component ERROR_OCCUPIED_BY_PIECE = Component.text("그곳에 다른 기물이 있어 이동할 수 없습니다. (공격은 좌클릭으로 하세요!)", NamedTextColor.YELLOW);
    private static final Component ERROR_ATTACK_OUT_OF_RANGE = Component.text("그곳에 있는 적은 공격할 수 없는 범위에 있습니다!", NamedTextColor.RED);
    private static final Component ERROR_FRIENDLY_FIRE_PROHIBITED = Component.text("아군을 공격할 수 없습니다!", NamedTextColor.RED);

    private final GameContext context;
    private final BoardManager boardManager;
    private final PieceManager pieceManager;
    private final PieceState pieceState;
    private final MoveValidator moveValidator;
    private final CombatPolicy combatPolicy;

    public void applyStats(final Player player, final PieceType type) {
        final AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        final AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(type.baseHealth());
            player.setHealth(type.baseHealth());
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(type.baseDamage());
        }
    }

    public void resetStats(final Player player) {
        final AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        final AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null) {
            maxHealth.setBaseValue(DEFAULT_HEALTH);
            player.setHealth(DEFAULT_HEALTH);
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(DEFAULT_ATTACK_DAMAGE);
        }
    }

    public void restoreStats(final Player player, final Double health, final Double damage) {
        final AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        final AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);

        if (maxHealth != null && health != null) {
            maxHealth.setBaseValue(health);
            player.setHealth(Math.min(player.getHealth(), health));
        }

        if (attackDamage != null && damage != null) {
            attackDamage.setBaseValue(damage);
        }
    }

    public void resetAllStats() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            resetStats(player);
        }
    }

    public void updateInvulnerability(final Player currentPlayer) {
        final Participant participant = context.participants().get(currentPlayer.getUniqueId());

        if (participant == null) {
            return;
        }

        final Coordinate commandTarget = participant.commanderTarget();
        final Piece attackerPiece = (commandTarget != null)
                ? pieceState.boardPieces().get(commandTarget)
                : combatPolicy.getPrimaryAttacker(participant, pieceState);

        if (attackerPiece == null) {
            return;
        }

        for (final Map.Entry<Coordinate, LivingEntity> entry : pieceState.pieceEntities().entrySet()) {
            final Coordinate coord = entry.getKey();
            final LivingEntity living = entry.getValue();

            if (living == null || !living.isValid()) {
                continue;
            }

            final Piece targetPiece = pieceState.boardPieces().get(coord);

            if (targetPiece == null) {
                continue;
            }

            final boolean canAttack = combatPolicy.canAttack(attackerPiece, targetPiece);

            if (!(living instanceof Player)) {
                living.setInvulnerable(!canAttack);
            }
        }
    }

    public boolean canTakeDamage(final Player participant) {
        return context.participants().containsKey(participant.getUniqueId()) && context.currentPhase() == GamePhase.BATTLE;
    }

    public boolean handleAttack(final Player attacker, final LivingEntity victim) {
        if (!isPlayerTurn(attacker)) {
            return false;
        }

        if (!boardManager.hasBoard()) {
            return false;
        }

        final Coordinate attackingCoordinate = pieceState.entityToCoordinate().get(attacker.getUniqueId());

        if (attackingCoordinate == null) {
            return false;
        }

        final Participant participant = context.participants().get(attacker.getUniqueId());
        final Coordinate commandTarget = participant.commanderTarget();
        final Coordinate finalAttackingCoordinate = (commandTarget != null) ? commandTarget : attackingCoordinate;
        final Coordinate targetCoordinate = pieceState.entityToCoordinate().get(victim.getUniqueId());

        if (targetCoordinate == null) {
            return false;
        }

        final Piece attackingPiece = pieceState.boardPieces().get(finalAttackingCoordinate);
        final Piece targetPiece = pieceState.boardPieces().get(targetCoordinate);

        if (attackingPiece == null || targetPiece == null) {
            return false;
        }

        if (targetPiece.team() == attackingPiece.team()) {
            handleCommanderOrFriendlyFire(
                    attacker,
                    attackingCoordinate,
                    targetCoordinate,
                    targetPiece,
                    commandTarget
            );

            return false;
        }

        if (!moveValidator.canMove(pieceState, finalAttackingCoordinate, targetCoordinate)) {
            attacker.sendMessage(ERROR_ATTACK_OUT_OF_RANGE);
            return false;
        }

        return performAttack(attacker, victim, targetCoordinate, finalAttackingCoordinate, attackingPiece, targetPiece);
    }

    public boolean handleMove(final Player player) {
        if (!isPlayerTurn(player)) {
            return false;
        }

        if (!boardManager.hasBoard()) {
            player.sendMessage(ERROR_NO_BOARD);
            return false;
        }

        final Coordinate to = boardManager.currentBoard().toCoordinate(player.getLocation());

        if (!to.isValid()) {
            player.sendMessage(ERROR_OUT_OF_BOARD);
            return false;
        }

        final Coordinate from = pieceState.entityToCoordinate().get(player.getUniqueId());

        if (from == null) {
            player.sendMessage(ERROR_NO_PIECE_ON_BOARD);
            return false;
        }

        final Participant participant = context.participants().get(player.getUniqueId());
        final Coordinate commandTarget = participant.commanderTarget();
        final Coordinate finalFrom = (commandTarget != null) ? commandTarget : from;

        if (finalFrom.equals(to)) {
            player.sendMessage(ERROR_SAME_POSITION);
            return false;
        }

        if (!moveValidator.canMove(pieceState, finalFrom, to)) {
            player.sendMessage(ERROR_INVALID_MOVE_RANGE);
            return false;
        }

        final Piece movingPiece = pieceState.boardPieces().get(finalFrom);

        if (movingPiece == null) {
            return false;
        }

        if (pieceState.boardPieces().containsKey(to)) {
            player.sendMessage(ERROR_OCCUPIED_BY_PIECE);
            return false;
        }

        pieceManager.movePiece(boardManager.currentBoard(), finalFrom, to);

        if (commandTarget != null) {
            participant.commanderTarget(null);
            player.sendMessage(Component.text()
                    .append(Component.text(movingPiece.type().displayName(), NamedTextColor.GOLD))
                    .append(Component.text(" 기물을 " + to.x() + ", " + to.y() + " 좌표로 이동시켰습니다.", NamedTextColor.GREEN))
                    .build());
        } else {
            player.sendMessage(Component.text(to.x() + ", " + to.y() + " 좌표로 이동했습니다.", NamedTextColor.GREEN));
        }

        return true;
    }

    private boolean isPlayerTurn(final Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }

        final UUID currentTurnPlayerId = context.currentTurnPlayerId();

        if (currentTurnPlayerId == null || !currentTurnPlayerId.equals(player.getUniqueId())) {
            player.sendMessage(ERROR_NOT_YOUR_TURN);
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
        final Piece myPiece = pieceState.boardPieces().get(myCoord);

        if (myPiece == null) {
            return;
        }

        if (!combatPolicy.canCommand(myPiece, targetPiece)) {
            attacker.sendMessage(ERROR_FRIENDLY_FIRE_PROHIBITED);
            return;
        }

        final Participant participant = context.participants().get(attacker.getUniqueId());

        if (targetCoord.equals(currentCommandTarget)) {
            participant.commanderTarget(null);
            attacker.sendMessage(Component.text()
                    .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                    .append(Component.text(" 지휘를 해제했습니다.", NamedTextColor.YELLOW))
                    .build());
            attacker.playSound(attacker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, SOUND_VOLUME_COMMAND, SOUND_PITCH_COMMAND_OFF);

            return;
        }

        participant.commanderTarget(targetCoord);
        attacker.sendMessage(Component.text()
                .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text("을(를) 지휘 대상으로 선택했습니다!", NamedTextColor.GOLD))
                .build());
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SOUND_VOLUME_COMMAND, SOUND_PITCH_COMMAND_ON);
    }

    private boolean performAttack(
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
        context.participants().get(attacker.getUniqueId()).statistics().addDamageDealt(damage);

        if (victim instanceof Player playerTarget) {
            final Participant victimParticipant = context.participants().get(playerTarget.getUniqueId());
            if (victimParticipant != null) {
                victimParticipant.statistics().addDamageTaken(damage);
            }
        }

        victim.getWorld().spawnParticle(
                Particle.CRIT,
                victim.getLocation().add(0, 1, 0),
                PARTICLE_COUNT_ATTACK, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED_ATTACK
        );
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, SOUND_VOLUME_ATTACK, SOUND_PITCH_ATTACK);
        targetPiece.currentHealth(victim.getHealth());

        if (victim.getHealth() <= 0) {
            handleKill(attacker, victim, targetCoord, attackCoord, targetPiece);
        }

        context.participants().get(attacker.getUniqueId()).commanderTarget(null);
        return true;
    }

    private void handleKill(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate targetCoord,
            final Coordinate attackCoord,
            final Piece targetPiece
    ) {
        context.participants().get(attacker.getUniqueId()).statistics().addKill();

        attacker.sendMessage(Component.text()
                .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text("을(를) 처치했습니다!", NamedTextColor.AQUA))
                .build());
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SOUND_VOLUME_KILL, SOUND_PITCH_KILL);
        victim.getWorld().spawnParticle(
                Particle.EXPLOSION,
                victim.getLocation().add(0, 1, 0),
                1, 0, 0, 0, 0
        );

        pieceManager.capturePiece(boardManager.currentBoard(), attackCoord, targetCoord);

        if (!(victim instanceof Player)) {
            victim.remove();
        }
    }
}
