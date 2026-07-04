package dev.tecte.chesswar.piece;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActionPatternTable {
    @NotNull
    private final ActionPattern[] normalTable;
    @NotNull
    private final ActionPattern[] leapTable;

    @NotNull
    public static ActionPatternTable create() {
        final int size = PieceType.values().length;
        final ActionPattern[] normalArr = new ActionPattern[size];
        final ActionPattern[] leapArr = new ActionPattern[size];

        register(normalArr, leapArr, PieceType.KING, LeaperPattern.ALL, LeaperPattern.ALL);
        register(normalArr, leapArr, PieceType.QUEEN, SliderPattern.ALL, SliderPattern.ALL_LEAP);
        register(normalArr, leapArr, PieceType.ROOK, SliderPattern.STRAIGHT, SliderPattern.STRAIGHT_LEAP);
        register(normalArr, leapArr, PieceType.BISHOP, SliderPattern.DIAGONAL, SliderPattern.DIAGONAL_LEAP);
        register(normalArr, leapArr, PieceType.KNIGHT, LeaperPattern.KNIGHT_JUMP, LeaperPattern.KNIGHT_JUMP);
        register(normalArr, leapArr, PieceType.PAWN, PawnPattern.INSTANCE, PawnPattern.INSTANCE);

        return new ActionPatternTable(normalArr, leapArr);
    }

    private static void register(
            @NotNull final ActionPattern[] normalArr,
            @NotNull final ActionPattern[] leapArr,
            @NotNull final PieceType type,
            @NotNull final ActionPattern normalPattern,
            @NotNull final ActionPattern leapPattern
    ) {
        final int index = type.ordinal();

        normalArr[index] = normalPattern;
        leapArr[index] = leapPattern;
    }

    @NotNull
    public ActionPattern patternFor(@NotNull final PieceType type) {
        return normalTable[type.ordinal()];
    }

    @NotNull
    public ActionPattern patternFor(@NotNull final PieceType type, final boolean hasLeap) {
        return hasLeap ? leapTable[type.ordinal()] : normalTable[type.ordinal()];
    }
}
