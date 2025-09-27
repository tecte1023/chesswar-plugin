package dev.tecte.chessWar.team.application.port;

import dev.tecte.chessWar.team.domain.model.TeamColor;
import lombok.NonNull;

import java.util.UUID;

/**
 * 팀 데이터의 영속성을 관리하는 리포지토리 인터페이스입니다.
 * 팀의 상태를 조회하거나 변경하는 메서드를 정의합니다.
 */
public interface TeamRepository {
    /**
     * 지정된 색상 팀의 현재 인원수를 조회합니다.
     *
     * @param teamColor 조회할 팀의 색상
     * @return 해당 팀의 현재 인원수
     */
    int getSize(@NonNull TeamColor teamColor);

    /**
     * 팀의 최대 허용 인원수를 조회합니다.
     *
     * @return 팀의 최대 인원수
     */
    int getMaxPlayers();

    /**
     * 팀의 최대 허용 인원수를 설정합니다.
     *
     * @param maxPlayers 설정할 최대 인원수
     * @return 실제로 적용된 최대 인원수
     */
    int setMaxPlayers(int maxPlayers);

    /**
     * 플레이어를 지정된 색상의 팀에 추가합니다.
     *
     * @param playerId  팀에 추가할 플레이어의 UUID
     * @param teamColor 플레이어가 추가될 팀의 색상
     */
    void addPlayer(@NonNull UUID playerId, @NonNull TeamColor teamColor);
}
