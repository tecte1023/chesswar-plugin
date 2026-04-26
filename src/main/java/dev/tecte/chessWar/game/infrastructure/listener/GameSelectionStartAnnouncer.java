package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.common.event.ChessWarStartedEvent;
import dev.tecte.chessWar.game.application.GameAnnouncer;
import dev.tecte.chessWar.game.domain.event.GameParticipantJoinedEvent;
import dev.tecte.chessWar.game.domain.event.GameSelectionStartedEvent;
import dev.tecte.chessWar.port.UserResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 기물 선택 가이드를 담당합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameSelectionStartAnnouncer implements Listener {
    private final GameAnnouncer gameAnnouncer;
    private final UserResolver userResolver;

    /**
     * 기물 선택 가이드를 재개합니다.
     *
     * @param event 시스템 시작 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void recoverGuidance(@NonNull ChessWarStartedEvent event) {
        gameAnnouncer.restoreSelectionGuidance();
    }

    /**
     * 참가자에게 기물 선택 가이드를 즉시 새로고침합니다.
     *
     * @param event 참가자 접속 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void recoverGuidance(@NonNull GameParticipantJoinedEvent event) {
        Optional<Player> foundPlayer = userResolver.findPlayer(event.playerId());

        if (foundPlayer.isEmpty()) {
            return;
        }

        gameAnnouncer.refreshSelectionStatus(foundPlayer.get());
    }

    /**
     * 기물 선택 시작을 알리고 가이드를 시작합니다.
     *
     * @param event 기물 선택 시작 이벤트
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void announceSelectionStart(@NonNull GameSelectionStartedEvent event) {
        Set<Player> targets = event.participants().keySet().stream()
                .map(userResolver::findPlayer)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        gameAnnouncer.announceSelectionStart(targets);
        gameAnnouncer.startSelectionGuidance();
    }
}
