package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.piece.ability.PieceAbility;
import dev.tecte.chesswar.team.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Piece {
    @Nullable
    private UUID id;
    private final boolean isPlayer;
    @NotNull
    private final Team team;
    @NotNull
    private final PieceType type;
    @NotNull
    private final List<PieceAbility> abilities = new ArrayList<>();
    @NotNull
    private final StatBuff personalBuff = StatBuff.create();
    @NotNull
    private final List<String> statusEffects = new ArrayList<>();

    private double currentHealth;
    private boolean leapActive;
    @Nullable
    private Coordinate commanderTarget;

    @NotNull
    public static Piece of(@NotNull final Team team, @NotNull final PieceType type) {
        return new Piece(null, false, team, type, type.baseHealth(), false, null);
    }

    @NotNull
    public static Piece of(@NotNull final UUID ownerId, @NotNull final Team team, @NotNull final PieceType type) {
        return new Piece(ownerId, true, team, type, type.baseHealth(), false, null);
    }

    public void addAbility(@NotNull final PieceAbility ability) {
        abilities.add(ability);
    }

    @Nullable
    public LivingEntity getLivingEntity() {
        if (id == null) {
            return null;
        }
        if (isPlayer) {
            return Bukkit.getPlayer(id);
        }
        final Entity entity = Bukkit.getEntity(id);
        return entity instanceof final LivingEntity living ? living : null;
    }

    @Nullable
    public UUID ownerId() {
        return isPlayer ? id : null;
    }

    @Nullable
    public Player asPlayer() {
        if (id == null || !isPlayer) {
            return null;
        }
        return Bukkit.getPlayer(id);
    }
}
