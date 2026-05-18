package dev.tecte.chessWar.team.infrastructure.listener;

import dev.tecte.chessWar.game.domain.event.GameParticipantJoinedEvent;
import dev.tecte.chessWar.game.domain.event.PiecesSpawnedEvent;
import dev.tecte.chessWar.port.UserResolver;
import dev.tecte.chessWar.team.application.TeamPhysicalApplier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * 팀 간 플레이어 정보 노출을 제어합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamVisibilityListener implements Listener {
    private final TeamPhysicalApplier teamPhysicalApplier;
    private final UserResolver userResolver;

    /**
     * 상대 팀 플레이어들의 위치 노출을 물리적으로 차단합니다.
     *
     * @param event 기물 소환 완료 이벤트
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPiecesSpawned(@NonNull PiecesSpawnedEvent event) {
        teamPhysicalApplier.concealEnemies();
    }

    /**
     * 신규 참여자에게 상대 팀의 위치 정보가 노출되지 않도록 처리합니다.
     *
     * @param event 참여자 합류 이벤트
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onParticipantJoined(@NonNull GameParticipantJoinedEvent event) {
        Optional<Player> foundPlayer = userResolver.findPlayer(event.playerId());

        if (foundPlayer.isEmpty()) {
            return;
        }

        Player player = foundPlayer.get();
        
        teamPhysicalApplier.concealEnemiesFor(player, event.playerTeam());
    }
}
