package dev.tecte.chesswar.economy;

import dev.tecte.chesswar.team.Team;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * [순수 데이터 객체] 게임 세션 내 모든 경제 데이터의 중앙 저장소.
 */
@Getter
@NoArgsConstructor(staticName = "create")
public class EconomyState {
    private final Map<UUID, GoldComponent> playerGold = new HashMap<>();
    private final Map<Team, GoldComponent> teamGold = new HashMap<>();

    public GoldComponent getPlayerGold(UUID playerId) {
        return playerGold.computeIfAbsent(playerId, k -> GoldComponent.createDefault());
    }

    public GoldComponent getTeamGold(Team team) {
        return teamGold.computeIfAbsent(team, k -> GoldComponent.of(0, 0));
    }

    public void reset() {
        playerGold.clear();
        teamGold.clear();
    }
}
