package dev.tecte.chesswar.piece;

import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * MythicMobs 관련 생명주기 이벤트를 감지하여 PieceManager에게 로직을 위임함.
 */
@RequiredArgsConstructor
public class MythicPieceListener implements Listener {
    private final PieceManager pieceManager;

    @EventHandler
    public void onMythicReload(final MythicReloadedEvent event) {
        final List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) {
            return;
        }

        // 리로드 시점에 캐시가 초기화되므로 즉시 자가 치유(Warm-up) 실행
        pieceManager.warmup(worlds.get(0));
    }
}
