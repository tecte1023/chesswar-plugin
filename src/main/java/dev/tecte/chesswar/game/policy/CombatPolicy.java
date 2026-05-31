package dev.tecte.chesswar.game.policy;

import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PieceType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CombatPolicy {
    private final GameManager gameManager;

    public Piece getRepresentativeAttacker(final Participant participant, final PieceManager pieceManager) {
        for (final Piece p : pieceManager.boardPieces().values()) {
            if (participant.playerId().equals(p.ownerId()) && p.type() == PieceType.KING) {
                return p;
            }
        }

        return Piece.of(participant.playerId(), participant.team(), PieceType.PAWN);
    }

    public boolean isWinConditionMet(final Piece killedPiece) {
        return killedPiece.type() == PieceType.KING;
    }

    public boolean canAttack(final Piece attacker, final Piece target) {
        if (attacker.team() != target.team()) {
            return true;
        }

        return canCommand(attacker, target);
    }

    public boolean canCommand(final Piece commander, final Piece target) {
        if (commander.team() != target.team()) {
            return false;
        }

        return commander.type() == PieceType.KING && !target.isPlayerPiece();
    }
}
