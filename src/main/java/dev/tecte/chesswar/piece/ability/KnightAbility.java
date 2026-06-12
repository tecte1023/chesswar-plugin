package dev.tecte.chesswar.piece.ability;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class KnightAbility implements PieceAbility {
    private final PieceState pieceState;
    private final MoveValidator moveValidator = new MoveValidator();
    private final GameAnnouncer announcer;

    public KnightAbility(final PieceState pieceState, final GameAnnouncer announcer) {
        this.pieceState = pieceState;
        this.announcer = announcer;
    }

    @Override
    public double onAttack(
            final Player attacker,
            final LivingEntity victim,
            final Coordinate attackerCoord,
            final Coordinate victimCoord,
            final Piece attackingPiece,
            final Piece victimPiece,
            final double damage
    ) {
        if (!moveValidator.canMove(pieceState, victimCoord, attackerCoord, false)) {
            announcer.announceKnightPreemptiveStrike(attacker);
            return damage + 15.0;
        }

        return damage;
    }
}
