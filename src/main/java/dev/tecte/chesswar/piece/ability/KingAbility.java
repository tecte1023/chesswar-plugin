package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.CombatPolicy;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KingAbility implements PieceAbility {
    private final PieceState pieceState;
    private final CombatPolicy combatPolicy;

    public KingAbility(final PieceState pieceState, final CombatPolicy combatPolicy) {
        this.pieceState = pieceState;
        this.combatPolicy = combatPolicy;
    }

    @Override
    public boolean onInteractSameTeam(
            final Player player,
            final Coordinate playerCoord,
            final Coordinate targetCoord,
            final Piece playerPiece,
            final Piece targetPiece,
            final Participant participant
    ) {
        if (!combatPolicy.canCommand(playerPiece, targetPiece)) {
            return false;
        }

        final Coordinate currentTarget = participant.commanderTarget();

        if (targetCoord.equals(currentTarget)) {
            deselect(player, participant, targetCoord, targetPiece);
            return true;
        }

        select(player, participant, targetCoord, targetPiece, currentTarget);
        return true;
    }

    private void select(
            final Player player,
            final Participant participant,
            final Coordinate targetCoord,
            final Piece targetPiece,
            final Coordinate previousTarget
    ) {
        applyGlowing(previousTarget, false);
        applyGlowing(targetCoord, true);
        participant.commanderTarget(targetCoord);

        player.sendMessage(Component.text()
                .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text("을(를) 지휘 대상으로 선택했습니다!", NamedTextColor.GOLD))
                .build());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    private void deselect(
            final Player player,
            final Participant participant,
            final Coordinate targetCoord,
            final Piece targetPiece
    ) {
        applyGlowing(targetCoord, false);
        participant.commanderTarget(null);

        player.sendMessage(Component.text()
                .append(Component.text(targetPiece.type().displayName(), NamedTextColor.GOLD))
                .append(Component.text(" 지휘를 해제했습니다.", NamedTextColor.YELLOW))
                .build());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 0.5f);
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
            entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        } else {
            entity.removePotionEffect(PotionEffectType.GLOWING);
        }
    }
}
