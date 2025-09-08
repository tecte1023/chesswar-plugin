package dev.tecte.chessWar.infrastructure.persistence.exception;

public class PersistenceInitializationException extends RuntimeException {
    public PersistenceInitializationException(String message) {
        super(message);
    }

    public PersistenceInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
