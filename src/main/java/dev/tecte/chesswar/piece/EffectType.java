package dev.tecte.chesswar.piece;

public enum EffectType {
    LEAP;

    public long getMask() {
        return 1L << ordinal();
    }
}
