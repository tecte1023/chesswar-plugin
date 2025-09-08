package dev.tecte.chessWar.infrastructure.persistence;

public interface PersistableState {
    void loadAll();

    void persistCache();
}
