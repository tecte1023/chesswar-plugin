package dev.tecte.chessWar.team.infrastructure.listener;

import dev.tecte.chessWar.team.application.TeamService;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

/**
 * 팀 선택 상호작용의 비즈니스 처리를 개시합니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamJoinInitiator implements Listener {
    private static final Map<Material, TeamColor> TEAM_COLOR_BY_ITEM = Map.of(
            Material.WHITE_WOOL, TeamColor.WHITE,
            Material.BLACK_WOOL, TeamColor.BLACK
    );

    private final TeamService teamService;

    /**
     * 선택한 아이템의 속성에 맞춰 플레이어의 소속 팀을 결정합니다.
     *
     * @param event 플레이어 상호작용 이벤트
     */
    @EventHandler
    public void onPlayerInteract(@NonNull PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Material itemType = player.getInventory().getItemInMainHand().getType();
        TeamColor teamColor = TEAM_COLOR_BY_ITEM.get(itemType);

        if (teamColor == null) {
            return;
        }

        teamService.joinTeam(player, teamColor);
    }
}
