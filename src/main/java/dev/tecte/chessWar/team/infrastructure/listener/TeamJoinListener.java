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
import java.util.Optional;

/**
 * 플레이어가 특정 양털을 우클릭했을 때 해당 색상의 팀에 참가하도록 하는 리스너입니다.
 */
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamJoinListener implements Listener {
    private static final Map<Material, TeamColor> TEAM_COLOR_BY_ITEM = Map.of(
            Material.WHITE_WOOL, TeamColor.WHITE,
            Material.BLACK_WOOL, TeamColor.BLACK
    );

    private final TeamService teamService;

    /**
     * 플레이어가 상호작용 이벤트를 발생시켰을 때 호출됩니다.
     * 플레이어가 양털을 들고 우클릭하면, 해당 색상의 팀에 참가를 시도합니다.
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

        Optional.ofNullable(TEAM_COLOR_BY_ITEM.get(itemType))
                .ifPresent(selectedTeam -> teamService.joinTeam(player, selectedTeam));
    }
}
