package dev.tecte.chesswar.game;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.core.Tickable;
import dev.tecte.chesswar.piece.CombatComponent;
import dev.tecte.chesswar.piece.PieceComponent;
import dev.tecte.chesswar.piece.PieceDeathEvent;
import dev.tecte.chesswar.piece.PieceType;
import dev.tecte.chesswar.piece.PlayerComponent;
import dev.tecte.chesswar.piece.Team;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GameLifecycle implements Tickable {
    public static final int INITIAL_TEAM_TICKS = 20 * 60 * 10;
    public static final int OVERTIME_TICKS = 20 * 30;
    public static final int ENDING_TICKS = 20 * 5;

    private final SimulationEventBus eventBus;
    private final Map<Team, List<Entity>> turnQueues = new EnumMap<>(Team.class);
    private final Map<Team, Integer> teamCursors = new EnumMap<>(Team.class);
    private final Set<UUID> participants = new HashSet<>();
    private GamePhase phase = GamePhase.WAITING;
    private Team currentTeam;
    @Getter
    private Entity currentPiece;
    private int whiteRemainingTicks;
    private int blackRemainingTicks;
    private int overtimeTicks;
    private int endingTicks;

    public static GameLifecycle create(final SimulationEventBus eventBus) {
        var lifecycle = new GameLifecycle(eventBus);
        eventBus.subscribe(PieceDeathEvent.class, lifecycle::onPieceDeath);
        return lifecycle;
    }

    @Override
    public void tick() {
        if (phase == GamePhase.ENDING) {
            endingTicks--;
            if (endingTicks <= 0) {
                stop();
            }

            return;
        }

        if (phase != GamePhase.BATTLE) {
            return;
        }

        if (consumeTeamTick(currentTeam)) {
            return;
        }

        overtimeTicks--;
        if (overtimeTicks <= 0) {
            eventBus.publish(new TurnTimeoutEvent(currentPiece));
            advanceTurn();
        }
    }

    public GameStartResult start(final List<Entity> playerPieces) {
        if (phase != GamePhase.WAITING) {
            return GameStartResult.ALREADY_STARTED;
        }

        if (playerPieces.isEmpty()) {
            return GameStartResult.NO_PLAYERS;
        }

        List<Entity> whitePieces = new ArrayList<>();
        List<Entity> blackPieces = new ArrayList<>();

        for (Entity piece : playerPieces) {
            Team team = piece.component(PieceComponent.class).team();
            if (team == Team.WHITE) {
                whitePieces.add(piece);
            } else {
                blackPieces.add(piece);
            }

            participants.add(piece.component(PlayerComponent.class).playerId());
        }

        Comparator<Entity> byId = Comparator.comparingInt(Entity::id);
        whitePieces.sort(byId);
        blackPieces.sort(byId);

        turnQueues.put(Team.WHITE, whitePieces);
        turnQueues.put(Team.BLACK, blackPieces);
        teamCursors.put(Team.WHITE, 0);
        teamCursors.put(Team.BLACK, 0);

        Team startingTeam = whitePieces.isEmpty() ? Team.BLACK : Team.WHITE;
        Entity startingPiece = selectNextAlivePiece(startingTeam);

        whiteRemainingTicks = INITIAL_TEAM_TICKS;
        blackRemainingTicks = INITIAL_TEAM_TICKS;
        overtimeTicks = OVERTIME_TICKS;
        endingTicks = 0;
        currentTeam = startingTeam;
        currentPiece = startingPiece;
        phase = GamePhase.BATTLE;
        eventBus.publish(new GameStartEvent(playerPieces));
        eventBus.publish(new TurnStartEvent(startingPiece));

        return GameStartResult.SUCCESS;
    }

    public void stop() {
        if (phase == GamePhase.WAITING) {
            return;
        }

        clearTurnState();
        whiteRemainingTicks = 0;
        blackRemainingTicks = 0;
        overtimeTicks = 0;
        endingTicks = 0;
        phase = GamePhase.WAITING;
        eventBus.publish(new GameStopEvent());
    }

    public void advanceTurn() {
        if (phase != GamePhase.BATTLE) {
            return;
        }

        Team nextTeam = oppositeTeam(currentTeam);
        Entity nextPiece = selectNextAlivePiece(nextTeam);
        if (nextPiece == null) {
            nextTeam = currentTeam;
            nextPiece = selectNextAlivePiece(nextTeam);
        }

        if (nextPiece == null) {
            stop();
            return;
        }

        currentTeam = nextTeam;
        currentPiece = nextPiece;
        overtimeTicks = OVERTIME_TICKS;
        eventBus.publish(new TurnStartEvent(nextPiece));
    }

    public boolean isWaiting() {
        return phase == GamePhase.WAITING;
    }

    public boolean isBattle() {
        return phase == GamePhase.BATTLE;
    }

    public boolean isRunning() {
        return phase != GamePhase.WAITING;
    }

    public boolean isParticipant(final UUID playerId) {
        return participants.contains(playerId);
    }

    public boolean isTurnOwner(final UUID playerId) {
        if (phase != GamePhase.BATTLE || currentPiece == null) {
            return false;
        }

        return currentPiece.component(PlayerComponent.class).playerId().equals(playerId);
    }

    public int remainingTeamTicks(final Team team) {
        return team == Team.WHITE ? whiteRemainingTicks : blackRemainingTicks;
    }

    private void onPieceDeath(final PieceDeathEvent event) {
        if (phase != GamePhase.BATTLE) {
            return;
        }

        Entity piece = event.piece();
        if (piece.hasComponent(PlayerComponent.class)) {
            participants.remove(piece.component(PlayerComponent.class).playerId());
        }

        var pieceComponent = piece.component(PieceComponent.class);
        if (pieceComponent.type() != PieceType.KING) {
            return;
        }

        clearTurnState();
        endingTicks = ENDING_TICKS;
        phase = GamePhase.ENDING;
        eventBus.publish(new GameWinEvent(oppositeTeam(pieceComponent.team())));
    }

    private void clearTurnState() {
        turnQueues.clear();
        teamCursors.clear();
        participants.clear();
        currentTeam = null;
        currentPiece = null;
    }

    private boolean consumeTeamTick(final Team team) {
        if (team == Team.WHITE) {
            if (whiteRemainingTicks > 0) {
                whiteRemainingTicks--;
                return true;
            }

            return false;
        }

        if (blackRemainingTicks > 0) {
            blackRemainingTicks--;
            return true;
        }

        return false;
    }

    private Team oppositeTeam(final Team team) {
        return team == Team.WHITE ? Team.BLACK : Team.WHITE;
    }

    private Entity selectNextAlivePiece(final Team team) {
        List<Entity> queue = turnQueues.get(team);
        int size = queue.size();
        if (size == 0) {
            return null;
        }

        int startIndex = teamCursors.get(team);
        for (int i = 0; i < size; i++) {
            int index = (startIndex + i) % size;
            Entity candidate = queue.get(index);
            if (!candidate.component(CombatComponent.class).isDead()) {
                teamCursors.put(team, (index + 1) % size);
                return candidate;
            }
        }

        return null;
    }
}
