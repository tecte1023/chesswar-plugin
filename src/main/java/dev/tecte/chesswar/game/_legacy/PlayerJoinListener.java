/*
package dev.tecte.chesswar.game;

import dev.tecte.chesswar.piece.PieceItemUtils;
import lombok.RequiredArgsConstructor;
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
        
        // 게임 단계가 WAITING일 때만 지연 초기화 수행
        if (gameManager.phase() == GamePhase.WAITING) {
            final Participant participant = gameManager.context().participant(player.getUniqueId());
            
            // 기존 참가자 정보가 있고 저장된 원래 상태가 있는 경우에만 복구 (Lazy Reset)
            if (participant != null && participant.originalGameMode() != null) {
                gameManager.clearOrderItems(player);
                PieceItemUtils.removePlayerPieceItems(player);
                
                // 저장된 원래 게임 모드 복구
                player.setGameMode(participant.originalGameMode());
                
                // 저장된 원래 스탯 복구
                final AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null && participant.originalHealth() != null) {
                    maxHealth.setBaseValue(participant.originalHealth());
                    player.setHealth(Math.min(player.getHealth(), participant.originalHealth()));
                }
                
                final AttributeInstance attackDamage = player.getAttribute(Attribute.ATTACK_DAMAGE);
                if (attackDamage != null && participant.originalAttackDamage() != null) {
                    attackDamage.setBaseValue(participant.originalAttackDamage());
                }
                
                player.setFoodLevel(20);
                player.setSaturation(5.0f);
            }
        }
    }
}
*/
