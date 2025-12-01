package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.common.exception.Notifiable;
import lombok.NonNull;

/**
 * 게임 시작 조건이 충족되지 않았을 때 발생하는 예외입니다.
 */
public class GameStartConditionException extends BusinessException implements Notifiable {
    private GameStartConditionException(@NonNull String message) {
        super(message);
    }

    /**
     * 체스판이 존재하지 않아 게임을 시작할 수 없을 때 이 예외를 생성합니다.
     *
     * @return {@link GameStartConditionException}의 새 인스턴스
     */
    @NonNull
    public static GameStartConditionException forBoardNotExists() {
        return new GameStartConditionException("체스판이 존재하지 않습니다. 체스판을 먼저 생성해 주세요.");
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
                String.format("각 팀에 최소 %d명 이상의 플레이어가 필요합니다.", minPlayers)
        );
    }

    /**
     * 체스판의 월드를 찾을 수 없을 때 이 예외를 생성합니다.
     *
     * @param worldName 찾을 수 없는 월드의 이름
     * @return {@link GameStartConditionException}의 새 인스턴스
     */
    @NonNull
    public static GameStartConditionException forWorldNotFound(@NonNull String worldName) {
        return new GameStartConditionException(String.format("게임을 시작할 월드 '%s'을(를) 찾을 수 없습니다.", worldName));
    }

    /**
     * 게임이 이미 진행 중이거나 시작할 수 없는 단계에 있을 때 이 예외를 생성합니다.
     *
     * @return {@link GameStartConditionException}의 새 인스턴스
     */
    @NonNull
    public static GameStartConditionException forGameAlreadyInProgress() {
        return new GameStartConditionException("게임이 이미 진행 중입니다.");
    }
}
