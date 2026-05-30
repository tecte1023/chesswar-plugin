package dev.tecte.chesswar.game.policy;

import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CombatPolicy {
    private final GameManager gameManager;

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
