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
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j(topic = "ChessWar")
public class ScoreboardManager {
    private static final Component TITLE = Component.text(
            "ChessWar",
            NamedTextColor.GOLD,
            TextDecoration.BOLD
    );

    private static final Component PREFIX_PHASE = Component.text("상태: ", NamedTextColor.YELLOW);
    private static final Component PREFIX_TURN = Component.text("현재 턴: ", NamedTextColor.YELLOW);
    private static final Component PREFIX_TIME = Component.text("제한 시간: ", NamedTextColor.YELLOW);
    private static final Component PREFIX_GOLD = Component.text("보유 골드: ", NamedTextColor.YELLOW);
    private static final Component PREFIX_STATUS = Component.text("상태: ", NamedTextColor.YELLOW);
    private static final Component SUFFIX_SECONDS = Component.text("초", NamedTextColor.RED);
    private static final Component SUFFIX_GOLD = Component.text(" G", NamedTextColor.GOLD);

    private static final Component PREFIX_TIME_WHITE = Component.text("백팀 시간: ", Team.WHITE.color());
    private static final Component PREFIX_TIME_BLACK = Component.text("흑팀 시간: ", Team.BLACK.color());
    private static final Component PREFIX_STATUS_WHITE = Component.text("백팀 생존: ", Team.WHITE.color());
    private static final Component PREFIX_STATUS_BLACK = Component.text("흑팀 생존: ", Team.BLACK.color());
    private static final Component MARKER_ACTIVE = Component.text("▶ ", NamedTextColor.GREEN, TextDecoration.BOLD);
    private static final Component MARKER_INACTIVE = Component.text("  ", NamedTextColor.DARK_GRAY);

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
    private final PlayerInventoryAdapter inventoryAdapter;
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    private Component turnLineCache;

    public ScoreboardManager(final GameContext context, final PlayerInventoryAdapter inventoryAdapter) {
        this.context = context;
        this.inventoryAdapter = inventoryAdapter;
    }

