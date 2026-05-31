package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.manager.GameManager;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * 게임의 각 단계를 정의하는 상태 인터페이스.
 * 각 상태는 필요한 이벤트를 직접 수신하며, 상태 전환 시 리스너를 동적으로 등록/해제함.
 */
public interface GameState extends Listener {
    
    /**
     * 상태에 진입할 때 실행되는 로직.
     * @param plugin 플러그인 인스턴스 (리스너 등록용)
     * @param gameManager 게임 매니저 참조
     */
    default void onEnter(dev.tecte.chesswar.ChessWar plugin, GameManager gameManager) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 현재 상태를 빠져나갈 때 실행되는 로직.
     */
    default void onExit() {
        HandlerList.unregisterAll(this);
    }

    /**
     * 다음 게임 단계를 결정하여 반환함.
     * @return 다음 게임 상태 객체
     */
    GameState nextState();

    /**
     * 현재 상태에 매핑되는 GamePhase를 반환함.
     */
    GamePhase phase();

    /**
     * 현재 상태의 이름을 반환 (디버깅 및 UI용).
     */
    String displayName();

    /**
     * 플레이어의 준비 완료 요청을 처리함.
     */
    default void handleReadyUp(GameManager gameManager, org.bukkit.entity.Player player, org.bukkit.Location location) {
        player.sendMessage(net.kyori.adventure.text.Component.text("지금은 준비를 완료할 수 있는 시간이 아닙니다!", net.kyori.adventure.text.format.NamedTextColor.RED));
    }

    /**
     * 플레이어의 기물 선택 요청을 처리함.
     */
    default void selectPiece(GameManager gameManager, org.bukkit.entity.Player player, dev.tecte.chesswar.board.Coordinate coordinate) {
        player.sendMessage(net.kyori.adventure.text.Component.text("지금은 기물을 선택할 수 있는 시간이 아닙니다!", net.kyori.adventure.text.format.NamedTextColor.RED));
    }

    /**
     * 다음 턴으로 넘어가는 로직을 처리함.
     */
    default void nextTurn(final GameManager gameManager) {
        // 기본적으로 아무것도 하지 않음
    }

    /**
     * 플레이어의 턴이 시작되었을 때 실행되는 로직.
     */
    default void onTurnStart(final GameManager gameManager, final org.bukkit.entity.Player player) {
        // 기본적으로 아무것도 하지 않음
    }

    /**
     * 타이머가 1초마다 틱될 때 실행되는 로직.
     */
    default void onTimerTick(final GameManager gameManager, final dev.tecte.chesswar.game.manager.TimerManager timerManager) {
        // 기본적으로 아무것도 하지 않음
    }

    /**
     * 타이머가 만료되었을 때 실행되는 로직.
     */
    default void onTimerExpire(final GameManager gameManager) {
        gameManager.advancePhase();
    }
}
