package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.component.TurnStartedEvent;
import dev.tecte.chesswar.game.manager.GameManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class BattleState implements GameState {
    private final ChessWar plugin;

    @Override
    public void onEnter(Plugin plugin, GameManager gameManager) {
        GameState.super.onEnter(plugin, gameManager);
        
        this.plugin.pieceManager().clearSpawnedEntities(true);
        this.plugin.boardManager().clearBarracksChests();
        gameManager.calculateTurnOrder();
        
        gameManager.participants().values().forEach(p -> {
            Player onlinePlayer = Bukkit.getPlayer(p.playerId());
            if (onlinePlayer != null && p.initialCoordinate() != null) {
                this.plugin.boardManager().deployToBattlefield(p.team(), p.initialCoordinate(), onlinePlayer);
            }
        });

        this.plugin.timerManager().startTurnTimer(gameManager.timerSettings().battleTurnTime());
        gameManager.currentTurnPlayer().ifPresent(uuid -> {
            Player firstPlayer = Bukkit.getPlayer(uuid);
            if (firstPlayer != null) {
                gameManager.updateInvulnerability(firstPlayer);
                Bukkit.getPluginManager().callEvent(new TurnStartedEvent(firstPlayer));
            }
        });
    }

    @Override
    public GameState nextState() {
        return new EndedState(plugin);
    }

    @Override
    public String displayName() {
        return "전투";
    }
}
