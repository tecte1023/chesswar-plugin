package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.GameAnnouncer;
import dev.tecte.chessWar.game.domain.event.AllParticipantsSelectedEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 기물 선택 완료 시 알림을 담당합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SelectionCompletionAnnouncer implements Listener {
    private final GameAnnouncer gameAnnouncer;

    /**
     * 기물 선택 완료 시 이를 공지합니다.
     *
     * @param event 기물 선택 완료 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAllParticipantsSelected(@NonNull AllParticipantsSelectedEvent event) {
        gameAnnouncer.announceSelectionCompletion();
    }
}