    public void updateAll() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
        cleanupStaleBoards();
    }

    public void updatePlayer(final Player player) {
        final UUID playerId = player.getUniqueId();
        FastBoard board = boards.get(playerId);

        if (board == null) {
            board = new FastBoard(player);
            board.updateTitle(TITLE);
            boards.put(playerId, board);
        }

        applyBoard(player, board);
    }

    public void updateTimer(final int seconds) {
        if (context.currentPhase() != GamePhase.BATTLE) {
            return;
        }

        final UUID currentTurnId = context.currentTurnPlayerId();
        if (currentTurnId == null) {
            return;
        }

        final Participant p = context.participants().get(currentTurnId);
        if (p == null) {
            return;
        }

        final Team turnTeam = p.team();

        for (final FastBoard board : boards.values()) {
            board.updateLine(5, getTeamTimeLine(Team.WHITE, turnTeam == Team.WHITE));
            board.updateLine(6, getTeamTimeLine(Team.BLACK, turnTeam == Team.BLACK));
        }
    }

    private Component getTeamTimeLine(final Team team, final boolean isActive) {
        // 현재 턴인 팀은 context.remainingSeconds()를, 아니면 팀 공유 시간을 표시
        final int displayTime = isActive ? context.remainingSeconds() : context.getTeamTime(team);

        return Component.text()
                .append(isActive ? MARKER_ACTIVE : MARKER_INACTIVE)
                .append(team == Team.WHITE ? PREFIX_TIME_WHITE : PREFIX_TIME_BLACK)
                .append(Component.text(formatTime(displayTime), NamedTextColor.WHITE))
                .build();
    }

    private String formatTime(final int seconds) {
        final int m = Math.max(0, seconds / 60);
        final int s = Math.max(0, seconds % 60);
        return String.format("%02d:%02d", m, s);
    }

    public void tick() {
        // 기존 tick 로직 제거 (외부에서 필요 시에만 호출하도록 변경)
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

    private void cleanupStaleBoards() {
        for (final Iterator<Map.Entry<UUID, FastBoard>> iterator = boards.entrySet().iterator(); iterator.hasNext(); ) {
            final Map.Entry<UUID, FastBoard> entry = iterator.next();
            final Player player = Bukkit.getPlayer(entry.getKey());

            if (player != null && player.isOnline()) {
                continue;
            }

            entry.getValue().delete();
            iterator.remove();
        }
    }

    private void applyBoard(final Player player, final FastBoard board) {
        final Participant me = context.participants().get(player.getUniqueId());

        // 팀에 참가하지 않은 플레이어는 스코어보드를 표시하지 않음
        if (me == null) {
            boards.remove(player.getUniqueId());
            board.delete();
            return;
        }

        if (context.currentPhase() == GamePhase.WAITING || (context.currentPhase() == GamePhase.PIECE_SELECTION && !context.isSelectionStarted())) {
            applyWaitingBoard(player, board);
            return;
        }

        if (context.currentPhase() == GamePhase.PIECE_SELECTION) {
            applySelectionBoard(player, board);
            return;
        }

        if (context.currentPhase() == GamePhase.TURN_ORDER) {
            applyTurnOrderBoard(player, board);
            return;
        }

        if (context.currentPhase() == GamePhase.BATTLE) {
            applyBattleBoard(player, board);
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

    private void applyWaitingBoard(final Player player, final FastBoard board) {
        final List<Component> lines = new ArrayList<>();
        final Participant me = context.participants().get(player.getUniqueId());

        lines.add(Component.empty());
        lines.add(Component.text(" 소속 팀: ").append(Component.text(me.team().teamName(), me.team().color())));
        lines.add(Component.empty());

        for (final Team team : Team.values()) {
            final List<Participant> teamMembers = context.participants().values().stream()
                    .filter(p -> p.team() == team)
                    .sorted(Comparator.comparing(Participant::playerName))
                    .toList();

            lines.add(Component.text(" [ " + team.teamName() + " : " + teamMembers.size() + "명 ]", team.color()));
            
            if (teamMembers.isEmpty()) {
                lines.add(Component.text(" ▶ -", NamedTextColor.DARK_GRAY));
            } else {
                for (final Participant p : teamMembers) {
                    final boolean isMe = p.playerId().equals(player.getUniqueId());
                    lines.add(Component.text(" ▶ " + p.playerName(), isMe ? NamedTextColor.GOLD : NamedTextColor.WHITE));
                }
            }
            lines.add(Component.empty());
        }

        board.updateLines(lines);
    }

    private void applyTurnOrderBoard(final Player player, final FastBoard board) {
        final Participant me = context.participants().get(player.getUniqueId());
        if (me == null) return;

        final Team myTeam = me.team();
        final List<Component> lines = new ArrayList<>();

        lines.add(Component.empty());
        lines.add(Component.text(" 소속 팀: ").append(Component.text(myTeam.teamName(), myTeam.color())));
        lines.add(Component.empty());

        lines.add(Component.text(" [ 턴 순서 ]", myTeam.color()));

        final List<TeamStatus> teamStatusList = new ArrayList<>();
        for (final Participant p : context.participants().values()) {
            if (p.team() != myTeam) continue;

            final Player pObj = Bukkit.getPlayer(p.playerId());
            final Integer currentOrder = pObj != null ? inventoryAdapter.extractTurnOrder(pObj).orElse(null) : null;
            teamStatusList.add(new TeamStatus(p, pObj, currentOrder));
        }

        // 정렬: 순서 번호 오름차순, 미정(null)은 맨 아래
        teamStatusList.sort((a, b) -> {
            if (a.order == null && b.order == null) return 0;
            if (a.order == null) return 1;
            if (b.order == null) return -1;
            return Integer.compare(a.order, b.order);
        });

        for (final TeamStatus status : teamStatusList) {
            final boolean isMe = status.participant.playerId().equals(player.getUniqueId());
            final boolean hasOrder = status.order != null;
            final String orderText = hasOrder ? status.order + "번" : "-";
            final String pieceText = status.participant.selectedType() != null 
                    ? status.participant.selectedType().symbol() + " " + status.participant.selectedType().displayName() 
                    : "?";

            final NamedTextColor color = isMe ? NamedTextColor.GOLD : (hasOrder ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY);
            
            lines.add(Component.text(" [" + orderText + "] " + status.participant.playerName() + ": " + pieceText, color));
        }

        lines.add(Component.empty());
        board.updateLines(lines);
    }

    private record TeamStatus(Participant participant, Player player, Integer order) {}

    private void applySelectionBoard(final Player player, final FastBoard board) {
        final Participant me = context.participants().get(player.getUniqueId());
        if (me == null) return;

        final Team myTeam = me.team();
        final List<Component> lines = new ArrayList<>();

        lines.add(Component.empty());
        lines.add(Component.text(" 소속 팀: ").append(Component.text(myTeam.teamName(), myTeam.color())));
        lines.add(Component.empty());
        
        lines.add(Component.text(" [ 내 기물 ]", myTeam.color()));
        final String myPieceText = me.hasPiece() && me.selectedType() != null 
                ? me.selectedType().symbol() + " " + me.selectedType().displayName() 
                : "-";
        lines.add(Component.text(" ▶ " + myPieceText, NamedTextColor.GOLD));
        lines.add(Component.empty());

        lines.add(Component.text(" [ 아군 현황 ]", myTeam.color()));

        final List<Participant> allies = context.participants().values().stream()
                .filter(p -> p.team() == myTeam && !p.playerId().equals(player.getUniqueId()))
                .sorted(Comparator.comparing(Participant::playerName))
                .toList();

        if (allies.isEmpty()) {
            lines.add(Component.text(" ▶ -", NamedTextColor.DARK_GRAY));
        } else {
            for (final Participant p : allies) {
                final boolean hasPiece = p.hasPiece() && p.selectedType() != null;
                final String pieceText = hasPiece 
                        ? p.selectedType().symbol() + " " + p.selectedType().displayName() 
                        : "-";

                lines.add(Component.text(" ▶ " + p.playerName() + ": " + pieceText, hasPiece ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY));
            }
        }

        lines.add(Component.empty());
        board.updateLines(lines);
    }

    private void applyBattleBoard(final Player player, final FastBoard board) {
        final Participant me = context.participants().get(player.getUniqueId());
        if (me == null) return;

        final List<Component> lines = new ArrayList<>();

        lines.add(Component.empty());
        lines.add(turnLineCache == null ? TURN_NONE_LINE : turnLineCache);
        lines.add(Component.empty());

        // 내 상태 정보 (골드 및 상태이상)
        final String statusStr = me.statusEffects().isEmpty() ? "정상" : String.join(", ", me.statusEffects());
        lines.add(Component.text()
                .append(PREFIX_GOLD).append(Component.text(me.gold() + "G", NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(PREFIX_STATUS).append(Component.text(statusStr, me.statusEffects().isEmpty() ? NamedTextColor.WHITE : NamedTextColor.RED))
                .build());

        lines.add(Component.empty());

        // 팀별 제한 시간 표시
        final UUID currentTurnId = context.currentTurnPlayerId();
        final Team turnTeam = currentTurnId != null && context.participants().containsKey(currentTurnId)
                ? context.participants().get(currentTurnId).team()
                : null;

        lines.add(getTeamTimeLine(Team.WHITE, turnTeam == Team.WHITE));
        lines.add(getTeamTimeLine(Team.BLACK, turnTeam == Team.BLACK));

        lines.add(Component.empty());

        // 팀별 생존 현황
        lines.add(Component.text()
                .append(PREFIX_STATUS_WHITE)
                .append(Component.text(context.countTeam(Team.WHITE), NamedTextColor.WHITE))
                .build());
        lines.add(Component.text()
                .append(PREFIX_STATUS_BLACK)
                .append(Component.text(context.countTeam(Team.BLACK), NamedTextColor.WHITE))
                .build());

        lines.add(Component.empty());

        board.updateLines(lines);
    }
}
