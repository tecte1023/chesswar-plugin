package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.PieceType;
import org.jetbrains.annotations.NotNull;

public record PieceInspectionResult(@NotNull PieceType type, @NotNull PieceSelectability selectability) {
}
