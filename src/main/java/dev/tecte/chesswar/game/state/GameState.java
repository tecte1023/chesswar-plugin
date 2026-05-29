package dev.tecte.chesswar.game.state;

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
    default void onEnter(Plugin plugin, GameManager gameManager) {
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
     * 현재 상태의 이름을 반환 (디버깅 및 UI용).
     */
    String displayName();
}
