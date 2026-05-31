package dev.tecte.chesswar.game.component;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class TimerContext {
    private int remainingSeconds;
    private int whiteTeamTime;
    private int blackTeamTime;
    private boolean running = false;

    public void reset(final int defaultTeamTime) {
        remainingSeconds = 0;
        whiteTeamTime = defaultTeamTime;
        blackTeamTime = defaultTeamTime;
        running = false;
    }
}
