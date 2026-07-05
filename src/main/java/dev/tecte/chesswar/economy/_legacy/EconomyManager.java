/*
package dev.tecte.chesswar.economy;

import dev.tecte.chesswar.game.GameContext;
import dev.tecte.chesswar.game.Participant;
import dev.tecte.chesswar.game.GameAnnouncer;
import dev.tecte.chesswar.game.ScoreboardManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

*/
/**
 * [관리자 및 시스템] 재화의 변동을 통제하는 시스템.
 *//*

@RequiredArgsConstructor
public class EconomyManager {
    private final GameContext gameContext;
    private final EconomyState economyState;
    private final GameAnnouncer announcer;
    private final ScoreboardManager scoreboardManager;

    */
/**
     * 특정 플레이어에게 골드를 지급합니다.
     *//*

    public void addGold(final UUID playerId, final int amount, final GoldSource source) {
        final GoldComponent gold = economyState.getPlayerGold(playerId);
        gold.add(amount);

        final Participant participant = gameContext.participant(playerId);
        if (participant != null) {
            participant.statistics().addGoldEarned(amount);
        }

        final Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            announcer.announceGoldEarned(player, amount, source.description());
        }

        scoreboardManager.updateAll();
    }

    */
/**
     * 골드를 차감합니다. 잔액이 부족하면 false를 반환합니다.
     *//*

    public boolean spendGold(final UUID playerId, final int amount) {
        final GoldComponent gold = economyState.getPlayerGold(playerId);
        if (!gold.subtract(amount)) {
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                announcer.announceInsufficientGold(player);
            }
            return false;
        }

        final Participant participant = gameContext.participant(playerId);
        if (participant != null) {
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

    */
/**
     * 해당 팀원 전원에게 월급을 지급합니다.
     *//*

    public void distributeSalary(final Team team) {
        for (final Participant p : gameContext.participantsValues()) {
            if (p.team() == team) {
                final int amount = economyState.getPlayerGold(p.playerId()).baseIncome();
                addGold(p.playerId(), amount, GoldSource.SALARY);
            }
        }
    }

    */
/**
     * 경제 상태를 초기화합니다.
     *//*

    public void reset() {
        economyState.reset();
    }
}
*/
