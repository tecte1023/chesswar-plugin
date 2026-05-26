package dev.tecte.chesswar.event;

import dev.tecte.chesswar.board.Coordinate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 킹(지휘자)이 지휘 대상을 새롭게 선택, 변경, 또는 해제했을 때 발생하는 이벤트입니다.
 */
@Getter
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ChessCommandTargetSelectedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Coordinate targetCoordinate;

    public Optional<Coordinate> getTargetCoordinate() {
        return Optional.ofNullable(targetCoordinate);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
