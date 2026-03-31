package dev.tecte.chessWar.team.application.port;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

/**
 * 팀의 영속성을 관리합니다.
 */
public interface TeamRepository {
    /**
     * 최대 인원수를 찾습니다.
     *
     * @return 최대 인원수
     */
    OptionalInt findMaxCapacity();

    /**
     * 최대 인원수를 저장합니다.
     *
     * @param capacity 최대 인원수
     */
    void saveMaxCapacity(int capacity);

    /**
     * 플레이어를 팀에 추가합니다.
     *
     * @param playerId  추가할 플레이어 ID
     * @param teamColor 추가할 팀
     */
    void addPlayer(@NonNull UUID playerId, @NonNull TeamColor teamColor);

    /**
     * 플레이어를 팀에서 제거합니다.
     *
     * @param playerId 제거할 플레이어 ID
     * @return 제거 성공 여부
     */
    boolean removePlayer(@NonNull UUID playerId);

    /**
     * 플레이어가 소속 팀을 찾습니다.
     *
     * @param playerId 찾을 플레이어 ID
     * @return 찾은 팀
     */
    @NonNull
    Optional<TeamColor> findTeam(@NonNull UUID playerId);

    /**
     * 팀 인원수를 확인합니다.
     *
     * @param teamColor 대상 팀
     * @return 인원수
     */
    int countPlayers(@NonNull TeamColor teamColor);

    /**
     * 팀 소속 플레이어 ID를 제공합니다.
     *
     * @param teamColor 대상 팀
     * @return 팀에 속한 플레이어 ID
     */
    @NonNull
    Set<UUID> findAllPlayerIds(@NonNull TeamColor teamColor);
}
