package dev.tecte.chessWar.team.application.port;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 팀의 영속성을 관리합니다.
 */
public interface TeamRepository {
    /**
     * 지정된 색상 팀의 현재 인원수를 확인합니다.
     *
     * @param teamColor 확인할 팀의 색상
     * @return 현재 인원수
     */
    int getSize(@NonNull TeamColor teamColor);

    /**
     * 팀의 최대 허용 인원수를 확인합니다.
     *
     * @return 최대 인원수
     */
    int getMaxPlayers();

    /**
     * 팀의 최대 허용 인원수를 설정합니다.
     *
     * @param maxPlayers 설정할 최대 인원수
     * @return 적용된 최대 인원수
     */
    int setMaxPlayers(int maxPlayers);

    /**
     * 플레이어를 지정된 색상의 팀에 추가합니다.
     *
     * @param playerId  추가할 플레이어의 ID
     * @param teamColor 추가할 팀의 색상
     */
    void addPlayer(@NonNull UUID playerId, @NonNull TeamColor teamColor);

    /**
     * 플레이어를 팀에서 제거합니다.
     *
     * @param playerId 제거할 플레이어의 ID
     * @return 제거 성공 여부
     */
    boolean removePlayer(@NonNull UUID playerId);

    /**
     * 지정된 색상 팀에 속한 모든 플레이어의 ID를 찾습니다.
     *
     * @param teamColor 찾을 팀의 색상
     * @return 소속된 모든 플레이어의 ID
     */
    @NonNull
    Set<UUID> getPlayerUUIDs(@NonNull TeamColor teamColor);

    /**
     * 플레이어가 속한 팀을 찾습니다.
     *
     * @param playerId 찾을 플레이어의 ID
     * @return 찾은 팀의 색상
     */
    @NonNull
    Optional<TeamColor> findTeam(@NonNull UUID playerId);
}
