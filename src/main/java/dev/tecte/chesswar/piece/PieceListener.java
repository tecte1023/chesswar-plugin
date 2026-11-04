package dev.tecte.chesswar.piece;

import dev.tecte.chesswar.board.BoardContext;
import dev.tecte.chesswar.board.GridComponent;
import dev.tecte.chesswar.board.GridPoint;
import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.Messages;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

@RequiredArgsConstructor
public final class PieceListener implements Listener {
    private final BoardContext board;
    private final PieceLifecycle pieceLifecycle;

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!board.isSetup()) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.getWorld().getUID().equals(board.worldId())) {
            return;
        }

        Entity piece = pieceLifecycle.pieceOf(player.getUniqueId());
        if (piece == null) {
            return;
        }

        GridPoint targetGridPos = board.toGridPos(player.getLocation().toVector());
        if (!board.isInBounds(targetGridPos)) {
            Messages.reply(player, Component.text("체스판 밖으로 이동할 수 없습니다.", NamedTextColor.RED));
            return;
        }

        var gridComponent = piece.component(GridComponent.class);
        if (gridComponent.gridPos().equals(targetGridPos)) {
            Messages.reply(player, Component.text("현재 위치와 같은 칸으로 이동할 수 없습니다.", NamedTextColor.RED));
            return;
        }

        if (pieceLifecycle.hasPieceAt(targetGridPos)) {
            Messages.reply(player, Component.text("해당 칸에 이미 다른 기물이 존재합니다.", NamedTextColor.RED));
            return;
        }

        pieceLifecycle.move(piece, targetGridPos);
        String message = "(" + targetGridPos.x() + ", " + targetGridPos.y() + ") 칸으로 이동했습니다.";
        Messages.reply(player, Component.text(message, NamedTextColor.GREEN));
    }
}
