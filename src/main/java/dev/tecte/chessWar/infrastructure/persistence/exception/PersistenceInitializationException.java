package dev.tecte.chessWar.infrastructure.persistence.exception;

/**
 * 영속성 계층 초기화 중에 오류가 발생했을 때 던져지는 예외입니다.
 */
public class PersistenceInitializationException extends RuntimeException {
    public PersistenceInitializationException(String message) {
        super(message);
    }

    public PersistenceInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
