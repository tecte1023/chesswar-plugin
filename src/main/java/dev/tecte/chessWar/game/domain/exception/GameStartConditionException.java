package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Notifiable;
import lombok.NonNull;

/**
 * 게임 시작 조건이 충족되지 않았을 때 발생하는 예외입니다.
 */
public class GameStartConditionException extends BusinessException implements Notifiable {
    public GameStartConditionException(@NonNull String message) {
        super(message);
    }

    /**
     * 체스판이 존재하지 않아 게임을 시작할 수 없을 때 이 예외를 생성합니다.
     *
     * @return {@link GameStartConditionException}의 새 인스턴스
     */
    @NonNull
    public static GameStartConditionException forBoardNotExists() {
        return new GameStartConditionException("게임 시작 실패: 체스판이 존재하지 않습니다.");
    }

    /**
     * 팀의 플레이어 수가 부족하여 게임을 시작할 수 없을 때 이 예외를 생성합니다.
     *
     * @param minPlayers 필요한 최소 플레이어 수
     * @return {@link GameStartConditionException}의 새 인스턴스
     */
    @NonNull
    public static GameStartConditionException forTeamsNotReady(int minPlayers) {
        return new GameStartConditionException(
                String.format("게임 시작 실패: 각 팀에 최소 %d명 이상의 플레이어가 필요합니다.", minPlayers)
        );
    }
}
