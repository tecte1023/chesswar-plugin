package dev.tecte.chessWar.common.exception;

import lombok.NonNull;
import org.slf4j.event.Level;

/**
 * 로그 기록이 필요한 예외입니다.
 */
public interface Loggable {
    /**
     * 예외가 기록될 로그 레벨을 반환합니다.
     * 기본값은 ERROR입니다.
     *
     * @return 로그 레벨
     */
    @NonNull
    default Level logLevel() {
        return Level.ERROR;
    }
}
