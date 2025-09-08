package dev.tecte.chessWar.infrastructure.persistence.exception;

public class PersistenceWriteException extends RuntimeException {
    public PersistenceWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
