/*
package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PieceSpawnEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity entity;
    private final PieceType type;
    private final Team team;

    public PieceSpawnEvent(final LivingEntity entity, final PieceType type, final Team team) {
        this.entity = entity;
        this.type = type;
        this.team = team;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
*/
