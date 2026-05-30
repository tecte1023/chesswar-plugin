package dev.tecte.chesswar.game.manager;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.GameResetEvent;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.component.PhaseTimerSettings;
import dev.tecte.chesswar.game.component.Statistics;
import dev.tecte.chesswar.game.component.TurnStartedEvent;
import dev.tecte.chesswar.game.state.GameState;
import dev.tecte.chesswar.game.state.WaitingState;
import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class GameManager {
    private final ChessWar plugin;
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final PhaseTimerSettings timerSettings = PhaseTimerSettings.createDefault();

    private GameState currentState;
    private Participant[] turnOrder = new Participant[0];
    private int currentTurnIndex = -1;

    public GameManager(final ChessWar plugin) {
        this.plugin = plugin;
        currentState = new WaitingState(plugin);
    }

    public GamePhase phase() {
        return currentState.phase();
    }

    public void advancePhase() {
        changeState(currentState.nextState());
    }

    public void nextTurn() {
        currentState.nextTurn(this);
    }

    public void win(final Team winner) {
        final Component winMessage = Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(winner.teamName(), winner.color(), TextDecoration.BOLD))
                .append(Component.text("가 승리했습니다!", NamedTextColor.WHITE, TextDecoration.BOLD))
                .build();

        Bukkit.broadcast(winMessage);
        advancePhase();
    }

    public void reset() {
        currentState = new WaitingState(plugin);
        turnOrder = new Participant[0];
        currentTurnIndex = -1;

        for (final Participant participant : participants.values()) {
            participant.initialCoordinate(null);
            participant.ready(false);
            participant.turnOrder(-1);
            participant.commanderTarget(null);

            final Player player = Bukkit.getPlayer(participant.playerId());

            if (player != null) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        Bukkit.getPluginManager().callEvent(new GameResetEvent());
    }

    public void join(final Player player, final Team team) {
        final UUID playerId = player.getUniqueId();

        participants.put(playerId, Participant.of(playerId, team));
    }

    public void leave(final Player player) {
        final UUID playerId = player.getUniqueId();

        participants.remove(playerId);

        for (int i = 0; i < turnOrder.length; i++) {
            if (turnOrder[i] != null && playerId.equals(turnOrder[i].playerId())) {
                turnOrder[i] = null;
                break;
            }
        }
    }

    public boolean isParticipant(final Player player) {
        return participants.containsKey(player.getUniqueId());
    }

    public Optional<Participant> findParticipant(final UUID playerId) {
        return Optional.ofNullable(participants.get(playerId));
    }

    public int countTeam(final Team team) {
        int count = 0;

        for (final Participant participant : participants.values()) {
            if (participant.team() == team) {
                count++;
            }
        }

        return count;
    }

    public Statistics stats(final UUID playerId) {
        final Participant participant = participants.get(playerId);

        return participant != null ? participant.statistics() : new Statistics();
    }

    public void handleReadyUp(final Player player, final Location location) {
        currentState.handleReadyUp(this, player, location);
    }

    public void toggleReady(final UUID playerId, final boolean ready) {
        final Participant participant = participants.get(playerId);

        if (participant != null) {
            participant.ready(ready);
        }
    }

    public boolean isReady(final UUID playerId) {
        final Participant participant = participants.get(playerId);

        return participant != null && participant.ready();
    }

    public boolean areAllParticipantsReady() {
        if (participants.isEmpty()) {
            return false;
        }

        for (final Participant participant : participants.values()) {
            if (!participant.ready()) {
                return false;
            }
        }

        return true;
    }

    public int countReady(final Team team) {
        int count = 0;

        for (final Participant participant : participants.values()) {
            if (participant.team() == team && participant.ready()) {
                count++;
            }
        }

        return count;
    }

    public void selectPiece(final Player participant, final Coordinate coordinate) {
        currentState.selectPiece(this, participant, coordinate);
    }

    public boolean areAllPiecesSelected() {
        if (participants.isEmpty()) {
            return false;
        }

        for (final Participant participant : participants.values()) {
            if (!participant.hasPiece()) {
                return false;
            }
        }

        return true;
    }

    public Optional<UUID> currentTurnPlayer() {
        if (turnOrder.length == 0 || currentTurnIndex < 0 || currentTurnIndex >= turnOrder.length) {
            return Optional.empty();
        }

        final Participant participant = turnOrder[currentTurnIndex];

        return participant == null ? Optional.empty() : Optional.of(participant.playerId());
    }

    public void calculateTurnOrder(final Map<UUID, Integer> playerOrders) {
        final List<Participant> whiteTeam = new ArrayList<>();
        final List<Participant> blackTeam = new ArrayList<>();

        for (final Participant participant : participants.values()) {
            final Integer order = playerOrders.get(participant.playerId());

            if (order != null) {
                participant.turnOrder(order);
            }

            if (participant.team() == Team.WHITE) {
                whiteTeam.add(participant);
            } else {
                blackTeam.add(participant);
            }
        }

        whiteTeam.sort(Comparator.comparingInt(Participant::turnOrder));
        blackTeam.sort(Comparator.comparingInt(Participant::turnOrder));
        turnOrder = new Participant[whiteTeam.size() + blackTeam.size()];

        final int maxSize = Math.max(whiteTeam.size(), blackTeam.size());
        int index = 0;

        for (int i = 0; i < maxSize; i++) {
            if (i < whiteTeam.size()) {
                turnOrder[index++] = whiteTeam.get(i);
            }

            if (i < blackTeam.size()) {
                turnOrder[index++] = blackTeam.get(i);
            }
        }

        currentTurnIndex = 0;
    }

    public void performNextTurnLogic() {
        if (turnOrder.length == 0) {
            return;
        }

        final Optional<UUID> currentPlayerId = currentTurnPlayer();

        if (currentPlayerId.isPresent()) {
            clearCommandTarget(currentPlayerId.get());
        }

        int attempts = 0;

        do {
            currentTurnIndex = (currentTurnIndex + 1) % turnOrder.length;
            attempts++;
        } while (turnOrder[currentTurnIndex] == null && attempts < turnOrder.length);

        final Optional<UUID> nextPlayerId = currentTurnPlayer();

        if (nextPlayerId.isEmpty()) {
            return;
        }

        final Player player = Bukkit.getPlayer(nextPlayerId.get());

        if (player != null) {
            Bukkit.getPluginManager().callEvent(new TurnStartedEvent(player));
        }
    }

    public void spawnAllPiecesOnMainBoard() {
        if (!plugin.boardManager().hasBoard()) {
            return;
        }

        plugin.pieceManager().spawnInitialLayout(plugin.boardManager().currentBoard(), participants.values());
    }

    public void registerCommandTarget(final UUID commanderId, final Coordinate targetCoord) {
        final Participant participant = participants.get(commanderId);

        if (participant != null) {
            participant.commanderTarget(targetCoord);
        }
    }

    public void clearCommandTarget(final UUID commanderId) {
        final Participant participant = participants.get(commanderId);

        if (participant != null) {
            participant.commanderTarget(null);
        }
    }

    public Optional<Coordinate> findCommandTarget(final UUID commanderId) {
        final Participant participant = participants.get(commanderId);

        return participant == null ? Optional.empty() : Optional.ofNullable(participant.commanderTarget());
    }

    private void changeState(final GameState nextState) {
        if (currentState != null) {
            currentState.onExit();
        }

        currentState = nextState;
        broadcastPhaseChange(currentState.phase());
        currentState.onEnter(plugin, this);
    }

    private void broadcastPhaseChange(final GamePhase nextPhase) {
        Bukkit.broadcast(Component.text()
                .append(Component.text(" [!] ", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("게임 단계가 변경되었습니다: ", NamedTextColor.YELLOW))
                .append(Component.text(nextPhase.displayName(), NamedTextColor.WHITE, TextDecoration.BOLD))
                .build());
    }
}
