package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GameResetEvent;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ScoreboardManager implements Listener {
    private static final Component TITLE = Component.text(
            "ChessWar",
            NamedTextColor.GOLD,
            TextDecoration.BOLD
    );

    private static final Component PREFIX_PHASE = Component.text("상태: ", NamedTextColor.YELLOW);

    private static final Component PREFIX_TURN = Component.text("현재 턴: ", NamedTextColor.YELLOW);

    private static final Component PREFIX_TIME = Component.text("남은 시간: ", NamedTextColor.YELLOW);

    private static final Component SUFFIX_SECONDS = Component.text("초", NamedTextColor.RED);

    private static final Component FOOTER = Component.text("tecte.dev", NamedTextColor.GRAY);

    private static final String NAME_OFFLINE = "오프라인";

    private static final String NAME_NONE = "없음";

    private static final long TICKS_PER_SECOND = 20L;

    private final ChessWar plugin;

    private final Map<UUID, FastBoard> boards = new HashMap<>();

    private BukkitTask heartbeatTask;

    public ScoreboardManager(final ChessWar plugin) {
        this.plugin = plugin;

        startHeartbeat();
    }

    public void startHeartbeat() {
        if (heartbeatTask != null) {
            return;
        }

        heartbeatTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    update();
                } catch (final Exception e) {
                    plugin.getLogger().severe("Scoreboard heartbeat error: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 0L, TICKS_PER_SECOND);
    }

    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();

            heartbeatTask = null;
        }
    }

    public void update() {
        final Map<UUID, ?> participants = plugin.gameManager().participants();

        for (final UUID playerId : participants.keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player == null) {
                continue;
            }

            final FastBoard board = boards.computeIfAbsent(playerId, uuid -> new FastBoard(player));

            applyBoard(board);
        }

        cleanupStaleBoards(participants);
    }

    public void remove(final Player player) {
        final FastBoard board = boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }

    public void reset() {
        for (final FastBoard board : boards.values()) {
            board.delete();
        }

        boards.clear();
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler
    public void onGameReset(final GameResetEvent event) {
        reset();
    }

    private void cleanupStaleBoards(final Map<UUID, ?> activeParticipants) {
        final Iterator<Map.Entry<UUID, FastBoard>> iterator = boards.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<UUID, FastBoard> entry = iterator.next();

            if (!activeParticipants.containsKey(entry.getKey())) {
                entry.getValue().delete();

                iterator.remove();
            }
        }
    }

    private void applyBoard(final FastBoard board) {
        final Optional<UUID> currentUuid = plugin.gameManager().currentTurnPlayer();

        final String turnPlayerName;

        if (currentUuid.isPresent()) {
            final Player turnPlayer = Bukkit.getPlayer(currentUuid.get());

            turnPlayerName = (turnPlayer != null) ? turnPlayer.getName() : NAME_OFFLINE;
        } else {
            turnPlayerName = NAME_NONE;
        }

        board.updateTitle(TITLE);

        board.updateLines(
                Component.empty(),
                Component.text()
                        .append(PREFIX_PHASE)
                        .append(Component.text(plugin.gameManager().phase().displayName(), NamedTextColor.WHITE))
                        .build(),
                Component.text()
                        .append(PREFIX_TURN)
                        .append(Component.text(turnPlayerName, NamedTextColor.GOLD))
                        .build(),
                Component.text()
                        .append(PREFIX_TIME)
                        .append(Component.text(plugin.timerManager().remainingSeconds()))
                        .append(SUFFIX_SECONDS)
                        .build(),
                Component.empty(),
                FOOTER
        );
    }
}
