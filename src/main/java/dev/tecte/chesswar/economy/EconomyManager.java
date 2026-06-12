package dev.tecte.chesswar.economy;

import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.game.manager.ScoreboardManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * [관리자 및 시스템] 재화의 변동을 통제하는 시스템.
 */
@RequiredArgsConstructor
public class EconomyManager {
    private final GameContext gameContext;
    private final EconomyState economyState;
    private final GameAnnouncer announcer;
    private final ScoreboardManager scoreboardManager;

    /**
     * 특정 플레이어에게 골드를 지급합니다.
     */
    public void addGold(final UUID playerId, final int amount, final GoldSource source) {
        final GoldComponent gold = economyState.getPlayerGold(playerId);
        gold.add(amount);

        // 기존 Participant 객체와 동기화 (레거시 호환성 및 스코어보드 출력용)
        final Participant participant = gameContext.participants().get(playerId);
        if (participant != null) {
            participant.gold(gold.currentGold());
            participant.statistics().addGoldEarned(amount);
        }

        final Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            announcer.announceGoldEarned(player, amount, source.description());
        }

        scoreboardManager.updateAll();
    }

    /**
     * 골드를 차감합니다. 잔액이 부족하면 false를 반환합니다.
     */
    public boolean spendGold(final UUID playerId, final int amount) {
        final GoldComponent gold = economyState.getPlayerGold(playerId);
        if (!gold.subtract(amount)) {
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                announcer.announceInsufficientGold(player);
            }
            return false;
        }

        // 기존 Participant 객체와 동기화
        final Participant participant = gameContext.participants().get(playerId);
        if (participant != null) {
            participant.gold(gold.currentGold());
            participant.statistics().addGoldSpent(amount);
        }

        final Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            announcer.announceGoldSpent(player);
        }

        scoreboardManager.updateAll();
        return true;
    }

    public boolean hasGold(final UUID playerId, final int amount) {
        return economyState.getPlayerGold(playerId).has(amount);
    }

    /**
     * 해당 팀원 전원에게 월급을 지급합니다.
     */
    public void distributeSalary(final Team team) {
        for (final Participant p : gameContext.participants().values()) {
            if (p.team() == team) {
                final int amount = economyState.getPlayerGold(p.playerId()).baseIncome();
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
