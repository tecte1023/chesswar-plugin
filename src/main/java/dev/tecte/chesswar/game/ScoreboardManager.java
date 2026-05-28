package dev.tecte.chesswar.game;

import dev.tecte.chesswar.ChessWar;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class ScoreboardManager {
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    private final ChessWar plugin;
    private final GameManager gameManager;
    private final TimerManager timerManager;

    private BukkitTask heartbeatTask;

    public void startHeartbeat() {
        if (heartbeatTask != null) {
            return;
        }

        heartbeatTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAll();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FastBoard board = boards.computeIfAbsent(player.getUniqueId(), uuid -> new FastBoard(player));

            applyBoard(board);
        }
    }

    public void removeBoard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }

    private void applyBoard(FastBoard board) {
        String turnPlayerName = "없음";
        Optional<UUID> currentUuid = gameManager.currentTurnPlayer();

        if (currentUuid.isPresent()) {
            Player p = Bukkit.getPlayer(currentUuid.get());
            turnPlayerName = (p != null) ? p.getName() : "오프라인";
        }

        board.updateTitle(Component.text("ChessWar", NamedTextColor.GOLD, TextDecoration.BOLD));
        board.updateLines(
                Component.empty(),
                Component.text()
                        .append(Component.text("상태: ", NamedTextColor.YELLOW))
                        .append(Component.text(gameManager.phase().displayName(), NamedTextColor.WHITE))
                        .build(),
                Component.text()
                        .append(Component.text("현재 턴: ", NamedTextColor.YELLOW))
                        .append(Component.text(turnPlayerName, NamedTextColor.GOLD))
                        .build(),
                Component.text()
                        .append(Component.text("남은 시간: ", NamedTextColor.YELLOW))
                        .append(Component.text(timerManager.remainingSeconds() + "초", NamedTextColor.RED))
                        .build(),
                Component.empty(),
                Component.text("tecte.dev", NamedTextColor.GRAY)
        );
    }
}
