package dev.tecte.chessWar.infrastructure.persistence.exception;

/**
 * 영속성 계층에서 파일 쓰기 작업 중에 오류가 발생했을 때 던져지는 예외입니다.
 */
public class PersistenceWriteException extends RuntimeException {
    public PersistenceWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
