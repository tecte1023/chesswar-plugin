package dev.tecte.chessWar.infrastructure.persistence.exception;

import dev.tecte.chessWar.common.exception.Loggable;
import lombok.NonNull;

/**
 * 영속성 계층에서 파일 쓰기 작업 중에 오류가 발생했을 때 던져지는 예외입니다.
 */
public class PersistenceWriteException extends RuntimeException implements Loggable {
    public PersistenceWriteException(@NonNull String fileName, @NonNull Throwable cause) {
        super("Could not save to " + fileName, cause);
    }
}
