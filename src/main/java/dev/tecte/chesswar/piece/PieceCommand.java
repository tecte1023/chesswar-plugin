package dev.tecte.chesswar.piece;

import co.aikar.commands.annotation.Subcommand;
import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.ChessWarCommand;
import dev.tecte.chesswar.core.SimulationEventBus;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.UUID;

@Subcommand("piece")
@RequiredArgsConstructor
public final class PieceCommand extends ChessWarCommand {
    private final SimulationEventBus eventBus;

    @Subcommand("spawn")
    public void onSpawn(final Player player) {
        Entity piece = new Entity(UUID.randomUUID());
        GridComponent pos = new GridComponent(new GridPoint(0, 0));
        piece.attach(GridComponent.class, pos);

        eventBus.publish(new PieceSpawnEvent(piece));
        reply(player, Component.text("더미 기물 스폰이 완료되었습니다.", NamedTextColor.GREEN));
    }
}
