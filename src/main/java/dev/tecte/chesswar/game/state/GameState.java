package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.board.Coordinate;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface GameState extends Listener {
    Component ERROR_NOT_READY_PHASE = Component.text(
            "지금은 준비를 완료할 수 있는 시간이 아닙니다!",
            NamedTextColor.RED
    );
    Component ERROR_NOT_SELECTION_PHASE = Component.text(
            "지금은 기물을 선택할 수 있는 시간이 아닙니다!",
            NamedTextColor.RED
    );

    GamePhase phase();

    String displayName();

    GameState nextState();

    void onEnter(final ChessWar plugin, final GameManager gameManager);

    default void onExit() {
        HandlerList.unregisterAll(this);
    }

    default void handleReadyUp(
            final GameManager gameManager,
            final Player player,
            final Location location
    ) {
        player.sendMessage(ERROR_NOT_READY_PHASE);
    }

    default void selectPiece(
            final GameManager gameManager,
            final Player player,
            final Coordinate coordinate
    ) {
        player.sendMessage(ERROR_NOT_SELECTION_PHASE);
    }

    default void onTurnStart(final GameManager gameManager, final Player player) {
    }

    default void nextTurn(final GameManager gameManager) {
    }

    default void onTimerTick(final GameManager gameManager, final TimerManager timerManager) {
    }

    default void onTimerExpire(final GameManager gameManager) {
        gameManager.advancePhase();
    }
}
