package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.team.Team;
import org.jetbrains.annotations.NotNull;

import dev.tecte.chesswar.economy.GoldComponent;

import java.util.UUID;

public record Piece(
        @NotNull UUID id,
        @NotNull Team team,
        @NotNull PieceType type,
        boolean isPlayer,
        @NotNull StatComponent stat,
        @NotNull ActionMaskComponent actionMask,
        @NotNull EffectComponent effect,
        @NotNull AbilityComponent ability,
        @NotNull GoldComponent gold
) {
}
