package dev.tecte.chesswar.game.event;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class KingDeathEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Team winnerTeam;

    public KingDeathEvent(final Team winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
