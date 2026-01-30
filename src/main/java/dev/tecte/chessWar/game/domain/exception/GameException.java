package dev.tecte.chessWar.game.domain.exception;

import dev.tecte.chessWar.common.exception.BusinessException;
import dev.tecte.chessWar.game.domain.model.GamePhase;
import lombok.NonNull;

/**
 * 게임 진행을 위한 전제 조건이나 규칙 위반 시 발생하는 비즈니스 예외입니다.
 * <p>
 * 이 예외는 로그를 남기지 않고, 사용자에게 알림 메시지만 전달합니다.
 */
public class GameException extends BusinessException {
    private GameException(@NonNull String message) {
        super(message);
    }

    /**
     * 진행 중인 게임이 없을 때 발생합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static GameException notFound() {
        return new GameException("진행 중인 게임이 없습니다.");
    }

    /**
     * 이미 진행 중인 게임이 있을 때 발생합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static GameException alreadyInProgress() {
        return new GameException("게임이 이미 진행 중입니다.");
    }

    /**
     * 체스판이 존재하지 않아 게임을 시작할 수 없을 때 발생합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static GameException boardNotSetup() {
        return new GameException("체스판이 존재하지 않습니다. 체스판을 먼저 생성해 주세요.");
    }

    /**
     * 게임을 시작할 월드 정보를 찾을 수 없을 때 발생합니다.
     *
     * @param worldName 찾을 수 없는 월드의 이름
     * @return 생성된 예외
     */
    @NonNull
    public static GameException worldNotFound(@NonNull String worldName) {
        return new GameException(String.format("게임을 시작할 월드 '%s'을(를) 찾을 수 없습니다.", worldName));
    }

    /**
     * 팀의 플레이어 수가 부족하여 게임을 시작할 수 없을 때 발생합니다.
     *
     * @param minPlayers 필요한 최소 플레이어 수
     * @return 생성된 예외
     */
    @NonNull
    public static GameException insufficientPlayers(int minPlayers) {
        return new GameException(String.format("각 팀에 최소 %d명 이상의 플레이어가 필요합니다.", minPlayers));
    }

    /**
     * 현재 게임 단계가 요구되는 단계와 일치하지 않아 작업을 수행할 수 없을 때 발생합니다.
     *
     * @param required 필요한 단계
     * @param current  현재 단계
     * @return 생성된 예외
     */
    @NonNull
    public static GameException phaseMismatch(@NonNull GamePhase required, @NonNull GamePhase current) {
        return new GameException(String.format(
                "해당 작업은 '%s' 단계에서만 가능합니다. (현재 단계: %s)",
                required.displayName(),
                current.displayName()
        ));
    }

    /**
     * 게임에 포함되지 않은 기물일 때 발생합니다.
     *
     * @return 생성된 예외
     */
    @NonNull
    public static GameException pieceNotFound() {
        return new GameException("게임에 포함되지 않은 기물입니다.");
    }
}
