package dev.tecte.chesswar.piece.bukkit;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.game.GameLifecycle;
import dev.tecte.chesswar.piece.PieceLifecycle;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@RequiredArgsConstructor
public final class PlayerSessionListener implements Listener {
    private final PieceLifecycle pieceLifecycle;
    private final GameLifecycle gameLifecycle;
    private final PieceView pieceView;
    private final TurnBorderView turnBorderView;

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        turnBorderView.onPlayerQuit(player);
        pieceView.onPlayerQuit(player);
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Entity boundPiece = pieceLifecycle.pieceOf(playerId);

        pieceView.onPlayerJoin(player, boundPiece);

        if (gameLifecycle.isBattle()) {
            turnBorderView.onPlayerJoin(player, boundPiece, gameLifecycle.isTurnOwner(playerId));
        }
    }
}
