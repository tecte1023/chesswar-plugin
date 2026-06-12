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

    public BishopAbility(final PieceState pieceState, final GameAnnouncer announcer) {
        this.pieceState = pieceState;
        this.announcer = announcer;
    }

    @Override
    public boolean onAttackTeammate(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate attackerCoord,
            final Coordinate victimCoord,
            final Piece attackingPiece,
            final Piece victimPiece,
            final Participant participant
    ) {
        if (!moveValidator.canReach(pieceState, attackerCoord, victimCoord, false)) {
            announcer.announceCombatError(attacker, Component.text("그곳에 있는 아군은 회복시킬 수 없는 범위에 있습니다!", NamedTextColor.RED));
            return true;
        }

        performHeal(attacker, victim, attackingPiece, victimPiece);
        participant.statistics().addHealingDone(attackingPiece.type().baseDamage());
        return true;
    }

    public double performHeal(final Player healer, final LivingEntity victim, final Piece healingPiece, final Piece targetPiece) {
        final double baseHeal = healingPiece.type().baseDamage();
        final StatBuff buff = pieceState.getBuff(healingPiece.team(), healingPiece.type());
        final double healAmount = baseHeal + buff.damage();

        final AttributeInstance maxHealth = victim.getAttribute(Attribute.MAX_HEALTH);
        final double currentHealth = victim.getHealth();
        final double maxHp = maxHealth != null ? maxHealth.getValue() : 20.0;

        final double newHealth = Math.min(maxHp, currentHealth + healAmount);
        final double actualHeal = newHealth - currentHealth;

        victim.setHealth(newHealth);

        announcer.announceHeal(healer, victim, targetPiece);

        return actualHeal;
    }
}
