package dev.tecte.chesswar.game.listener;

import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.piece.PieceItemUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {
    private final GameManager gameManager;

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        
        // 게임 단계가 WAITING일 때만 초기화 (Lazy Reset)
        if (gameManager.phase() == GamePhase.WAITING) {
            // 인벤토리 초기화
            gameManager.clearOrderItems(player);
            PieceItemUtils.removePlayerPieceItems(player);
            
            // 게임모드 복구 (기본 서바이벌)
            player.setGameMode(GameMode.SURVIVAL);
            
            // 스탯 복구 (기본값)
            final AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(20.0);
                player.setHealth(20.0);
            }
            
            final AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.setBaseValue(2.0); // 마인크래프트 기본 주먹 데미지
            }
            
            player.setFoodLevel(20);
            player.setSaturation(5.0f);
        }
    }
}
