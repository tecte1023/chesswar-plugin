package dev.tecte.chesswar.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public final class WaitingPhaseManager implements GamePhaseChangeListener {
    @NotNull
    private final StartTriggerUIComponent triggerComponent;

    @NotNull
    private final GamePhaseComponent phaseComponent;

    @NotNull
    private final WaitingPhasePresenter presenter;

    @NotNull
    private final InternalEventBus internalEventBus;

    public void forceBindStartTrigger(@NotNull final Location location) {
        final Location triggerLocation = location.clone().add(0, 1.0, 0);

        triggerComponent.startTriggerLocation(triggerLocation);

        if (phaseComponent.phase() != GamePhase.WAITING) {
            return;
        }

        spawnTrigger();
    }

    @Override
    public void onPhaseChanged(@NotNull final GamePhase newPhase) {
        if (newPhase == GamePhase.WAITING) {
            spawnTrigger();
        } else {
            hideTrigger();
        }
    }

    private void hideTrigger() {
        final StartTriggerIds oldTriggerIds = triggerComponent.activeTriggerIds();

        if (oldTriggerIds == null) {
            return;
        }

        triggerComponent.activeTriggerIds(null);
        presenter.hideStartTrigger(oldTriggerIds);
    }

    private void spawnTrigger() {
        final Location location = triggerComponent.startTriggerLocation();

        if (location == null) {
            return;
        }

        hideTrigger();

        final StartTriggerIds newTriggerIds = presenter.showStartTrigger(location);

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
        internalEventBus.publishPhaseChange(GamePhase.STARTING);
    }
}
