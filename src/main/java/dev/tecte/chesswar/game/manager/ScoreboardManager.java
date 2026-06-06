package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.team.Team;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor
public class ScoreboardManager {
    private static final Component TITLE = Component.text(
            "ChessWar",
            NamedTextColor.GOLD,
            TextDecoration.BOLD
    );

    private static final Component PREFIX_PHASE = Component.text("상태: ", NamedTextColor.YELLOW);
    private static final Component PREFIX_TURN = Component.text("현재 턴: ", NamedTextColor.YELLOW);
    private static final Component PREFIX_TIME = Component.text("남은 시간: ", NamedTextColor.YELLOW);
    private static final Component SUFFIX_SECONDS = Component.text("초", NamedTextColor.RED);

    private static final String NAME_NONE = "없음";
    private static final int MAX_CACHE_SECONDS = 3600;

    private static final Map<GamePhase, Component> PHASE_LINE_CACHE = new EnumMap<>(GamePhase.class);
    private static final Component[] TIME_LINE_CACHE = new Component[MAX_CACHE_SECONDS + 1];
    private static final Component TURN_NONE_LINE = Component.text()
            .append(PREFIX_TURN)
            .append(Component.text(NAME_NONE, NamedTextColor.GOLD))
            .build();

    static {
        for (final GamePhase phase : GamePhase.values()) {
            PHASE_LINE_CACHE.put(phase, Component.text()
                    .append(PREFIX_PHASE)
                    .append(Component.text(phase.displayName(), NamedTextColor.WHITE))
                    .build());
        }

        for (int i = 0; i <= MAX_CACHE_SECONDS; i++) {
            TIME_LINE_CACHE[i] = Component.text()
                    .append(PREFIX_TIME)
                    .append(Component.text(i))
                    .append(SUFFIX_SECONDS)
                    .build();
        }
    }

    private final GameContext context;
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    private Component turnLineCache;

    public void tick() {
        if (context.currentPhase() == GamePhase.WAITING) {
            if (!boards.isEmpty()) {
                reset();
            }
            return;
        }

        if (context.currentPhase() == GamePhase.PIECE_SELECTION && !context.isSelectionStarted()) {
            if (!boards.isEmpty()) {
                reset();
            }
            return;
        }

        final Map<UUID, ?> participants = context.participants();

        for (final UUID playerId : participants.keySet()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player == null) {
                continue;
            }

            FastBoard board = boards.get(playerId);

            if (board == null) {
                board = new FastBoard(player);
                board.updateTitle(TITLE);
                boards.put(playerId, board);
            }

            applyBoard(player, board);
        }

        cleanupStaleBoards(participants);
    }

    public void remove(final Player player) {
        final FastBoard board = boards.remove(player.getUniqueId());

        if (board == null) {
            return;
        }

        board.delete();
    }

    public void reset() {
        for (final FastBoard board : boards.values()) {
            board.delete();
        }

        boards.clear();
        turnLineCache = null;
    }

    public void updateTurnLine(final Player player) {
        turnLineCache = Component.text()
                .append(PREFIX_TURN)
                .append(Component.text(player.getName(), NamedTextColor.GOLD))
                .build();
    }

    public void handlePhaseChange(final GamePhase nextPhase) {
        if (nextPhase == GamePhase.BATTLE) {
            return;
        }

        clearTurnLine();
    }

    public void clearTurnLine() {
        turnLineCache = null;
    }

    private void cleanupStaleBoards(final Map<UUID, ?> activeParticipants) {
        for (final Iterator<Map.Entry<UUID, FastBoard>> iterator = boards.entrySet().iterator(); iterator.hasNext(); ) {
            final Map.Entry<UUID, FastBoard> entry = iterator.next();

            if (activeParticipants.containsKey(entry.getKey())) {
                continue;
            }

            entry.getValue().delete();
            iterator.remove();
        }
    }

    private void applyBoard(final Player player, final FastBoard board) {
        if (context.currentPhase() == GamePhase.PIECE_SELECTION) {
            applySelectionBoard(player, board);
            return;
        }

        final int remaining = context.remainingSeconds();
        final Component timeLine;

        if (remaining >= 0 && remaining <= MAX_CACHE_SECONDS) {
            timeLine = TIME_LINE_CACHE[remaining];
        } else {
            timeLine = Component.text()
                    .append(PREFIX_TIME)
                    .append(Component.text(remaining))
                    .append(SUFFIX_SECONDS)
                    .build();
        }

        board.updateLines(
                Component.empty(),
                PHASE_LINE_CACHE.getOrDefault(context.currentPhase(), Component.empty()),
                turnLineCache == null ? TURN_NONE_LINE : turnLineCache,
                timeLine,
                Component.empty()
        );
    }

    private void applySelectionBoard(final Player player, final FastBoard board) {
        final Participant me = context.participants().get(player.getUniqueId());
        if (me == null) return;

        final Team myTeam = me.team();
        final List<Component> lines = new ArrayList<>();
        
        lines.add(Component.empty());
        lines.add(Component.text(" [ 내 기물 ]", myTeam.color()));

        if (me.hasPiece() && me.selectedType() != null) {
            lines.add(Component.text(" ▶ " + me.selectedType().symbol() + " " + me.selectedType().displayName(), NamedTextColor.WHITE));
        } else {
            lines.add(Component.text(" ▶ -", NamedTextColor.GRAY));
        }

        final List<Participant> allies = context.participants().values().stream()
                .filter(p -> p.team() == myTeam && !p.playerId().equals(player.getUniqueId()))
                .toList();

        if (!allies.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.text(" [ 아군 현황 ]", myTeam.color()));

            for (final Participant p : allies) {
                final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(p.playerId());
                final String name = targetPlayer.getName() != null ? targetPlayer.getName() : "Unknown";

                if (p.hasPiece() && p.selectedType() != null) {
                    lines.add(Component.text(" " + name + ": " + p.selectedType().symbol() + " " + p.selectedType().displayName(), NamedTextColor.WHITE));
                } else {
                    lines.add(Component.text(" " + name + ": -", NamedTextColor.DARK_GRAY));
                }
            }
        }

        lines.add(Component.empty());
        board.updateLines(lines);
    }
}
