package dev.tecte.chessWar.common.exception;

import lombok.NonNull;
import org.slf4j.event.Level;

/**
 * 이 인터페이스를 구현하는 예외는 발생 시 로그를 남겨야 함을 나타내는 인터페이스입니다.
 * <p>
 * {@link #getLogLevel()}을 통해 예외의 심각도에 따른 로그 레벨을 결정할 수 있습니다.
 */
public interface Loggable {
    /**
     * 예외가 기록될 로그 레벨을 반환합니다.
     * 기본값은 {@link Level#ERROR}입니다.
     *
     * @return 로그 레벨
     */
    @NonNull
    default Level getLogLevel() {
        return Level.ERROR;
    }
}
