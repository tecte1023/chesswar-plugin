/*
package dev.tecte.chesswar.game;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.piece.EffectType;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.piece.PieceType;
import org.bukkit.entity.Player;

public class CombatPolicy {
    public boolean canAttack(final Piece attacker, final Piece target) {
        if (attacker.team() != target.team()) {
            return true;
        }

        if (attacker.type() == PieceType.BISHOP) {
            return true;
        }

        return canCommand(attacker, target);
    }

    public boolean canCommand(final Piece commander, final Piece target) {
        if (commander.team() != target.team()) {
            return false;
        }

        return commander.type() == PieceType.KING && !target.isPlayer();
    }

    public Piece getPrimaryAttacker(final Participant participant, final PieceState state) {
        final Coordinate kingCoord = state.coordinate(participant.playerId());

        if (kingCoord != null) {
            final Piece king = state.piece(kingCoord);
            if (king != null && king.type() == PieceType.KING) {
                return king;
            }
        }

        return Piece.of(participant.playerId(), participant.team(), PieceType.PAWN);
    }

    public boolean isWinConditionMet(final Piece killedPiece) {
        return killedPiece.type() == PieceType.KING;
    }

    public double calculateDamage(
            final long abilityMask, 
            final long effectMask, 
            final PieceState pieceState,
            final Coordinate attackerCoord, 
            final Coordinate victimCoord,
            final Player attacker,
            final double baseDamage,
            final MoveValidator moveValidator,
            final GameAnnouncer announcer) {
        
        double finalDamage = baseDamage;

        // O(1) Fast-Fail filtering for LEAP (Placeholder for testing bitmask)
        if (((abilityMask | effectMask) & EffectType.LEAP.getMask()) != 0) {
            if (!moveValidator.canMove(pieceState, victimCoord, attackerCoord)) {
                announcer.announceKnightPreemptiveStrike(attacker);
                finalDamage += 15.0;
            }
        }

        return finalDamage;
    }
}
*/
