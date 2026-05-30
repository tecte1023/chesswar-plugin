package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class EndedState implements GameState {
    private final ChessWar plugin;

    @Override
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        GameState.super.onEnter(plugin, gameManager);
        
        plugin.timerManager().stopTimer();
        plugin.boardVisualManager().displayStatisticsHologram();
    }

    @Override
    public GameState nextState() {
        return new WaitingState(plugin);
    }

    @Override
    public GamePhase phase() {
        return GamePhase.ENDED;
    }

    @Override
    public String displayName() {
        return "종료";
    }
}
