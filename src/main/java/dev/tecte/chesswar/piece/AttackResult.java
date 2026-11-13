package dev.tecte.chesswar.piece;

public sealed interface AttackResult {
    record Success(int damage) implements AttackResult {
    }

    enum Failure implements AttackResult {
        FRIENDLY_FIRE,
        INVALID_PATTERN,
        PATH_BLOCKED
    }
}
