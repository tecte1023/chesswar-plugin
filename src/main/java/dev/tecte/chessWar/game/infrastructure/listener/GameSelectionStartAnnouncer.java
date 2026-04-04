package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.common.event.ChessWarStartedEvent;
import dev.tecte.chessWar.game.application.GameAnnouncer;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.event.GameSelectionStartedEvent;
import dev.tecte.chessWar.game.domain.model.Game;
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
 * 기물 선택 단계의 시각적 안내를 책임집니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameSelectionStartAnnouncer implements Listener {
    private final GameRepository gameRepository;
    private final GameAnnouncer gameAnnouncer;
    private final UserResolver userResolver;

    /**
     * 기물 선택 단계의 가이드를 복구합니다.
     *
     * @param event 시스템 시작 이벤트
     */
    @EventHandler
    public void recoverSelectionGuidance(@NonNull ChessWarStartedEvent event) {
        Optional<Game> foundGame = gameRepository.find();

        if (foundGame.isEmpty()) {
            return;
        }

        Game game = foundGame.get();

        if (!game.isInSelectionPhase()) {
            return;
        }

        gameAnnouncer.startSelectionGuidance();
    }


    /**
     * 참여자에게 기물 선택 단계의 시작을 알리고 가이드를 안내합니다.
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
