package dev.tecte.chesswar.game;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public final class WaitingPhaseManager {
    @NotNull
    private final StartTriggerUIComponent triggerComponent;

    @NotNull
    private final GamePhaseComponent phaseComponent;

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

        final Location triggerLocation = triggerComponent.startTriggerLocation();

        if (triggerLocation == null) {
            return;
        }

        phaseComponent.phase(phaseComponent.phase().next());
        presenter.showGameStartFeedback(triggerLocation);
    }
}
