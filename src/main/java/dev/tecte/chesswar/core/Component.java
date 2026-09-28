package dev.tecte.chesswar.core;

public interface Component {
    void onAttach(final Entity entity);

    Entity getEntity();
}
