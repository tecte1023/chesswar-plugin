package dev.tecte.chesswar.economy;

import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * [관리자 및 시스템] 재화의 변동을 통제하는 시스템.
 */
@RequiredArgsConstructor
public class EconomyManager {
    private final GameContext gameContext;
    private final EconomyState economyState;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * 특정 플레이어에게 골드를 지급합니다.
     */
    public void addGold(UUID playerId, int amount, GoldSource source) {
        GoldComponent gold = economyState.getPlayerGold(playerId);
        gold.add(amount);

        // 기존 Participant 객체와 동기화 (레거시 호환성 및 스코어보드 출력용)
        Participant participant = gameContext.participants().get(playerId);
        if (participant != null) {
            participant.gold(gold.currentGold());
            participant.statistics().addGoldEarned(amount);
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            // Engine-Native Feedback
            String message = String.format("<gold>+ %d Gold</gold> <gray>(%s)</gray>", amount, source.description());
            player.sendActionBar(miniMessage.deserialize(message));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }

    /**
     * 골드를 차감합니다. 잔액이 부족하면 false를 반환합니다.
     */
    public boolean spendGold(UUID playerId, int amount) {
        GoldComponent gold = economyState.getPlayerGold(playerId);
        if (!gold.subtract(amount)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(miniMessage.deserialize("<red>골드가 부족합니다!</red>"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            }
            return false;
        }

        // 기존 Participant 객체와 동기화
        Participant participant = gameContext.participants().get(playerId);
        if (participant != null) {
            participant.gold(gold.currentGold());
            participant.statistics().addGoldSpent(amount);
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.2f);
        }
        return true;
    }

    /**
     * 해당 팀원 전원에게 월급을 지급합니다.
     */
    public void distributeSalary(Team team) {
        for (Participant p : gameContext.participants().values()) {
            if (p.team() == team) {
                int amount = economyState.getPlayerGold(p.playerId()).baseIncome();
                addGold(p.playerId(), amount, GoldSource.SALARY);
            }
        }
    }

    /**
     * 경제 상태를 초기화합니다.
     */
    public void reset() {
        economyState.reset();
    }
}
