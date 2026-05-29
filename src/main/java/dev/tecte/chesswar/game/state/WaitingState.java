package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class WaitingState implements GameState {
    
    @Override
    public void onEnter(Plugin plugin, GameManager gameManager) {
        GameState.super.onEnter(plugin, gameManager);
    }

    @Override
    public GameState nextState() {
        return null; // GameManager will provide SelectionState via startStartSequence
    }

    @Override
    public String displayName() {
        return "대기 중";
    }
}
