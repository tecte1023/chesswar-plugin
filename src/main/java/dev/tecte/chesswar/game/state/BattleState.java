package dev.tecte.chesswar.game.state;

import dev.tecte.chesswar.ChessWar;
import dev.tecte.chesswar.game.component.GamePhase;
import dev.tecte.chesswar.game.component.Participant;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class BattleState implements GameState {
    private final ChessWar plugin;

    @Override
    public GamePhase phase() {
        return GamePhase.BATTLE;
    }

    @Override
    public String displayName() {
        return phase().displayName();
    }

    @Override
    public GameState nextState() {
        return new EndedState(plugin);
    }

    @Override
    public void onEnter(final ChessWar plugin, final GameManager gameManager) {
        gameManager.prepareBattleContext();
    }

    @Override
    public void onTurnStart(final GameManager gameManager, final Player player) {
        final Optional<Participant> participant = gameManager.findParticipant(player.getUniqueId());

        if (participant.isEmpty()) {
            return;
        }

        final Team team = participant.get().team();
        final int teamTime = plugin.timerManager().teamTime(team);
        final int minTime = gameManager.timerSettings().battleTurnTime();

        plugin.timerManager().startTimer(Math.max(teamTime, minTime));
    }

    @Override
    public void nextTurn(final GameManager gameManager) {
        gameManager.performNextTurnLogic();
    }

    @Override
    public void onTimerTick(final GameManager gameManager, final TimerManager timerManager) {
        gameManager.currentTurnPlayer()
                .flatMap(gameManager::findParticipant)
                .ifPresent(participant -> {
                    final Team team = participant.team();
                    final int threshold = gameManager.timerSettings().battleTurnTime();

                    if (timerManager.teamTime(team) > threshold) {
                        timerManager.teamTime(team, timerManager.remainingSeconds());
                    }
                });
    }

    @Override
    public void onTimerExpire(final GameManager gameManager) {
        gameManager.nextTurn();
    }
}
