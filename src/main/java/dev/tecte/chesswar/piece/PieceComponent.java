package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.Component;

public record PieceComponent(Team team, PieceType type) implements Component {
    public boolean isAlly(final PieceComponent other) {
        return other.team == team;
    }

    public boolean canMoveTo(final GridPoint from, final GridPoint to) {
        return type.canMove(team, from, to);
    }

    public boolean canAttackTo(final GridPoint from, final GridPoint to) {
        return type.canAttack(team, from, to);
    }

    public boolean canJump() {
        return type.jumps();
    }
}
