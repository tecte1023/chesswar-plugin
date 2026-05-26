package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.piece.PieceManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class GameEntityListener implements Listener {
    private final GameManager gameManager;
    private final PieceManager pieceManager;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        syncBoardState(entity);
    }

    private void syncBoardState(Entity entity) {
        NamespacedKey xKey = new NamespacedKey(JavaPlugin.getPlugin(ChessWar.class), "barracks_piece_x");
        NamespacedKey yKey = new NamespacedKey(JavaPlugin.getPlugin(ChessWar.class), "barracks_piece_y");

        Integer x = entity.getPersistentDataContainer().get(xKey, PersistentDataType.INTEGER);
        Integer y = entity.getPersistentDataContainer().get(yKey, PersistentDataType.INTEGER);

        if (x != null && y != null) {
            Coordinate coord = Coordinate.of(x, y);
            pieceManager.removePiece(coord);
        }
    }
}
