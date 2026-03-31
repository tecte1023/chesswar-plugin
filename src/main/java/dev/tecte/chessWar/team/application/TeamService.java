package dev.tecte.chessWar.team.application;

import dev.tecte.chessWar.port.UserResolver;
import dev.tecte.chessWar.team.application.port.TeamRepository;
import dev.tecte.chessWar.team.domain.exception.TeamException;
import dev.tecte.chessWar.team.domain.model.TeamColor;
import dev.tecte.chessWar.team.domain.policy.TeamCapacityPolicy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 팀 가입, 탈퇴 및 정보 조회를 관리합니다.
 */
@Slf4j(topic = "ChessWar")
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TeamService {
    private final TeamCapacityPolicy teamCapacityPolicy;
    private final TeamRepository teamRepository;
    private final UserResolver userResolver;
    private final TeamNotifier teamNotifier;

    /**
     * 모든 참여자 정보를 제공합니다.
     *
     * @return 참여자 정보 맵
     */
    @NonNull
    public Map<UUID, TeamColor> findAllParticipants() {
        Map<UUID, TeamColor> participants = new HashMap<>();

        for (TeamColor color : TeamColor.values()) {
            teamRepository.findAllPlayerIds(color).forEach(id -> participants.put(id, color));
        }

        return Map.copyOf(participants);
    }

    /**
     * 플레이어 소속 팀을 찾습니다.
     */
    @NonNull
    public Optional<TeamColor> findTeam(@NonNull Player player) {
        return teamRepository.findTeam(player.getUniqueId());
    }

    /**
     * 팀 소속 플레이어를 찾습니다.
     */
    @NonNull
    public Set<Player> findPlayers(@NonNull TeamColor teamColor) {
        return teamRepository.findAllPlayerIds(teamColor).stream()
                .map(userResolver::findPlayer)
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 최대 인원수를 제공합니다.
     *
     * @return 현재 최대 인원수
     */
    public int getMaxCapacity() {
        int storedCapacity = teamRepository.findMaxCapacity().orElseGet(() -> {
            int defaultCapacity = teamCapacityPolicy.defaultCapacity();

            teamRepository.saveMaxCapacity(defaultCapacity);

            return defaultCapacity;
        });
        int adjustedCapacity = teamCapacityPolicy.apply(storedCapacity);

        if (storedCapacity != adjustedCapacity) {
            teamRepository.saveMaxCapacity(adjustedCapacity);
        }

        return adjustedCapacity;
    }

    /**
     * 최소 인원수 충족 여부를 확인합니다.
     *
     * @throws TeamException 플레이어 수 부족 시
     */
    public void ensureMinimumCapacityMet() {
        for (TeamColor team : TeamColor.values()) {
            if (teamCapacityPolicy.isLacking(teamRepository.countPlayers(team))) {
                throw TeamException.minimumCapacityNotMet(teamCapacityPolicy.minCapacity());
            }
        }
    }

    /**
     * 플레이어를 팀에 참가시킵니다.
     *
     * @param player    참가할 플레이어
     * @param teamColor 참가할 팀
     */
    public void joinTeam(@NonNull Player player, @NonNull TeamColor teamColor) {
        if (teamCapacityPolicy.isFull(teamRepository.countPlayers(teamColor))) {
            throw TeamException.capacityExceeded(teamColor);
        }

        teamRepository.addPlayer(player.getUniqueId(), teamColor);
        teamNotifier.informJoin(player, teamColor);
    }

    /**
     * 플레이어를 팀에서 제외합니다.
     *
     * @param player 제외할 플레이어
     */
    public void leaveTeam(@NonNull Player player) {
        if (!teamRepository.removePlayer(player.getUniqueId())) {
            throw TeamException.notInTeam();
        }

        teamNotifier.informLeave(player);
    }
}
