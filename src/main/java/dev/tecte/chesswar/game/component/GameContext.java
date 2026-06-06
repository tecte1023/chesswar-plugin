package dev.tecte.chesswar.game.component;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor(staticName = "create")
public class GameContext {
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final PhaseTimerSettings timerSettings = PhaseTimerSettings.createDefault();

    private GamePhase currentPhase = GamePhase.WAITING;
    private Participant[] turnOrder = new Participant[0];
    private int currentTurnIndex = -1;

    private int remainingSeconds = 0;
    private int initialSeconds = 0;
    private int whiteTeamTime = 0;
    private int blackTeamTime = 0;
    private boolean timerRunning = false;
    private boolean isSelectionStarted = false;
    private TimerPolicy currentTimerPolicy = TimerPolicy.GRACEFUL;

    private Location startButtonLocation;
    private UUID bindingAdmin;
    private Entity startButtonHologram;

    public void currentPhase(final GamePhase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public void isSelectionStarted(final boolean isSelectionStarted) {
        this.isSelectionStarted = isSelectionStarted;
    }

    public void currentTimerPolicy(final TimerPolicy currentTimerPolicy) {
        this.currentTimerPolicy = currentTimerPolicy;
    }

    public void turnOrder(final Participant[] turnOrder) {
        this.turnOrder = turnOrder;
    }

    public void currentTurnIndex(final int currentTurnIndex) {
        this.currentTurnIndex = currentTurnIndex;
    }

    public int getTeamTime(final Team team) {
        return team == Team.WHITE ? whiteTeamTime : blackTeamTime;
    }

    public UUID currentTurnPlayerId() {
        if (turnOrder.length == 0 || currentTurnIndex < 0 || currentTurnIndex >= turnOrder.length) {
            return null;
        }

        final Participant participant = turnOrder[currentTurnIndex];

        return participant == null ? null : participant.playerId();
    }

    public int countTeam(final Team team) {
        int count = 0;

        for (final Participant participant : participants.values()) {
            if (participant.team() != team) {
                continue;
            }

            count++;
        }

        return count;
    }

    public int countReady(final Team team) {
        int count = 0;

        for (final Participant participant : participants.values()) {
            if (participant.team() != team || !participant.ready()) {
                continue;
            }

            count++;
        }

        return count;
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

    public void startTimer(final int seconds) {
        remainingSeconds = seconds;
        initialSeconds = seconds;
        timerRunning = true;
    }

    public void stopTimer() {
        timerRunning = false;
    }

    public void tickTimer() {
        if (remainingSeconds < 0) {
            return;
        }

        remainingSeconds--;
    }

    public void accelerateTimer(final int seconds) {
        if (remainingSeconds <= seconds) {
            return;
        }

        remainingSeconds = seconds;
    }

    public void updateTeamTime(final Team team, final int seconds) {
        if (team == Team.WHITE) {
            whiteTeamTime = seconds;
            return;
        }

        blackTeamTime = seconds;
    }

    public void resetTurnIndex() {
        currentTurnIndex = -1;
    }

    public void advanceTurnIndex() {
        if (turnOrder.length == 0) {
            return;
        }

        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.length;
    }

    public void reset() {
        turnOrder = new Participant[0];
        currentTurnIndex = -1;
        remainingSeconds = 0;
        timerRunning = false;
        isSelectionStarted = false;
        currentTimerPolicy = TimerPolicy.GRACEFUL;
        whiteTeamTime = blackTeamTime = timerSettings.teamTotalTime();

        for (final Participant participant : participants.values()) {
            participant.initialCoordinate(null);
            participant.ready(false);
            participant.turnOrder(-1);
            participant.commanderTarget(null);
        }
    }
}

