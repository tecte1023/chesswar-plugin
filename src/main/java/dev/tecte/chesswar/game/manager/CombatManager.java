package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.economy.EconomyManager;
import dev.tecte.chesswar.economy.GoldSource;
import dev.tecte.chesswar.game.CombatPolicy;
import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.event.KingDeathEvent;
import dev.tecte.chesswar.game.event.PieceSpawnEvent;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.piece.StatBuff;
import dev.tecte.chesswar.piece.ability.PieceAbility;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class CombatManager implements Listener {
    private static final double DEFAULT_HEALTH = 20.0;
    private static final double DEFAULT_ATTACK_DAMAGE = 1.0;

    private static final float SOUND_VOLUME_ATTACK = 1.0f;
    private static final float SOUND_PITCH_ATTACK = 1.0f;

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
    private final EconomyManager economyManager;
    private final GameAnnouncer announcer;

    @Getter
    private boolean processingAttack = false;

    @EventHandler
    public void onPieceSpawn(final PieceSpawnEvent event) {
        applyStats(event.getEntity(), event.getType(), event.getTeam());
    }

    public void applyStats(final LivingEntity entity, final PieceType type, final Team team) {
        final AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        final AttributeInstance attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        final StatBuff teamBuff = pieceState.getBuff(team, type);

        double personalHealth = 0.0;
        double personalDamage = 0.0;

        final Coordinate coord = pieceManager.findCoordinate(entity).orElse(null);
        if (coord != null) {
            final Piece piece = pieceState.boardPieces().get(coord);
            if (piece != null) {
                personalHealth = piece.personalBuff().health();
                personalDamage = piece.personalBuff().damage();
            }
        }

        if (maxHealth != null) {
            final double finalHealth = type.baseHealth() + teamBuff.health() + personalHealth;
            maxHealth.setBaseValue(finalHealth);
            entity.setHealth(finalHealth);
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(type.baseDamage() + teamBuff.damage() + personalDamage);
        }
    }

    public void upgradePieceClass(final Team team, final PieceType type, final double healthInc, final double damageInc) {
        final StatBuff buff = pieceState.getBuff(team, type);
        buff.addHealth(healthInc);
        buff.addDamage(damageInc);

        for (final Map.Entry<Coordinate, Piece> entry : pieceState.boardPieces().entrySet()) {
            final Piece piece = entry.getValue();
            if (piece.team() != team || piece.type() != type) {
                continue;
            }

            final LivingEntity mob = pieceState.pieceEntities().get(entry.getKey());
            if (mob != null && mob.isValid()) {
                applyStats(mob, type, team);
            }

            if (piece.isPlayerPiece()) {
                final Player player = Bukkit.getPlayer(piece.ownerId());
                if (player != null && player.isOnline()) {
                    applyStats(player, type, team);
                }
            }
        }
    }

    public boolean upgradeIndividualPiece(final Player player, final double healthInc, final double damageInc) {
        final Coordinate coord = pieceManager.findCoordinate(player).orElse(null);
        if (coord == null) {
            announcer.announceCombatError(player, Component.text("§c[DEBUG] 기물의 위치를 찾을 수 없어 강화에 실패했습니다."));
            return false;
        }

        final Piece piece = pieceState.boardPieces().get(coord);
        if (piece == null) {
            announcer.announceCombatError(player, Component.text("§c[DEBUG] 해당 위치에 등록된 기물이 없습니다."));
            return false;
        }

        piece.personalBuff().addHealth(healthInc);
        piece.personalBuff().addDamage(damageInc);

        applyStats(player, piece.type(), piece.team());
        return true;
    }

    public void repairRooks(final Team team) {
        for (final Map.Entry<Coordinate, Piece> entry : pieceState.boardPieces().entrySet()) {
            final Piece piece = entry.getValue();
            if (piece.team() == team && piece.type() == PieceType.ROOK) {
                final LivingEntity entity = pieceState.pieceEntities().get(entry.getKey());
                if (entity != null && entity.isValid()) {
                    final AttributeInstance maxAbsorption = entity.getAttribute(Attribute.MAX_ABSORPTION);
                    if (maxAbsorption != null) {
                        maxAbsorption.setBaseValue(40.0);
                    }
                    entity.setAbsorptionAmount(40.0);
                    entity.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, entity.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }

    public void resetStats(final LivingEntity entity) {
        final AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        final AttributeInstance attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        final AttributeInstance maxAbsorption = entity.getAttribute(Attribute.MAX_ABSORPTION);

        if (maxHealth != null) {
            maxHealth.setBaseValue(DEFAULT_HEALTH);
            entity.setHealth(Math.min(entity.getHealth(), DEFAULT_HEALTH));
        }

        if (attackDamage != null) {
            attackDamage.setBaseValue(DEFAULT_ATTACK_DAMAGE);
        }

        if (maxAbsorption != null) {
            maxAbsorption.setBaseValue(0.0);
        }
        entity.setAbsorptionAmount(0.0);
    }

    public void restoreStats(final LivingEntity entity, final Double health, final Double damage) {
        final AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        final AttributeInstance attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        final AttributeInstance maxAbsorption = entity.getAttribute(Attribute.MAX_ABSORPTION);

        if (maxHealth != null && health != null) {
            maxHealth.setBaseValue(health);
            entity.setHealth(Math.min(entity.getHealth(), health));
        }

        if (attackDamage != null && damage != null) {
            attackDamage.setBaseValue(damage);
        }

        if (maxAbsorption != null) {
            maxAbsorption.setBaseValue(0.0);
        }
        entity.setAbsorptionAmount(0.0);
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
        if (processingAttack) {
            return false;
        }

        if (!isPlayerTurn(attacker)) {
            return false;
        }

        if (!boardManager.hasBoard()) {
            return false;
        }

        final Coordinate attackingCoordinate = pieceManager.findCoordinate(attacker).orElse(null);

        if (attackingCoordinate == null) {
            return false;
        }

        final Participant participant = context.participants().get(attacker.getUniqueId());
        final Coordinate commandTarget = participant.commanderTarget();
        final Coordinate finalAttackingCoordinate = (commandTarget != null) ? commandTarget : attackingCoordinate;
        final Coordinate targetCoordinate = pieceManager.findCoordinate(victim).orElse(null);

        if (targetCoordinate == null) {
            return false;
        }

        final Piece attackingPiece = pieceState.boardPieces().get(finalAttackingCoordinate);
        final Piece targetPiece = pieceState.boardPieces().get(targetCoordinate);

        if (attackingPiece == null || targetPiece == null) {
            return false;
        }

        if (targetPiece.team() == attackingPiece.team()) {
            processingAttack = true;
            try {
                for (final dev.tecte.chesswar.piece.ability.PieceAbility ability : attackingPiece.abilities()) {
                    if (ability.onAttackTeammate(attacker, victim, finalAttackingCoordinate, targetCoordinate, attackingPiece, targetPiece, participant)) {
                        if (participant.commanderTarget() != null) {
                            applyGlowing(participant.commanderTarget(), false);
                            participant.commanderTarget(null);
                        }
                        return true;
                    }
                }
            } finally {
                processingAttack = false;
            }

            handleCommanderOrFriendlyFire(
                    attacker,
                    attackingCoordinate,
                    targetCoordinate,
                    attackingPiece,
                    targetPiece,
                    participant
            );
            return false;
        }

        if (!moveValidator.canMove(pieceState, finalAttackingCoordinate, targetCoordinate, participant.leapActive())) {
            announcer.announceCombatError(attacker, ERROR_ATTACK_OUT_OF_RANGE);
            return false;
        }

        return performAttack(attacker, victim, targetCoordinate, finalAttackingCoordinate, attackingPiece, targetPiece);
    }

    public void onTurnStart(final Player player) {
        Coordinate coord = pieceManager.findCoordinate(player).orElse(null);
        final Participant participant = context.participants().get(player.getUniqueId());

        if (coord == null && participant != null && participant.initialCoordinate() != null) {
            coord = participant.initialCoordinate();
            pieceManager.registerPlayerPiece(player, participant.team(), participant.selectedType(), coord);
        }

        if (coord == null) {
            return;
        }

        final Piece piece = pieceState.boardPieces().get(coord);
        if (piece == null || participant == null) {
            return;
        }

        for (final PieceAbility ability : piece.abilities()) {
            ability.onTurnStart(player, piece, participant);
        }
    }

    public void clearCommanderVisuals(final Player player) {
        final Participant participant = context.participants().get(player.getUniqueId());
        if (participant != null && participant.commanderTarget() != null) {
            applyGlowing(participant.commanderTarget(), false);
        }
    }

    private void applyGlowing(final Coordinate coord, final boolean enabled) {
        if (coord == null) {
            return;
        }
        final LivingEntity entity = pieceState.pieceEntities().get(coord);
        if (entity == null) {
            return;
        }

        if (enabled) {
            entity.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        } else {
            entity.removePotionEffect(org.bukkit.potion.PotionEffectType.GLOWING);
        }
    }

    public boolean handleMove(final Player player) {
        if (!isPlayerTurn(player)) {
            return false;
        }

        if (!boardManager.hasBoard()) {
            announcer.announceCombatError(player, ERROR_NO_BOARD);
            return false;
        }

        final Coordinate to = boardManager.currentBoard().toCoordinate(player.getLocation());

        if (!to.isValid()) {
            announcer.announceCombatError(player, ERROR_OUT_OF_BOARD);
            return false;
        }

        final Coordinate from = pieceManager.findCoordinate(player).orElse(null);

        if (from == null) {
            announcer.announceCombatError(player, ERROR_NO_PIECE_ON_BOARD);
            return false;
        }

        final Participant participant = context.participants().get(player.getUniqueId());
        final Coordinate commandTarget = participant.commanderTarget();
        final Coordinate finalFrom = (commandTarget != null) ? commandTarget : from;

        if (finalFrom.equals(to)) {
            announcer.announceCombatError(player, ERROR_SAME_POSITION);
            return false;
        }

        if (!moveValidator.canMove(pieceState, finalFrom, to, participant.leapActive())) {
            announcer.announceCombatError(player, ERROR_INVALID_MOVE_RANGE);
            return false;
        }

        final Piece movingPiece = pieceState.boardPieces().get(finalFrom);

        if (movingPiece == null) {
            return false;
        }

        if (pieceState.boardPieces().containsKey(to)) {
            announcer.announceCombatError(player, ERROR_OCCUPIED_BY_PIECE);
            return false;
        }

        if (commandTarget != null) {
            applyGlowing(commandTarget, false);
            participant.commanderTarget(null);
        }

        for (final PieceAbility ability : movingPiece.abilities()) {
            if (!ability.onMove(player, movingPiece, finalFrom, to)) {
                return false;
            }
        }

        pieceManager.movePiece(boardManager.currentBoard(), finalFrom, to);

        announcer.announceMoveSuccess(player, movingPiece, to, commandTarget != null);

        return true;
    }

    private boolean isPlayerTurn(final Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }

        final UUID currentTurnPlayerId = context.currentTurnPlayerId();

        if (currentTurnPlayerId == null || !currentTurnPlayerId.equals(player.getUniqueId())) {
            announcer.announceCombatError(player, ERROR_NOT_YOUR_TURN);
            return false;
        }

        return true;
    }

    private void handleCommanderOrFriendlyFire(
            final Player attacker,
            final Coordinate myCoord,
            final Coordinate targetCoord,
            final Piece attackingPiece,
            final Piece targetPiece,
            final Participant participant
    ) {
        for (final PieceAbility ability : attackingPiece.abilities()) {
            if (ability.onInteractSameTeam(attacker, myCoord, targetCoord, attackingPiece, targetPiece, participant)) {
                return;
            }
        }

        announcer.announceCombatError(attacker, ERROR_FRIENDLY_FIRE_PROHIBITED);
    }

    private boolean performAttack(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate targetCoord,
            final Coordinate attackCoord,
            final Piece attackingPiece,
            final Piece targetPiece
    ) {
        processingAttack = true;
        try {
            final Participant participant = context.participants().get(attacker.getUniqueId());
            if (participant.commanderTarget() != null) {
                applyGlowing(participant.commanderTarget(), false);
                participant.commanderTarget(null);
            }

            final double baseDamage = attackingPiece.type().baseDamage();
            final dev.tecte.chesswar.piece.StatBuff buff = pieceState.getBuff(attackingPiece.team(), attackingPiece.type());
            double damage = baseDamage + buff.damage();

            for (final PieceAbility ability : attackingPiece.abilities()) {
                damage = ability.onAttack(attacker, victim, attackCoord, targetCoord, attackingPiece, targetPiece, damage);
            }

            victim.setNoDamageTicks(0);
            victim.damage(damage, attacker);
            context.participants().get(attacker.getUniqueId()).statistics().addDamageDealt(damage);

            if (victim instanceof Player playerTarget) {
                final Participant victimParticipant = context.participants().get(playerTarget.getUniqueId());
                if (victimParticipant != null) {
                    victimParticipant.statistics().addDamageTaken(damage);
                }
            }

            announcer.announceAttack(victim);
            targetPiece.currentHealth(victim.getHealth());

            if (victim.getHealth() <= 0) {
                handleKill(attacker, victim, targetCoord, attackCoord, targetPiece);
            }

            return true;
        } finally {
            processingAttack = false;
        }
    }

    private void handleKill(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate targetCoord,
            final Coordinate attackCoord,
            final Piece targetPiece
    ) {
        final Participant participant = context.participants().get(attacker.getUniqueId());
        participant.statistics().addKill();

        // 처치 보상 지급 (200G)
        economyManager.addGold(attacker.getUniqueId(), 200, GoldSource.KILL);

        announcer.announceKill(victim);

        pieceManager.capturePiece(boardManager.currentBoard(), attackCoord, targetCoord);

        if (targetPiece.type() == PieceType.KING) {
            Bukkit.getPluginManager().callEvent(new KingDeathEvent(participant.team()));
        }

        if (!(victim instanceof Player)) {
            pieceManager.purgeEntity(victim);
            victim.remove();
        }
    }
}
