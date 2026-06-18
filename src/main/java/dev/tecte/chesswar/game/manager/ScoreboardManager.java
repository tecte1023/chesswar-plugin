package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.piece.Piece;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.team.Team;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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
    private static final Map<Team, Component> TEAM_TIME_PREFIX_CACHE = new EnumMap<>(Team.class);
    private static final Map<Team, Component> TEAM_STATUS_PREFIX_CACHE = new EnumMap<>(Team.class);
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

        TEAM_TIME_PREFIX_CACHE.put(Team.WHITE, PREFIX_TIME_WHITE);
        TEAM_TIME_PREFIX_CACHE.put(Team.BLACK, PREFIX_TIME_BLACK);

        TEAM_STATUS_PREFIX_CACHE.put(Team.WHITE, PREFIX_STATUS_WHITE);
        TEAM_STATUS_PREFIX_CACHE.put(Team.BLACK, PREFIX_STATUS_BLACK);

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
    private final dev.tecte.chesswar.economy.EconomyState economyState;
    private final PieceState pieceState;
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    private Component turnLineCache;

    public ScoreboardManager(
            final GameContext context,
            final PlayerInventoryAdapter inventoryAdapter,
            final dev.tecte.chesswar.economy.EconomyState economyState,
            final PieceState pieceState
    ) {
        this.context = context;
        this.inventoryAdapter = inventoryAdapter;
        this.economyState = economyState;
        this.pieceState = pieceState;
    }

    public void updateAll() {
        for (final UUID playerId : context.participantIds()) {
            final Player player = Bukkit.getPlayer(playerId);

            if (player == null || !player.isOnline()) {
                continue;
            }

            updatePlayer(player);
        }
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

        final Participant p = context.participant(currentTurnId);
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
        final int displayTime = isActive ? context.remainingSeconds() : context.getTeamTime(team);

        return Component.text()
                .append(isActive ? MARKER_ACTIVE : MARKER_INACTIVE)
                .append(TEAM_TIME_PREFIX_CACHE.get(team))
                .append(Component.text(formatTime(displayTime), NamedTextColor.WHITE))
                .build();
    }

    private String formatTime(final int seconds) {
        final int m = Math.max(0, seconds / 60);
        final int s = Math.max(0, seconds % 60);
        return String.format("%02d:%02d", m, s);
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
        final UUID playerId = player.getUniqueId();

        if (context.participant(playerId) == null) {
            boards.remove(playerId);
            board.delete();
            return;
        }

        switch (context.currentPhase()) {
            case WAITING -> applyWaitingBoard(player, board);
            case PIECE_SELECTION -> {
                if (context.isSelectionStarted()) {
                    applySelectionBoard(player, board);
                } else {
                    applyWaitingBoard(player, board);
                }
            }
            case TURN_ORDER -> applyTurnOrderBoard(player, board);
            case BATTLE -> applyBattleBoard(player, board);
            default -> applyDefaultBoard(player, board);
        }
    }

    private void applyDefaultBoard(final Player player, final FastBoard board) {
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

        board.updateLines(ScoreboardBuilder.create()
                .blank()
                .line(PHASE_LINE_CACHE.getOrDefault(context.currentPhase(), Component.empty()))
                .line(turnLineCache == null ? TURN_NONE_LINE : turnLineCache)
                .line(timeLine)
                .blank()
                .build());
    }

    private void applyWaitingBoard(final Player player, final FastBoard board) {
        final UUID playerId = player.getUniqueId();
        final Participant me = context.participant(playerId);
        final ScoreboardBuilder builder = ScoreboardBuilder.create();

        builder.blank()
                .line(Component.text(" 소속 팀: ").append(Component.text(me.team().teamName(), me.team().color())))
                .blank();

        for (final Team team : Team.values()) {
            final List<Participant> teamMembers = context.participantsValues().stream()
                    .filter(p -> p.team() == team)
                    .sorted(Comparator.comparing(Participant::playerName))
                    .toList();

            builder.line(Component.text(" [ " + team.teamName() + " : " + teamMembers.size() + "명 ]", team.color()));

            if (teamMembers.isEmpty()) {
                builder.line(Component.text(" ▶ -", NamedTextColor.DARK_GRAY));
            } else {
                for (final Participant p : teamMembers) {
                    final boolean isMe = p.playerId().equals(playerId);
                    builder.line(Component.text(" ▶ " + p.playerName(), isMe ? NamedTextColor.GOLD : NamedTextColor.WHITE));
                }
            }
            builder.blank();
        }

        board.updateLines(builder.build());
    }

    private void applyTurnOrderBoard(final Player player, final FastBoard board) {
        final UUID playerId = player.getUniqueId();
        final Participant me = context.participant(playerId);
        if (me == null) return;

        final Team myTeam = me.team();
        final ScoreboardBuilder builder = ScoreboardBuilder.create();

        builder.blank()
                .line(Component.text(" 소속 팀: ").append(Component.text(myTeam.teamName(), myTeam.color())))
                .blank()
                .line(Component.text(" [ 턴 순서 ]", myTeam.color()));

        final List<TeamStatus> teamStatusList = new ArrayList<>();
        for (final Participant p : context.participantsValues()) {
            if (p.team() != myTeam) continue;

            final Player pObj = Bukkit.getPlayer(p.playerId());
            final int currentOrder = pObj != null ? inventoryAdapter.extractTurnOrder(pObj) : -1;
            teamStatusList.add(new TeamStatus(p, pObj, currentOrder));
        }

        teamStatusList.sort((a, b) -> {
            if (a.order == -1 && b.order == -1) return 0;
            if (a.order == -1) return 1;
            if (b.order == -1) return -1;
            return Integer.compare(a.order, b.order);
        });

        for (final TeamStatus status : teamStatusList) {
            final boolean isMe = status.participant.playerId().equals(playerId);
            final boolean hasOrder = status.order != -1;
            final String orderText = hasOrder ? status.order + "번" : "-";

            final Piece piece = status.participant.getPiece(pieceState);
            final String pieceText = piece != null
                    ? piece.type().symbol() + " " + piece.type().displayName()
                    : "?";

            final NamedTextColor color = isMe ? NamedTextColor.GOLD : (hasOrder ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY);
            builder.line(Component.text(" [" + orderText + "] " + status.participant.playerName() + ": " + pieceText, color));
        }

        builder.blank();
        board.updateLines(builder.build());
    }

    private record TeamStatus(Participant participant, Player player, int order) {
    }

    private void applySelectionBoard(final Player player, final FastBoard board) {
        final UUID playerId = player.getUniqueId();
        final Participant me = context.participant(playerId);
        if (me == null) return;

        final Team myTeam = me.team();
        final ScoreboardBuilder builder = ScoreboardBuilder.create();

        builder.blank()
                .line(Component.text(" 소속 팀: ").append(Component.text(myTeam.teamName(), myTeam.color())))
                .blank()
                .line(Component.text(" [ 내 기물 ]", myTeam.color()));

        final Piece myPiece = me.getPiece(pieceState);
        final String myPieceText = myPiece != null
                ? myPiece.type().symbol() + " " + myPiece.type().displayName()
                : "-";

        builder.line(Component.text(" ▶ " + myPieceText, NamedTextColor.GOLD))
                .blank()
                .line(Component.text(" [ 아군 현황 ]", myTeam.color()));

        final List<Participant> allies = context.participantsValues().stream()
                .filter(p -> p.team() == myTeam && !p.playerId().equals(playerId))
                .sorted(Comparator.comparing(Participant::playerName))
                .toList();

        if (allies.isEmpty()) {
            builder.line(Component.text(" ▶ -", NamedTextColor.DARK_GRAY));
        } else {
            for (final Participant p : allies) {
                final Piece allyPiece = p.getPiece(pieceState);
                final String pieceText = allyPiece != null
                        ? allyPiece.type().symbol() + " " + allyPiece.type().displayName()
                        : "-";

                builder.line(Component.text(" ▶ " + p.playerName() + ": " + pieceText, allyPiece != null ? NamedTextColor.WHITE : NamedTextColor.DARK_GRAY));
            }
        }

        builder.blank();
        board.updateLines(builder.build());
    }

    private void applyBattleBoard(final Player player, final FastBoard board) {
        final Participant me = context.participant(player.getUniqueId());

        if (me == null) {
            return;
        }

        final Team turnTeam = context.currentTurnTeam();

        board.updateLines(ScoreboardBuilder.create()
                .blank()
                .line(turnLineCache == null ? TURN_NONE_LINE : turnLineCache)
                .blank()
                .line(getPersonalStatusLine(me))
                .blank()
                .line(getTeamTimeLine(Team.WHITE, turnTeam == Team.WHITE))
                .line(getTeamTimeLine(Team.BLACK, turnTeam == Team.BLACK))
                .blank()
                .line(getTeamSurvivalLine(Team.WHITE))
                .line(getTeamSurvivalLine(Team.BLACK))
                .blank()
                .build());
    }

    private Component getPersonalStatusLine(final Participant me) {
        final Piece piece = me.getPiece(pieceState);
        final List<String> effects = piece != null ? piece.statusEffects() : new java.util.ArrayList<>();

        final Component statusComponent = effects.isEmpty()
                ? Component.text("정상", NamedTextColor.WHITE)
                : Component.text(String.join(", ", effects), NamedTextColor.RED);

        final int gold = economyState.getPlayerGold(me.playerId()).currentGold();

        return Component.text()
                .append(PREFIX_GOLD).append(Component.text(gold + "G", NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(PREFIX_STATUS).append(statusComponent)
                .build();
    }

    private Component getTeamSurvivalLine(final Team team) {
        return Component.text()
                .append(TEAM_STATUS_PREFIX_CACHE.get(team))
                .append(Component.text(context.countTeam(team), NamedTextColor.WHITE))
                .build();
    }

    private static class ScoreboardBuilder {
        private final List<Component> lines = new ArrayList<>();

        public static ScoreboardBuilder create() {
            return new ScoreboardBuilder();
        }

        public ScoreboardBuilder blank() {
            lines.add(Component.empty());
            return this;
        }

        public ScoreboardBuilder line(final Component line) {
            lines.add(line);
            return this;
        }

        public List<Component> build() {
            return lines;
        }
    }
}
