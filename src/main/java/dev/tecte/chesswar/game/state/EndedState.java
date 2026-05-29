package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class EndedState implements GameState {
    private final ChessWar plugin;

    @Override
    public void onEnter(Plugin plugin, GameManager gameManager) {
        GameState.super.onEnter(plugin, gameManager);
        
        this.plugin.timerManager().stopTimer();
        this.plugin.boardVisualManager().displayStatisticsHologram();
    }

    @Override
    public GameState nextState() {
        return new WaitingState();
    }

    @Override
    public String displayName() {
        return "게임 종료";
    }
}
