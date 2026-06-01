package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

@RequiredArgsConstructor
public class SelectionState implements GameState {
    private final ChessWar plugin;

    private GameManager gameManager;

    private boolean isSelectionStarted = false;

    @Override
    public GamePhase phase() {
        return GamePhase.PIECE_SELECTION;
    }

    @Override
    public String displayName() {
        return phase().displayName();
    }

    @Override
    public GameState nextState() {
        return new TurnOrderState(plugin);
    }

    @Override
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        this.gameManager = gameManager;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        final int preSelectionCountdown = 3;
        gameManager.startPreSelectionCountdown(preSelectionCountdown);
    }

    @Override
    public void selectPiece(final GameManager gameManager, final Player player, final Coordinate coordinate) {
        gameManager.processPieceSelection(player, coordinate);
    }

    @EventHandler
    public void onEntityInteract(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();

        if (!gameManager.isParticipant(player)) {
            return;
        }

        event.setCancelled(true);
        gameManager.inspectBarracksPiece(player, event.getRightClicked());
    }

    @Override
    public void onTimerTick(final GameManager gameManager, final TimerManager timerManager) {
        if (!isSelectionStarted) {
            gameManager.broadcastSelectionCountdown(timerManager.remainingSeconds() + 1);
        }
    }

    @Override
    public void onTimerExpire(final GameManager gameManager) {
        if (!isSelectionStarted) {
            isSelectionStarted = true;
            gameManager.prepareSelectionContext();
            return;
        }

        gameManager.advancePhase();
    }
}
