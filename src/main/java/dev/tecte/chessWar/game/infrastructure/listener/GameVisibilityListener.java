package dev.tecte.chessWar.game.infrastructure.listener;

import dev.tecte.chessWar.game.application.PieceService;
import dev.tecte.chessWar.game.application.port.GameRepository;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import dev.tecte.chessWar.team.application.TeamService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 게임 진행 상태에 따라 플레이어의 시야(Visibility)를 제어하는 리스너입니다.
 * 플레이어가 게임에 난입하거나 재접속했을 때 적절한 시야 상태를 복구합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GameVisibilityListener implements Listener {
    private final TeamService teamService;
    private final PieceService pieceService;
    private final GameRepository gameRepository;

    /**
     * 플레이어 접속 시 현재 게임 단계에 맞춰 시야를 업데이트합니다.
     * 직업 선택 단계(CLASS_SELECTION)인 경우, 적 팀 플레이어와 기물을 숨깁니다.
     *
     * @param event 플레이어 접속 이벤트
     */
    @EventHandler
    public void onPlayerJoin(@NonNull PlayerJoinEvent event) {
        gameRepository.find().ifPresent(game -> {
            if (game.phase() != GamePhase.CLASS_SELECTION) {
                return;
            }

            Player joinedPlayer = event.getPlayer();

            teamService.findTeam(joinedPlayer).ifPresent(joinedPlayerTeam -> {
                teamService.concealEnemiesFor(joinedPlayer, joinedPlayerTeam);
                pieceService.concealPiecesFor(game, joinedPlayer, joinedPlayerTeam);
            });
        });
    }
}

