package dev.tecte.chesswar.game;

import lombok.RequiredArgsConstructor;
import dev.tecte.chesswar.team.TeamRosterComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public final class WaitingPhaseManager {
    @NotNull
    private final StartTriggerUIComponent triggerComponent;

    @NotNull
    private final GamePhaseComponent phaseComponent;

    @NotNull
    private final TeamRosterComponent teamRosterComponent;

    @NotNull
    private final WaitingPhasePresenter presenter;

    public void forceBindStartTrigger(@NotNull final Location location) {
        final StartTriggerIds oldTriggerIds = triggerComponent.activeTriggerIds();

        if (oldTriggerIds != null) {
            presenter.hideStartTrigger(oldTriggerIds.interactionId(), oldTriggerIds.textId());
        }

        final Location triggerLocation = location.clone().add(0, 1.0, 0);
        final StartTriggerIds newTriggerIds = presenter.showStartTrigger(triggerLocation);

        triggerComponent.startTriggerLocation(triggerLocation);
        triggerComponent.activeTriggerIds(newTriggerIds);
    }

    public void tryStartGame(@NotNull final UUID clickedEntityId) {
        if (phaseComponent.phase() != GamePhase.WAITING) {
            return;
        }

        final StartTriggerIds currentTriggerIds = triggerComponent.activeTriggerIds();

        if (currentTriggerIds == null || !currentTriggerIds.matchesEntity(clickedEntityId)) {
            return;
        }

        applyGameStart();
    }

    public void tryStartGame() {
        if (phaseComponent.phase() != GamePhase.WAITING) {
            return;
        }

        applyGameStart();
    }

    private void applyGameStart() {
        phaseComponent.phase(phaseComponent.phase().next());

        final UUID[][] rosters = teamRosterComponent.teamRosters();

        for (final UUID[] team : rosters) {
            for (final UUID memberId : team) {
                if (memberId == null) {
                    continue;
                }

                final Player player = Bukkit.getPlayer(memberId);

                if (player == null) {
                    continue;
                }

                presenter.showGameStartFeedback(player);
            }
        }
    }

    public void tryStopGame() {
        if (phaseComponent.phase() == GamePhase.WAITING) {
            return;
        }

        phaseComponent.phase(GamePhase.WAITING);

        final UUID[][] rosters = teamRosterComponent.teamRosters();

        for (final UUID[] team : rosters) {
            for (final UUID memberId : team) {
                if (memberId == null) {
                    continue;
                }

                final Player player = Bukkit.getPlayer(memberId);

                if (player == null) {
                    continue;
                }

                presenter.showGameStopFeedback(player);
            }
        }
    }
}
