package dev.tecte.chesswar.listener;

import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class ChessEntityListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        syncBoardState(entity);
    }

    private void syncBoardState(Entity entity) {
        NamespacedKey xKey = new NamespacedKey(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), "barracks_piece_x");
        NamespacedKey yKey = new NamespacedKey(JavaPlugin.getPlugin(dev.tecte.chesswar.ChessWar.class), "barracks_piece_y");

        Integer x = entity.getPersistentDataContainer().get(xKey, PersistentDataType.INTEGER);
        Integer y = entity.getPersistentDataContainer().get(yKey, PersistentDataType.INTEGER);

        if (x != null && y != null) {
            Coordinate coord = Coordinate.of(x, y);
            gameManager.removePiece(coord);
        }
    }
}
