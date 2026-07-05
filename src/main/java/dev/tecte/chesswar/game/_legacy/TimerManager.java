/*
package dev.tecte.chesswar.game;

import dev.tecte.chesswar.team.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "ChessWar")
@RequiredArgsConstructor
public class TimerManager {
    private static final int TICKS_PER_SECOND = 20;
    private final GameContext context;
    private int subTickCounter = 0;

    public void tick() {
        if (!context.timerRunning()) {
            return;
        }

        subTickCounter++;
        if (subTickCounter >= TICKS_PER_SECOND) {
            subTickCounter = 0;
            context.tickTimer();
        }
    }

    public boolean isExpired() {
        if (!context.timerRunning()) {
            return false;
        }

        if (context.currentTimerPolicy() == TimerPolicy.IMMEDIATE) {
            return context.remainingSeconds() <= 0;
        }

        return context.remainingSeconds() < 0;
    }

    public void startTimer(final int seconds) {
        startTimer(seconds, TimerPolicy.GRACEFUL);
    }

    public void startTimer(final int seconds, final TimerPolicy policy) {
        subTickCounter = 0;
        context.currentTimerPolicy(policy);
        context.startTimer(seconds);
    }

public void stopTimer() {
        context.stopTimer();
    }

    public void accelerateTimerTo(final int seconds) {
        if (context.remainingSeconds() > seconds) {
            context.accelerateTimer(seconds);
        }
    }

    public void updateTeamTime(final Team team, final int seconds) {
        context.updateTeamTime(team, seconds);
    }

    public void reset() {
        context.reset();
    }
}
*/
