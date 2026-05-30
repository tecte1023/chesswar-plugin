package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class WaitingState implements GameState {
    private final ChessWar plugin;
    
    @Override
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        GameState.super.onEnter(plugin, gameManager);
    }

    @Override
    public GameState nextState() {
        return new SelectionState(plugin);
    }

    @Override
    public GamePhase phase() {
        return GamePhase.WAITING;
    }

    @Override
    public String displayName() {
        return "대기 중";
    }
}
