package dev.tecte.chessWar.piece.infrastructure.listener;

import dev.tecte.chessWar.game.domain.event.GameParticipantJoinedEvent;
import dev.tecte.chessWar.game.domain.event.PiecesSpawnedEvent;
import dev.tecte.chessWar.piece.application.PieceService;
import dev.tecte.chessWar.port.UserResolver;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 전장 기물의 전략적 노출 여부를 제어합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PieceVisibilityListener implements Listener {
    private final PieceService pieceService;
    private final UserResolver userResolver;

    /**
     * 적 팀 기물의 위치 정보를 차단하여 정보 비대칭을 유지합니다.
     *
     * @param event 기물 소환 완료 이벤트
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPiecesSpawned(@NonNull PiecesSpawnedEvent event) {
        Map<Player, TeamColor> participantPlayers = new HashMap<>();

        event.participants().forEach((uuid, team) ->
                userResolver.findPlayer(uuid).ifPresent(player -> participantPlayers.put(player, team))
        );

        pieceService.updateVisibilityFor(participantPlayers, event.unitPlacements().values());
    }

    /**
     * 합류한 참여자의 시야에서 적 팀 기물을 차단합니다.
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
        
        pieceService.updateVisibilityFor(
                player,
                event.playerTeam(),
                event.unitPlacements().values()
        );
    }
}
