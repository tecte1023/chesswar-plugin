package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.piece.StatBuff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BishopAbility implements PieceAbility {
    private final PieceState pieceState;
    private final dev.tecte.chesswar.board.MoveValidator moveValidator;

    public BishopAbility(final PieceState pieceState) {
        this.pieceState = pieceState;
        this.moveValidator = new dev.tecte.chesswar.board.MoveValidator();
    }

    @Override
    public boolean onAttackTeammate(
            final Player attacker,
            final LivingEntity victim,
            final dev.tecte.chesswar.board.Coordinate attackerCoord,
            final dev.tecte.chesswar.board.Coordinate victimCoord,
            final Piece attackingPiece,
            final Piece victimPiece,
            final dev.tecte.chesswar.game.component.Participant participant
    ) {
        if (!moveValidator.canReach(pieceState, attackerCoord, victimCoord)) {
            attacker.sendMessage(net.kyori.adventure.text.Component.text("그곳에 있는 아군은 회복시킬 수 없는 범위에 있습니다!", net.kyori.adventure.text.format.NamedTextColor.RED));
            return true; // 범위 밖이어도 이벤트는 핸들링한 것으로 간주하여 아군 공격 메시지 방지
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

        victim.getWorld().spawnParticle(
                Particle.HEART,
                victim.getLocation().add(0, 1, 0),
                10, 0.5, 0.5, 0.5, 0.1
        );
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        targetPiece.currentHealth(newHealth);

        healer.sendMessage(Component.text()
                .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text("의 체력을 회복시켰습니다!", NamedTextColor.GREEN))
                .build());

        return actualHeal;
    }
}
