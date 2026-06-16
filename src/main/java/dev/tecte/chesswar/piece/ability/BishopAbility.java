package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.piece.StatBuff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BishopAbility implements PieceAbility {
    private final PieceState pieceState;
    private final MoveValidator moveValidator = new MoveValidator();
    private final GameAnnouncer announcer;
    private final double efficiency;

    public BishopAbility(final PieceState pieceState, final GameAnnouncer announcer, final double efficiency) {
        this.pieceState = pieceState;
        this.announcer = announcer;
        this.efficiency = efficiency;
    }

    @Override
    public InteractionResult onAttackTeammate(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate attackerCoord,
            final Coordinate victimCoord,
            final Piece attackingPiece,
            final Piece victimPiece,
            final Participant participant
    ) {
        if (!attackerCoord.equals(victimCoord) && !moveValidator.canReach(pieceState, attackerCoord, victimCoord, false)) {
            announcer.announceCombatError(attacker, Component.text("그곳에 있는 아군은 회복시킬 수 없는 범위에 있습니다!", NamedTextColor.RED));
            return InteractionResult.FAIL_HANDLED;
        }

        final org.bukkit.attribute.AttributeInstance maxHealthAttr = victim.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        final double maxHp = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;

        if (victim.getHealth() >= maxHp) {
            announcer.announceCombatError(attacker, Component.text("대상의 체력이 이미 최대입니다!", NamedTextColor.RED));
            return InteractionResult.FAIL_HANDLED;
        }

        performHeal(attacker, victim, attackingPiece, victimPiece, participant);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canInteract(final Player player, final Piece myPiece, final Piece targetPiece, final LivingEntity targetEntity) {
        if (targetEntity == null) {
            return false;
        }
        final org.bukkit.attribute.AttributeInstance maxHealthAttr = targetEntity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        final double maxHp = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
        return targetEntity.getHealth() < maxHp;
    }

    public double performHeal(final Player healer, final LivingEntity victim, final Piece healingPiece, final Piece targetPiece, final Participant participant) {
        final double baseHeal = healingPiece.type().baseDamage() * efficiency;
        final StatBuff buff = pieceState.getBuff(healingPiece.team(), healingPiece.type());
        final double healAmount = Math.round(baseHeal + (buff.damage() * efficiency));

        final org.bukkit.attribute.AttributeInstance maxHealth = victim.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        final double currentHealth = victim.getHealth();
        final double maxHp = maxHealth != null ? maxHealth.getValue() : 20.0;

        final double newHealth = Math.min(maxHp, Math.round(currentHealth + healAmount));
        final double actualHeal = newHealth - currentHealth;

        victim.setHealth(newHealth);
        targetPiece.currentHealth(newHealth);

        announcer.announceHeal(healer, victim, targetPiece, actualHeal);

        if (participant != null) {
            participant.statistics().addHealingDone(actualHeal);
        }

        return actualHeal;
    }
}
