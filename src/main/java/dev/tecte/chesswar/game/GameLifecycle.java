package dev.tecte.chesswar.game;

import dev.tecte.chesswar.core.Entity;
import dev.tecte.chesswar.core.SimulationEventBus;
import dev.tecte.chesswar.piece.CombatComponent;
import dev.tecte.chesswar.piece.PieceComponent;
import dev.tecte.chesswar.piece.PlayerComponent;
import dev.tecte.chesswar.piece.Team;
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

@RequiredArgsConstructor
public final class GameLifecycle {
    private final SimulationEventBus eventBus;
    private final Map<Team, List<Entity>> turnQueues = new EnumMap<>(Team.class);
    private final Map<Team, Integer> teamCursors = new EnumMap<>(Team.class);
    private final Set<UUID> participants = new HashSet<>();
    private GamePhase phase = GamePhase.WAITING;
    private Team currentTeam;
    @Getter
    private Entity currentPiece;

    public GameStartResult start(final List<Entity> playerPieces) {
        if (phase == GamePhase.BATTLE) {
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

        currentTeam = startingTeam;
        currentPiece = startingPiece;
        phase = GamePhase.BATTLE;
        eventBus.publish(new TurnStartEvent(startingPiece));

        return GameStartResult.SUCCESS;
    }

    public void stop() {
        if (phase != GamePhase.BATTLE) {
            return;
        }

        turnQueues.clear();
        teamCursors.clear();
        participants.clear();
        currentTeam = null;
        currentPiece = null;
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
        eventBus.publish(new TurnStartEvent(nextPiece));
    }

    public boolean isBattle() {
        return phase == GamePhase.BATTLE;
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
