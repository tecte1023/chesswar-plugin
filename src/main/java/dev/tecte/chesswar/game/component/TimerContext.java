package dev.tecte.chesswar.game.component;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class TimerContext {
    private int remainingSeconds;
    private int whiteTeamTime = 600;
    private int blackTeamTime = 600;
    private boolean running = false;

    public void reset() {
        remainingSeconds = 0;
        whiteTeamTime = 600;
        blackTeamTime = 600;
        running = false;
    }
}
