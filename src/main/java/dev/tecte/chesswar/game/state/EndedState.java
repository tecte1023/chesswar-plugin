package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EndedState implements GameState {
    private final ChessWar plugin;

    @Override
    public GamePhase phase() {
        return GamePhase.ENDED;
    }

    @Override
    public String displayName() {
        return phase().displayName();
    }

    @Override
    public GameState nextState() {
        return new WaitingState(plugin);
    }

    @Override
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        gameManager.prepareEndContext();
    }
}
