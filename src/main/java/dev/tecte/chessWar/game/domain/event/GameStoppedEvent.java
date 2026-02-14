package dev.tecte.chessWar.game.domain.event;

import dev.tecte.chessWar.common.event.DomainEvent;
import dev.tecte.chessWar.piece.domain.model.UnitPiece;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * 게임이 중단된 사건을 기록합니다.
 */
@Getter
@Accessors(fluent = true)
public class GameStoppedEvent extends DomainEvent {
    private final List<UnitPiece> unitPieces;
    private final CommandSender stopper;

    public GameStoppedEvent(@NonNull List<UnitPiece> unitPieces, @NonNull CommandSender stopper) {
        this.unitPieces = List.copyOf(unitPieces);
        this.stopper = stopper;
    }
}
