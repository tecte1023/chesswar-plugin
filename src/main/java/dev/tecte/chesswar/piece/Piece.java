package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.piece.ability.PieceAbility;
import dev.tecte.chesswar.team.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Piece {
    @NotNull
    private final UUID id;

    private final boolean isPlayer;

    @NotNull
    private final Team team;

    @NotNull
    private final PieceType type;

    @NotNull
    private final List<PieceAbility> abilities;

    @NotNull
    private final StatBuff personalBuff;

    @NotNull
    private final List<PieceEffect> statusEffects;

    private double currentHealth;

    @NotNull
    public static Piece of(@NotNull final UUID id, @NotNull final Team team, @NotNull final PieceType type) {
        return new Piece(
                id,
                false,
                team,
                type,
                new ArrayList<>(),
                StatBuff.create(),
                new ArrayList<>(),
                type.baseHealth()
        );
    }

    @NotNull
    public static Piece ofPlayer(@NotNull final UUID ownerId, @NotNull final Team team, @NotNull final PieceType type) {
        return new Piece(
                ownerId,
                true,
                team, type,
                new ArrayList<>(),
                StatBuff.create(),
                new ArrayList<>(),
                type.baseHealth()
        );
    }

    public boolean hasEffect(@NotNull final String name) {
        for (final PieceEffect effect : statusEffects) {
            if (effect.name().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public void addEffect(@NotNull final PieceEffect effect) {
        removeEffect(effect.name());
        statusEffects.add(effect);
    }

    public void removeEffect(@NotNull final String name) {
        for (int i = 0; i < statusEffects.size(); i++) {
            if (statusEffects.get(i).name().equals(name)) {
                statusEffects.remove(i);
                return;
            }
        }
    }

    public void tickEffects() {
        for (int i = statusEffects.size() - 1; i >= 0; i--) {
            final PieceEffect effect = statusEffects.get(i);

            effect.decrementDuration();

            if (effect.isExpired()) {
                statusEffects.remove(i);
            }
        }
    }
}
