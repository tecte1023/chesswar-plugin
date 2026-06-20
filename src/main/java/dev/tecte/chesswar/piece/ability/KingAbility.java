package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.CombatPolicy;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KingAbility implements PieceAbility {
    private final PieceState pieceState;
    private final CombatPolicy combatPolicy;
    private final GameAnnouncer announcer;

    public KingAbility(final PieceState pieceState, final CombatPolicy combatPolicy, final GameAnnouncer announcer) {
        this.pieceState = pieceState;
        this.combatPolicy = combatPolicy;
        this.announcer = announcer;
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

    @Override
    public boolean canInteract(final Player player, final Piece myPiece, final Piece targetPiece, final LivingEntity targetEntity) {
        return combatPolicy.canCommand(myPiece, targetPiece);
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

        announcer.announceKingCommanderSelect(player, targetPiece);
    }

    private void deselect(
            final Player player,
            final Participant participant,
            final Coordinate targetCoord,
            final Piece targetPiece
    ) {
        applyGlowing(targetCoord, false);
        participant.commanderTarget(null);

        announcer.announceKingCommanderDeselect(player, targetPiece);
    }

    private void applyGlowing(final Coordinate coord, final boolean enabled) {
        if (coord == null) {
            return;
        }

        final Piece piece = pieceState.piece(coord);
        final LivingEntity entity = piece != null ? (piece.isPlayer() ? Bukkit.getPlayer(piece.id()) : (Bukkit.getEntity(piece.id()) instanceof LivingEntity e ? e : null)) : null;
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
