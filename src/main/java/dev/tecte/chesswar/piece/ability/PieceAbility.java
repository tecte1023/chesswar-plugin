package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.piece.Piece;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface PieceAbility {
    default void onTurnStart(final Player player, final Piece piece, final Participant participant) {
    }

    default double onAttack(final Player attacker, final LivingEntity victim, final Coordinate attackerCoord, final Coordinate victimCoord, final Piece attackingPiece, final Piece victimPiece, final double damage) {
        return damage;
    }

    default double onDefend(final LivingEntity attacker, final Player victim, final Piece attackingPiece, final Piece victimPiece, final double damage) {
        return damage;
    }

    default boolean onMove(final Player player, final Piece piece, final Coordinate from, final Coordinate to) {
        return true;
    }

    default InteractionResult onAttackTeammate(final Player attacker, final LivingEntity victim, final Coordinate attackerCoord, final Coordinate victimCoord, final Piece attackingPiece, final Piece victimPiece, final Participant participant) {
        return InteractionResult.IGNORED;
    }

    default boolean onInteractSameTeam(final Player player, final Coordinate playerCoord, final Coordinate targetCoord, final Piece playerPiece, final Piece targetPiece, final Participant participant) {
        return false;
    }

    default boolean canInteract(final Player player, final Piece myPiece, final Piece targetPiece, final LivingEntity targetEntity) {
        return false;
    }
}
